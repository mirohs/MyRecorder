package de.luh.hci.mi.myrecorder.ui.recordings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.luh.hci.mi.myrecorder.MyRecorder
import de.luh.hci.mi.myrecorder.data.Recording
import de.luh.hci.mi.myrecorder.data.RecordingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class RecordingsViewModel(
    private val repository: RecordingsRepository
) : ViewModel() {

    var selectedRecording: Recording? by mutableStateOf(null)

    val recordings: Flow<List<Recording>> = repository.recordings()

    fun deleteRecording(id: Long) {
        viewModelScope.launch {
            repository.deleteRecording(id)
        }
    }

    companion object {
        // Companion object for creating the view model in the right lifecycle scope.
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyRecorder
                RecordingsViewModel(app.recordingsRepository)
            }
        }
    }

}
