package com.macode.realla.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.macode.realla.R
import com.macode.realla.databinding.ActivitySignUpBinding
import com.macode.realla.models.User
import java.text.SimpleDateFormat
import java.util.*

class SignUpActivity : BaseActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth

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
        supportActionBar?.setHomeAsUpIndicator(R.drawable.blue_back)
        toolbar.setTitleTextColor(Color.parseColor("#66AFD4"))

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        auth = FirebaseAuth.getInstance()

        binding.signUpButton.setOnClickListener {
            if (binding.fullNameEditInput.text.isNullOrEmpty() || !binding.fullNameEditInput.text!!.contains(" ")) {
                showError(binding.fullNameInput, "Please enter your full name!")
            } else if (binding.emailEditInput.text.isNullOrEmpty() || !binding.emailEditInput.text!!.contains("@")) {
                showError(binding.emailInput, "Please enter a valid email address!")
            } else if (binding.passwordEditInput.text.isNullOrEmpty()) {
                showError(binding.passwordInput, "Please enter a password for your account!")
            } else if (binding.confirmPasswordEditInput.text.isNullOrEmpty() || binding.confirmPasswordEditInput.text.toString() != binding.passwordEditInput.text.toString()) {
                showError(binding.confirmPasswordInput, "Password does not match! Please confirm password!")
            } else {
                val name = binding.fullNameEditInput.text.toString()
                val email = binding.emailEditInput.text.toString()
                val password = binding.passwordEditInput.text.toString()
                signUpUser(name, email, password)
            }
        }

        binding.logInText.setOnClickListener {
            startActivity(Intent(this, LogInActivity::class.java))
            finish()
        }
    }

    private fun signUpUser(name: String, email: String, password: String) {
        showProgressDialog("Please wait...")
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                auth.currentUser!!.sendEmailVerification()
                Toast.makeText(this, "Check your email to verify your account!", Toast.LENGTH_LONG).show()
                val firebaseUser: FirebaseUser = task.result!!.user!!
                val firstName = name.substring(0, name.indexOf(" "))
                val lastName = name.substring(name.indexOf(" ") + 1)
                val registeredEmail = firebaseUser.email!!
                val user = User()
                user.id = firebaseUser.uid
                user.dateUserCreated = getDate()
                user.firstName = firstName
                user.lastName = lastName
                user.email = registeredEmail
                fireStoreClass.registerUser(this, user)
                hideProgressDialog()
                startActivity(Intent(this, LogInActivity::class.java))
                finish()
            } else {
                showErrorToastException(task, this)
            }
        }
    }
}