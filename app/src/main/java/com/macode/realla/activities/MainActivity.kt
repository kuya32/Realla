package com.macode.realla.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.messaging.FirebaseMessaging
import com.macode.realla.R
import com.macode.realla.adapters.BoardItemsAdapter
import com.macode.realla.databinding.ActivityMainBinding
import com.macode.realla.models.Board
import com.macode.realla.models.User
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fab: FloatingActionButton
    private lateinit var usersFullName: String
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setUpActionBar()

        binding.navView.setNavigationItemSelectedListener(this@MainActivity)

        sharedPreferences = this.getSharedPreferences("reallaPref", Context.MODE_PRIVATE)

        val tokenUpdated = sharedPreferences.getBoolean("fcmUpdatedToken", false)

        if (tokenUpdated) {
            showProgressDialog("Loading user...")
            fireStoreClass.establishUser(this, true)
        } else {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    fcmToken = task.result
                    updateFCMToken(fcmToken)
                }
            }
        }

        fireStoreClass.establishUser(this, true)

        fab = findViewById(R.id.addBoardFab)
        fab.setOnClickListener {
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra("usersFullName", usersFullName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE) {
            fireStoreClass.establishUser(this)
        } else if (resultCode == Activity.RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE) {
            fireStoreClass.getBoardList(this)
        } else {
            Log.e("MainActivityResult", "Cancelled")
        }
    }

    private fun setUpActionBar() {
        val toolbar = findViewById<Toolbar>(R.id.mainToolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.nav_menu)
        toolbar.setTitleTextColor(Color.parseColor("#FFFFFFFF"))
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

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
            R.id.navAccountSettings -> {
                startActivity(Intent(this, AccountSettingsActivity::class.java))
            }
            R.id.logOut -> {
                sharedPreferences.edit().clear().apply()
                fireStoreClass.logoutUser(this)
            }
        }
        binding.mainDrawerLayout.closeDrawer(GravityCompat.START)

        return true
    }

    fun updateNavigationUserDetails(loggedInUser: User, readBoardsList: Boolean) {
        hideProgressDialog()
        val navUsername = findViewById<TextView>(R.id.navUsername)
        val navProfileImage = findViewById<CircleImageView>(R.id.navProfileImage)
        Glide
            .with(this)
            .load(loggedInUser.image)
            .centerCrop()
            .placeholder(R.drawable.person)
            .into(navProfileImage)

        navUsername.text = loggedInUser.username
        usersFullName = "${loggedInUser.firstName} ${loggedInUser.lastName}"

        if (readBoardsList) {
//            showProgressDialog("Loading board list...")
            fireStoreClass.getBoardList(this)
        }
    }

    fun successfulLogout() {
        val intent = Intent(this, IntroActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    fun populateBoardListToUI(boardList: ArrayList<Board>) {
        hideProgressDialog()
        val boardRecyclerView = findViewById<RecyclerView>(R.id.boardsRecyclerView)
        val noBoardsAvailable = findViewById<TextView>(R.id.noBoardsAvailableText)

        if (boardList.size > 0) {
            boardRecyclerView.visibility = View.VISIBLE
            noBoardsAvailable.visibility = View.GONE

            boardRecyclerView.layoutManager = LinearLayoutManager(this)
            boardRecyclerView.setHasFixedSize(true)

            val adapter = BoardItemsAdapter(this, boardList)
            boardRecyclerView.adapter = adapter

            adapter.setOnClickListener(object: BoardItemsAdapter.OnClickListener {
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra("documentID", model.documentID)
                    startActivity(intent)
                }

            })

        } else {
            boardRecyclerView.visibility = View.GONE
            noBoardsAvailable.visibility = View.VISIBLE
        }
    }

    fun tokenUpdateSuccess() {
        hideProgressDialog()
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean("fcmTokenUpdated", true)
        editor.apply()
        showProgressDialog("Loading user...")
        fireStoreClass.establishUser(this, true)
    }

    private fun updateFCMToken(token: String) {
        val userHashMap = HashMap<String, Any>()
        userHashMap["fcmToken"] = token
        showProgressDialog("Updating user info...")
        fireStoreClass.updatedUserProfileData(this, userHashMap)
    }
}