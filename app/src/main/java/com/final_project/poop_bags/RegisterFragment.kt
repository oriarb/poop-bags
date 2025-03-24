package com.final_project.poop_bags

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.final_project.poop_bags.databinding.FragmentRegisterBinding
import com.final_project.poop_bags.models.FirebaseModel

class RegisterFragment : Fragment() {

    private var binding: FragmentRegisterBinding? = null
    private var firebaseModel: FirebaseModel = FirebaseModel.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding?.root as View
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.let {
            it.backButton.setOnClickListener {
                findNavController().navigateUp()
            }

            it.registerButton.setOnClickListener { _ ->
                val username = it.usernameInput.text.toString().trim()
                val email = it.emailInput.text.toString().trim()
                val password = it.passwordInput.text.toString().trim()

                if (username.isEmpty()) {
                    Toast.makeText(requireContext(), "Username cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (email.isEmpty()) {
                    Toast.makeText(requireContext(), "Email cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (password.isEmpty()) {
                    Toast.makeText(requireContext(), "Password cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                firebaseModel.registerUser(email, username, password) { message ->
                    if (message == "Registration successful") {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        requireActivity().finish()
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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