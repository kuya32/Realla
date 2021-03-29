package com.macode.realla.firebase

import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.macode.realla.LogInActivity
import com.macode.realla.SignUpActivity
import com.macode.realla.models.User

class FireStoreClass {

    private val fireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User) {
        fireStore.collection("Users").document(getCurrentUserID()).set(userInfo, SetOptions.merge()).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(activity, "User added to Firebase", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, "User was not added to Firebase", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun logInUser(activity: LogInActivity) {
        fireStore.collection("Users").document(getCurrentUserID()).get().addOnSuccessListener { document ->
            val loggedInUser = document.toObject(User::class.java)
            if(loggedInUser != null) {
                activity.logInSuccess(loggedInUser)
            }
        }.addOnFailureListener { e ->
            Log.e("LogInUser", "Error writing document", e)
        }
    }

    private fun getCurrentUserID(): String {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }
}