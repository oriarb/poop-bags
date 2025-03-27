package com.final_project.poop_bags.modules.map

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.final_project.poop_bags.models.Station
import com.final_project.poop_bags.repository.StationRepository
import com.final_project.poop_bags.utils.LocationUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(private val locationUtil: LocationUtil, private val stationRepository: StationRepository
): ViewModel() {

    val stations = stationRepository.allStations.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        emptyList()
    )

    private val _selectedStation = MutableLiveData<Station?>()
    val selectedStation: LiveData<Station?> = _selectedStation

    private val _currentLocation = MutableLiveData<Location?>()
    val currentLocation: LiveData<Location?> = _currentLocation

    fun selectStation(station: Station) {
        _selectedStation.value = station
    }

    fun clearSelectedStation() {
        _selectedStation.value = null
    }

    fun fetchCurrentLocation() {
        viewModelScope.launch {
             _currentLocation.value = locationUtil.getCurrentLocation()
        }
    }
}