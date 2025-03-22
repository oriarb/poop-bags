package com.final_project.poop_bags.modules.posts

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
import com.final_project.poop_bags.R
import com.final_project.poop_bags.databinding.FragmentPostsBinding
import com.final_project.poop_bags.models.Post
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.core.os.bundleOf

@AndroidEntryPoint
class PostsFragment : Fragment() {
    private var _binding: FragmentPostsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PostsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            setupUI()
            observeViewModel()
            observePosts()
        } catch (e: Exception) {
            showError("Error initializing view: ${e.message}")
        }
    }

    private fun setupUI() {
        try {
            binding.btnBack.setOnClickListener {
                try {
                    findNavController().navigateUp()
                } catch (e: Exception) {
                    showError("Navigation error: ${e.message}")
                }
            }
            
            binding.progressBar.visibility = View.VISIBLE
            binding.emptyStateText.visibility = View.GONE
            binding.scrollView.visibility = View.GONE
        } catch (e: Exception) {
            showError("Error setting up UI: ${e.message}")
        }
    }
    
    private fun observeViewModel() {
        try {
            viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
                if (!errorMessage.isNullOrEmpty()) {
                    showError(errorMessage)
                }
            }
        } catch (e: Exception) {
            showError("Error observing view model: ${e.message}")
        }
    }

    private fun observePosts() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    try {
                        viewModel.userPosts.collect { posts ->
                            try {
                                binding.progressBar.visibility = View.GONE
                                updatePostsList(posts)
                            } catch (e: Exception) {
                                showError("Error updating posts list: ${e.message}")
                            }
                        }
                    } catch (e: Exception) {
                        binding.progressBar.visibility = View.GONE
                        binding.emptyStateText.text = getString(R.string.error_loading_posts)
                        binding.emptyStateText.visibility = View.VISIBLE
                        binding.scrollView.visibility = View.GONE
                        showError("Error collecting posts: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                showError("Error in lifecycle scope: ${e.message}")
            }
        }
    }

    private fun updatePostsList(posts: List<Post>) {
        if (!isAdded) return 
        
        try {
            binding.postsContainer.removeAllViews()
            
            if (posts.isEmpty()) {
                binding.emptyStateText.visibility = View.VISIBLE
                binding.scrollView.visibility = View.GONE
                binding.emptyStateText.text = getString(R.string.no_posts)
            } else {
                binding.emptyStateText.visibility = View.GONE
                binding.scrollView.visibility = View.VISIBLE
                
                posts.forEach { post ->
                    try {
                        val postItem = PostItemView(requireContext()).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(0, 0, 0, 16)
                            }
                        }
                        binding.postsContainer.addView(postItem)
                        bindPost(post, postItem)
                    } catch (e: Exception) {
                        showError("Error creating post item: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            showError("Error updating posts list: ${e.message}")
        }
    }

    private fun bindPost(post: Post, postItem: PostItemView) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    try {
                        viewModel.isPostLiked(post.postId).collect { isLiked ->
                            try {
                                postItem.bind(
                                    post = post,
                                    config = PostItemView.ViewConfig(
                                        isDelete = true,
                                        isEdit = true,
                                        isLikeEnabled = true,
                                        isFavorite = true
                                    ),
                                    onDeleteClick = { 
                                        try {
                                            viewModel.deletePost(it)
                                        } catch (e: Exception) {
                                            showError("Error deleting post: ${e.message}")
                                        }
                                    },
                                    onEditClick = {
                                        try {
                                            findNavController().navigate(
                                                R.id.navigation_add_post,
                                                bundleOf("postId" to post.postId)
                                            )
                                        } catch (e: Exception) {
                                            showError("Error navigating to edit post: ${e.message}")
                                        }
                                    },
                                    onLikeClick = { 
                                        try {
                                            viewModel.toggleLike(it)
                                        } catch (e: Exception) {
                                            showError("Error toggling like: ${e.message}")
                                        }
                                    },
                                    onFavoriteClick = { 
                                        try {
                                            viewModel.toggleFavorite(it)
                                        } catch (e: Exception) {
                                            showError("Error toggling favorite: ${e.message}")
                                        }
                                    },
                                    isLiked = isLiked
                                )
                            } catch (e: Exception) {
                                showError("Error binding post: ${e.message}")
                            }
                        }
                    } catch (e: Exception) {
                        showError("Error collecting like status: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                showError("Error in lifecycle scope for post binding: ${e.message}")
            }
        }
    }
    
    private fun showError(message: String) {
        try {
            if (isAdded && _binding != null) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            // Last resort - log the error if even showing the error fails
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 
