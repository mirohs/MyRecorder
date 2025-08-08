package de.luh.hci.mi.myrecorder.record

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class AudioRecorderImpl(
    private val context: Context,
) : AudioRecorder {

    override val FILENAME_EXTENSION = "m4a"

    private var recorder: MediaRecorder? = null

    private fun createRecorder(): MediaRecorder {
        @Suppress("DEPRECATION")
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context)
        else MediaRecorder()
    }

    override fun start(outputFile: File, maxDuration: Int, onMaxDuration: (() -> Unit)?) {
        recorder?.let {
            stop()
        }
        recorder = createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4) // MP4 container format
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC) // AAC audio
            // .m4a is the standard extension for MPEG-4 audio-only files
            // https://developer.android.com/reference/android/media/MediaRecorder#setMaxDuration(int)

            // | Bit Rate (kbps) | Sample Rate (Hz) | File Size per Second | Quality Level        |
            // | --------------- | ---------------- | -------------------- | -------------------- |
            // |  24 kbps        | 8000–16000       |  3 KB/sec            | Very Low (telephone) |
            // |  32 kbps        | 16000            |  4 KB/sec            | Low (VoIP)           |
            // |  64 kbps        | 22050–32000      |  8 KB/sec            | Medium               |
            // |  96 kbps        | 44100            | 12 KB/sec            | Medium-High          |
            // | 128 kbps        | 44100–48000      | 16 KB/sec            | High (streaming)     |
            // | 192 kbps        | 48000            | 24 KB/sec            | Very High (near CD)  |
            setAudioEncodingBitRate(96000)
            setAudioSamplingRate(44100)

            setMaxDuration(maxDuration)
            if (onMaxDuration != null) {
                setOnInfoListener { _, _, _ -> onMaxDuration() }
            }
            setOutputFile(outputFile)
            prepare()
            start()
        }
    }

    override fun stop() {
        recorder?.stop()
        recorder?.reset()
        recorder?.release()
        recorder = null
    }
}
