package com.final_project.poop_bags.modules.addStation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.final_project.poop_bags.R
import com.final_project.poop_bags.databinding.FragmentAddStationBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddStationFragment : Fragment() {

    private var _binding: FragmentAddStationBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AddStationViewModel by viewModels()
    
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
        _binding = FragmentAddStationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
        
        arguments?.getString("stationId")?.let { stationId ->
            viewModel.loadStation(stationId)
        }
    }

    private fun setupUI() {
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

        binding.uploadButton.setOnClickListener {
            try {
                val stationName = binding.stationNameInput.text.toString()
                viewModel.saveStation(stationName)
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.uploadButton.isEnabled = true
                showError("Error processing your request: ${e.message}")
            }
        }

        viewModel.isEditMode.observe(viewLifecycleOwner) { isEditMode ->
            binding.toolbarTitle.text = if (isEditMode) getString(R.string.edit_station) else getString(R.string.add_station)
            binding.uploadButton.text = if (isEditMode) getString(R.string.save_changes) else getString(R.string.upload)
            binding.updateLocationContainer.visibility = if (isEditMode) View.VISIBLE else View.GONE
        }
    }

    private fun observeViewModel() {
        viewModel.currentStation.observe(viewLifecycleOwner) { station ->
            binding.stationNameInput.setText(station.name)
            binding.addImageIcon.visibility = View.GONE
            
            if (station.imageUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(station.imageUrl)
                    .apply(RequestOptions().centerCrop())
                    .into(binding.stationImage)
            }
        }

        viewModel.uploadSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                viewModel.success.value?.let { message ->
                    findNavController().previousBackStackEntry?.savedStateHandle?.set("success_message", message)
                    findNavController().navigateUp()
                }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.apply {
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                uploadButton.isEnabled = !isLoading
                stationNameInput.isEnabled = !isLoading
                imageContainer.isEnabled = !isLoading
                updateLocationCheckbox.isEnabled = !isLoading
            }
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("success_message")?.observe(
            viewLifecycleOwner
        ) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                findNavController().currentBackStackEntry?.savedStateHandle?.remove<String>("success_message")
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun clearSuccess() {
        viewModel.clearSuccess()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearSuccess()
        _binding = null
    }
} 