package io.github.achmalearning.externat.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.github.achmalearning.externat.R
import io.github.achmalearning.externat.data.Catalog
import io.github.achmalearning.externat.data.UserState
import io.github.achmalearning.externat.ui.favoris.FavorisScreen
import io.github.achmalearning.externat.ui.guides.GuidesScreen
import io.github.achmalearning.externat.ui.objectifs.ObjectifsScreen
import io.github.achmalearning.externat.ui.settings.SettingsScreen
import io.github.achmalearning.externat.ui.viewer.ViewerScreen
import kotlinx.serialization.Serializable

@Serializable data object ObjectifsRoute
@Serializable data object GuidesRoute
@Serializable data object FavorisRoute
@Serializable data object SettingsRoute
@Serializable data class ViewerRoute(val docKey: String)

private data class TopLevel(val route: Any, val label: Int, val icon: Int)

private val topLevel = listOf(
    TopLevel(ObjectifsRoute, R.string.nav_objectifs, R.drawable.ic_checklist),
    TopLevel(GuidesRoute, R.string.nav_guides, R.drawable.ic_book),
    TopLevel(FavorisRoute, R.string.nav_favoris, R.drawable.ic_star),
)

@Composable
fun ExternatApp(viewModel: AppViewModel) {
    val catalogState by viewModel.catalog.collectAsStateWithLifecycle()
    val user by viewModel.user.collectAsStateWithLifecycle()
    val catalog = (catalogState as? CatalogState.Ready)?.catalog
    val currentUser = user

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when {
            catalogState is CatalogState.Error -> Box(Modifier.fillMaxSize().padding(24.dp), Alignment.Center) {
                Text(stringResource(R.string.loading_error, (catalogState as CatalogState.Error).message))
            }
            catalog != null && currentUser != null -> AppScaffold(catalog, currentUser, viewModel)
        }
    }
}

@Composable
private fun AppScaffold(catalog: Catalog, user: UserState, viewModel: AppViewModel) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val destination = backStack?.destination
    val fullScreen = destination?.hasRoute(ViewerRoute::class) == true || destination?.hasRoute(SettingsRoute::class) == true

    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    val layoutType = if (fullScreen) NavigationSuiteType.None
    else NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)

    NavigationSuiteScaffold(
        layoutType = layoutType,
        navigationSuiteItems = {
            topLevel.forEach { item ->
                val selected = destination?.hierarchy?.any { it.hasRoute(item.route::class) } == true
                item(
                    selected = selected,
                    onClick = { nav.navigateTopLevel(item.route) },
                    icon = { Icon(painterResource(item.icon), contentDescription = null) },
                    label = { Text(stringResource(item.label)) },
                )
            }
        },
    ) {
        NavHost(navController = nav, startDestination = ObjectifsRoute) {
            val openSettings = { nav.navigate(SettingsRoute) { launchSingleTop = true } }
            composable<ObjectifsRoute> { ObjectifsScreen(catalog, user, viewModel, openSettings) }
            composable<GuidesRoute> {
                GuidesScreen(catalog, user, viewModel, onOpen = { nav.navigate(ViewerRoute(it)) }, onOpenSettings = openSettings)
            }
            composable<FavorisRoute> {
                FavorisScreen(
                    catalog, user, viewModel,
                    onOpenKey = { key ->
                        when {
                            key.startsWith("g:") -> nav.navigate(ViewerRoute(key))
                            key.startsWith("s:") -> {
                                viewModel.requestOpen(key.removePrefix("s:"), null)
                                nav.navigateTopLevel(ObjectifsRoute)
                            }
                            key.startsWith("e:") -> catalog.entry(key.removePrefix("e:"))?.let { e ->
                                viewModel.requestOpen(e.dossier, e.key)
                                nav.navigateTopLevel(ObjectifsRoute)
                            }
                        }
                    },
                    onOpenSettings = openSettings,
                )
            }
            composable<SettingsRoute> { SettingsScreen(user, viewModel, onBack = { nav.popBackStack() }) }
            composable<ViewerRoute> { entry ->
                ViewerScreen(entry.toRoute<ViewerRoute>().docKey, catalog, user, viewModel, onBack = { nav.popBackStack() })
            }
        }
    }
}

/** Navigation entre onglets : une seule copie de chaque onglet, état conservé. */
private fun NavHostController.navigateTopLevel(route: Any) = navigate(route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}
