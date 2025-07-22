package de.luh.hci.mi.myrecorder.ui.places

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import de.luh.hci.mi.myrecorder.LatLon
import de.luh.hci.mi.myrecorder.R
import de.luh.hci.mi.myrecorder.data.MapPosition
import de.luh.hci.mi.myrecorder.data.Place
import de.luh.hci.mi.myrecorder.ui.DiscreteSlider
import de.luh.hci.mi.myrecorder.ui.IconButton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.take
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay

@Composable
fun PlacesScreen(
    viewModel: PlacesViewModel
) {
    Box(modifier = Modifier.fillMaxSize()) {
        OsmMapWithPlaces(
            viewModel.places,
            viewModel.selectedPlaceId,
            viewModel::addPlace,
            viewModel::selectPlace,
            viewModel.mapPosition,
            viewModel::updateMapPosition
        )
        val focusManager = LocalFocusManager.current
        viewModel.selectedPlace?.let { _ ->
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .fillMaxWidth()
                    .padding(8.dp)
                    .align(Alignment.TopCenter)
                    .pointerInput(Unit) { // prevent touch events falling through to map
                        detectTapGestures(onPress = { tryAwaitRelease() })
                    }
            ) {
                OutlinedTextField(
                    value = viewModel.name,
                    onValueChange = { viewModel.name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Name") },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        viewModel.updatePlace()
                    }),
                    singleLine = true
                )
                Text("Radius: ${viewModel.getRadius()} m")
                DiscreteSlider(
                    tickValues = viewModel.radiusTicks,
                    sliderPosition = viewModel.radiusIndex,
                    setValue = { viewModel.updatePlace() }
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton("Delete", Icons.Filled.Delete, 24.dp, onClick = {
                        focusManager.clearFocus()
                        viewModel.deletePlace()
                    })
                }
            }
        }
    }
}

@Composable
fun OsmMapWithPlaces(
    places: Flow<List<Place>>,
    selectedPlaceId: Flow<Long?>,
    addPlace: (location: LatLon) -> Unit,
    selectPlace: (place: Place) -> Unit,
    mapPosition: Flow<MapPosition?>,
    saveMapPosition: (pos: MapPosition) -> Unit
) {
    println("OsmMapWithPlaces")
    lateinit var mapView: MapView
    val context = LocalContext.current
    val locationOutlined = context.getDrawable(R.drawable.location_outlined)!!
    val locationFilled = context.getDrawable(R.drawable.location_filled)!!
    val focusManager = LocalFocusManager.current

    // Create OSM MapView, wrapped in AndroidView
    AndroidView(
        factory = {
            @Suppress("DEPRECATION")
            Configuration.getInstance().load(
                context,
                android.preference.PreferenceManager.getDefaultSharedPreferences(context)
            )
            println("creating MapView")
            mapView = MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                // Listen to map panning and zooming.
                addMapListener(object : MapListener {
                    override fun onScroll(event: ScrollEvent?): Boolean {
                        focusManager.clearFocus()
                        val pos = mapCenter
                        val zoom = zoomLevelDouble
                        saveMapPosition(MapPosition(pos.latitude, pos.longitude, zoom))
                        // Log.d("MapListener", "onScroll: ${pos.latitude}, ${pos.longitude} $zoom")
                        return true
                    }

                    override fun onZoom(event: ZoomEvent?): Boolean {
                        focusManager.clearFocus()
                        val pos = mapCenter
                        val zoom = zoomLevelDouble
                        saveMapPosition(MapPosition(pos.latitude, pos.longitude, zoom))
                        // Log.d("MapListener", "onZoom: ${pos.latitude}, ${pos.longitude} $zoom")
                        return true
                    }
                })
            }
            println("assigned map view")
            // Listen to taps somewhere on the map.
            mapView.overlays.add(object : Overlay() {
                override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                    println("Overlay::onSingleTapConfirmed $e")
                    focusManager.clearFocus()
                    val projection = mapView.projection
                    val loc = projection.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint
                    addPlace(LatLon(loc.latitude, loc.longitude))
                    return true
                }
            })
            mapView
        },
        modifier = Modifier.fillMaxSize()
    )

    println("before LaunchedEffect for combined")
    // Update the map view if the list of places or the selected place changes.
    LaunchedEffect(Unit) {
        println("LaunchedEffect for combined")
        val combined = places.combine(selectedPlaceId) { place, selectedPlace ->
            Pair(place, selectedPlace)
        }
        combined.collect { pair ->
            val places = pair.first
            val selectedPlaceId = pair.second
            println("collect $places, $selectedPlaceId")
            mapView.overlays.removeAll { it is Marker }
            for (place in places) {
                val marker = Marker(mapView).apply {
                    icon = if (place.id == selectedPlaceId) locationFilled else locationOutlined
                    position = GeoPoint(place.location.lat, place.location.lon)
                    title = place.name
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    // Listen to taps on a place marker.
                    setOnMarkerClickListener { marker, mapView ->
                        focusManager.clearFocus()
                        println("Marker::onMarkerClick: $marker $place")
                        // marker.showInfoWindow()
                        selectPlace(place)
                        true
                    }
                }
                mapView.overlays.add(marker)
            }
            mapView.invalidate()
        }
    }

    // Restore a certain map position (including zoom level) after initialization.
    println("before LaunchedEffect for mapPosition")
    LaunchedEffect(Unit) {
        println("LaunchedEffect for mapPosition")
        mapPosition.take(1).collect { pos ->
            println("collect mapPosition: $pos")
            val controller = mapView.controller
            if (pos != null && controller != null) {
                controller.setZoom(pos.zoom)
                controller.setCenter(GeoPoint(pos.latitude, pos.longitude))
            }
        }
        println("LaunchedEffect for mapPosition: done")
    }

}
