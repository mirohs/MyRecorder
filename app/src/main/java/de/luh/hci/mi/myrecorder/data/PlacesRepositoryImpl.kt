package de.luh.hci.mi.myrecorder.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PlacesRepositoryImpl(
    private val context: Context,
    private val db: Database,
    private val dbDispatcher: CoroutineContext,
) : PlacesRepository {

    private val _places = MutableStateFlow(db.places())
    override fun places(): Flow<List<Place>> = _places

    private val _placesCount = MutableStateFlow(db.placesCount())
    override fun placesCount(): Flow<Int> = _placesCount

    private val _selectedPlace = MutableStateFlow(db.selectedPlace())
    override fun selectedPlaceAsFlow(): Flow<Long?> = _selectedPlace

    override suspend fun addPlace(place: Place): Long =
        withContext(dbDispatcher) {
            val id = db.insertPlace(place)
            val ps = db.places()
            _places.value = ps
            _placesCount.value = ps.size
            id
        }

    override suspend fun updatePlace(place: Place) {
        withContext(dbDispatcher) {
            db.updatePlace(place)
            _places.value = db.places()
        }
    }

    override suspend fun place(id: Long): Place? =
        withContext(dbDispatcher) {
            db.place(id)
        }

    override suspend fun deletePlace(id: Long) =
        withContext(dbDispatcher) {
            val deleteSelected = id == db.selectedPlace()
            if (db.deletePlace(id)) {
                val ps = db.places()
                _places.value = ps
                _placesCount.value = ps.size
                if (deleteSelected) {
                    db.deselectPlace()
                    _selectedPlace.value = null
                }
            }
        }

    override suspend fun selectPlace(id: Long) =
        withContext(dbDispatcher) {
            db.selectPlace(id)
            _selectedPlace.value = id
        }

    override suspend fun deselectPlace() =
        withContext(dbDispatcher) {
            db.deselectPlace()
            _selectedPlace.value = null
        }

    override suspend fun selectedPlace(): Long? =
        withContext(dbDispatcher) {
            db.selectedPlace()
        }

    private val _mapPosition = MutableStateFlow(db.mapPosition())
    override fun mapPositionAsFlow(): Flow<MapPosition?> = _mapPosition

    override suspend fun updateMapPosition(pos: MapPosition) =
        withContext(dbDispatcher) {
            db.updateMapPosition(pos)
            _mapPosition.value = pos
        }

    override suspend fun mapPosition(): MapPosition? =
        withContext(dbDispatcher) {
            db.mapPosition()
        }

    // Returns the current location (or throws an exception).
    override suspend fun currentLocation(): Location = suspendCancellableCoroutine { cont ->
        val hasPermission =
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) {
            cont.resumeWithException(SecurityException("Location permission not granted"))
        } else {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                locationManager.getCurrentLocation(
                    LocationManager.NETWORK_PROVIDER,
                    null,
                    context.mainExecutor
                ) { location ->
                    if (location != null) {
                        cont.resume(location)
                    } else {
                        cont.resumeWithException(IllegalStateException("Location is null"))
                    }
                }
            } else {
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        cont.resume(location)
                        locationManager.removeUpdates(this)
                    }
                }
                @Suppress("DEPRECATION")
                locationManager.requestSingleUpdate(
                    LocationManager.NETWORK_PROVIDER,
                    listener,
                    Looper.getMainLooper()
                )
                cont.invokeOnCancellation {
                    locationManager.removeUpdates(listener)
                }
            }
        }
    }

    // Returns the closest of the set of predefined locations or null if we are not
    // within the radius of one of these locations.
    override suspend fun closestPlace(here: Location): Place? {
        var closest: Place? = null
        var closestDist = 0f
        val results = floatArrayOf(0f)
        for (place in db.places()) {
            Location.distanceBetween(
                here.latitude,
                here.longitude,
                place.location.lat,
                place.location.lon,
                results
            )
            val dist = results[0]
            // log("${place.name}: $dist m")
            if (dist < place.radius) {
                if (closest == null || dist < closestDist) {
                    closest = place
                    closestDist = dist
                }
            }
        }
        return closest
    }

    /*// Logs a debug message.
    private fun log(msg: String) {
        Log.d(javaClass.simpleName, msg)
    }*/

}
