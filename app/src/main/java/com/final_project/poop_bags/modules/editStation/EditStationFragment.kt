package com.final_project.poop_bags.modules.editStation

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.final_project.poop_bags.databinding.FragmentEditStationBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditStationFragment : Fragment() {

    private var _binding: FragmentEditStationBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: EditStationViewModel by viewModels()

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        try {
            if (uri != null) {
                binding.addImageIcon.visibility = View.GONE
                Glide.with(this)
                    .load(uri)
                    .apply(RequestOptions().centerCrop())
                    .into(binding.stationImage)
                viewModel.setSelectedImage(uri)
            }
        } catch (e: Exception) {
            showError("Error selecting image: ${e.message}")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditStationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        arguments?.getString("stationId")?.let { stationId ->
            android.util.Log.d("EditStationFragment", "Loading station: $stationId")
            viewModel.loadStation(stationId)
        } ?: run {
            showError("No station ID provided")
            findNavController().navigateUp()
        }
        
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.imageContainer.setOnClickListener {
            try {
                getContent.launch("image/*")
            } catch (e: Exception) {
                showError("Failed to open image picker: ${e.message}")
            }
        }

        binding.saveButton.setOnClickListener {
            try {
                val stationName = binding.stationNameInput.text.toString()
                viewModel.updateStation(stationName, binding.updateLocationCheckbox.isChecked)
            } catch (e: Exception) {
                showError("Error processing your request: ${e.message}")
            }
        }
    }

    @SuppressLint("SetTextI18s", "SetTextI18n")
    private fun observeViewModel() {
        viewModel.station.observe(viewLifecycleOwner) { station ->
            binding.apply {
                stationNameInput.setText(station.name)
                currentCoordinatesText.text = "the location will update automatically from your location"
                
                if (station.imageUrl.isNotEmpty()) {
                    addImageIcon.visibility = View.GONE
                    Glide.with(requireContext())
                        .load(station.imageUrl)
                        .apply(RequestOptions().centerCrop())
                        .into(stationImage)
                }
            }
        }

        viewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                viewModel.success.value?.let { message ->
                    findNavController().previousBackStackEntry?.savedStateHandle?.set("success_message", message)
                }
                findNavController().navigateUp()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                showError(it)
                viewModel.clearError()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.apply {
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                saveButton.isEnabled = !isLoading
                stationNameInput.isEnabled = !isLoading
                imageContainer.isEnabled = !isLoading
                updateLocationCheckbox.isEnabled = !isLoading
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    fun clearSuccess() {
        viewModel.clearSuccess()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearSuccess()
        _binding = null
    }
} 