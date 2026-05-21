package com.instadown.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.instadown.app.ui.navigation.AppNavigation
import com.instadown.app.ui.screens.gallery.GalleryViewModel
import com.instadown.app.ui.screens.gallery.GalleryViewModelFactory
import com.instadown.app.ui.screens.home.HomeViewModel
import com.instadown.app.ui.screens.home.HomeViewModelFactory
import com.instadown.app.ui.screens.settings.SettingsViewModel
import com.instadown.app.ui.screens.settings.SettingsViewModelFactory
import com.instadown.app.ui.theme.InstaDownTheme

/**
 * Main Activity of the InstaDown application.
 * Manages runtime notification permission checks for Android 13+ and hosts the compose-based navigation.
 */
class MainActivity : ComponentActivity() {

    // Launcher for POST_NOTIFICATIONS runtime permission on Android 13+
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle permission result if necessary
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission if running on API 33+ (Android 13+)
        checkAndRequestNotificationPermission()

        // Access the Application class instance to resolve dependencies manually
        val app = application as InstaDownApp
        val repository = app.repository
        val settingsManager = app.settingsManager

        // Instantiate ViewModels using custom manual factories
        val homeViewModel = ViewModelProvider(
            this,
            HomeViewModelFactory(repository)
        )[HomeViewModel::class.java]

        val galleryViewModel = ViewModelProvider(
            this,
            GalleryViewModelFactory(repository)
        )[GalleryViewModel::class.java]

        val settingsViewModel = ViewModelProvider(
            this,
            SettingsViewModelFactory(settingsManager)
        )[SettingsViewModel::class.java]

        setContent {
            InstaDownTheme {
                AppNavigation(
                    homeViewModel = homeViewModel,
                    galleryViewModel = galleryViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }

    /**
     * Checks and requests the POST_NOTIFICATIONS permission at runtime on Android 13 (API 33) and above.
     */
    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
