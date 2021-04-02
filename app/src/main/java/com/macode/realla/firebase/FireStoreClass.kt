package com.macode.realla.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.macode.realla.activities.*
import com.macode.realla.models.Board
import com.macode.realla.models.User

class FireStoreClass {

    private val fireStore = FirebaseFirestore.getInstance()
    private val userReference = fireStore.collection("Users")
    private val boardReference = fireStore.collection("Boards")

    fun registerUser(activity: SignUpActivity, userInfo: User) {
        userReference.document(getCurrentUserID()).set(userInfo, SetOptions.merge()).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(activity, "User added to Firebase", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, "User was not added to Firebase", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun createBoard(activity: CreateBoardActivity, boardInfo: Board) {
        boardReference.document().set(boardInfo, SetOptions.merge()).addOnSuccessListener {
            Log.i(activity.javaClass.simpleName, "Board created successfully")
            Toast.makeText(activity, "Board created successfully!", Toast.LENGTH_SHORT).show()
            activity.boardCreatedSuccessfully()
        }.addOnFailureListener { e ->
            activity.hideProgressDialog()
            Log.e(activity.javaClass.simpleName, "Board failed to create", e)
            Toast.makeText(activity, "Sorry, board failed to create!", Toast.LENGTH_SHORT).show()
        }
    }

    fun establishUser(activity: Activity, readBoardsList: Boolean = false) {
        userReference.document(getCurrentUserID()).get().addOnSuccessListener { document ->
            val loggedInUser = document.toObject(User::class.java)
            when (activity) {
                is LogInActivity -> {
                    userReference.document(getCurrentUserID()).update("status", "Online").addOnSuccessListener {
                        activity.logInSuccess(loggedInUser!!)
                    }.addOnFailureListener { e ->
                        activity.hideProgressDialog()
                        Log.e(activity.javaClass.simpleName, "Error logging in user", e)
                        Toast.makeText(activity, "Sorry, we can't log you in!", Toast.LENGTH_SHORT).show()
                    }
                }
                is MainActivity -> {
                    activity.updateNavigationUserDetails(loggedInUser!!, readBoardsList)
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

    fun getBoardList(activity: MainActivity) {
        boardReference.whereArrayContains("assignedTo", getCurrentUserID()).get().addOnSuccessListener { document ->
            Log.i(activity.javaClass.simpleName, document.documents.toString())
            val boardList: ArrayList<Board> = ArrayList()
            for (i in document.documents) {
                val board = i.toObject(Board::class.java)!!
                board.documentID = i.id
                boardList.add(board)
            }
            activity.populateBoardListToUI(boardList)
        }.addOnFailureListener { e ->
            activity.hideProgressDialog()
            Log.e(activity.javaClass.simpleName, "Error while uploading boards to recycler view", e)
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
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    fun getBoardDetails(activity: TaskListActivity, documentID: String) {
        boardReference.document(documentID).get().addOnSuccessListener { document ->
            Log.i(activity.javaClass.simpleName, document.toString())
            val board = document.toObject(Board::class.java)!!
            board.documentID = document.id
            activity.boardDetails(board)
        }.addOnFailureListener { e ->
            activity.hideProgressDialog()
            Log.e(activity.javaClass.simpleName, "Error while uploading boards to recycler view", e)
        }
    }
}