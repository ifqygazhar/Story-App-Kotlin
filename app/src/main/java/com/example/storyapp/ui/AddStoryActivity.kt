package com.example.storyapp.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.storyapp.R
import com.example.storyapp.data.Result
import com.example.storyapp.data.pref.UserPreferences
import com.example.storyapp.databinding.ActivityAddStoryBinding
import com.example.storyapp.ui.CameraActivity.Companion.CAMERAX_RESULT
import com.example.storyapp.ui.viewmodel.AddStoryViewModel
import com.example.storyapp.ui.viewmodel.factory.AddStoryViewModelFactory
import com.example.storyapp.util.reduceFileImage
import com.example.storyapp.util.showToast
import com.example.storyapp.util.uriToFile
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLat: Double? = null
    private var currentLon: Double? = null

    private val addStoryViewModel: AddStoryViewModel by viewModels {
        AddStoryViewModelFactory.getInstance(this)
    }

    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.includeToolbar.toolbar)
        supportActionBar?.title = getString(R.string.add_story)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)

        val savedImageUri = addStoryViewModel.getImageUri()
        if (savedImageUri != null) {
            currentImageUri = savedImageUri
            showImage()
        }

        binding.includeToolbar.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

// Handle switch for enabling location
        binding.switchLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                getCurrentLocation()
            } else {
                currentLat = null
                currentLon = null
            }
        }


        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        val token = UserPreferences(this).getToken()

        observeViewModel()

        binding.btnGallery.setOnClickListener { startGallery() }
        binding.btnCamera.setOnClickListener { startCameraX() }
        binding.btnUpload.setOnClickListener { uploadImage(token!!) }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun observeViewModel() {
        addStoryViewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) showProgressBar() else hideProgressBar()
        }


        addStoryViewModel.uploadResult.observe(this) { result ->
            when (result) {
                is Result.Success -> {
                    hideProgressBar()
                    showToast(this, getString(R.string.story_uploaded_successfully))
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }

                is Result.Loading -> showProgressBar()

                is Result.Error -> {
                    hideProgressBar()
                    showToast(this, result.error ?: "Failed to upload story.")
                }
            }
        }

    }

    private fun uploadImage(token: String) {
        val description = binding.etDescription.text.toString().trim()

        if (description.isEmpty()) {
            showToast(this, getString(R.string.please_provide_a_description))
            return
        }

        if (currentImageUri == null) {
            showToast(this, getString(R.string.please_select_an_image))
            return
        }

        val file = uriToFile(currentImageUri!!, this)
        val compressedFile = file.reduceFileImage()

        val descriptionRequestBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val photoRequestBody = compressedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val photoMultipart =
            MultipartBody.Part.createFormData("photo", compressedFile.name, photoRequestBody)

        val latRequestBody = currentLat?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
        val lonRequestBody = currentLon?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

        addStoryViewModel.uploadStory(
            token,
            descriptionRequestBody,
            photoMultipart,
            latRequestBody,
            lonRequestBody
        )
    }


    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                showToast(this, getString(R.string.permission_request_granted))
            } else {
                showToast(this, getString(R.string.permission_request_denied))
            }
        }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            addStoryViewModel.setImageUri(uri)
            currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }


    private fun startCameraX() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERAX_RESULT) {
            val uri = it.data?.getStringExtra(CameraActivity.EXTRA_CAMERAX_IMAGE)?.toUri()
            addStoryViewModel.setImageUri(uri)
            currentImageUri = uri
            showImage()
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        currentLat = location.latitude
                        currentLon = location.longitude
                        showToast(this, "Location Added")
                    } else {
                        showToast(this, "Location not found")
                    }
                }
        } else {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            showToast(this, "Permission Location Denied")
        }
    }


    private fun showImage() {
        val imageUri = addStoryViewModel.getImageUri()
        if (imageUri != null) {
            currentImageUri = imageUri
            binding.ivPicture.setImageURI(imageUri)
        } else {
            Log.d("Image URI", "No image to show")
        }
    }

    private fun showProgressBar() {
        binding.btnUpload.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.btnUpload.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}