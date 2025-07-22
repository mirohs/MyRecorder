package de.luh.hci.mi.myrecorder.data

import android.location.Location
import de.luh.hci.mi.myrecorder.LatLon
import kotlinx.coroutines.flow.Flow

data class Place(val id: Long = 0, val name: String, val location: LatLon, val radius: Int)
data class MapPosition(val latitude: Double, val longitude: Double, val zoom: Double)

// A repository is an interface to (a category of) the app's data.
// The repository interface isolates the data layer from the rest of the app.
// https://developer.android.com/topic/architecture/data-layer
interface PlacesRepository {
    suspend fun addPlace(place: Place): Long
    suspend fun updatePlace(place: Place)
    suspend fun place(id: Long): Place?
    suspend fun deletePlace(id: Long)
    fun places(): Flow<List<Place>>
    fun placesCount(): Flow<Int>
    suspend fun selectPlace(id: Long)
    suspend fun deselectPlace()
    suspend fun selectedPlace(): Long?
    fun selectedPlaceAsFlow(): Flow<Long?>
    suspend fun updateMapPosition(pos: MapPosition)
    suspend fun mapPosition(): MapPosition?
    fun mapPositionAsFlow(): Flow<MapPosition?>
    suspend fun currentLocation(): Location
    suspend fun closestPlace(here: Location): Place?
}
