package de.luh.hci.mi.myrecorder.ui.record

import android.location.Location
import android.util.Log
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
import de.luh.hci.mi.myrecorder.data.PlacesRepository
import de.luh.hci.mi.myrecorder.data.Recording
import de.luh.hci.mi.myrecorder.data.RecordingsRepository
import de.luh.hci.mi.myrecorder.record.AudioRecorder
import kotlinx.coroutines.launch
import java.io.File
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private fun formatDuration(duration: Duration): String {
    val totalSeconds = duration.seconds
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.")
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

// ViewModel for the record screen.
class RecordViewModel(
    private val recordingsRepository: RecordingsRepository,
    private val placesRepository: PlacesRepository,
    private val recorder: AudioRecorder,
    private val navigateBack: () -> Unit
) : ViewModel() {

    // Start time of the recording, which will be used to name the file.
    private lateinit var startDateTime: LocalDateTime
    var startDate by mutableStateOf("")
    var startTime by mutableStateOf("")
    private var startTimestamp: Long = 0
    private lateinit var recordingFile: File

    private var location: Location? = null
    var latitude: String by mutableStateOf("")
    var longitude: String by mutableStateOf("")

    var place by mutableStateOf("")

    init {
        startRecording()
    }

    private fun startRecording() {
        startDateTime = LocalDateTime.now()
        startDate = startDateTime.format(dateFormatter)
        startTime = startDateTime.format(timeFormatter)

        val zoneId = ZoneId.systemDefault()
        startTimestamp = startDateTime.atZone(zoneId).toInstant().epochSecond
        log("$startDate, $startTime, $startDateTime, $zoneId, $startTimestamp")

        recordingFile =
            recordingsRepository.recordingFile("$startTimestamp.${recorder.FILENAME_EXTENSION}")
        recorder.start(recordingFile, 5 * 60 * 1000) { stopRecording() }
        viewModelScope.launch {
            val loc = placesRepository.currentLocation()
            latitude = loc.latitude.toString()
            longitude = loc.longitude.toString()
            location = loc
            place = placesRepository.closestPlace(loc)?.name ?: ""
        }
    }

    fun stopRecording() {
        recorder.stop()
        val stopTime = LocalDateTime.now()
        val duration = Duration.between(startDateTime, stopTime)
        log("duration: $duration ${formatDuration(duration)}")
        viewModelScope.launch {
            recordingsRepository.addRecording(
                Recording(
                    timestamp = startTimestamp,
                    startDate = startDateTime.format(dateFormatter),
                    startTime = startDateTime.format(timeFormatter),
                    duration = formatDuration(duration),
                    file = recordingFile,
                    latitude = location?.latitude?.toString() ?: "",
                    longitude = location?.longitude?.toString() ?: "",
                    place = place,
                )
            )
            navigateBack()
        }
    }

    // Called when this ViewModel is no longer used and will be destroyed. Can be used for cleanup.
    override fun onCleared() {
        log("onCleared")
        recorder.stop()
    }

    // Logs a debug message.
    private fun log(msg: String) {
        Log.d(this.javaClass.simpleName, msg)
    }

    companion object {
        // Companion object for creating the view model in the right lifecycle scope.
        fun factory(navigateBack: () -> Unit): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    val app = this[APPLICATION_KEY] as MyRecorder
                    RecordViewModel(
                        app.recordingsRepository,
                        app.placesRepository,
                        app.audioRecorder,
                        navigateBack
                    )
                }
            }
        }
    }

}
