package de.luh.hci.mi.myrecorder.ui.recordings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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

    class Factory(
        private val app: MyRecorder,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return RecordingsViewModel(app.recordingsRepository) as T
        }
    }

}
