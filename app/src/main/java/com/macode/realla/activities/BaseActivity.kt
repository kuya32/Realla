package com.macode.realla.activities

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.webkit.MimeTypeMap
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.macode.realla.R
import com.macode.realla.databinding.ActivityBaseBinding
import com.macode.realla.firebase.FireStoreClass
import com.macode.realla.models.User
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

open class BaseActivity : AppCompatActivity() {

    companion object {
        const val GALLERY = 1
        const val CAMERA = 2
        const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
        const val MY_PROFILE_REQUEST_CODE = 11
        const val CREATE_BOARD_REQUEST_CODE = 12
        const val EDIT_BOARD_REQUEST_CODE = 13
        const val MEMBERS_REQUEST_CODE = 14
        const val CARD_DETAILS_REQUEST_CODE = 15
        const val IMAGE_DIRECTORY = "ReallaAppImages"
        const val NOTI = "Notification"
    }

    private lateinit var binding: ActivityBaseBinding
    lateinit var fcmToken: String
    var anyChangesMade: Boolean = false
    var userDetails: User = User()
    private val fireStore = FirebaseFirestore.getInstance()
    val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val currentFirebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    val userReference = fireStore.collection("Users")
    val storageReference = FirebaseStorage.getInstance()
    var fireStoreClass: FireStoreClass = FireStoreClass()
    var selectedImageFileUri: Uri? = null
    var profileImageURL: String? = ""
    var boardImageURL: String? = ""

    private var doubleBackToExitPressedOnce = false

    private var progressDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
    }

    fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf("Select photo from gallery", "Capture photo from camera")
        pictureDialog.setItems(pictureDialogItems) {
                _, which ->
            when(which) {
                0 -> choosePhotoFromGallery()
                1 -> takePhotoWithCamera()
            }
        }
        pictureDialog.show()
    }

    private fun choosePhotoFromGallery() {
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY)
                }
            }

            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()
    }

    private fun takePhotoWithCamera() {
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val galleryIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(galleryIntent, CAMERA)
                }
            }

            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()
    }


    fun convertToImageFile(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.png")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return Uri.fromFile(file.absoluteFile)
    }

//    fun getFileExtension(uri: Uri?): String? {
//        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri!!))
//    }

    fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this).setMessage("It looks like you have turned off permission required for this feature. " +
                "It can be enabled under Application Settings.")
            .setPositiveButton("Go to settings")
            {_, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel")
            {dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    fun showProgressDialog(text: String) {
        progressDialog = Dialog(this)
        progressDialog!!.setContentView(R.layout.custom_dialog_progress)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val progressText = progressDialog!!.requireViewById<TextView>(R.id.pleaseWaitText)
            progressText.text = text
        }
        progressDialog!!.show()
    }

    fun hideProgressDialog() {
        progressDialog?.dismiss()
    }

    fun getCurrentID(): String {
        currentFirebaseUser
        var currentUserID = ""
        if (currentFirebaseUser != null) {
            currentUserID = currentFirebaseUser.uid
        }
        return currentUserID
    }

    fun getDate(): String {
        val sdf = SimpleDateFormat("MM/dd/yyyy hh:mm:ss", Locale.US)
        return sdf.format(Date())
    }

    fun doubleBackToExit() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click again to exit!", Toast.LENGTH_LONG).show()

        Handler(Looper.myLooper()!!).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }

    fun showErrorSnackBar(message: String) {
        val snackBar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
    }

    fun showError(layout: TextInputLayout, text: String) {
        layout.error = text
        layout.requestFocus()
    }

    fun showErrorToastMessage(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showErrorToastException(task: Task<AuthResult>, context: Context) {
        Toast.makeText(context, task.exception!!.message, Toast.LENGTH_SHORT).show()
    }
}