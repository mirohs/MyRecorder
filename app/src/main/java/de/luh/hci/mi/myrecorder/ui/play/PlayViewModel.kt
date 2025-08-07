package de.luh.hci.mi.myrecorder.ui.play

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.luh.hci.mi.myrecorder.MyRecorder
import de.luh.hci.mi.myrecorder.PlayRoute
import de.luh.hci.mi.myrecorder.data.Recording
import de.luh.hci.mi.myrecorder.data.RecordingsRepository
import de.luh.hci.mi.myrecorder.play.AudioPlayer
import de.luh.hci.mi.myrecorder.ui.NavigateBack
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
    key: PlayRoute,
    repository: RecordingsRepository, // the underlying repository (data model)
    private val player: AudioPlayer
) : ViewModel() {

    private val recordingId: Long = key.recordingId

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
                navigateBack.trigger()
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
        navigateBack.trigger()
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
        navigateBack.trigger()
    }

    private val navigateBack = NavigateBack(viewModelScope)
    val navigateBackFlow = navigateBack.flow

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

    class Factory(
        private val key: PlayRoute,
        private val app: MyRecorder
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return PlayViewModel(
                key,
                app.recordingsRepository,
                app.audioPlayer
            ) as T
        }
    }

}
