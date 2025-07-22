package de.luh.hci.mi.myrecorder.record

import java.io.File

interface AudioRecorder {

    // This filename extension is used for audio files.
    val FILENAME_EXTENSION: String

    // Starts recording audio into the given file, up to the given maximum duration (in milliseconds).
    // When recording stops because the maximum duration has exceeded, then onMaxDuration is called.
    fun start(outputFile: File, maxDuration: Int, onMaxDuration: (() -> Unit)?)

    // Stops recording and releases resources.
    fun stop()
}