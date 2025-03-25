package id.erela.surveyproduct.fragments

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.LocationManager
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity.LOCATION_SERVICE
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import id.erela.surveyproduct.BuildConfig
import id.erela.surveyproduct.R
import id.erela.surveyproduct.databinding.FragmentCheckOutBinding
import id.erela.surveyproduct.dialogs.LoadingDialog
import id.erela.surveyproduct.helpers.PermissionHelper
import id.erela.surveyproduct.helpers.SharedPreferencesHelper
import id.erela.surveyproduct.helpers.customs.CustomToast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CheckOutFragment(private val context: Context) : Fragment() {
    private var binding: FragmentCheckOutBinding? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var cameraCaptureFileName: String = ""
    private var imageUri: Uri? = null
    private val cameraLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        with(it) {
            binding?.apply {
                if (resultCode == RESULT_OK) {
                    SharedPreferencesHelper.getSharedPreferences(context).edit {
                        putString(IMAGE_URI, imageUri.toString())
                    }
                    photoContainer.visibility = View.VISIBLE
                    photoPlaceholder.visibility = View.GONE
                    photoPreview.visibility = View.VISIBLE
                    photoPreview.setImageURI(imageUri)
                }
            }
        }
    }

    companion object {
        const val LATITUDE = "CHECK_OUT_LATITUDE"
        const val LONGITUDE = "CHECK_OUT_LONGITUDE"
        const val IMAGE_URI = "CHECK_OUT_IMAGE_URI"

        fun clearCheckInData(context: Context) {
            SharedPreferencesHelper.getSharedPreferences(context).edit {
                remove(IMAGE_URI)
                remove(LATITUDE)
                remove(LONGITUDE)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Mapbox.getInstance(context)
        binding = FragmentCheckOutBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restoreUIState()
        setupListeners()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionHelper.REQUEST_CODE_CAMERA) {
            if (grantResults.isNotEmpty()) {
                if (grantResults[0] == PERMISSION_GRANTED) {
                    if (VERSION.SDK_INT <= VERSION_CODES.P) {
                        handlePhotoCapture()
                    } else {
                        openCamera()
                    }
                }
            }
        }
    }

    private fun restoreUIState() {
        binding?.apply {
            imageUri =
                SharedPreferencesHelper.getSharedPreferences(context)
                    .getString(IMAGE_URI, null)?.toUri()
            imageUri?.let {
                photoContainer.visibility = View.VISIBLE
                photoPlaceholder.visibility = View.GONE
                photoPreview.visibility = View.VISIBLE
                photoPreview.setImageURI(it)
            }
            latitude = SharedPreferencesHelper.getSharedPreferences(context)
                .getFloat(LATITUDE, 0f).toDouble()
            longitude = SharedPreferencesHelper.getSharedPreferences(context)
                .getFloat(LONGITUDE, 0f).toDouble()
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            if (!isLocationEnabled()) {
                showLocationError()
            } else if (latitude == 0.0 && longitude == 0.0) {
                getLastKnownLocation()
            } else {
                setMapPreview()
            }
        }
    }

    private fun setupListeners() {
        binding?.apply {
            refreshButton.setOnClickListener {
                getLastKnownLocation()
            }

            takePhotoButton.setOnClickListener {
                handlePhotoCapture()
            }
        }
    }

    private fun setMapPreview() {
        binding?.apply {
            mapPreview.getMapAsync { map ->
                with(map) {
                    setStyle(BuildConfig.MAP_URL + BuildConfig.MAP_API_KEY)
                    uiSettings.apply {
                        isCompassEnabled = false
                        isLogoEnabled = true
                        isZoomGesturesEnabled = false
                        isRotateGesturesEnabled = false
                        isScrollGesturesEnabled = false
                        isTiltGesturesEnabled = false
                        isHorizontalScrollGesturesEnabled = false
                    }
                    cameraPosition =
                        com.mapbox.mapboxsdk.camera.CameraPosition.Builder().target(
                            LatLng(latitude, longitude)
                        ).zoom(17.5).build()
                }
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            context.getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun getLastKnownLocation() {
        if (
            ContextCompat.checkSelfPermission(
                context, PermissionHelper.ACCESS_COARSE_LOCATION
            ) != PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                context, PermissionHelper.ACCESS_FINE_LOCATION
            ) != PERMISSION_GRANTED
        ) {
            PermissionHelper.requestPermission(
                requireActivity(),
                arrayOf(
                    PermissionHelper.ACCESS_COARSE_LOCATION,
                    PermissionHelper.ACCESS_FINE_LOCATION
                ),
                PermissionHelper.REQUEST_LOCATION_GPS
            )
        } else {
            val dialog = LoadingDialog(context)
            if (dialog.window != null)
                dialog.show()
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    dialog.dismiss()
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude
                        SharedPreferencesHelper.getSharedPreferences(context).edit {
                            putFloat(CheckInFragment.LATITUDE, latitude.toFloat())
                            putFloat(CheckInFragment.LONGITUDE, longitude.toFloat())
                        }
                        setMapPreview()
                    }
                }
        }
    }

    private fun showLocationError() {
        CustomToast(context.applicationContext)
            .setMessage("Please turn on your location first!")
            .setBackgroundColor(
                ContextCompat.getColor(
                    context.applicationContext,
                    R.color.custom_toast_background_failed
                )
            )
            .setFontColor(
                ContextCompat.getColor(
                    context.applicationContext,
                    R.color.custom_toast_font_failed
                )
            ).show()
    }

    private fun openCamera() {
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.forLanguageTag("id-ID")).format(Date())
        cameraCaptureFileName = "Super_CheckOut_Capture_$timeStamp.jpg"
        imageUri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().also {
                with(it) {
                    put(MediaStore.Images.Media.TITLE, cameraCaptureFileName)
                    put(MediaStore.Images.Media.DESCRIPTION, "Image capture by camera")
                }
            }
        )!!

        cameraLauncher.launch(
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
                with(it) {
                    putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                }
            }
        )
    }

    private fun handlePhotoCapture() {
        if (PermissionHelper.isPermissionGranted(
                requireActivity(),
                PermissionHelper.CAMERA
            )
        ) {
            openCamera()
        } else {
            PermissionHelper.requestPermission(
                requireActivity(),
                arrayOf(PermissionHelper.CAMERA),
                PermissionHelper.REQUEST_CODE_CAMERA
            )
        }
    }
}