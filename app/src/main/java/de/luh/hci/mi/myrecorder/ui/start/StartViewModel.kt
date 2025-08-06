package de.luh.hci.mi.myrecorder.ui.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

    class Factory(
        private val app: MyRecorder,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return StartViewModel(app.recordingsRepository, app.placesRepository) as T
        }
    }

}
