package com.macode.realla.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import com.macode.realla.R

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        Handler(Looper.myLooper()!!).postDelayed({

            var currentUserID = fireStoreClass.getCurrentUserID()

            if (currentUserID.isNotEmpty()) {
                userReference.document(currentUserID).get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        Log.d("UserDocument", "DocumentSnapshot data: ${document.data}")
                        val image = document.data?.getValue("image")
                        val username = document.data?.getValue("username")
                        val city = document.data?.getValue("cityLocation")
                        val state = document.data?.get("stateLocation")
                        val occupation = document.data?.get("occupation")

                        if (image != "" && username != "" && city != "" && state != "" && occupation != "") {
                            startActivity(Intent(this, MainActivity::class.java))
                        } else {
                            startActivity(Intent(this, SetUpActivity::class.java))
                        }
                    } else {
                        Log.d("UserDocument", "No such document")
                    }
                }

            } else {
                startActivity(Intent(this, IntroActivity::class.java))
            }

            finish()
        }, 2500)
    }
}