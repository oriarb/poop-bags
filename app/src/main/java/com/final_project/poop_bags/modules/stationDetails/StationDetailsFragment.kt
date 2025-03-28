package com.final_project.poop_bags.modules.stationDetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.final_project.poop_bags.databinding.FragmentStationDetailsBinding
import com.final_project.poop_bags.models.Comment
import com.final_project.poop_bags.models.Station
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StationDetailsFragment : Fragment() {

    companion object {
        const val ARG_STATION_ID = "stationId"
    }

    private var _binding: FragmentStationDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StationDetailsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStationDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val stationId = arguments?.getString(ARG_STATION_ID)

        if (stationId != null) {
            viewModel.fetchStation(stationId)
        } else {
            // Handle the case where stationId is null
            Toast.makeText(requireContext(), "Station ID is missing", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }

        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupObservers() {
        viewModel.station.observe(viewLifecycleOwner) { station ->
            if (station != null) {
                updateUI(station)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Show/hide loading indicator
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    private fun updateUI(station: Station) {
        binding.stationName.text = station.name
        // ... update other UI elements with station data ...

        // Display comments
        displayComments(station.comments ?: emptyList())
    }

    private fun displayComments(comments: List<Comment>) {
        // Clear any previous comments
        binding.commentsContainer.removeAllViews()

        for (comment in comments) {
            // Create a TextView for the comment text
            val commentTextView = TextView(requireContext()).apply {
                text = comment.text
                // ... set other properties (e.g., padding, text size)
            }

            // Add the comment to the container
            binding.commentsContainer.addView(commentTextView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}