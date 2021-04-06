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
import com.google.firebase.messaging.FirebaseMessaging
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
import kotlin.collections.HashMap

class SetUpActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivitySetUpBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
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
                    uploadUserInformationToFirebase()
                }
            }
        }
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
                        selectedImageFileUri = convertToImageFile(selectedImageBitmap)
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
                    selectedImageFileUri = convertToImageFile(bitmap)
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

    private fun uploadUserInformationToFirebase() {
        if (selectedImageFileUri != null) {
            val tokenRef = FirebaseMessaging.getInstance().token
            val storageRef = storageReference.reference.child("ProfileImage${System.currentTimeMillis()}.png")
            tokenRef.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    fcmToken = task.result
                    storageRef.putFile(selectedImageFileUri!!).addOnSuccessListener { taskSnapshot ->
                        Log.i("ProfileImageURL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())
                        taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                            Log.i("DownloadableImageURL", uri.toString())
                            profileImageURL = uri.toString()
                            val cityName = binding.setUpLocationEditInput.text!!.substring(0, binding.setUpLocationEditInput.text!!.indexOf(","))
                            val stateName = binding.setUpLocationEditInput.text!!.substring(binding.setUpLocationEditInput.text!!.indexOf(",") + 2)
                            val userHashMap = HashMap<String, Any>()
                            userHashMap["image"] = profileImageURL.toString()
                            userHashMap["username"] = binding.usernameEditInput.text.toString()
                            userHashMap["phone"] = binding.phoneEditInput.text.toString()
                            userHashMap["cityLocation"] = cityName
                            userHashMap["stateLocation"] = stateName
                            userHashMap["occupation"] = binding.occupationEditInput.text.toString()
                            userHashMap["fcmToken"] = fcmToken
                            fireStoreClass.updatedUserProfileData(this, userHashMap)
                        }
                    }.addOnFailureListener { exception ->
                        Toast.makeText(this@SetUpActivity, exception.message, Toast.LENGTH_LONG).show()
                        hideProgressDialog()
                    }
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this@SetUpActivity, e.message, Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }
        }
    }

    fun updateUserSuccess() {
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}