package de.luh.hci.mi.myrecorder.ui.record

import android.location.Location
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.luh.hci.mi.myrecorder.MyRecorder
import de.luh.hci.mi.myrecorder.data.PlacesRepository
import de.luh.hci.mi.myrecorder.data.Recording
import de.luh.hci.mi.myrecorder.data.RecordingsRepository
import de.luh.hci.mi.myrecorder.record.AudioRecorder
import de.luh.hci.mi.myrecorder.ui.NavigateBack
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
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
    private val recorder: AudioRecorder
) : ViewModel() {

    // Start time of the recording, which will be used to name the file.
    private lateinit var startDateTime: LocalDateTime
    var startDate by mutableStateOf("")
    var startTime by mutableStateOf("")
    private var startTimestamp: Long = 0
    private var recordingFile: File? = null
    var elapsedTime by mutableStateOf("00:00")

    private var location: Location? = null
    var latitude: String by mutableStateOf("")
    var longitude: String by mutableStateOf("")

    var place by mutableStateOf("")

    init {
        log("init")
        startRecording()
    }

    private fun startRecording() {
        startDateTime = LocalDateTime.now()
        startDate = startDateTime.format(dateFormatter)
        startTime = startDateTime.format(timeFormatter)

        val zoneId = ZoneId.systemDefault()
        startTimestamp = startDateTime.atZone(zoneId).toInstant().epochSecond
        log("$startDate, $startTime, $startDateTime, $zoneId, $startTimestamp")

        val file =
            recordingsRepository.recordingFile("$startTimestamp.${recorder.FILENAME_EXTENSION}")
        recordingFile = file
        recorder.start(file, 5 * 60 * 1000) { stopRecording() }
        viewModelScope.launch {
            val loc = placesRepository.currentLocation()
            latitude = loc.latitude.toString()
            longitude = loc.longitude.toString()
            location = loc
            place = placesRepository.closestPlace(loc)?.name ?: ""
        }
        viewModelScope.launch {
            var min = 0
            var sec = 0
            while (recordingFile != null) {
                delay(1000L)
                sec++
                if (sec >= 60) {
                    min++
                    sec = 0
                }
                elapsedTime = String.format(Locale.getDefault(), "%02d:%02d", min, sec)
            }
        }
    }

    fun stopRecording() {
        log("stopRecording")
        recorder.stop()
        val file = recordingFile
        recordingFile = null
        if (file != null) {
            val stopTime = LocalDateTime.now()
            val duration = Duration.between(startDateTime, stopTime)
            log("duration: $duration ${formatDuration(duration)}")
            viewModelScope.launch {
                log("stopRecording::launch")
                recordingsRepository.addRecording(
                    Recording(
                        timestamp = startTimestamp,
                        startDate = startDateTime.format(dateFormatter),
                        startTime = startDateTime.format(timeFormatter),
                        duration = formatDuration(duration),
                        file = file,
                        latitude = location?.latitude?.toString() ?: "",
                        longitude = location?.longitude?.toString() ?: "",
                        place = place,
                    )
                )
                log("stopRecording::navigateBack")
                navigateBack.trigger()
            }
        }
    }

    private val navigateBack = NavigateBack(viewModelScope)
    val navigateBackFlow = navigateBack.flow

    // Called when this ViewModel is no longer used and will be destroyed. Can be used for cleanup.
    override fun onCleared() {
        log("onCleared")
        recorder.stop()
        // discard recording file if back button is pressed while recording
        val file = recordingFile
        if (file != null) {
            try {
                file.delete()
            } catch (ex: IOException) {
                log("onCleared: cannot delete recording file: $ex")
            }
        }
    }

    // Logs a debug message.
    private fun log(msg: String) {
        Log.d(this.javaClass.simpleName, msg)
    }

    class Factory(private val app: MyRecorder) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return RecordViewModel(
                app.recordingsRepository,
                app.placesRepository,
                app.audioRecorder
            ) as T
        }
    }

}
