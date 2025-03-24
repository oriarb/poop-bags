package com.final_project.poop_bags

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.final_project.poop_bags.models.FirebaseModel
import com.google.android.material.textfield.TextInputEditText

class RegisterFragment : Fragment() {

    private var firebaseModel: FirebaseModel = FirebaseModel.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        val backButton = view.findViewById<ImageButton>(R.id.back_button)
        val registerButton = view.findViewById<Button>(R.id.register_button1)
        val usernameInput = view.findViewById<TextInputEditText>(R.id.username_input)
        val emailInput = view.findViewById<TextInputEditText>(R.id.email_input1)
        val passwordInput = view.findViewById<TextInputEditText>(R.id.password_input1)

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        registerButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

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

            if (password.length < 6) {
                Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
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

        return view
    }
}