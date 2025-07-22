package de.luh.hci.mi.myrecorder.ui.places

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.luh.hci.mi.myrecorder.LatLon
import de.luh.hci.mi.myrecorder.MyRecorder
import de.luh.hci.mi.myrecorder.data.MapPosition
import de.luh.hci.mi.myrecorder.data.Place
import de.luh.hci.mi.myrecorder.data.PlacesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

private const val DEFAULT_NAME = ""
private const val DEFAULT_RADIUS = 100

class PlacesViewModel(
    private val repository: PlacesRepository
) : ViewModel() {

    val places: Flow<List<Place>> = repository.places()
    var selectedPlace: Place? by mutableStateOf(null)
    val selectedPlaceId: Flow<Long?> = repository.selectedPlaceAsFlow()
    val mapPosition: Flow<MapPosition?> = repository.mapPositionAsFlow()

    init {
        viewModelScope.launch {
            selectedPlaceId.collect { id ->
                if (id != null) {
                    val place = repository.place(id)
                    selectedPlace = place
                    if (place != null) {
                        name = place.name
                        radiusIndex.intValue = radiusToIndex(place.radius)
                    }
                } else {
                    selectedPlace = null
                }
            }
        }
    }

    var name by mutableStateOf(DEFAULT_NAME)
    val radiusTicks = listOf(10, 50, 100, 250, 500, 1000)
    val radiusIndex = mutableIntStateOf(2)

    fun getRadius(): Int {
        return radiusTicks[radiusIndex.intValue]
    }

    fun radiusToIndex(radius: Int) =
        radiusTicks.binarySearch(radius)

    fun deletePlace() {
        selectedPlace?.let {
            viewModelScope.launch {
                repository.deletePlace(it.id)
                repository.deselectPlace()
            }
        }
    }

    fun addPlace(location: LatLon) {
        viewModelScope.launch {
            if (repository.selectedPlace() == null) {
                val place = Place(0, DEFAULT_NAME, location, DEFAULT_RADIUS)
                val id = repository.addPlace(place)
                repository.selectPlace(id)
            } else {
                repository.deselectPlace()
            }
        }
    }

    fun updatePlace() {
        selectedPlace?.let {
            viewModelScope.launch {
                val updatedPlace = Place(it.id, name, it.location, getRadius())
                repository.updatePlace(updatedPlace)
            }
        }
    }

    fun selectPlace(place: Place) {
        viewModelScope.launch {
            name = place.name
            radiusIndex.intValue = radiusToIndex(place.radius)
            repository.selectPlace(place.id)
        }
    }

    fun updateMapPosition(pos: MapPosition) {
        viewModelScope.launch {
            repository.updateMapPosition(pos)
        }
    }

    /*// Logs a debug message.
    private fun log(msg: String) {
        Log.d(this.javaClass.simpleName, msg)
    }*/

    companion object {
        // Companion object for creating the view model in the right lifecycle scope.
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyRecorder
                PlacesViewModel(app.placesRepository)
            }
        }
    }

}
