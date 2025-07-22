package de.luh.hci.mi.myrecorder.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.CoroutineContext

// Implementation of the repository interface. It relies on a database as a data source,
// which is provided via the constructor (dependency injection).
// https://developer.android.com/topic/architecture/data-layer
class RecordingsRepositoryImpl(
    private val recordingsDirectory: File,
    private val db: Database,
    private val dbDispatcher: CoroutineContext,
) : RecordingsRepository {

    override fun recordingFile(name: String): File {
        return File(recordingsDirectory, name)
    }

    private val _recordings = MutableStateFlow(db.recordings())
    override fun recordings(): Flow<List<Recording>> = _recordings

    private val _recordingsCount = MutableStateFlow(db.recordingsCount())
    override fun recordingsCount(): Flow<Int> = _recordingsCount

    private val _latestRecording = MutableStateFlow(db.latestRecording())
    override fun latestRecordingAsFlow(): Flow<Recording?> = _latestRecording

    override suspend fun addRecording(recording: Recording): Long {
        log("storeRecording: " + Thread.currentThread().name)
        return withContext(dbDispatcher) {
            log("storeRecording2: " + Thread.currentThread().name)
            val id = db.insertRecording(recording)
            val recordings = db.recordings()
            _recordings.value = recordings
            _recordingsCount.value = recordings.size
            _latestRecording.value = latestRecording()
            id
        }
    }

    override suspend fun deleteRecording(id: Long) {
        withContext(dbDispatcher) {
            val recording = db.recording(id)
            recording?.file?.delete()
            val deleteLatest = id == latestRecording()?.id
            if (db.deleteRecording(id)) {
                val recordings = db.recordings()
                _recordings.value = recordings
                _recordingsCount.value = recordings.size
                if (deleteLatest) {
                    _latestRecording.value = latestRecording()
                }
            }
        }
    }

    override suspend fun recording(id: Long): Recording? =
        withContext(dbDispatcher) {
            db.recording(id)
        }

    override suspend fun latestRecording(): Recording? =
        withContext(dbDispatcher) {
            db.latestRecording()
        }

    // Logs a debug message.
    private fun log(msg: String) {
        Log.d(javaClass.simpleName, msg)
    }

}