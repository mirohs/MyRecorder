package de.luh.hci.mi.myrecorder.play

import android.content.Context
import android.media.MediaPlayer
import androidx.core.net.toUri
import java.io.File

class AudioPlayerImpl(private val context: Context) : AudioPlayer {

    // https://developer.android.com/reference/android/media/MediaPlayer
    private var player: MediaPlayer? = null

    override fun init(file: File, onCompletion: (() -> Unit)?) {
        player?.let {
            stop()
        }
        player = MediaPlayer.create(context, file.toUri()).apply {
            if (onCompletion != null) {
                setOnCompletionListener { onCompletion() }
            }
        }
    }

    override fun play() {
        player?.start()
    }

    override fun pause() {
        player?.pause()
    }

    override fun stop() {
        player?.stop()
        player?.release()
        player = null
    }

    override fun seekTo(ms: Int) {
        player?.seekTo(ms)
    }

    override fun duration(): Int {
        return player?.duration ?: 0
    }

    override fun position(): Int {
        return player?.currentPosition ?: 0
    }

}
