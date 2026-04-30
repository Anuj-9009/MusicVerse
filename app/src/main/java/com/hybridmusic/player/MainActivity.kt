package com.hybridmusic.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hybridmusic.player.ui.screens.home.HomeScreen
import com.hybridmusic.player.ui.theme.HybridColors
import com.hybridmusic.player.ui.theme.HybridMusicTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity — Single-Activity architecture with Jetpack Navigation.
 * Edge-to-edge rendering with transparent system bars.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HybridMusicTheme {
                val navController = rememberNavController()
                HybridMusicNavHost(navController = navController)
            }
        }
    }
}

/**
 * Navigation routes.
 */
object Routes {
    const val HOME = "home"
    const val IMPORT = "import"
    const val LIBRARY = "library"
    const val PLAYER = "player/{trackId}"
    const val SETTINGS = "settings"

    fun player(trackId: String) = "player/$trackId"
}

/**
 * Main NavHost with spring-physics transitions.
 */
@Composable
fun HybridMusicNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HybridColors.DeepCharcoal)
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            enterTransition = {
                fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) +
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
            },
            exitTransition = {
                fadeOut(animationSpec = spring(stiffness = Spring.StiffnessHigh))
            },
            popEnterTransition = {
                fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) +
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
            },
            popExitTransition = {
                fadeOut(animationSpec = spring(stiffness = Spring.StiffnessHigh))
            }
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onNavigateToImport = { navController.navigate(Routes.IMPORT) },
                    onNavigateToLibrary = { navController.navigate(Routes.LIBRARY) },
                    onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
                )
            }

            composable(Routes.IMPORT) {
                // Placeholder — Step 2 implementation
                PlaceholderScreen(title = "Smart Importer")
            }

            composable(Routes.LIBRARY) {
                // Placeholder — Step 3 implementation
                PlaceholderScreen(title = "Library")
            }

            composable(Routes.PLAYER) {
                // Placeholder — Step 6 implementation
                PlaceholderScreen(title = "Player")
            }

            composable(Routes.SETTINGS) {
                PlaceholderScreen(title = "Settings")
            }
        }
    }
}

/**
 * Temporary placeholder for screens not yet implemented.
 */
@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HybridColors.DeepCharcoal),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = title,
            style = androidx.compose.material3.MaterialTheme.typography.displayMedium,
            color = HybridColors.TextTertiary
        )
    }
}
