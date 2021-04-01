package com.macode.realla.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.macode.realla.activities.LogInActivity
import com.macode.realla.activities.MainActivity
import com.macode.realla.activities.SignUpActivity
import com.macode.realla.activities.MyProfileActivity
import com.macode.realla.activities.SetUpActivity
import com.macode.realla.models.User

class FireStoreClass {

    private val fireStore = FirebaseFirestore.getInstance()
    private val userReference = fireStore.collection("Users")

    fun registerUser(activity: SignUpActivity, userInfo: User) {
        userReference.document(getCurrentUserID()).set(userInfo, SetOptions.merge()).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(activity, "User added to Firebase", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, "User was not added to Firebase", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun establishUser(activity: Activity) {
        userReference.document(getCurrentUserID()).get().addOnSuccessListener { document ->
            val loggedInUser = document.toObject(User::class.java)
            when (activity) {
                is LogInActivity -> {
                    userReference.document(getCurrentUserID()).update("status", "Online").addOnSuccessListener {
                        activity.logInSuccess(loggedInUser!!)
                    }.addOnFailureListener { e ->
                        Log.e(activity.javaClass.simpleName, "Error logging in user", e)
                        Toast.makeText(activity, "Sorry, we can't log you in!", Toast.LENGTH_SHORT).show()
                    }
                }
                is MainActivity -> {
                    activity.updateNavigationUserDetails(loggedInUser!!)
                }
                is MyProfileActivity -> {
                    activity.updateMyProfileUserDetails(loggedInUser!!)
                }
            }
        }.addOnFailureListener { e ->
            when (activity) {
                is LogInActivity -> {
                    activity.hideProgressDialog()
                }
                is MainActivity -> {
                    activity.hideProgressDialog()
                }
                is MyProfileActivity -> {
                    activity.hideProgressDialog()
                }
            }
            Log.e("LogInUser", "Error writing document", e)
        }
    }

    fun updateUser(activity: SetUpActivity, userInfo: User) {
        userReference.document(getCurrentUserID()).update(mapOf(
            "image" to userInfo.image,
            "username" to userInfo.username,
            "phone" to userInfo.phone,
            "cityLocation" to userInfo.cityLocation,
            "stateLocation" to userInfo.stateLocation,
            "occupation" to userInfo.occupation
        )).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(activity, "User updated to Firebase", Toast.LENGTH_SHORT).show()
                activity.updateUserSuccess()
            } else {
                Toast.makeText(activity, "User was not updated to Firebase", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun updatedUserProfileData(activity: MyProfileActivity, userHashMap: HashMap<String, Any>) {
        userReference.document(getCurrentUserID()).update(userHashMap).addOnSuccessListener {
            Log.i(activity.javaClass.simpleName, "Profile data updated successfully")
            Toast.makeText(activity, "Profile data updated successfully", Toast.LENGTH_SHORT).show()
            activity.profileUpdatedSuccess()
        }.addOnFailureListener { e ->
            activity.hideProgressDialog()
            Log.e(activity.javaClass.simpleName, "Error while updating profile", e)
            Toast.makeText(activity, "Error while updating profile", Toast.LENGTH_SHORT).show()
        }
    }

    fun logoutUser(activity: MainActivity) {
        userReference.document(getCurrentUserID()).update("status", "Offline").addOnSuccessListener {
            FirebaseAuth.getInstance().signOut()
            activity.successfulLogout()
        }.addOnFailureListener { e ->
            Log.e(activity.javaClass.simpleName, "Error logging out user", e)
            Toast.makeText(activity, "Sorry, we can't log you out!", Toast.LENGTH_SHORT).show()
        }
    }

    fun getCurrentUserID(): String {
        var currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }
        return currentUserID
    }
}