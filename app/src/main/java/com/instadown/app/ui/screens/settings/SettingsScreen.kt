package com.instadown.app.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instadown.app.R
import com.instadown.app.ui.components.GlassmorphicCard
import com.instadown.app.ui.components.GlassmorphicTextField
import com.instadown.app.ui.components.GlowingButton
import com.instadown.app.ui.theme.BackgroundCanvas
import com.instadown.app.ui.theme.TextMuted
import com.instadown.app.ui.theme.TextPrimary
import com.instadown.app.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val savedApiKey by viewModel.apiKey.collectAsState()
    val savedSubfolder by viewModel.subfolder.collectAsState()

    // Local mutable state for editing
    var apiKeyInput by remember(savedApiKey) { mutableStateOf(savedApiKey) }
    var subfolderInput by remember(savedSubfolder) { mutableStateOf(savedSubfolder) }

    var isApiKeyFocused by remember { mutableStateOf(false) }
    var isSubfolderFocused by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundCanvas)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(28.dp))
        
        Text(
            text = stringResource(R.string.tab_settings),
            color = TextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Configure seu app para download",
            color = TextSecondary,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // API Key Field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.api_key_label),
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    GlassmorphicTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        placeholder = stringResource(R.string.api_key_hint),
                        isFocused = isApiKeyFocused,
                        modifier = Modifier.onFocusChanged { isApiKeyFocused = it.isFocused }
                    )
                    Text(
                        text = "Pegue sua chave no console em fastsaver.io/docs",
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                // Subfolder Field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.save_folder_label),
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    GlassmorphicTextField(
                        value = subfolderInput,
                        onValueChange = { subfolderInput = it },
                        placeholder = stringResource(R.string.save_folder_hint),
                        isFocused = isSubfolderFocused,
                        modifier = Modifier.onFocusChanged { isSubfolderFocused = it.isFocused }
                    )
                    Text(
                        text = "Os vídeos serão salvos em /Downloads/SuaPasta",
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                GlowingButton(
                    text = stringResource(R.string.btn_save_settings),
                    onClick = {
                        if (apiKeyInput.isBlank()) {
                            Toast.makeText(context, context.getString(R.string.error_invalid_api_key), Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.saveSettings(apiKeyInput, subfolderInput) {
                                Toast.makeText(context, context.getString(R.string.settings_saved_success), Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
