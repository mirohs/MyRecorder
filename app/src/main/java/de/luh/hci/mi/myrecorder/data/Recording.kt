package de.luh.hci.mi.myrecorder.data

import java.io.File

data class Recording(
    val id: Long = 0,      // database primary key
    val timestamp: Long,   // seconds since epoch of 1970-01-01
    val startDate: String, // 03.07.25
    val startTime: String, // 08:36
    val duration: String,  // 02:34
    val file: File,        // /data/.../12345678.m4a
    val latitude: String,  // ...
    val longitude: String, // ...
    val place: String      // Stand 1

) {
    override fun toString(): String {
        return "($id, $timestamp, $startDate, $startTime, $duration, $file, $latitude, $longitude, $place)"
    }
}
