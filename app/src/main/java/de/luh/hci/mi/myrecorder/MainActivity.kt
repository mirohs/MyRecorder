package de.luh.hci.mi.myrecorder

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
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

// Each NavKey/Route refers to a separate page/screen of the app.
// https://developer.android.com/guide/navigation/navigation-3
// https://developer.android.com/guide/navigation/principles

@Serializable
data object StartRoute : NavKey

@Serializable
data object RecordRoute : NavKey

@Serializable
data class PlayRoute(val recordingId: Long) : NavKey

@Serializable
data object RecordingsRoute : NavKey

@Serializable
data object PlacesRoute : NavKey

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
                Navigation()
            }
        }
    }

    @Composable
    private fun Navigation() {
        val backStack = rememberNavBackStack(StartRoute)
        val app = application as MyRecorder
        val back: () -> Unit = {
            log("backStack: $backStack")
            backStack.removeLastOrNull()
        }

        NavDisplay(
            backStack = backStack,
            entryDecorators = listOf(
                rememberSceneSetupNavEntryDecorator(),
                rememberSavedStateNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                entry<StartRoute> {
                    StartScreen(
                        { backStack.add(RecordRoute) },
                        { backStack.add(PlayRoute(it)) },
                        { backStack.add(RecordingsRoute) },
                        { backStack.add(PlacesRoute) },
                        viewModel(factory = StartViewModel.Factory(app))
                    )
                }
                entry<RecordRoute> {
                    RecordScreen(
                        back,
                        viewModel(factory = RecordViewModel.Factory(app))
                    )
                }
                entry<PlayRoute> { key ->
                    PlayScreen(
                        back,
                        viewModel(factory = PlayViewModel.Factory(key, app))
                    )
                }
                entry<RecordingsRoute> {
                    RecordingsScreen(
                        { backStack.add(PlayRoute(it)) },
                        viewModel(factory = RecordingsViewModel.Factory(app))
                    )
                }
                entry<PlacesRoute> {
                    PlacesScreen(
                        viewModel(factory = PlacesViewModel.Factory(app))
                    )
                }
            }
        )
    }

    // Logs a debug message.
    private fun log(msg: String) {
        Log.d(javaClass.simpleName, msg)
    }

}
