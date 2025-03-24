package com.final_project.poop_bags

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.final_project.poop_bags.models.FirebaseModel
import com.google.android.material.textfield.TextInputEditText

class SignInFragment : Fragment() {

    private var firebaseModel: FirebaseModel = FirebaseModel.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)

        val backButton = view.findViewById<ImageButton>(R.id.back_button)
        val signInButton = view.findViewById<Button>(R.id.sign_in_button2)
        val emailInput = view.findViewById<TextInputEditText>(R.id.email_input2)
        val passwordInput = view.findViewById<TextInputEditText>(R.id.password_input2)

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        signInButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Email cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(requireContext(), "Password cannot be empty", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            firebaseModel.signInUser(email, password) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Signed in successfully", Toast.LENGTH_SHORT).show()
                    requireActivity().finish()
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "Sign in failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return view
    }
}