package it.anak.waktusembahyangbrunei.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import it.anak.waktusembahyangbrunei.WaktuApp
import it.anak.waktusembahyangbrunei.presentation.theme.WaktuSembahyangBruneiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: PrayerViewModel =
                viewModel { PrayerViewModel((application as WaktuApp).repository) }
            WearApp(viewModel)
        }
    }
}

@androidx.compose.runtime.Composable
fun WearApp(viewModel: PrayerViewModel) {
    WaktuSembahyangBruneiTheme {
        AppScaffold {
            val navController = rememberSwipeDismissableNavController()
            val state by viewModel.state.collectAsStateWithLifecycle()
            SwipeDismissableNavHost(
                navController = navController,
                startDestination = "times",
            ) {
                composable("times") {
                    PrayerTimesScreen(
                        state = state,
                        onRefresh = viewModel::refresh,
                        onOpenSettings = { navController.navigate("settings") },
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        state = state,
                        onSelectDistrict = viewModel::setDistrict,
                        onSelectLanguage = viewModel::setLanguage,
                    )
                }
            }
        }
    }
}
