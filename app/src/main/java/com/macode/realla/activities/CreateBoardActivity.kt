package com.macode.realla.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.macode.realla.R
import com.macode.realla.databinding.ActivityCreateBoardBinding
import com.macode.realla.firebase.FireStoreClass
import com.macode.realla.models.Board
import java.io.IOException

class CreateBoardActivity : BaseActivity() {
    private lateinit var binding: ActivityCreateBoardBinding
    private lateinit var usersFullName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setUpToolbar()

        if (intent.hasExtra("usersFullName")) {
            usersFullName = intent.getStringExtra("usersFullName").toString()
        }

        binding.boardImage.setOnClickListener {
            showPictureDialog()
        }

        binding.createBoardButton.setOnClickListener {
            showProgressDialog("Creating board...")
            if (selectedImageFileUri == null) {
                Toast.makeText(this@CreateBoardActivity, "Please select an image!", Toast.LENGTH_SHORT).show()
            } else if (binding.boardNameEditInput.text.isNullOrEmpty()) {
                showError(binding.boardNameInput, "Please enter a name for the board!")
            } else {
                uploadBoardInformationToFirebase()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                if (data != null) {
                    selectedImageFileUri = data.data
                    try {
                        val selectedImageBitmap = ImageDecoder.decodeBitmap(
                            ImageDecoder.createSource(this.contentResolver,
                                selectedImageFileUri!!
                            ))
                        selectedImageFileUri = convertToImageFile(selectedImageBitmap)
                        Log.i("Saved image: ", "Path :: $selectedImageFileUri")
                        Glide
                            .with(this)
                            .load(selectedImageFileUri)
                            .centerCrop()
                            .placeholder(R.drawable.person)
                            .into(binding.boardImage)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this@CreateBoardActivity, "Failed to load the image!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (requestCode == CAMERA) {
                if (data != null) {
                    val bitmap: Bitmap = data.extras!!.get("data") as Bitmap
                    selectedImageFileUri = convertToImageFile(bitmap)
                    Log.i("Saved image: ", "Path :: $selectedImageFileUri")
                    Glide
                        .with(this)
                        .load(selectedImageFileUri)
                        .centerCrop()
                        .placeholder(R.drawable.person)
                        .into(binding.boardImage)
                }
            }
        }
    }

    private fun uploadBoardInformationToFirebase() {
        if (selectedImageFileUri != null) {
            val storageRef = storageReference.reference.child("BoardImages${System.currentTimeMillis()}.png")
            storageRef.putFile(selectedImageFileUri!!).addOnSuccessListener { taskSnapshot ->
                Log.i("BoardImageURL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    Log.i("DownloadableImageURL", uri.toString())
                    boardImageURL = uri.toString()
                    createBoard()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this@CreateBoardActivity, exception.message, Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }
        }
    }

    private fun createBoard() {
        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(getCurrentID())

        var board = Board(
            "",
            getDate(),
            binding.boardNameEditInput.text.toString(),
            boardImageURL!!,
            usersFullName,
            assignedUsersArrayList
        )

        fireStoreClass.createBoard(this, board)
    }

    fun boardCreatedSuccessfully() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun setUpToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.createBoardToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Create Board"
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        supportActionBar?.setHomeAsUpIndicator(R.drawable.red_back)
        toolbar.setTitleTextColor(Color.parseColor("#763626"))
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}