package com.macode.realla

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputLayout
import com.macode.realla.databinding.ActivityLogInBinding

class LogInActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLogInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val toolbar = findViewById<Toolbar>(R.id.loginToolbar)
        setSupportActionBar(toolbar)
        toolbar.setTitleTextColor(Color.parseColor("#66AFD4"))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Log In"
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.forgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding.loginButton.setOnClickListener {
            if (binding.emailEditInput.text.isNullOrEmpty() || !binding.emailEditInput.text!!.contains("@")) {
                showError(binding.emailInput, "Please enter a valid email address!")
            } else if (binding.passwordEditInput.text.isNullOrEmpty()) {
                showError(binding.passwordInput, "Please enter your password!")
            } else {

            }
        }

        binding.signUpText.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }
    }

    private fun showError(layout: TextInputLayout, text: String) {
        layout.error = text
        layout.requestFocus()
    }
}