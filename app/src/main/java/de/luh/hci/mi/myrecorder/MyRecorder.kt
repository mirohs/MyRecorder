package de.luh.hci.mi.myrecorder

import android.app.Application
import android.util.Log
import de.luh.hci.mi.myrecorder.data.Database
import de.luh.hci.mi.myrecorder.data.PlacesRepository
import de.luh.hci.mi.myrecorder.data.PlacesRepositoryImpl
import de.luh.hci.mi.myrecorder.data.RecordingsRepository
import de.luh.hci.mi.myrecorder.data.RecordingsRepositoryImpl
import de.luh.hci.mi.myrecorder.play.AudioPlayer
import de.luh.hci.mi.myrecorder.play.AudioPlayerImpl
import de.luh.hci.mi.myrecorder.record.AudioRecorder
import de.luh.hci.mi.myrecorder.record.AudioRecorderImpl
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

data class LatLon(val lat: Double, val lon: Double)

class MyRecorder : Application() {

    // Wraps an SQLite database.
    private lateinit var database: Database

    // SQLite will be called from a single worker thread.
    private val databaseDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    // Repository for audio recordings.
    lateinit var recordingsRepository: RecordingsRepository
        private set

    // Repository for places.
    lateinit var placesRepository: PlacesRepository
        private set

    // Playback audio
    val audioPlayer: AudioPlayer = AudioPlayerImpl(this)

    // Recording audio.
    val audioRecorder: AudioRecorder = AudioRecorderImpl(this)

    override fun onCreate() {
        super.onCreate()
        val databaseFile = applicationContext.getDatabasePath("recordings.db")
        log("databaseFile: $databaseFile")
        database = Database(databaseFile)
        recordingsRepository = RecordingsRepositoryImpl(
            filesDir, database, databaseDispatcher
        )
        placesRepository = PlacesRepositoryImpl(
            this, database, databaseDispatcher
        )
    }

    // Logs a debug message.
    private fun log(msg: String) {
        Log.d(javaClass.simpleName, msg)
    }

}
