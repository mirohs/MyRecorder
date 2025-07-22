package de.luh.hci.mi.myrecorder.play

import java.io.File

interface AudioPlayer {

    // Initializes the audio player to play the given file and to call onCompletion when playing has ended.
    fun init(file: File, onCompletion: (() -> Unit)?)

    // Start or resume playing the initialized audio file.
    fun play()

    // Pause playback.
    fun pause()

    // Stop playback and release resources. The AudioPlayer is now uninitialized until init is called again.
    fun stop()

    // Go to the given position (milliseconds from the start).
    fun seekTo(ms: Int)

    // The duration of the audio content in milliseconds.
    fun duration(): Int

    // The current playback position in milliseconds.
    fun position(): Int
}
