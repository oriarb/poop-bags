package com.final_project.poop_bags.modules.editProfile

import android.app.AlertDialog
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.final_project.poop_bags.R
import com.final_project.poop_bags.databinding.FragmentEditProfileBinding
import android.widget.Toast
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditProfileViewModel by viewModels()
    private var tempImageUri: Uri? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            showImagePickerDialog()
        } else {
            Toast.makeText(requireContext(), "Camera permission required to take photos", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempImageUri?.let { uri ->
                handleSelectedImage(uri)
            }
        }
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { handleSelectedImage(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
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

        binding.saveButton.setOnClickListener {
            try {
                val username = binding.usernameInput.text.toString()
                val email = binding.emailInput.text.toString()
                viewModel.updateProfile(username, email)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error saving profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        binding.profileImage.setOnClickListener {
            try {
                if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == 
                    PackageManager.PERMISSION_GRANTED) {
                    showImagePickerDialog()
                } else {
                    requestPermissionLauncher.launch(arrayOf(android.Manifest.permission.CAMERA))
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error accessing camera: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        AlertDialog.Builder(requireContext())
            .setTitle("Choose Profile Picture")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == 
                            PackageManager.PERMISSION_GRANTED) {
                            takePhoto()
                        } else {
                            requestPermissionLauncher.launch(arrayOf(android.Manifest.permission.CAMERA))
                        }
                    }
                    1 -> pickFromGallery()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun takePhoto() {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "PROFILE_$timeStamp.jpg"
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            }
            tempImageUri = requireContext().contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
            )
            tempImageUri?.let { uri ->
                takePictureLauncher.launch(uri)
            } ?: run {
                Toast.makeText(requireContext(), "Failed to create image file", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error taking photo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickFromGallery() {
        try {
            getContent.launch("image/*")
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error opening gallery: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSelectedImage(uri: Uri) {
        try {
            Glide.with(this)
                .load(uri)
                .circleCrop()
                .placeholder(R.drawable.default_profile)
                .into(binding.profileImage)
            
            viewModel.updateProfilePicture(uri)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Error handling selected image: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun observeViewModel() {
        try {
            viewModel.user.observe(viewLifecycleOwner) { profile ->
                try {
                    binding.usernameInput.setText(profile.username)
                    binding.emailInput.setText(profile.email)
                    
                    profile.image?.let { imageUrl ->
                        if (imageUrl.isNotEmpty()) {
                            Glide.with(this)
                                .load(imageUrl)
                                .circleCrop()
                                .placeholder(R.drawable.default_profile)
                                .error(R.drawable.default_profile)
                                .into(binding.profileImage)
                        }
                    } ?: run {
                        Glide.with(this)
                            .load(R.drawable.default_profile)
                            .circleCrop()
                            .into(binding.profileImage)
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "Error loading profile data: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            
            viewModel.imageUploadStatus.observe(viewLifecycleOwner) { status ->
                when (status) {
                    is ImageUploadStatus.Loading -> {
                        binding.progressBar.isVisible = true
                    }
                    is ImageUploadStatus.Success -> {
                        binding.progressBar.isVisible = false
                        Toast.makeText(
                            requireContext(),
                            "Profile picture updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is ImageUploadStatus.Error -> {
                        binding.progressBar.isVisible = false
                        Toast.makeText(
                            requireContext(),
                            "Failed to update profile picture: ${status.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                binding.loadingOverlay.isVisible = isLoading
                binding.progressBar.isVisible = isLoading
                binding.saveButton.isEnabled = !isLoading
                binding.profileImage.isEnabled = !isLoading
                binding.usernameInput.isEnabled = !isLoading
                binding.emailInput.isEnabled = !isLoading
            }

            viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
                if (errorMessage.isNotEmpty()) {
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }
            }

            viewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Error in view model observation: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}