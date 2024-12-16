package com.example.mess.ui.representative

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mess.databinding.FragmentMessRepresentativeBinding

class MessRepresentativeFragment : Fragment() {
    private var _binding: FragmentMessRepresentativeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessRepresentativeBinding.inflate(inflater, container, false)
        setupLoginButton()
        setupTestingHelper()
        return binding.root
    }

    private fun setupLoginButton() {
        binding.loginButton.setOnClickListener {
            val username = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                showError("Please fill in all fields")
                return@setOnClickListener
            }

            if (username == "RGUKT" && password == "mess") {
                val intent = Intent(requireContext(), RepresentativeHomeActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            } else {
                showError("Invalid username or password")
            }
        }
    }

    private fun setupTestingHelper() {
        // Long press to fill test credentials
        binding.loginButton.setOnLongClickListener {
            binding.emailInput.setText("RGUKT")
            binding.passwordInput.setText("mess")
            true
        }
    }

    private fun showError(message: String) {
        binding.errorText.apply {
            text = message
            visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 