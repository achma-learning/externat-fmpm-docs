package io.github.achmalearning.externat

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.achmalearning.externat.data.ThemeMode
import io.github.achmalearning.externat.ui.AppViewModel
import io.github.achmalearning.externat.ui.CatalogState
import io.github.achmalearning.externat.ui.ExternatApp
import io.github.achmalearning.externat.ui.theme.ExternatTheme
import io.github.achmalearning.externat.ui.theme.isAppInDarkTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        // Garde l'écran de démarrage le temps de lire le catalogue et les préférences (hors thread UI).
        splash.setKeepOnScreenCondition {
            viewModel.catalog.value is CatalogState.Loading || viewModel.user.value == null
        }
        enableEdgeToEdge()

        setContent {
            val user by viewModel.user.collectAsStateWithLifecycle()
            val dark = isAppInDarkTheme(user?.themeMode ?: ThemeMode.SYSTEM)
            DisposableEffect(dark) {
                val style = if (dark) SystemBarStyle.dark(Color.TRANSPARENT)
                else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose {}
            }
            ExternatTheme(dark = dark, dynamicColor = user?.dynamicColor ?: true) {
                ExternatApp(viewModel)
            }
        }
    }
}
