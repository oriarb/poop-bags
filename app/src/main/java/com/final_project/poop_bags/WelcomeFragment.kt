package com.final_project.poop_bags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.final_project.poop_bags.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment() {

    private var binding: FragmentWelcomeBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding?.root as View
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.let {
            it.signInButton.setOnClickListener {
                findNavController().navigate(R.id.action_welcomeFragment_to_signInFragment)
            }

            it.registerButton.setOnClickListener {
                findNavController().navigate(R.id.action_welcomeFragment_to_registerFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}