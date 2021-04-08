package com.macode.realla.activities

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputEditText
import com.macode.realla.R
import com.macode.realla.databinding.ActivityAccountSettingsBinding
import com.macode.realla.fragments.AuthenticateUserFragment

class AccountSettingsActivity : BaseActivity() {
    private lateinit var binding: ActivityAccountSettingsBinding
    private lateinit var fragmentId: String
    private val detailBundle: Bundle = Bundle()
    private val authenticateUserFragment: AuthenticateUserFragment = AuthenticateUserFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountSettingsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        loadToolbar()

        binding.accountSettingsEmail.text = currentFirebaseUser?.email

        binding.accountSettingsEditEmailImage.setOnClickListener {
            binding.mainRelative.visibility = View.GONE
            binding.secondaryRelative.visibility = View.VISIBLE
            val editEmailText = findViewById<TextInputEditText>(R.id.authenticationEmailEditInput)
            val editPasswordText = findViewById<TextInputEditText>(R.id.authenticationPasswordEditInput)
            if (editEmailText != null && editPasswordText != null) {
                editEmailText.text?.clear()
                editPasswordText.text?.clear()
            }
            fragmentId = "email"
            detailBundle.putString("fragmentId", fragmentId)
            authenticateUserFragment.arguments = detailBundle
            supportFragmentManager.beginTransaction().replace(R.id.accountSettingsFragmentContainer, authenticateUserFragment).commit()
        }

        binding.accountSettingsEditPasswordImage.setOnClickListener {
            binding.mainRelative.visibility = View.GONE
            binding.secondaryRelative.visibility = View.VISIBLE
            val editEmailText = findViewById<TextInputEditText>(R.id.authenticationEmailEditInput)
            val editPasswordText = findViewById<TextInputEditText>(R.id.authenticationPasswordEditInput)
            if (editEmailText != null && editPasswordText != null) {
                editEmailText.text?.clear()
                editPasswordText.text?.clear()
            }
            fragmentId = "password"
            detailBundle.putString("fragmentId", fragmentId)
            authenticateUserFragment.arguments = detailBundle
            supportFragmentManager.beginTransaction().replace(R.id.accountSettingsFragmentContainer, authenticateUserFragment).commit()
        }
    }

    private fun loadToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.accountSettingsToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Account Settings"
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        toolbar.setTitleTextColor(Color.parseColor("#FFFFFFFF"))
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back_white)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}