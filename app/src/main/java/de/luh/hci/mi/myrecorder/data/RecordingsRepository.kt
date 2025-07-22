package de.luh.hci.mi.myrecorder.data

import kotlinx.coroutines.flow.Flow
import java.io.File

// A repository is an interface to (a category of) the app's data.
// The repository interface isolates the data layer from the rest of the app.
// https://developer.android.com/topic/architecture/data-layer
interface RecordingsRepository {

    // The returned file represents the absolute path and basename of the audio file of a recording.
    fun recordingFile(name: String): File

    // Stores a recording. May throw an IOException.
    suspend fun addRecording(recording: Recording): Long

    // Deletes a recording including the referenced file. May throw an IOException.
    suspend fun deleteRecording(id: Long)

    // Returns the recording with the given ID, if any. May throw an IOException.
    suspend fun recording(id: Long): Recording?

    // Returns the latest (newest) recording, if any. May throw an IOException.
    suspend fun latestRecording(): Recording?

    // Returns the latest (newest) recording, if any. May throw an IOException.
    fun latestRecordingAsFlow(): Flow<Recording?>

    // Returns all recordings. May throw an IOException.
    fun recordings(): Flow<List<Recording>>

    // Returns the number of recordings. May throw an IOException.
    fun recordingsCount(): Flow<Int>
}
