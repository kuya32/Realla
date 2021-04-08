package com.macode.realla.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.macode.realla.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : BaseActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.forgotPasswordButton.setOnClickListener {
            val email = binding.forgotPasswordEmailEditInput.text.toString()
            if (email.isEmpty()) {
                showError(binding.forgotPasswordEmailInput, "Please enter your email!")
            } else if (!binding.forgotPasswordEmailEditInput.text?.contains("@")!!) {
                showError(binding.forgotPasswordEmailInput, "Please provide a valid email!")
            } else {
                firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this@ForgotPasswordActivity,
                            "Please check your email!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@ForgotPasswordActivity,
                            "Email did not send!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        binding.backToLogin.setOnClickListener {
            startActivity(Intent(this, LogInActivity::class.java))
            finish()
        }
    }
}