package com.final_project.poop_bags.modules.welcome

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.final_project.poop_bags.MainActivity
import com.final_project.poop_bags.databinding.FragmentSignInBinding
import com.final_project.poop_bags.models.firebase.FirebaseModel
import com.final_project.poop_bags.repository.UserRepository
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SignInFragment : Fragment() {

    private var binding: FragmentSignInBinding? = null
    @Inject
    lateinit var firebaseModel: FirebaseModel
    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding?.root as View
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.let {
            it.backButton.setOnClickListener {
                findNavController().navigateUp()
            }

            it.signInButton.setOnClickListener { _ ->
                val email = it.emailInput.text.toString().trim()
                val password = it.passwordInput.text.toString().trim()

                if (email.isEmpty()) {
                    Toast.makeText(requireContext(), "Email cannot be empty", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                if (password.isEmpty()) {
                    Toast.makeText(requireContext(), "Password cannot be empty", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                it.progressBar.visibility = View.VISIBLE
                it.signInButton.isEnabled = false

                firebaseModel.signInUser(email, password) { success, userId ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        it.progressBar.visibility = View.GONE
                        it.signInButton.isEnabled = true

                        if (success && userId != null) {
                            userRepository.updateUserFromFirebase(userId)
                            Toast.makeText(requireContext(), "Sign in successful", Toast.LENGTH_SHORT)
                                .show()
                            val intent = Intent(requireContext(), MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            requireActivity().finish()
                        } else {
                            Toast.makeText(requireContext(), "Sign in failed", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}