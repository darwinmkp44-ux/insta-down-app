package com.instadown.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.instadown.app.R
import com.instadown.app.ui.screens.gallery.GalleryScreen
import com.instadown.app.ui.screens.gallery.GalleryViewModel
import com.instadown.app.ui.screens.home.HomeScreen
import com.instadown.app.ui.screens.home.HomeViewModel
import com.instadown.app.ui.screens.settings.SettingsScreen
import com.instadown.app.ui.screens.settings.SettingsViewModel
import com.instadown.app.ui.theme.BackgroundCanvas
import com.instadown.app.ui.theme.GlassBase
import com.instadown.app.ui.theme.NeonPink
import com.instadown.app.ui.theme.TextPrimary
import com.instadown.app.ui.theme.TextSecondary

sealed class NavigationItem(val route: String, val icon: ImageVector, val titleResId: Int) {
    object Home : NavigationItem("home", Icons.Default.Home, R.string.tab_home)
    object Gallery : NavigationItem("gallery", Icons.Default.PlayArrow, R.string.tab_gallery)
    object Settings : NavigationItem("settings", Icons.Default.Settings, R.string.tab_settings)
}

@Composable
fun AppNavigation(
    homeViewModel: HomeViewModel,
    galleryViewModel: GalleryViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundCanvas,
        bottomBar = {
            GlassmorphicBottomBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavigationItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavigationItem.Home.route) {
                HomeScreen(viewModel = homeViewModel)
            }
            composable(NavigationItem.Gallery.route) {
                GalleryScreen(viewModel = galleryViewModel)
            }
            composable(NavigationItem.Settings.route) {
                SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}

@Composable
fun GlassmorphicBottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavigationItem.Home,
        NavigationItem.Gallery,
        NavigationItem.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Floating Glassmorphic Bottom Navigation Container
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(GlassBase)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.02f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                GlassmorphicBottomBarItem(
                    item = item,
                    selected = selected,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun RowScope.GlassmorphicBottomBarItem(
    item: NavigationItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) NeonPink.copy(alpha = 0.12f) else Color.Transparent
    val contentColor = if (selected) NeonPink else TextSecondary
    
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        CompositionLocalProvider(
            LocalContentColor provides contentColor
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = stringResource(item.titleResId),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = stringResource(item.titleResId),
                fontSize = 11.sp,
                color = contentColor
            )
        }
    }
}
