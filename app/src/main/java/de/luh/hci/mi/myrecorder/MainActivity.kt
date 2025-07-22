package de.luh.hci.mi.myrecorder

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.luh.hci.mi.myrecorder.ui.places.PlacesScreen
import de.luh.hci.mi.myrecorder.ui.places.PlacesViewModel
import de.luh.hci.mi.myrecorder.ui.play.PlayScreen
import de.luh.hci.mi.myrecorder.ui.play.PlayViewModel
import de.luh.hci.mi.myrecorder.ui.record.RecordScreen
import de.luh.hci.mi.myrecorder.ui.record.RecordViewModel
import de.luh.hci.mi.myrecorder.ui.recordings.RecordingsScreen
import de.luh.hci.mi.myrecorder.ui.recordings.RecordingsViewModel
import de.luh.hci.mi.myrecorder.ui.start.StartScreen
import de.luh.hci.mi.myrecorder.ui.start.StartViewModel
import de.luh.hci.mi.myrecorder.ui.theme.MyRecorderTheme
import kotlinx.serialization.Serializable

@Serializable
object StartDestination

@Serializable
object RecordDestination

@Serializable
data class PlayDestination(val recordingId: Long)

@Serializable
object RecordingsDestination

@Serializable
object PlacesDestination

// The main activity sets the content root, which is a navigation graph.
// Each "composable" in the navigation graph is a separate page/screen of the app.
// https://developer.android.com/jetpack/compose/navigation
// https://developer.android.com/guide/navigation/principles
class MainActivity : ComponentActivity() {

    // Register launcher: register(contract, callback).
    // contract: the kind of activity result we want (here: a permission)
    // callback: processing the result (here: granted/rejected permission)
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted: Map<String, Boolean> ->
        val allGranted = granted.all { it.value }
        if (allGranted) {
            log("Required permission granted")
        } else {
            log("Permission not granted, stopping the app")
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()

        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )

        setContent {
            MyRecorderTheme {
                // The NavController manages navigation in the navigation graph (e.g, back and up).
                val navController = rememberNavController()
                // The NavHost composable is a container for navigation destinations.
                NavHost(
                    navController = navController,
                    startDestination = StartDestination
                ) {
                    // The NavGraph has a composable for each navigation destination.

                    // navigation destination
                    composable<StartDestination> {
                        StartScreen(
                            { navController.navigate(RecordDestination) },
                            { navController.navigate(PlayDestination(it)) },
                            { navController.navigate(RecordingsDestination) },
                            { navController.navigate(PlacesDestination) },
                            viewModel(factory = StartViewModel.Factory) // create view model (or get from cache)
                        )
                    }

                    // navigation destination
                    composable<RecordDestination> {
                        RecordScreen(
                            viewModel(factory = RecordViewModel.factory(navController::popBackStack)) // create view model (or get from cache)
                        )
                    }

                    // navigation destination
                    composable<PlayDestination> {
                        PlayScreen(
                            viewModel(factory = PlayViewModel.factory(navController::popBackStack)) // create view model (or get from cache)
                        )
                    }

                    // navigation destination
                    composable<RecordingsDestination> {
                        RecordingsScreen(
                            { navController.navigate(PlayDestination(it)) },
                            viewModel(factory = RecordingsViewModel.Factory) // create view model (or get from cache)
                        )
                    }

                    // navigation destination
                    composable<PlacesDestination> {
                        PlacesScreen(
                            viewModel(factory = PlacesViewModel.Factory) // create view model (or get from cache)
                        )
                    }
                }
            }
        }
    }

    // Logs a debug message.
    private fun log(msg: String) {
        Log.d(javaClass.simpleName, msg)
    }

}
