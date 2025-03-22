package com.final_project.poop_bags.modules.favorites

import com.final_project.poop_bags.modules.PostItemView.PostItemView
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.final_project.poop_bags.models.Post
import com.final_project.poop_bags.databinding.FragmentFavoritesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CancellationException

@AndroidEntryPoint
class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: FavoritesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        try {
            binding.btnBack.setOnClickListener {
                findNavController().navigateUp()
            }
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Navigation error: ${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun observeViewModel() {
        try {
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    try {
                        viewModel.favoritePosts.collect { posts ->
                            updateFavoritesList(posts)
                        }
                    } catch (e: CancellationException) {
                        // Ignore cancellation exceptions
                    } catch (e: Exception) {
                        Snackbar.make(binding.root, "Error loading favorites: ${e.message}", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Error in view model observation: ${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun updateFavoritesList(posts: List<Post>) {
        try {
            binding.favoritesContainer.removeAllViews()
            
            if (posts.isEmpty()) {
                binding.emptyStateText.visibility = View.VISIBLE
                binding.scrollView.visibility = View.GONE
            } else {
                binding.emptyStateText.visibility = View.GONE
                binding.scrollView.visibility = View.VISIBLE
                
                posts.forEach { post ->
                    try {
                        val favoriteItem = PostItemView(requireContext()).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(0, 0, 0, 16)
                            }
                        }
                        
                        viewLifecycleOwner.lifecycleScope.launch {
                            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                try {
                                    viewModel.isPostLiked(post.postId).collect { isLiked ->
                                        favoriteItem.bind(
                                            post = post.copy(isFavorite = true),
                                            config = PostItemView.ViewConfig(
                                                isFavorite = true,
                                                isLikeEnabled = true
                                            ),
                                            onFavoriteClick = { 
                                                try {
                                                    viewModel.removeFromFavorites(it)
                                                } catch (e: Exception) {
                                                    Snackbar.make(binding.root, "Error removing from favorites: ${e.message}", Snackbar.LENGTH_LONG).show()
                                                }
                                            },
                                            onLikeClick = { 
                                                try {
                                                    viewModel.toggleLike(it)
                                                } catch (e: Exception) {
                                                    Snackbar.make(binding.root, "Error toggling like: ${e.message}", Snackbar.LENGTH_LONG).show()
                                                }
                                            },
                                            isLiked = isLiked
                                        )
                                    }
                                } catch (e: Exception) {
                                    if (e.javaClass.simpleName != "CancellationException") {
                                        Snackbar.make(binding.root, "Error checking if post is liked: ${e.message}", Snackbar.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                        
                        binding.favoritesContainer.addView(favoriteItem)
                    } catch (e: Exception) {
                        Snackbar.make(binding.root, "Error creating favorite item: ${e.message}", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Error updating favorites list: ${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 