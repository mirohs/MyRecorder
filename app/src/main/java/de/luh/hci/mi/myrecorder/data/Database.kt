package de.luh.hci.mi.myrecorder.data

import android.content.ContentValues
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
import android.database.sqlite.SQLiteDatabase.openOrCreateDatabase
import android.util.Log
import androidx.core.database.sqlite.transaction
import de.luh.hci.mi.myrecorder.LatLon
import java.io.File

// An SQLite database that stores recording and location information.
// https://www.sqlite.org/lang.html
// officially: Use Room abstraction layer rather than SQLite directly.
// Room is an object-relational mapping library.
// https://developer.android.com/topic/architecture/data-layer
// https://developer.android.com/training/data-storage/room
// https://developer.android.com/reference/android/database/sqlite/package-summary
class Database(databaseFile: File) {

    // Represents the underlying SQLite database instance.
    // https://developer.android.com/training/data-storage/sqlite
    private val db: SQLiteDatabase = openOrCreateDatabase(databaseFile, null)

    // Initializes the database object by creating tables if necessary.
    init {
        log("end Database::init")

        db.setForeignKeyConstraintsEnabled(true)
        // alternative: db.execSQL("PRAGMA foreign_keys = ON")

        var sql = "CREATE TABLE IF NOT EXISTS recording (" +
                "id INTEGER PRIMARY KEY, " +
                "timestamp INTEGER NOT NULL, " +
                "startDate TEXT NOT NULL, " +
                "startTime TEXT NOT NULL, " +
                "duration TEXT NOT NULL, " +
                "filename TEXT NOT NULL, " +
                "latitude TEXT NOT NULL, " +
                "longitude TEXT NOT NULL, " +
                "place TEXT NOT NULL)"
        db.execSQL(sql)

        // delete all rows
        // db.delete("recording", null, arrayOf())

        sql = "CREATE TABLE IF NOT EXISTS place (" +
                "id INTEGER PRIMARY KEY, " +
                "name TEXT NOT NULL, " +
                "latitude TEXT NOT NULL, " +
                "longitude TEXT NOT NULL, " +
                "radius INTEGER NOT NULL)"
        db.execSQL(sql)

        sql = "CREATE TABLE IF NOT EXISTS selected_place (" +
                "place_id INTEGER UNIQUE REFERENCES place(id) ON DELETE CASCADE)"
        //    "CHECK ((place_id IS NULL) OR (place_id IN (SELECT id FROM items)))"
        db.execSQL(sql)

        sql = "CREATE TABLE IF NOT EXISTS map (" +
                "latitude REAL NOT NULL, " +
                "longitude REAL NOT NULL, " +
                "zoom REAL NOT NULL)"
        db.execSQL(sql)

        log("end Database::init")
    }

    // Inserts a recording into the database.
    fun insertRecording(recording: Recording): Long {
        log("insertRecording: $recording")
        val values = ContentValues().apply {
            put("timestamp", recording.timestamp)
            put("startDate", recording.startDate)
            put("startTime", recording.startTime)
            put("duration", recording.duration)
            put("filename", recording.file.canonicalPath)
            put("latitude", recording.latitude)
            put("longitude", recording.longitude)
            put("place", recording.place)
        }
        val id = db.insertWithOnConflict("recording", null, values, CONFLICT_REPLACE)
        log("insert into recording: id = $id")
        if (id == -1L) {
            throw android.database.SQLException("could not insert recording")
        }
        return id
    }

    // Removes a recording from the database.
    fun deleteRecording(id: Long): Boolean {
        log("deleteRecording: $id")
        // DELETE FROM recording WHERE filename = ?
        val deletedCount = db.delete("recording", "id = ?", arrayOf(id.toString()))
        log("deletedCount: $deletedCount")
        return deletedCount > 0
    }

    // Returns the recording with the given ID.
    fun recording(id: Long): Recording? {
        log("latestRecording")
        val cursor = db.rawQuery("SELECT * FROM recording WHERE id = ?", arrayOf(id.toString()))
        cursor.use {
            return if (cursor.moveToFirst())
                Recording(
                    cursor.getLong(0), // cursor.getColumnIndexOrThrow("id")
                    cursor.getLong(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    File(cursor.getString(5)),
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8),
                ) else null
        }
    }

    // Returns the latest (newest) recording.
    fun latestRecording(): Recording? {
        log("latestRecording")
        // SELECT * FROM recording WHERE timestamp = (SELECT MAX(timestamp) FROM recording)
        // SELECT * FROM recording ORDER BY id DESC LIMIT 1
        val cursor = db.rawQuery("SELECT * FROM recording ORDER BY id DESC LIMIT 1", null)
        cursor.use {
            return if (cursor.moveToFirst())
                Recording(
                    cursor.getLong(0),
                    cursor.getLong(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    File(cursor.getString(5)),
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8),
                ) else null
        }
    }

    // Returns the recordings in the database.
    fun recordings(): List<Recording> {
        val cursor = db.rawQuery("SELECT * FROM recording ORDER BY timestamp", null)
        cursor.use {
            // for (col in cursor.columnNames) log(col)
            val recordings = mutableListOf<Recording>()
            while (cursor.moveToNext()) {
                recordings.add(
                    Recording(
                        cursor.getLong(0),
                        cursor.getLong(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        File(cursor.getString(5)),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getString(8),
                    )
                )
            }
            // for (r in recordings) log(r.toString())
            return recordings
        }
    }

    // Returns the number of recordings.
    fun recordingsCount(): Int {
        /*
        val cursor = db.rawQuery("SELECT count(id) FROM recording", null)
        cursor.use {
            return if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
        */
        return DatabaseUtils.queryNumEntries(db, "recording").toInt()
    }

    // Adds the given place.
    fun insertPlace(place: Place): Long {
        log("insertPlace: $place")
        val values = ContentValues().apply {
            put("name", place.name)
            put("latitude", place.location.lat.toString())
            put("longitude", place.location.lon.toString())
            put("radius", place.radius)
        }
        val id = db.insertWithOnConflict("place", null, values, CONFLICT_REPLACE)
        log("insert into place: id = $id")
        if (id == -1L) {
            throw android.database.SQLException("could not insert place")
        }
        return id
    }

    // Updates the given place.
    fun updatePlace(place: Place) {
        log("updatePlace: $place")
        val values = ContentValues().apply {
            put("name", place.name)
            put("latitude", place.location.lat.toString())
            put("longitude", place.location.lon.toString())
            put("radius", place.radius)
        }
        val count = db.update("place", values, "id = ?", arrayOf(place.id.toString()))
        log("update place: count = $count")
        if (count != 1) {
            throw android.database.SQLException("could not update place")
        }
    }

    // Returns the place with the given ID.
    fun place(id: Long): Place? {
        log("getPlace")
        val cursor = db.rawQuery("SELECT * FROM place WHERE id = ?", arrayOf(id.toString()))
        cursor.use {
            return if (cursor.moveToFirst())
                Place(
                    cursor.getLong(0),
                    cursor.getString(1),
                    LatLon(
                        cursor.getString(2).toDouble(),
                        cursor.getString(3).toDouble()
                    ),
                    cursor.getInt(4),
                ) else null
        }
    }

    // Deletes the place with the given ID.
    fun deletePlace(id: Long): Boolean {
        log("deletePlace: $id")
        // DELETE FROM place WHERE id = ?
        val deletedCount = db.delete("place", "id = ?", arrayOf(id.toString()))
        log("deletedCount: $deletedCount")
        return deletedCount > 0
    }

    // Returns the places available in the database.
    fun places(): List<Place> {
        log("getPlaces")
        val cursor = db.rawQuery("SELECT * FROM place ORDER BY name", null)
        log("cursor.count = ${cursor.count}")
        cursor.use {
            // for (col in cursor.columnNames) log(col)
            val places = mutableListOf<Place>()
            while (cursor.moveToNext()) {
                places.add(
                    Place(
                        cursor.getLong(0),
                        cursor.getString(1),
                        LatLon(
                            cursor.getString(2).toDouble(),
                            cursor.getString(3).toDouble()
                        ),
                        cursor.getInt(4),
                    )
                )
            }
            // for (r in places) log(r.toString())
            return places
        }
    }

    // Returns the number of places in the database.
    fun placesCount(): Int {
        return DatabaseUtils.queryNumEntries(db, "place").toInt()
    }

    // Selects the place with the given ID. Either one or no place may be selected at any given time.
    fun selectPlace(id: Long) {
        db.transaction {
            // DELETE FROM selected_place
            delete("selected_place", null, null)

            // INSERT INTO selected_place (place_id) VALUES (?);
            val values = ContentValues()
            values.put("place_id", id)
            insertOrThrow("selected_place", null, values)
        }
    }

    // Deselects any selected place.
    fun deselectPlace() {
        db.delete("selected_place", null, null)
    }

    // Returns the ID of the selected place or null if no place is selected.
    fun selectedPlace(): Long? {
        val cursor = db.rawQuery("SELECT * FROM selected_place LIMIT 1", null)
        cursor.use {
            return if (cursor.moveToFirst()) cursor.getLong(0) else null
        }
    }

    // Updates the map position.
    fun updateMapPosition(pos: MapPosition) {
        db.transaction {
            delete("map", null, null)
            val values = ContentValues().apply {
                put("latitude", pos.latitude)
                put("longitude", pos.longitude)
                put("zoom", pos.zoom)
            }
            insertOrThrow("map", null, values)
        }
    }

    // Returns the current map position or null if not map position is stored in the database.
    fun mapPosition(): MapPosition? {
        val cursor = db.rawQuery("SELECT * FROM map LIMIT 1", null)
        cursor.use {
            return if (cursor.moveToFirst())
                MapPosition(
                    cursor.getDouble(0),
                    cursor.getDouble(1),
                    cursor.getDouble(2),
                )
            else null
        }
    }

    // Logs a debug message.
    private fun log(msg: String) {
        Log.d(javaClass.simpleName, msg)
    }

}
