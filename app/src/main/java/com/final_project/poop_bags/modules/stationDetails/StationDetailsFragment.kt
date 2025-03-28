package com.final_project.poop_bags.modules.stationDetails

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.final_project.poop_bags.R
import com.final_project.poop_bags.databinding.FragmentStationDetailsBinding
import com.final_project.poop_bags.models.Comment
import com.final_project.poop_bags.models.Station
import com.squareup.picasso.Picasso
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

    @SuppressLint("SetTextI18n")
    private fun updateUI(station: Station) {
        binding.stationName.text = station.name
        binding.likesCount.text = "${station.likes.size} Likes"

        Picasso.get()
            .load(station.imageUrl)
            .placeholder(R.drawable.ic_missing_image)
            .error(R.drawable.ic_missing_image)
            .into(binding.stationImage)

        displayComments(station.comments)
    }

    private fun displayComments(comments: List<Comment>) {
        binding.commentsContainer.removeAllViews()

        for (comment in comments) {
            val commentLayout = LinearLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 8) // Add some bottom margin between comments
                }
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.comment_background))
                setPadding(16, 16, 16, 16) // Add padding inside the comment
            }

            val commentTextView = TextView(requireContext()).apply {
                text = comment.text
            }

            commentLayout.addView(commentTextView)
            binding.commentsContainer.addView(commentLayout)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}