package com.final_project.poop_bags.ui.posts

import com.final_project.poop_bags.ui.PostItemView.PostItemView
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
import com.final_project.poop_bags.data.models.Post
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
        setupUI()
        observePosts()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyStateText.visibility = View.GONE
        binding.scrollView.visibility = View.GONE
    }

    private fun observePosts() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    viewModel.userPosts.collect { posts ->
                        binding.progressBar.visibility = View.GONE
                        updatePostsList(posts)
                    }
                } catch (e: Exception) {
                    binding.progressBar.visibility = View.GONE
                    binding.emptyStateText.text = getString(R.string.error_loading_posts)
                    binding.emptyStateText.visibility = View.VISIBLE
                    binding.scrollView.visibility = View.GONE
                }
            }
        }
    }

    private fun updatePostsList(posts: List<Post>) {
        if (!isAdded) return 
        
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
                    // Handle error for individual post
                }
            }
        }
    }

    private fun bindPost(post: Post, postItem: PostItemView) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
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
                            onDeleteClick = { viewModel.deletePost(it) },
                            onEditClick = {
                                try {
                                    findNavController().navigate(
                                        R.id.navigation_add_post,
                                        bundleOf("postId" to post.postId)
                                    )
                                } catch (e: Exception) {
                                    // Log the error or show a message to the user
                                }
                            },
                            onLikeClick = { viewModel.toggleLike(it) },
                            onFavoriteClick = { viewModel.toggleFavorite(it) },
                            isLiked = isLiked
                        )
                    } catch (e: Exception) {
                        // Handle binding error
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 
