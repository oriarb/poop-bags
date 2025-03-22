package com.final_project.poop_bags.ui.addPost

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.final_project.poop_bags.databinding.FragmentAddPostBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddPostFragment : Fragment() {

    private var _binding: FragmentAddPostBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddPostViewModel by viewModels()
    private val args: AddPostFragmentArgs by navArgs()

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { handleSelectedImage(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
        
        args.postId?.let { 
            viewModel.setEditPost(it)
        }
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.imageContainer.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.uploadButton.setOnClickListener {
            val stationName = binding.stationNameInput.text.toString()
            val address = binding.addressInput.text.toString()
            viewModel.uploadPost(stationName, address)
        }

        viewModel.isEditMode.observe(viewLifecycleOwner) { isEditMode ->
            binding.toolbarTitle.text = if (isEditMode) "Edit Post" else "Add Post"
            binding.uploadButton.text = if (isEditMode) "Save Changes" else "Upload"
        }

        viewModel.currentPost.observe(viewLifecycleOwner) { post ->
            binding.stationNameInput.setText(post.title)
            binding.addressInput.setText(post.address)
            binding.addImageIcon.visibility = View.GONE
            Glide.with(this)
                .load(post.imageUrl)
                .apply(RequestOptions().centerCrop())
                .into(binding.postImage)
        }
    }

    private fun handleSelectedImage(uri: Uri) {
        viewModel.setSelectedImage(uri)
        binding.addImageIcon.visibility = View.GONE
        Glide.with(this)
            .load(uri)
            .apply(RequestOptions().centerCrop())
            .into(binding.postImage)
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.uploadButton.isEnabled = !isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.uploadSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                val message = if (viewModel.isEditMode.value == true) {
                    "Post updated successfully"
                } else {
                    "Post uploaded successfully"
                }
                Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}