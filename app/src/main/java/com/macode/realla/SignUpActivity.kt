package com.macode.realla

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputLayout
import com.macode.realla.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val toolbar = findViewById<Toolbar>(R.id.signUpToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Sign Up"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back)
        toolbar.setTitleTextColor(Color.parseColor("#66AFD4"))

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.signUpButton.setOnClickListener {
            if (binding.fullNameEditInput.text.isNullOrEmpty()) {
                showError(binding.fullNameInput, "Please enter your full name!")
            } else if (binding.emailEditInput.text.isNullOrEmpty() || !binding.emailEditInput.text!!.contains("@")) {
                showError(binding.emailInput, "Please enter a valid email address!")
            } else if (binding.passwordEditInput.text.isNullOrEmpty()) {
                showError(binding.passwordInput, "Please enter a password for your account!")
            } else if (binding.confirmPasswordEditInput.text.isNullOrEmpty() || binding.confirmPasswordEditInput != binding.passwordEditInput.text) {
                showError(binding.confirmPasswordInput, "Password does not match! Please confirm password!")
            } else {

            }
        }

        binding.logInText.setOnClickListener {
            startActivity(Intent(this, LogInActivity::class.java))
            finish()
        }
    }

    private fun showError(layout: TextInputLayout, text: String) {
        layout.error = text
        layout.requestFocus()
    }
}