package com.final_project.poop_bags.ui.profile

import android.app.AlertDialog
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.final_project.poop_bags.R
import com.final_project.poop_bags.databinding.FragmentProfileBinding
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by viewModels()
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val permissions = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
            // Android 14+
            arrayOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            )
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
            // Android 13
            arrayOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.READ_MEDIA_IMAGES
            )
        }
        else -> {
            // Android 12 and below
            arrayOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } else {
                showImagePickerDialog()
            }
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempImageUri?.let { uri ->
                viewModel.updateProfilePicture(uri)
            }
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.updateProfilePicture(it) }
    }

    private var tempImageUri: Uri? = null

    private data class ProfileButton(
        val id: Int,
        @StringRes val textRes: Int,
        val onClick: () -> Unit
    )

    private val profileButtons by lazy {
        listOf(
            ProfileButton(
                id = View.generateViewId(),
                textRes = R.string.favourites
            ) {
                // לוגיקה למעבר למסך המועדפים
            },
            ProfileButton(
                id = View.generateViewId(),
                textRes = R.string.posts
            ) {
                // לוגיקה למעבר למסך הפוסטים
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupProfileImageClick()
        setupButtons()
        observeViewModel()
    }

    private fun setupProfileImageClick() {
        binding.profileImage.setOnClickListener {
            if (hasPermissions()) {
                showImagePickerDialog()
            } else {
                requestPermissionLauncher.launch(permissions)
            }
        }
    }

    private fun hasPermissions(): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("take a photo", "choose from gallery")
        AlertDialog.Builder(requireContext())
            .setTitle("choose profile picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> takePhoto()
                    1 -> pickFromGallery()
                }
            }
            .show()
    }

    private fun takePhoto() {
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
        }
    }

    private fun pickFromGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            pickImageLauncher.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        } else {
            pickImageLauncher.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }
    }

    private fun setupButtons() {
        val buttonContainer = binding.buttonContainer
        
        profileButtons.forEach { buttonData ->
            val buttonView = layoutInflater.inflate(
                R.layout.component_profile_button,
                buttonContainer,
                false
            ) as ViewGroup
            
            buttonView.findViewById<TextView>(R.id.buttonText).setText(buttonData.textRes)
            buttonView.setOnClickListener { buttonData.onClick() }
            
            if (buttonContainer.childCount > 0) {
                val params = buttonView.layoutParams as ViewGroup.MarginLayoutParams
                params.topMargin = resources.getDimensionPixelSize(R.dimen.button_margin)
            }
            
            buttonContainer.addView(buttonView)
        }
    }

    private fun observeViewModel() {
        viewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            binding.toolbarTitle.text = getString(R.string.welcome_user, profile.username)
            
            profile.profilePicture?.let { imagePath ->
                Glide.with(this)
                    .load(imagePath)
                    .circleCrop()
                    .placeholder(R.drawable.default_profile)
                    .into(binding.profileImage)
            }
            
            binding.username.text = profile.username
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
