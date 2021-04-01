package com.macode.realla.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.macode.realla.R
import com.macode.realla.databinding.FragmentEditEmailBinding

class EditEmailFragment : Fragment() {
    private lateinit var binding: FragmentEditEmailBinding
    private lateinit var toolbar: Toolbar
    private lateinit var firebaseAuth: FirebaseAuth
    private var firebaseUser: FirebaseUser? = null
    private val fireStore = FirebaseFirestore.getInstance()
    private val userReference = fireStore.collection("Users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditEmailBinding.inflate(inflater, container, false)
        val view = binding.root
        toolbar = view.findViewById(R.id.editEmailToolbar)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Edit Email"
        (requireActivity() as AppCompatActivity).supportActionBar?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        (requireActivity() as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.blue_back)
        toolbar.setTitleTextColor(Color.parseColor("#B3FCFF"))
        toolbar.setNavigationOnClickListener {
            requireActivity().finish()
            startActivity(requireActivity().intent)
        }

        binding.editEmailEditInput.setText(firebaseUser?.email.toString())

        binding.editEmailSaveButton.setOnClickListener {
            saveEditedEmail()
        }

        return view
    }

    private fun saveEditedEmail() {
        val newEmail = binding.editEmailEditInput.text.toString()

        if (newEmail.isEmpty() || !newEmail.contains("@")) {
            showError(binding.editEmailInput, "Please provide a valid email!")
        } else {
            firebaseUser!!.updateEmail(newEmail).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    firebaseUser!!.sendEmailVerification().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            userReference.document(firebaseUser!!.uid).update("email", newEmail).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(requireActivity(), "Verification email sent", Toast.LENGTH_SHORT).show()
                                    requireActivity().finish()
                                    startActivity(requireActivity().intent)
                                }
                            }.addOnFailureListener { e ->
                                Log.e(requireActivity().javaClass.simpleName, "Failed to update email in FireStore", e)
                                Toast.makeText(requireActivity(), "Sorry, we couldn't update your email in the database!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }.addOnFailureListener { e ->
                        Log.e(requireActivity().javaClass.simpleName, "Failed to send verification email", e)
                        Toast.makeText(requireActivity(), "Sorry, we couldn't send verification email!", Toast.LENGTH_SHORT).show()
                    }
                }
            }.addOnFailureListener { e ->
                Log.e(requireActivity().javaClass.simpleName, "Failed to update user email", e)
                Toast.makeText(requireActivity(), "Sorry, we couldn't update your email!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showError(layout: TextInputLayout, string: String) {
        layout.error = string
        layout.requestFocus()
    }
}