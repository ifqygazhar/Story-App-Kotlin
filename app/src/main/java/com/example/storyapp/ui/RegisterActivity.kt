package com.example.storyapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import com.example.storyapp.R
import com.example.storyapp.data.Result
import com.example.storyapp.databinding.ActivityRegisterBinding
import com.example.storyapp.ui.viewmodel.RegisterViewModel
import com.example.storyapp.ui.viewmodel.factory.RegisterViewModelFactory


class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val registerViewModel: RegisterViewModel by viewModels {
        RegisterViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState != null) {
            binding.nameEditText.setText(savedInstanceState.getString("name"))
            binding.emailEditText.setText(savedInstanceState.getString("email"))
            binding.passwordEditText.setText(savedInstanceState.getString("password"))
        }


        setupUI()
        observeViewModel()


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("name", binding.nameEditText.text.toString())
        outState.putString("email", binding.emailEditText.text.toString())
        outState.putString("password", binding.passwordEditText.text.toString())
    }

    private fun setupUI() {
        binding.btnRegister?.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (validateInputs(name, email, password)) {
                registerViewModel.register(name, email, password)
            }
        }
    }

    private fun validateInputs(name: String, email: String, password: String): Boolean {
        var isValid = true
        
        if (name.isEmpty()) {
            binding.nameEditTextLayout.error = getString(R.string.error_empty_name)
            isValid = false
        } else {
            binding.nameEditTextLayout.error = null
        }

        if (email.isEmpty()) {
            binding.emailEditTextLayout.error = getString(R.string.error_empty_email)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEditTextLayout.error = getString(R.string.validationEmail)
            isValid = false
        } else {
            binding.emailEditTextLayout.error = null
        }


        if (password.isEmpty()) {
            binding.passwordEditTextLayout.error = getString(R.string.error_empty_password)
            isValid = false
        } else {
            binding.passwordEditTextLayout.error = null
        }
        return isValid
    }

    private fun observeViewModel() {

        registerViewModel.isLoading.observe(this, Observer { isLoading ->
            if (isLoading) {
                showProgressBar()
            } else {
                hideProgressBar()
            }
        })


        registerViewModel.registerResult.observe(this, Observer { result ->
            when (result) {
                is Result.Success -> {
                    hideProgressBar()
                    Toast.makeText(this, getString(R.string.register_success), Toast.LENGTH_SHORT)
                        .show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                is Result.Error -> {
                    hideProgressBar()
                    Toast.makeText(this, result.error, Toast.LENGTH_SHORT).show()
                }

                is Result.Loading -> {
                    showProgressBar()
                }
            }
        })
    }

    private fun showProgressBar() {
        binding.btnRegister.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.btnRegister.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }
}
