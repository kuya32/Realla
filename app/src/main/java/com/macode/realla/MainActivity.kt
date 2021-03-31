package com.macode.realla

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.macode.realla.activities.MyProfileActivity
import com.macode.realla.databinding.ActivityMainBinding
import com.macode.realla.databinding.NavHeaderMainBinding
import com.macode.realla.models.User
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setUpActionBar()

        binding.navView.setNavigationItemSelectedListener(this@MainActivity)

        fireStoreClass.establishUser(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE) {
            fireStoreClass.establishUser(this)
        } else {
            Log.e("MainActivityResult", "Cancelled")
        }
    }

    private fun setUpActionBar() {
        val toolbar = findViewById<Toolbar>(R.id.mainToolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.nav_menu)

        toolbar.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    private fun toggleDrawer() {
        if (binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            binding.mainDrawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            doubleBackToExit()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navMyProfile -> {
                startActivityForResult(Intent(this, MyProfileActivity::class.java), MY_PROFILE_REQUEST_CODE)
            }
            R.id.logOut -> {
                fireStoreClass.logoutUser(this)
            }
        }
        binding.mainDrawerLayout.closeDrawer(GravityCompat.START)

        return true
    }

    fun updateNavigationUserDetails(loggedInUser: User) {
        val navUsername = findViewById<TextView>(R.id.navUsername)
        val navProfileImage = findViewById<CircleImageView>(R.id.navProfileImage)
        Glide
            .with(this)
            .load(loggedInUser.image)
            .centerCrop()
            .placeholder(R.drawable.person)
            .into(navProfileImage)

        navUsername.text = loggedInUser.username
    }

    fun successfulLogout() {
        val intent = Intent(this, IntroActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}