package com.instadown.app.ui.screens.home

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.instadown.app.R
import com.instadown.app.data.model.DownloadEntity
import com.instadown.app.ui.components.GlassmorphicCard
import com.instadown.app.ui.components.GlassmorphicTextField
import com.instadown.app.ui.components.GlowingButton
import com.instadown.app.ui.theme.BackgroundCanvas
import com.instadown.app.ui.theme.GlowAlert
import com.instadown.app.ui.theme.NeonPink
import com.instadown.app.ui.theme.NeonViolet
import com.instadown.app.ui.theme.TextMuted
import com.instadown.app.ui.theme.TextPrimary
import com.instadown.app.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val downloads by viewModel.downloads.collectAsState()
    
    // Filter active and failed downloads for home screen visualization
    val activeQueue = remember(downloads) {
        downloads.filter { it.status == "DOWNLOADING" || it.status == "PENDING" || it.status == "FAILED" }
    }

    var urlInput by remember { mutableStateOf("") }
    var isInputFocused by remember { mutableStateOf(false) }

    // Clipboard monitoring states
    var showClipboardDialog by remember { mutableStateOf(false) }
    var clipboardUrl by remember { mutableStateOf("") }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Detect clipboard changes on screen resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                if (clipboard.hasPrimaryClip() && 
                    clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true
                ) {
                    val clipText = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                    if (clipText.contains("instagram.com") && clipText != urlInput && downloads.none { it.url == clipText && it.status != "FAILED" }) {
                        clipboardUrl = clipText
                        showClipboardDialog = true
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundCanvas)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(28.dp))
        
        Text(
            text = "InstaDown",
            color = TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Cole o link e assista seus vídeos offline",
            color = TextSecondary,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // URL input card
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GlassmorphicTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    placeholder = stringResource(R.string.placeholder_url),
                    isFocused = isInputFocused,
                    modifier = Modifier.onFocusChanged { isInputFocused = it.isFocused }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Paste Button
                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            if (clipboard.hasPrimaryClip()) {
                                val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                                urlInput = text
                            } else {
                                Toast.makeText(context, "Área de transferência vazia", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(0.4f)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_paste),
                            color = NeonPink,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Download Button
                    GlowingButton(
                        text = stringResource(R.string.btn_download),
                        onClick = {
                            if (urlInput.contains("instagram.com")) {
                                viewModel.startDownload(urlInput)
                                urlInput = "" // Clear input field on start
                            } else {
                                Toast.makeText(context, "Por favor insira um link válido do Instagram", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(0.6f),
                        enabled = urlInput.isNotEmpty()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Fila de Downloads",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (activeQueue.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum download ativo.",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(activeQueue, key = { it.id }) { item ->
                    ActiveDownloadCard(item)
                }
            }
        }
    }

    // Auto-detect Clipboard dialog
    if (showClipboardDialog) {
        AlertDialog(
            onDismissRequest = { showClipboardDialog = false },
            title = { Text(text = "Link detectado") },
            text = { Text(text = stringResource(R.string.msg_clipboard_detected)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.startDownload(clipboardUrl)
                        showClipboardDialog = false
                    }
                ) {
                    Text(text = stringResource(R.string.btn_download), color = NeonPink)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClipboardDialog = false }) {
                    Text(text = "Cancelar", color = TextSecondary)
                }
            },
            containerColor = Color(0xFF1B163B),
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }
}

@Composable
fun ActiveDownloadCard(item: DownloadEntity) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.caption,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(0.7f)
                )
                
                Text(
                    text = if (item.status == "FAILED") "Erro" else "${item.progress}%",
                    color = if (item.status == "FAILED") GlowAlert else NeonPink,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(0.3f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Neon progress bar
            val progressColor = if (item.status == "FAILED") GlowAlert else NeonPink
            LinearProgressIndicator(
                progress = item.progress / 100f,
                color = progressColor,
                trackColor = Color(0xFF2C2555),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
            
            if (item.status == "FAILED") {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Erro no download. Verifique sua conexão ou API Key.",
                    color = GlowAlert.copy(alpha = 0.8f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

