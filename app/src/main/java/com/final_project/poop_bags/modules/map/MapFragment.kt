package com.final_project.poop_bags.modules.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.final_project.poop_bags.R
import com.final_project.poop_bags.models.Station
import com.final_project.poop_bags.modules.stationPopup.StationPopupFragment
import com.final_project.poop_bags.utils.BitmapUtil
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private val viewModel: MapViewModel by viewModels()

    private var selectedStationCircle: Circle? = null
    private val stationMarkers = mutableMapOf<String, Marker>()

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            enableMapLocation()
            viewModel.fetchCurrentLocation()
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_map, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        requestLocationPermission()
        setupMapListeners()
        observeViewModel()
    }

    override fun onPause() {
        super.onPause()
        viewModel.clearSelectedStation()
        selectedStationCircle?.remove()
    }

    private fun observeViewModel() {
        viewModel.currentLocation.observe(viewLifecycleOwner) { location ->
            location?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f), 1500, null)
            }
        }

        viewModel.selectedStation.observe(viewLifecycleOwner) { station ->
            station?.let { selectStation(it) }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.stations.collect { stations ->
                updateStationsOnMap(stations)
            }
        }
    }

    private fun setupMapListeners() {
        map.setOnMyLocationButtonClickListener {
            viewModel.fetchCurrentLocation()
            false
        }

        map.setOnMarkerClickListener { marker ->
            (marker.tag as? Station)?.let {
                viewModel.selectStation(it)
            }
            true
        }

        map.setOnMapClickListener {
            viewModel.clearSelectedStation()
            selectedStationCircle?.remove()
        }

        map.setOnCameraIdleListener {
            val zoomLevel = map.cameraPosition.zoom
            stationMarkers.values.forEach { marker ->
                marker.isVisible = zoomLevel >= 15
            }
        }
    }

    private fun enableMapLocation() {
        if (hasLocationPermission()) {
            try {
                map.isMyLocationEnabled = true
            } catch (e: SecurityException) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Location permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun updateStationsOnMap(stations: List<Station>) {
        val existingIds = stations.map { it.id }
        stationMarkers.keys.filter { it !in existingIds }.forEach { removedId ->
            stationMarkers[removedId]?.remove()
            stationMarkers.remove(removedId)
        }

        stations.forEach { station ->
            if (stationMarkers.containsKey(station.id)) {
                stationMarkers[station.id]?.tag = station
            } else {
                addStationMarker(station)
            }
        }
    }

    private fun addStationMarker(station: Station) {
        val latLng = LatLng(station.latitude, station.longitude)
        val icon = BitmapUtil.resizeBitmap(requireContext(), R.drawable.ic_poop_fill, 70, 70)
        val marker = map.addMarker(
            MarkerOptions().position(latLng).title(station.name).icon(icon).visible(false)
        )

        marker?.let {
            it.tag = station
            stationMarkers[station.id] = it
        }
    }

    private fun selectStation(station: Station) {
        highlightSelectedStation(station.latitude, station.longitude)
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(LatLng(station.latitude, station.longitude), 18f),
            1500, null
        )

        val fragment = StationPopupFragment.newInstance(station)
        fragment.show(parentFragmentManager, "StationPopupFragment")
    }

    private fun highlightSelectedStation(latitude: Double, longitude: Double) {
        selectedStationCircle?.remove()
        val zoomLevel = map.cameraPosition.zoom

        if (zoomLevel >= 17f) {
            selectedStationCircle = map.addCircle(
                CircleOptions()
                    .center(LatLng(latitude, longitude))
                    .radius(20.0)
                    .strokeColor(android.graphics.Color.GRAY)
                    .strokeWidth(5f)
                    .fillColor(android.graphics.Color.argb(40, 128, 128, 128))
            )
        }
    }
}
