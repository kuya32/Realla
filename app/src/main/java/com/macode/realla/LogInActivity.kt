package com.macode.realla

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.macode.realla.activities.SetUpActivity
import com.macode.realla.databinding.ActivityLogInBinding
import com.macode.realla.models.User

class LogInActivity : BaseActivity() {
    private lateinit var binding: ActivityLogInBinding
    private lateinit var auth: FirebaseAuth

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
        supportActionBar?.setHomeAsUpIndicator(R.drawable.blue_back)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        auth = FirebaseAuth.getInstance()

        binding.forgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding.loginButton.setOnClickListener {
            if (binding.emailEditInput.text.isNullOrEmpty() || !binding.emailEditInput.text!!.contains("@")) {
                showError(binding.emailInput, "Please enter a valid email address!")
            } else if (binding.passwordEditInput.text.isNullOrEmpty()) {
                showError(binding.passwordInput, "Please enter your password!")
            } else {
                val email = binding.emailEditInput.text.toString()
                val password = binding.passwordEditInput.text.toString()
                logInUser(email, password)
            }
        }

        binding.signUpText.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }
    }

    private fun logInUser(email: String, password: String) {
        showProgressDialog("Please wait...")
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (auth.currentUser!!.isEmailVerified) {
                    fireStoreClass.establishUser(this)
                    Log.i("Log In", "Log in with email was successful!")
                } else {
                    showErrorToastMessage(this, "Please verify your account!")
                }
            } else {
                task.exception!!.message?.let { Log.e("Log In", it) }
                showErrorToastException(task, this)
            }
        }
    }

    fun logInSuccess(loggedInUser: User) {
        hideProgressDialog()
        val image = loggedInUser.image
        val username = loggedInUser.username
        val city = loggedInUser.cityLocation
        val state = loggedInUser.stateLocation
        val occupation = loggedInUser.occupation

        if (image != "" && username != "" && city != "" && state != "" && occupation != "") {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            startActivity(Intent(this, SetUpActivity::class.java))
        }
        finish()
    }
}