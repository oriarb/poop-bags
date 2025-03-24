package com.final_project.poop_bags.modules.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.final_project.poop_bags.R
import com.final_project.poop_bags.databinding.FragmentProfileBinding
import com.bumptech.glide.Glide
import android.widget.Toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by viewModels()
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

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
                findNavController().navigate(R.id.action_navigation_profile_to_favouritesFragment)
            },
            ProfileButton(
                id = View.generateViewId(),
                textRes = R.string.my_stations
            ) {
                findNavController().navigate(R.id.action_profileFragment_to_stationsFragment)
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
        setupUI()
        setupButtons()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadUserProfile()
    }

    private fun setupUI() {
        binding.btnEdit.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupButtons() {
        val buttonContainer = binding.buttonContainer
        
        try {
            profileButtons.forEach { buttonData ->
                val buttonView = layoutInflater.inflate(
                    R.layout.component_profile_button,
                    buttonContainer,
                    false
                ) as ViewGroup
                
                buttonView.findViewById<TextView>(R.id.buttonText).setText(buttonData.textRes)
                buttonView.setOnClickListener { 
                    try {
                        buttonData.onClick() 
                    } catch (e: Exception) {
                        showError(getString(R.string.navigation_error, e.message))
                    }
                }
                
                if (buttonContainer.childCount > 0) {
                    val params = buttonView.layoutParams as ViewGroup.MarginLayoutParams
                    params.topMargin = resources.getDimensionPixelSize(R.dimen.button_margin)
                }
                
                buttonContainer.addView(buttonView)
            }
        } catch (e: Exception) {
            showError(getString(R.string.ui_setup_error, e.message))
        }
    }

    private fun observeViewModel() {
        viewModel.user.observe(viewLifecycleOwner) { profile ->
            binding.toolbarTitle.text = getString(R.string.welcome_user, profile.username)
            
            profile.image?.let { imagePath ->
                Glide.with(this)
                    .load(imagePath)
                    .circleCrop()
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .into(binding.profileImage)
            }
            
            binding.username.text = profile.username
        }
        
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                showError(it)
                viewModel.onErrorShown()
            }
        }
    }
    
    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
