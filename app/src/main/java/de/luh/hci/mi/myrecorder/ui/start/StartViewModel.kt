package de.luh.hci.mi.myrecorder.ui.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.luh.hci.mi.myrecorder.MyRecorder
import de.luh.hci.mi.myrecorder.data.PlacesRepository
import de.luh.hci.mi.myrecorder.data.Recording
import de.luh.hci.mi.myrecorder.data.RecordingsRepository
import kotlinx.coroutines.flow.Flow

// ViewModel for the start screen.
class StartViewModel(
    recordingsRepository: RecordingsRepository,
    placesRepository: PlacesRepository,
) : ViewModel() {

    // The latest completed recording (if any).
    val latestRecording: Flow<Recording?> = recordingsRepository.latestRecordingAsFlow()

    // All available recordings
    val recordingsCount: Flow<Int> = recordingsRepository.recordingsCount()

    val placesCount: Flow<Int> = placesRepository.placesCount()

    /*
    val recordingsCount2: StateFlow<Int> = recordingsCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0)
     */

    companion object {
        // Companion object for creating the view model in the right lifecycle scope.
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyRecorder
                StartViewModel(app.recordingsRepository, app.placesRepository)
            }
        }
    }

}
