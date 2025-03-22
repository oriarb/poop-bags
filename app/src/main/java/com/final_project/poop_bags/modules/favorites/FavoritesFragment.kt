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
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.favoritePosts.collect { posts ->
                    updateFavoritesList(posts)
                }
            }
        }
    }

    private fun updateFavoritesList(posts: List<Post>) {
        binding.favoritesContainer.removeAllViews()
        
        if (posts.isEmpty()) {
            binding.emptyStateText.visibility = View.VISIBLE
            binding.scrollView.visibility = View.GONE
        } else {
            binding.emptyStateText.visibility = View.GONE
            binding.scrollView.visibility = View.VISIBLE
            
            posts.forEach { post ->
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
                        viewModel.isPostLiked(post.postId).collect { isLiked ->
                            favoriteItem.bind(
                                post = post.copy(isFavorite = true),
                                config = PostItemView.ViewConfig(
                                    isFavorite = true,
                                    isLikeEnabled = true
                                ),
                                onFavoriteClick = { viewModel.removeFromFavorites(it) },
                                onLikeClick = { viewModel.toggleLike(it) },
                                isLiked = isLiked
                            )
                        }
                    }
                }
                
                binding.favoritesContainer.addView(favoriteItem)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 