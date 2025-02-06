package chu.monscout.kagamin.feature

import LocalSnackbarHostState
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.java.KoinJavaComponent.get

@Composable
fun KagaminApp(
    kagaminViewModel: KagaminViewModel = get(KagaminViewModel::class.java),
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    Scaffold(snackbarHost = { SnackbarHost(LocalSnackbarHostState.current) }, modifier = modifier) {
        NavHost(navController,
            startDestination = MainScreenDestination.toString()) {
            composable(MainScreenDestination.toString()) {
                MainScreen(kagaminViewModel, navController)
            }

            composable(SettingsDestination.toString()) {
                SettingsScreen(kagaminViewModel, navController)
            }
        }
    }
}