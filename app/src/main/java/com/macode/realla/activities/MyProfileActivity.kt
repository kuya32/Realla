package com.macode.realla.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
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
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.macode.realla.R
import com.macode.realla.databinding.ActivityMyProfileBinding
import com.macode.realla.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.lang.Exception
import java.util.*
import kotlin.collections.HashMap

class MyProfileActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivityMyProfileBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val toolbar = findViewById<Toolbar>(R.id.myProfileToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Profile"
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        supportActionBar?.setHomeAsUpIndicator(R.drawable.red_back)
        toolbar.setTitleTextColor(Color.parseColor("#430300"))
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        fireStoreClass.establishUser(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!Places.isInitialized()) {
            Places.initialize(this@MyProfileActivity, resources.getString(R.string.google_api_key))
        }

        binding.myProfileImage.setOnClickListener(this)
        binding.myProfileLocationEditInput.setOnClickListener(this)
        binding.myProfileGetCurrentLocationButton.setOnClickListener(this)
        binding.myProfileSaveButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.myProfileImage -> {
                showPictureDialog()
            }
            // TODO: Include Google Places API, but first figure out why the functionality to enter an address is not working
            R.id.myProfileLocationEditInput -> {
                try {
                    val fields = listOf(
                        Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS
                    )
                    val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this@MyProfileActivity)
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            R.id.myProfileGetCurrentLocationButton -> {
                if (!isLocationEnabled()) {
                    Toast.makeText(
                        this,
                        "Your location provider is turned off. Please turn it on!",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                } else {
                    Dexter.withContext(this).withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ).withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            if (report!!.areAllPermissionsGranted()) {
                                println("Made it to request new location method")
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
                    showProgressDialog("Getting current location...")
                }
            }
            R.id.myProfileSaveButton -> {
                if (selectedImageFileUri != null) {
                    uploadUserImageToFirebase()
                } else {
                    showProgressDialog("Updating user info...")
                    updateUserProfileData()
                }
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
                            .into(binding.myProfileImage)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this@MyProfileActivity, "Failed to load the image!", Toast.LENGTH_SHORT).show()
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
                        .into(binding.myProfileImage)
                }
            } else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
                val place: Place = Autocomplete.getPlaceFromIntent(data!!)
                val regex = "([^,]+), ([A-Z]{2,})".toRegex()
                val matchResult = regex.find(place.address.toString())
                binding.myProfileLocationEditInput.setText(matchResult?.value.toString())
                latitude = place.latLng!!.latitude
                longitude = place.latLng!!.longitude
            }
        }
    }

    fun updateMyProfileUserDetails(loggedInUser: User) {
        Glide
            .with(this)
            .load(loggedInUser.image)
            .centerCrop()
            .placeholder(R.drawable.person)
            .into(binding.myProfileImage)

        binding.nameTitle.text = loggedInUser.firstName
        binding.firstNameEditInput.setText(loggedInUser.firstName)
        binding.lastNameEditInput.setText(loggedInUser.lastName)
        binding.usernameEditInput.setText(loggedInUser.username)
        binding.phoneEditInput.setText(loggedInUser.phone)
        binding.myProfileLocationEditInput.setText("${loggedInUser.cityLocation}, ${loggedInUser.stateLocation}")
        binding.occupationEditInput.setText(loggedInUser.occupation)
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000
            numUpdates = 2
        }
        println("Made it to request location updates")
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallBack, Looper.myLooper())
        println("Request location updates has been called")
    }

    private val locationCallBack = object: LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            val lastLocation: Location = result.lastLocation
            latitude = lastLocation.latitude
            println(latitude.toString())
            Log.i("Current Latitude", "$latitude")
            longitude = lastLocation.longitude
            println(longitude.toString())
            Log.i("Current Longitude", "$longitude")

            println("Made it to set address")
            setAddressToLocationInput()
            println("Set address has been called")
        }
    }

    @SuppressLint("SetTextI18n")
    fun setAddressToLocationInput() = runBlocking {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val addressList = use()
                if(addressList != null && addressList.isNotEmpty()) {
                    val address: Address = addressList[0]
                    val cityName = address.locality
                    val stateName = address.adminArea
                    println(cityName + stateName)
                    hideProgressDialog()
                    binding.myProfileLocationEditInput.setText("$cityName, $stateName")
                    println("location has been updated!")
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

    private fun uploadUserImageToFirebase() {
        showProgressDialog("Uploading user image...")
        if (selectedImageFileUri != null) {
            val storageRef = storageReference.reference.child("ProfileImage${System.currentTimeMillis()}.png")
            storageRef.putFile(selectedImageFileUri!!).addOnSuccessListener { taskSnapshot ->
                Log.i("MyProfileImageURL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    Log.i("DownloadableImageURL", uri.toString())
                    profileImageURL = uri.toString()
                    updateUserProfileData()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this@MyProfileActivity, exception.message, Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }
        }
    }

    private fun updateUserProfileData() {
        val userHashMap = HashMap<String, Any>()
        if (profileImageURL!!.isNotEmpty() && profileImageURL != userDetails.image) {
            userHashMap["image"] = profileImageURL!!
        }

        if (binding.firstNameEditInput.text.toString() != userDetails.firstName) {
            userHashMap["firstName"] = binding.firstNameEditInput.text.toString()
        }

        if (binding.lastNameEditInput.text.toString() != userDetails.lastName) {
            userHashMap["lastName"] = binding.lastNameEditInput.text.toString()
        }

        if (binding.usernameEditInput.text.toString() != userDetails.username) {
            userHashMap["username"] = binding.usernameEditInput.text.toString()
        }

        if (binding.phoneEditInput.text.toString() != userDetails.phone) {
            userHashMap["phone"] = binding.phoneEditInput.text.toString()
        }

        if (binding.myProfileLocationEditInput.text.toString() != userDetails.cityLocation) {
            val cityName = binding.myProfileLocationEditInput.text!!.substring(0, binding.myProfileLocationEditInput.text!!.indexOf(","))
            userHashMap["cityLocation"] = cityName
        }

        if (binding.myProfileLocationEditInput.text.toString() != userDetails.stateLocation) {
            val stateName = binding.myProfileLocationEditInput.text!!.substring(binding.myProfileLocationEditInput.text!!.indexOf(",") + 2)
            userHashMap["stateLocation"] = stateName
        }

        if (binding.occupationEditInput.text.toString() != userDetails.occupation) {
            userHashMap["occupation"] = binding.occupationEditInput.text.toString()
        }

        fireStoreClass.updatedUserProfileData(this, userHashMap)
    }

    fun profileUpdatedSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

}