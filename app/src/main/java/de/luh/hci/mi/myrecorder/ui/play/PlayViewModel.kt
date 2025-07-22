package de.luh.hci.mi.myrecorder.ui.play

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import de.luh.hci.mi.myrecorder.MyRecorder
import de.luh.hci.mi.myrecorder.PlayDestination
import de.luh.hci.mi.myrecorder.data.Recording
import de.luh.hci.mi.myrecorder.data.RecordingsRepository
import de.luh.hci.mi.myrecorder.play.AudioPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

private fun formatPlaybackTime(positionMs: Int, durationMs: Int): String {
    val durationTotalSec = durationMs / 1000
    val durationMin = durationTotalSec / 60
    val durationSec = durationTotalSec % 60
    val positionTotalSec = positionMs / 1000
    val positionMin = positionTotalSec / 60
    val positionSec = positionTotalSec % 60
    return String.format(
        Locale.getDefault(),
        "%02d:%02d / %02d:%02d",
        positionMin,
        positionSec,
        durationMin,
        durationSec
    )
}

class PlayViewModel(
    repository: RecordingsRepository, // the underlying repository (data model)
    private val player: AudioPlayer,
    private val navigateBack: () -> Unit,
    savedStateHandle: SavedStateHandle // a map that contains the it of the recording to play
) : ViewModel() {

    // The recording ID is available in a key-value-map (savedStateHandle).
    private val recordingId: Long = savedStateHandle.toRoute<PlayDestination>().recordingId

    var recording: Recording? by mutableStateOf(null)
        private set

    var recordingDuration by mutableIntStateOf(0)
        private set

    var currentPlaybackTime by mutableStateOf("00:00 / 00:00")
        private set

    var isPlaying by mutableStateOf(false)
        private set

    var sliderPosition by mutableFloatStateOf(0f)
        private set

    private var sliderIsSeeking = false

    fun sliderChange(value: Float) {
        sliderPosition = value
        sliderIsSeeking = true
        currentPlaybackTime = formatPlaybackTime(value.toInt(), recordingDuration)
        // pausePlaying()
    }

    fun sliderChangeFinished() {
        sliderIsSeeking = false
        player.seekTo(sliderPosition.toInt())
    }

    init {
        log("init")
        viewModelScope.launch {
            val r = repository.recording(recordingId)
            recording = r
            if (r != null) {
                player.init(r.file, this@PlayViewModel::onCompletion)
                recordingDuration = player.duration()
                play()
            } else {
                log("No recording found for id $recordingId")
                navigateBack()
            }
        }
    }

    private fun play() {
        player.play()
        isPlaying = true
        viewModelScope.launch {
            while (isPlaying) {
                val position = player.position()
                if (!sliderIsSeeking) {
                    sliderPosition = position.toFloat()
                }
                currentPlaybackTime = formatPlaybackTime(position, recordingDuration)
                delay(33) // 30 Hz
            }
        }
    }

    fun stopPlaying() {
        player.stop()
        isPlaying = false
        navigateBack()
    }

    fun pausePlaying() {
        player.pause()
        isPlaying = false
    }

    fun resumePlaying() {
        play()
    }

    private fun onCompletion() {
        isPlaying = false
        navigateBack()
    }

    // Called when this ViewModel is no longer used and will be destroyed. Can be used for cleanup.
    override fun onCleared() {
        log("onCleared")
        player.stop()
        isPlaying = false
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
                    val app =
                        this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyRecorder
                    val savedStateHandle = createSavedStateHandle()
                    PlayViewModel(
                        app.recordingsRepository,
                        app.audioPlayer,
                        navigateBack,
                        savedStateHandle
                    )
                }
            }
        }
    }

}
