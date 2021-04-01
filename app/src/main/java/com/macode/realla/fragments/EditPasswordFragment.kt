package com.macode.realla.fragments

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.macode.realla.R
import com.macode.realla.activities.ForgotPasswordActivity
import com.macode.realla.databinding.FragmentEditPasswordBinding

class EditPasswordFragment : Fragment() {
    private lateinit var binding: FragmentEditPasswordBinding
    private lateinit var toolbar: Toolbar
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authCredential: AuthCredential
    private var firebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditPasswordBinding.inflate(inflater, container, false)
        val view = binding.root
        toolbar = view.findViewById(R.id.editPasswordToolbar)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Edit Password"
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

        binding.editPasswordForgotPassword.setOnClickListener {
            startActivity(Intent(requireActivity(), ForgotPasswordActivity::class.java))
            requireActivity().finish()
        }

        binding.editPasswordSaveButton.setOnClickListener {
            saveEditedPassword()
        }

        return view
    }

    private fun saveEditedPassword() {
        val currentPassword = binding.editCurrentPasswordEditInput.text.toString()
        val newPassword = binding.editNewPasswordEditInput.text.toString()
        val confirmNewPassword = binding.confirmEditNewPasswordEditInput.text.toString()

        if (currentPassword.isEmpty() || currentPassword.length < 6) {
            showError(binding.editCurrentPasswordInput, "Current password required!")
        } else if (currentPassword == newPassword) {
            showError(binding.editNewPasswordInput, "New password should be different from the current password!")
        } else if (newPassword.isEmpty() || newPassword.length < 6) {
            showError(binding.editNewPasswordInput, "New password must be greater than 6 characters!")
        } else if (confirmNewPassword != newPassword) {
            showError(binding.confirmEditNewPasswordInput, "New password does not match!")
        } else {
            authCredential = EmailAuthProvider.getCredential(firebaseUser?.email.toString(), currentPassword)
            firebaseUser!!.reauthenticate(authCredential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    firebaseUser!!.updatePassword(newPassword).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(requireActivity(), "Verification email sent", Toast.LENGTH_SHORT).show()
                            requireActivity().finish()
                            startActivity(requireActivity().intent)
                        }
                    }.addOnFailureListener { e ->
                        Log.e(requireActivity().javaClass.simpleName, "Failed to update user in firebase", e)
                        Toast.makeText(requireActivity(), "Sorry, we couldn't update your password!", Toast.LENGTH_SHORT).show()
                    }
                }
            }.addOnFailureListener { e ->
                Log.e(requireActivity().javaClass.simpleName, "Failed to re-authenticate user", e)
                Toast.makeText(requireActivity(), "Sorry, there was a problem with re-authentication!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showError(layout: TextInputLayout, string: String) {
        layout.error = string
        layout.requestFocus()
    }
}