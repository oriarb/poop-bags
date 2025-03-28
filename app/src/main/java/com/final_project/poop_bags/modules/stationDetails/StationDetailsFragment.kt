package com.final_project.poop_bags.modules.stationDetails

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.final_project.poop_bags.R
import com.final_project.poop_bags.databinding.FragmentStationDetailsBinding
import com.final_project.poop_bags.models.Comment
import com.final_project.poop_bags.models.Station
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StationDetailsFragment : Fragment() {

    companion object {
        const val ARG_STATION_ID = "stationId"
    }

    private var _binding: FragmentStationDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StationDetailsViewModel by viewModels()
    private var isLiked: Boolean = false
    private var isFavourite: Boolean = false

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
            viewModel.getCurrentUser()
        } else {
            Toast.makeText(requireContext(), "Station ID is missing", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }

        hideBottomNavBar()
        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.sendCommentBtn.setOnClickListener {
            val comment = binding.commentInput.text.toString()
            if (comment.isNotEmpty()) {
                viewModel.addComment(comment)
                binding.commentInput.text.clear()
            }
        }
    }

    private fun setupObservers() {
        viewModel.station.observe(viewLifecycleOwner) { station ->
            if (station != null) {
                updateUI(station)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

        viewModel.currentUser.observe(viewLifecycleOwner) { currentUser ->
            if (currentUser != null) {
                context?.let {
                    Glide.with(it)
                        .load(currentUser.image)
                        .transform(CenterCrop(), RoundedCorners(100))
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(binding.currentUserImage)
                }
            }
        }
    }

    private fun updateUI(station: Station) {
        binding.stationName.text = station.name
        context?.let {
            Glide.with(it)
                .load(station.imageUrl)
                .transform(CenterCrop(), RoundedCorners(16))
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(binding.stationImage)
        }
        isLiked = viewModel.isStationLiked.value ?: false
        isFavourite = viewModel.isStationFavourite.value ?: false
        setupLikes(station)
        setupFavourite(station)
        displayComments(station.comments)
    }

    private fun setupLikes(station: Station) {
        binding.likesContainer.apply {
            setOnClickListener {
                viewModel.toggleLike(station.id)
                updateLikesUI(station)
            }
        }
        updateLikesUI(station)
    }

    private fun setupFavourite(station: Station) {
        binding.favouriteContainer.apply {
            setOnClickListener {
                viewModel.toggleFavorite(station.id)
                updateFavouriteUI()
            }
        }
        updateFavouriteUI()
    }

    @SuppressLint("SetTextI18n")
    private fun updateLikesUI(station: Station) {
        binding.likesCount.text = station.likes.size.toString()
        if (isLiked) {
            binding.heartIcon.setImageResource(R.drawable.ic_heart_filled)
        } else {
            binding.heartIcon.setImageResource(R.drawable.ic_heart_outline)
        }
    }

    private fun updateFavouriteUI() {
        if (isFavourite) {
            binding.starIcon.setImageResource(R.drawable.ic_star_filled)
        } else {
            binding.starIcon.setImageResource(R.drawable.ic_star_outline)
        }
    }

    private fun displayComments(comments: List<Comment>) {
        binding.commentsContainer.removeAllViews()

        comments.forEach { comment ->
            viewModel.fetchUser(comment.userId) { user ->
                val commentLayout = LinearLayout(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 0, 0, 16)
                    }
                    orientation = LinearLayout.HORIZONTAL
                    setBackgroundResource(R.drawable.comment_background)
                    setPadding(16, 8, 16, 8)
                }

                val commentUserImage = ImageView(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                        marginEnd = 32
                    }
                    Glide.with(this@StationDetailsFragment)
                        .load(user?.image)
                        .circleCrop()
                        .placeholder(R.drawable.default_profile)
                        .into(this)
                }

                val commentTextView = TextView(requireContext()).apply {
                    text = comment.text
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                    textSize = 24f
                    gravity = Gravity.START or Gravity.CENTER_VERTICAL
                }

                commentLayout.addView(commentUserImage)
                commentLayout.addView(commentTextView)
                binding.commentsContainer.addView(commentLayout)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        showBottomNavBar()
        _binding = null
    }

    private fun hideBottomNavBar() {
        val bottomNavBar = activity?.findViewById<BottomNavigationView>(R.id.nav_view)
        bottomNavBar?.visibility = View.GONE
    }

    private fun showBottomNavBar() {
        val bottomNavBar = activity?.findViewById<BottomNavigationView>(R.id.nav_view)
        bottomNavBar?.visibility = View.VISIBLE
    }
}