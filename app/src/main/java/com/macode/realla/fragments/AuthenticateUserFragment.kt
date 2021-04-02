package com.macode.realla.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.macode.realla.R
import com.macode.realla.databinding.FragmentAuthenticateUserBinding

class AuthenticateUserFragment : Fragment() {
    private lateinit var binding: FragmentAuthenticateUserBinding
    private lateinit var toolbar: Toolbar
    private lateinit var main: RelativeLayout
    private lateinit var secondary: RelativeLayout
    private lateinit var authenticateRetrieveBundle: Bundle
    private lateinit var authCredential: AuthCredential
    private lateinit var firebaseAuth: FirebaseAuth
    private var firebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthenticateUserBinding.inflate(inflater, container, false)
        val view = binding.root
        toolbar = view.findViewById(R.id.authenticationToolbar)
        main = requireActivity().findViewById(R.id.mainRelative)
        secondary = requireActivity().findViewById(R.id.secondaryRelative)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Account Authentication"
        (requireActivity() as AppCompatActivity).supportActionBar?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        (requireActivity() as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.blue_back)
        toolbar.setTitleTextColor(Color.parseColor("#B3FCFF"))
        toolbar.setNavigationOnClickListener {
            main.visibility = View.VISIBLE
            secondary.visibility = View.GONE
        }

        binding.authorizeButton.setOnClickListener {
            authenticateUser()
        }

        return view
    }

    private fun authenticateUser() {
        authenticateRetrieveBundle = this.arguments!!
        val fragmentId = authenticateRetrieveBundle.getString("fragmentId")
        val email = binding.authenticationEmailEditInput.text.toString()
        val password = binding.authenticationPasswordEditInput.text.toString()

        if (email.isEmpty() || !email.contains("@")) {
            showError(binding.authenticationEmailInput, "Email is not valid!")
        } else if (password.isEmpty() || password.length < 6) {
            showError(binding.authenticationPasswordInput, "Password must be greater than 6 characters!")
        } else {
            authCredential = EmailAuthProvider.getCredential(email, password);
            firebaseUser?.reauthenticate(authCredential)?.addOnCompleteListener { task ->
                if (task.isSuccessful && fragmentId.equals("email")) {
                    val editEmailFragment = EditEmailFragment()
                    requireActivity().supportFragmentManager.beginTransaction().replace(R.id.accountSettingsFragmentContainer, editEmailFragment).commit()
                } else if (task.isSuccessful && fragmentId.equals("password")) {
                    val editPasswordFragment = EditPasswordFragment()
                    requireActivity().supportFragmentManager.beginTransaction().replace(R.id.accountSettingsFragmentContainer, editPasswordFragment).commit()
                } else {
                    showError(binding.authenticationEmailInput, "Email or password are incorrect!")
                    showError(binding.authenticationPasswordInput, "Email or password are incorrect!")
                }
            }
        }
    }

    private fun showError(layout: TextInputLayout, string: String) {
        layout.error = string
        layout.requestFocus()
    }

}