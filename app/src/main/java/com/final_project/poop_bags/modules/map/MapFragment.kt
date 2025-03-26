package com.final_project.poop_bags.modules.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.final_project.poop_bags.R
import com.final_project.poop_bags.models.Station
import com.final_project.poop_bags.utils.BitmapUtil

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MapFragment: Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private val viewModel: MapViewModel by viewModels()
    private var selectedStationCircle: Circle? = null

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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

        initStations()

        viewModel.currentLocation.observe(viewLifecycleOwner) { location ->
            location?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f), 1500, null)
            }
        }

        map.setOnMyLocationButtonClickListener {
            viewModel.fetchCurrentLocation()
            false
        }

        viewModel.selectedStation.observe(viewLifecycleOwner, { station ->
            station?.let { selectStation(it) }
        })

        map.setOnMarkerClickListener { marker ->
            val station = marker.tag as Station
            station.let {
                viewModel.selectStation(it)
            }
            true
        }

        map.setOnMapClickListener { _ ->
            viewModel.clearSelectedStation()
            selectedStationCircle?.remove()
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
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun initStations() {
        viewLifecycleOwner.lifecycleScope.launch { viewModel.stations.collect { stations ->
            stations.forEach { station ->
                initStation(station)
            }
        } }
    }

    private fun initStation(station: Station) {
        val latLng = LatLng(station.latitude, station.longitude)
        val poopIcon = BitmapUtil.resizeBitmap(requireContext(), R.drawable.ic_poop_fill, 70, 70)
        val marker = map.addMarker(MarkerOptions()
            .position(latLng)
            .title(station.name)
            .icon(poopIcon))
        marker?.tag = station

        map.setOnCameraIdleListener {
            val zoomLevel = map.cameraPosition.zoom
            val newSize = BitmapUtil.calculateSizeByZoom(zoomLevel)
            val newIcon = BitmapUtil.resizeBitmap(requireContext(), R.drawable.ic_poop_fill, newSize, newSize)
            marker?.setIcon(newIcon)
        }
    }

    private fun selectStation(station: Station) {
        addStationCircle(station.latitude, station.longitude)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(station.latitude, station.longitude), 18f), 1500, null)
//        val fragment = StationDetails.newInstance(station)
//        fragment.show(parentFragmentManager, "StationDetails")
    }

    private fun addStationCircle(latitude: Double, longitude: Double) {
        map.setOnCameraIdleListener {
            if (viewModel.selectedStation.value != null) {
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
                    )            }
            }
        }
    }
}