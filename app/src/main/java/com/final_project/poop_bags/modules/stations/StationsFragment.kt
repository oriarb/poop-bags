package com.final_project.poop_bags.modules.stations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.final_project.poop_bags.R
import com.final_project.poop_bags.common.views.StationItemView
import com.final_project.poop_bags.databinding.FragmentStationsBinding
import com.final_project.poop_bags.models.Station
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.widget.Toast

@AndroidEntryPoint
class StationsFragment : Fragment() {

    private var _binding: FragmentStationsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: StationsViewModel by viewModels()
    private val args: StationsFragmentArgs by navArgs()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStationsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
        
        args.userId?.let { userId ->
            viewModel.loadUserStations(userId)
        } ?: run {
            viewModel.loadUserStations()
        }
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewModel.stations.observe(viewLifecycleOwner) { stations ->
            updateStationsList(stations)
            binding.progressBar.visibility = View.GONE
        }

        viewModel.success.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearSuccess()
            }
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("success_message")?.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                findNavController().currentBackStackEntry?.savedStateHandle?.remove<String>("success_message")
                viewModel.refreshStations()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun updateStationsList(stations: List<Station>) {
        try {
            binding.stationsContainer.removeAllViews()
            
            if (stations.isEmpty()) {
                binding.emptyStateText.visibility = View.VISIBLE
                binding.scrollView.visibility = View.GONE
            } else {
                binding.emptyStateText.visibility = View.GONE
                binding.scrollView.visibility = View.VISIBLE
                
                stations.forEach { station ->
                    try {
                        val stationItem = StationItemView(requireContext()).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(0, 0, 0, 16)
                            }
                        }
                        
                        binding.stationsContainer.addView(stationItem)
                        bindStation(station, stationItem)
                    } catch (e: Exception) {
                        showError("Error creating station item: ${e.message}")
                    }
                }
            }
            binding.progressBar.visibility = View.GONE
        } catch (e: Exception) {
            showError("Error updating stations list: ${e.message}")
        }
    }

    private fun bindStation(station: Station, stationItem: StationItemView) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isStationLiked(station.id).collect { isLiked ->
                    stationItem.bind(
                        station = station,
                        config = StationItemView.ViewConfig(
                            isDelete = args.userId == null,
                            isEdit = args.userId == null,
                            isLikeEnabled = true,
                            isFavorite = true
                        ),
                        onDeleteClick = { viewModel.deleteStation(it) },
                        onEditClick = {
                            android.util.Log.d("StationsFragment", "Navigating to edit station: ${it.id}")
                            findNavController().navigate(
                                R.id.navigation_edit_station,
                                bundleOf("stationId" to it.id)
                            )
                        },
                        onFavoriteClick = { viewModel.toggleFavorite(it) },
                        onLikeClick = { viewModel.toggleLike(it) },
                        isLiked = isLiked
                    )
                }
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 