package com.macode.realla

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.macode.realla.databinding.ActivityIntroBinding

class IntroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.signInButton.setOnClickListener {
            startActivity(Intent(this, LogInActivity::class.java))
        }

        binding.signUpButton.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}