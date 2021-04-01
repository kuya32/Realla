package com.macode.realla.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.*
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.telephony.PhoneNumberFormattingTextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.auth.FirebaseAuth
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.macode.realla.*
import com.macode.realla.R
import com.macode.realla.databinding.ActivitySetUpBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.lang.Exception
import java.util.*

class SetUpActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivitySetUpBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var progressDialog: Dialog
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var phoneNumberFormattingTextWatcher: PhoneNumberFormattingTextWatcher = PhoneNumberFormattingTextWatcher()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetUpBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val toolbar = findViewById<Toolbar>(R.id.setUpToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Set Up Account Information"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        supportActionBar?.setHomeAsUpIndicator(R.drawable.blue_back)

        toolbar.setTitleTextColor(Color.parseColor("#66AFD4"))
        toolbar.setNavigationOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, IntroActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!Places.isInitialized()) {
            Places.initialize(this@SetUpActivity, resources.getString(R.string.google_api_key))
        }

        binding.setUpProfileImage.setOnClickListener(this)
        binding.setUpLocationEditInput.setOnClickListener(this)
        binding.getCurrentLocationButton.setOnClickListener(this)
        binding.setUpSaveButton.setOnClickListener(this)

        binding.phoneEditInput.addTextChangedListener(phoneNumberFormattingTextWatcher)
    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.setUpProfileImage -> {
                showPictureDialog()
            }
            // TODO: Google Places API is not letting me enter a address
            R.id.setUpLocationEditInput -> {
                try {
                    val fields = listOf(
                        Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS
                    )
                    val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this@SetUpActivity)
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            R.id.getCurrentLocationButton -> {
                if (!isLocationEnabled()) {
                    Toast.makeText(this, "Your location provider is turned off. Please turn it on!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                } else {
                    Dexter.withContext(this).withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ).withListener(object: MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            if (report!!.areAllPermissionsGranted()) {
                                requestNewLocationData()
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permissions: MutableList<PermissionRequest>?,
                            token: PermissionToken?
                        ) {
                            showRationalDialogForPermissions()
                        }

                    }).onSameThread().check()
                    showProgressDialog("Getting your location...")
                }
            }
            R.id.setUpSaveButton -> {
                showProgressDialog("Saving account info...")
                if (selectedImageFileUri == null) {
                    Toast.makeText(this@SetUpActivity, "Please select an image!", Toast.LENGTH_SHORT).show()
                } else if (binding.usernameEditInput.text.isNullOrEmpty()) {
                    showError(binding.usernameInput, "Please enter a username!")
                } else if (binding.phoneEditInput.text.isNullOrEmpty() || binding.phoneEditInput.text!!.length != 14) {
                    showError(binding.phoneInput, "Please enter a valid phone number!")
                } else if (binding.setUpLocationEditInput.text.isNullOrEmpty() || !binding.setUpLocationEditInput.text!!.contains(" ") || !binding.setUpLocationEditInput.text!!.contains(",")) {
                    showError(binding.setUpLocationInput, "Please provide a location!")
                } else if (binding.occupationEditInput.text.isNullOrEmpty()) {
                    showError(binding.occupationInput, "Please provide a occupation!")
                } else {
                    val cityName = binding.setUpLocationEditInput.text!!.substring(0, binding.setUpLocationEditInput.text!!.indexOf(","))
                    val stateName = binding.setUpLocationEditInput.text!!.substring(binding.setUpLocationEditInput.text!!.indexOf(",") + 2)
                    userDetails.image = selectedImageFileUri.toString()
                    userDetails.username = binding.usernameEditInput.text.toString()
                    userDetails.phone = binding.phoneEditInput.text.toString()
                    userDetails.cityLocation = cityName
                    userDetails.stateLocation = stateName
                    userDetails.occupation = binding.occupationEditInput.text.toString()
                    fireStoreClass.updateUser(this, userDetails)
                }
            }
        }
    }

    private fun showPictureDialog() {
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

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000
            numUpdates = 1
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallBack, Looper.myLooper())
    }

    private val locationCallBack = object: LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            val lastLocation: Location = result.lastLocation
            latitude = lastLocation.latitude
            Log.i("Current Latitude", "$latitude")
            longitude = lastLocation.longitude
            Log.i("Current Longitude", "$longitude")

            setAddressToLocationInput()
        }
    }

    fun setAddressToLocationInput() = runBlocking {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val addressList = use()
                if(addressList != null && addressList.isNotEmpty()) {
                    val address: Address = addressList[0]
                    val cityName = address.locality
                    val stateName = address.adminArea
                    hideProgressDialog()
                    binding.setUpLocationEditInput.setText("$cityName, $stateName")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun use(): List<Address>? {
        val geoCoder: Geocoder = Geocoder(this, Locale.getDefault())
        return geoCoder.getFromLocation(latitude, longitude, 1)
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
                        selectedImageFileUri = saveImageToInternalStorage(selectedImageBitmap)
                        Log.i("Saved image: ", "Path :: $selectedImageFileUri")
                        Glide
                            .with(this)
                            .load(selectedImageFileUri)
                            .centerCrop()
                            .placeholder(R.drawable.person)
                            .into(binding.setUpProfileImage)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this@SetUpActivity, "Failed to load the image!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (requestCode == CAMERA) {
                if (data != null) {
                    val bitmap: Bitmap = data.extras!!.get("data") as Bitmap
                    selectedImageFileUri = saveImageToInternalStorage(bitmap)
                    Log.i("Saved image: ", "Path :: $selectedImageFileUri")
                    Glide
                        .with(this)
                        .load(selectedImageFileUri)
                        .centerCrop()
                        .placeholder(R.drawable.person)
                        .into(binding.setUpProfileImage)
                }
            } else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
                val place: Place = Autocomplete.getPlaceFromIntent(data!!)
                val regex = "([^,]+), ([A-Z]{2,})".toRegex()
                val matchResult = regex.find(place.address.toString())
                binding.setUpLocationEditInput.setText(matchResult?.value.toString())
                latitude = place.latLng!!.latitude
                longitude = place.latLng!!.longitude
            }
        }
    }


    fun updateUserSuccess() {
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}