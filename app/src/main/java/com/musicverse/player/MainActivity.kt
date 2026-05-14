package com.musicverse.player

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.musicverse.player.ui.screens.home.HomeScreen
import com.musicverse.player.ui.screens.home.HomeViewModel
import com.musicverse.player.ui.screens.import_.ImportScreen
import com.musicverse.player.ui.screens.import_.ImportViewModel
import com.musicverse.player.ui.screens.library.LibraryScreen
import com.musicverse.player.ui.screens.library.LibraryViewModel
import com.musicverse.player.ui.screens.library.TrackDetailScreen
import com.musicverse.player.ui.screens.library.TrackDetailViewModel
import com.musicverse.player.ui.screens.discovery.DiscoveryScreen
import com.musicverse.player.ui.screens.discovery.DiscoveryViewModel
import com.musicverse.player.ui.screens.login.LoginScreen
import com.musicverse.player.ui.screens.player.PlayerScreen
import com.musicverse.player.ui.screens.player.PlayerViewModel
import com.musicverse.player.ui.screens.settings.SettingsScreen
import com.musicverse.player.ui.screens.settings.SettingsViewModel
import com.musicverse.player.ui.screens.profile.ProfileScreen
import com.musicverse.player.ui.theme.InterFont
import com.musicverse.player.ui.theme.MusicVerseColors
import com.musicverse.player.ui.theme.MusicVerseTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity — Single-Activity architecture with Jetpack Navigation.
 * Edge-to-edge rendering with transparent system bars.
 *
 * Flow: Login → Main App (4 tabs: Home, Discover, Vault, Profile)
 *
 * Handles:
 *   - Spotify OAuth deep-link callback (musicverse://callback)
 *   - Navigation between all screens
 *   - Bottom nav bar with warm muted theme
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Hold a reference to the ImportViewModel to pass OAuth callbacks
    private var pendingAuthCallback: ((android.net.Uri) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MusicVerseTheme {
                val navController = rememberNavController()
                MusicVerseNavHost(
                    navController = navController,
                    onSetAuthCallback = { callback ->
                        pendingAuthCallback = callback
                    }
                )
            }
        }

        // Handle deep link on cold start
        handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme == "musicverse" && uri.host == "callback") {
            pendingAuthCallback?.invoke(uri)
        }
    }
}

/**
 * Navigation routes.
 */
object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val IMPORT = "import"
    const val LIBRARY = "library"
    const val DISCOVERY = "discovery"
    const val PROFILE = "profile"
    const val TRACK_DETAIL = "track/{trackId}"
    const val PLAYER = "player/{trackId}"
    const val SETTINGS = "settings"

    fun trackDetail(trackId: String) = "track/$trackId"
    fun player(trackId: String) = "player/$trackId"
}

/**
 * Bottom nav destinations — matching Stitch design (4 tabs, icons-first).
 */
sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    data object Home : BottomNavItem(Routes.HOME, Icons.Rounded.Home, "Home")
    data object Discovery : BottomNavItem(Routes.DISCOVERY, Icons.Rounded.Explore, "Discover")
    data object Library : BottomNavItem(Routes.LIBRARY, Icons.Rounded.LibraryMusic, "Vault")
    data object Profile : BottomNavItem(Routes.PROFILE, Icons.Rounded.Person, "Profile")
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Discovery,
    BottomNavItem.Library,
    BottomNavItem.Profile
)

/**
 * Main NavHost with warm muted bottom navigation.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MusicVerseNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onSetAuthCallback: ((android.net.Uri) -> Unit) -> Unit = {}
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Show bottom nav only on main tabs
    val mainTabRoutes = listOf(Routes.HOME, Routes.LIBRARY, Routes.DISCOVERY, Routes.PROFILE)
    val showBottomNav = currentDestination?.route in mainTabRoutes

    Scaffold(
        containerColor = MusicVerseColors.DeepCharcoal,
        bottomBar = {
            if (showBottomNav) {
                NavigationBar(
                    containerColor = MusicVerseColors.Surface1,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    fontFamily = InterFont,
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MusicVerseColors.SunsetOrange,
                                selectedTextColor = MusicVerseColors.SunsetOrange,
                                unselectedIconColor = MusicVerseColors.TextTertiary,
                                unselectedTextColor = MusicVerseColors.TextTertiary,
                                indicatorColor = MusicVerseColors.SunsetOrangeGlow
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MusicVerseColors.DeepCharcoal)
                .padding(paddingValues)
        ) {
            SharedTransitionLayout {
                NavHost(
                    navController = navController,
                    startDestination = Routes.LOGIN,
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
                    // ── Login ──
                    composable(Routes.LOGIN) {
                        LoginScreen(
                            onConnectSpotify = {
                                navController.navigate(Routes.IMPORT) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            },
                            onConnectYouTube = {
                                // Navigate to Home for now
                                navController.navigate(Routes.HOME) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            },
                            onSkip = {
                                navController.navigate(Routes.HOME) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            }
                        )
                    }

                    // ── Home ──
                    composable(Routes.HOME) {
                        val viewModel: HomeViewModel = hiltViewModel()
                        HomeScreen(
                            viewModel = viewModel,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this@composable,
                            onNavigateToImport = { navController.navigate(Routes.IMPORT) },
                            onNavigateToLibrary = { navController.navigate(Routes.LIBRARY) },
                            onNavigateToDiscovery = { navController.navigate(Routes.DISCOVERY) },
                            onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                            onNavigateToSearch = {
                                navController.navigate(Routes.LIBRARY)
                            },
                            onNavigateToPlayer = { trackId ->
                                navController.navigate(Routes.player(trackId))
                            }
                        )
                    }

                    // ── Import ──
                    composable(Routes.IMPORT) {
                        val viewModel: ImportViewModel = hiltViewModel()

                        onSetAuthCallback { uri ->
                            viewModel.handleAuthCallback(uri)
                        }

                        ImportScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onLaunchSpotifyAuth = { intent ->
                                navController.context.startActivity(intent)
                            }
                        )
                    }

                    // ── Library (Vault) ──
                    composable(Routes.LIBRARY) {
                        val viewModel: LibraryViewModel = hiltViewModel()
                        LibraryScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToImport = { navController.navigate(Routes.IMPORT) },
                            onTrackClick = { track ->
                                navController.navigate(Routes.player(track.id))
                            }
                        )
                    }

                    // ── Track Detail ──
                    composable(
                        route = Routes.TRACK_DETAIL,
                        arguments = listOf(navArgument("trackId") { type = NavType.StringType })
                    ) {
                        val viewModel: TrackDetailViewModel = hiltViewModel()
                        val track by viewModel.track.collectAsState()
                        val isLoading by viewModel.isLoading.collectAsState()
                        val trackId = it.arguments?.getString("trackId") ?: ""

                        if (isLoading) {
                            Box(
                                modifier = Modifier.fillMaxSize().background(MusicVerseColors.DeepCharcoal),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MusicVerseColors.Amber)
                            }
                        } else if (track != null) {
                            TrackDetailScreen(
                                track = track!!,
                                onNavigateBack = { navController.popBackStack() },
                                onPlayTrack = { navController.navigate(Routes.player(trackId)) }
                            )
                        } else {
                            PlaceholderScreen(title = "Track not found")
                        }
                    }

                    // ── Player ──
                    composable(
                        route = Routes.PLAYER,
                        arguments = listOf(navArgument("trackId") { type = NavType.StringType })
                    ) {
                        val viewModel: PlayerViewModel = hiltViewModel()
                        PlayerScreen(
                            viewModel = viewModel,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this@composable,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // ── Discovery ──
                    composable(Routes.DISCOVERY) {
                        val viewModel: DiscoveryViewModel = hiltViewModel()
                        DiscoveryScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToPlayer = { trackId ->
                                navController.navigate(Routes.player(trackId))
                            }
                        )
                    }

                    // ── Profile ──
                    composable(Routes.PROFILE) {
                        ProfileScreen(
                            onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
                        )
                    }

                    // ── Settings ──
                    composable(Routes.SETTINGS) {
                        val viewModel: SettingsViewModel = hiltViewModel()
                        SettingsScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
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
            .background(MusicVerseColors.DeepCharcoal),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.displayMedium,
            color = MusicVerseColors.TextTertiary
        )
    }
}

/**
 * Profile screen placeholder matching the Stitch design.
 */
@Composable
fun ProfilePlaceholderScreen(
    onNavigateToSettings: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MusicVerseColors.DeepCharcoal),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Rounded.Person,
                contentDescription = "Profile",
                tint = MusicVerseColors.Amber,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineMedium,
                color = MusicVerseColors.TextPrimary
            )
            Text(
                text = "Coming soon",
                style = MaterialTheme.typography.bodyMedium,
                color = MusicVerseColors.TextTertiary
            )
        }
    }
}
