package id.erela.surveyproduct.fragments

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
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
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import id.erela.surveyproduct.BuildConfig
import id.erela.surveyproduct.R
import id.erela.surveyproduct.activities.StartSurveyActivity
import id.erela.surveyproduct.bottom_sheets.SelectOutletBottomSheet
import id.erela.surveyproduct.databinding.FragmentCheckInBinding
import id.erela.surveyproduct.dialogs.LoadingDialog
import id.erela.surveyproduct.helpers.PermissionHelper
import id.erela.surveyproduct.helpers.customs.CustomToast
import id.erela.surveyproduct.objects.OutletItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CheckInFragment(private val context: Context) : Fragment() {
    private var binding: FragmentCheckInBinding? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var selectedOutlet = 0
    private var latitude: Double = 0.0
    private var selectedOutletText: String? = null
    private var longitude: Double = 0.0
    private var cameraCaptureFileName: String = ""
    private var imageUri: Uri? = null
    private val activity: StartSurveyActivity by lazy {
        requireActivity() as StartSurveyActivity
    }
    private val cameraLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        with(it) {
            binding?.apply {
                if (resultCode == RESULT_OK) {
                    photoContainer.visibility = View.VISIBLE
                    photoPlaceholder.visibility = View.GONE
                    photoPreview.visibility = View.VISIBLE
                    photoPreview.setImageURI(imageUri)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retain this fragment across configuration changes.
        retainInstance = true
        if (savedInstanceState != null) {
            restoreState(savedInstanceState)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Mapbox.getInstance(context)
        binding = FragmentCheckInBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Restore UI state
        restoreUIState()
        setupListeners()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    fun restoreState(savedInstanceState: Bundle) {
        with(savedInstanceState) {
            selectedOutlet = getInt("selectedOutlet")
            selectedOutletText = getString("selectedOutletText")
            latitude = getDouble("latitude")
            longitude = getDouble("longitude")
            imageUri = getString("imageUri")?.toUri()
            // Restore the state after view is created
            view?.post {
                binding?.apply {
                    // Restore image preview if exists
                    imageUri?.let {
                        photoContainer.visibility = View.VISIBLE
                        photoPlaceholder.visibility = View.GONE
                        photoPreview.visibility = View.VISIBLE
                        photoPreview.setImageURI(it)
                    }
                    // Restore outlet selection text
                    selectedOutletText?.let {
                        outletText.text = it
                    }
                    // Restore map position if needed
                    if (latitude != 0.0 && longitude != 0.0) {
                        setMapPreview()
                    }
                }
            }
        }
    }

    fun saveState(outState: Bundle) {
        outState.apply {
            putInt("selectedOutlet", selectedOutlet)
            putString("selectedOutletText", selectedOutletText)
            putDouble("latitude", latitude)
            putDouble("longitude", longitude)
            putString("imageUri", imageUri?.toString())
        }
    }

    private fun restoreUIState() {
        binding?.apply {
            // Restore outlet text
            if (selectedOutletText != null) {
                outletText.text = selectedOutletText
            }
            // Restore photo preview if exists
            if (imageUri != null) {
                photoContainer.visibility = View.VISIBLE
                photoPlaceholder.visibility = View.GONE
                photoPreview.visibility = View.VISIBLE
                photoPreview.setImageURI(imageUri)
            }
            // Initialize location services
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            // Check and restore location
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

            chooseOutletButton.setOnClickListener {
                showOutletSelector()
            }

            takePhotoButton.setOnClickListener {
                handlePhotoCapture()
            }
        }
    }

    // ... (rest of the existing methods remain the same)
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
                        CameraPosition.Builder().target(
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
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                context, PermissionHelper.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
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
                        activity.setCheckInData(selectedOutlet, latitude, longitude, imageUri)
                        setMapPreview()
                    }
                }
        }
    }

    private fun openCamera() {
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.forLanguageTag("id-ID")).format(Date())
        cameraCaptureFileName = "FixMe_Capture_$timeStamp.jpg"
        imageUri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().also {
                with(it) {
                    put(MediaStore.Images.Media.TITLE, cameraCaptureFileName)
                    put(MediaStore.Images.Media.DESCRIPTION, "Image capture by camera")
                }
            }
        )!!
        activity.setCheckInData(selectedOutlet, latitude, longitude, imageUri)

        cameraLauncher.launch(
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
                with(it) {
                    putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                }
            }
        )
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

    private fun showOutletSelector() {
        val bottomSheet = SelectOutletBottomSheet(context).also {
            with(it) {
                setOnOutletSelectedListener(object :
                    SelectOutletBottomSheet.OnOutletSelectedListener {
                    @SuppressLint("SetTextI18n")
                    override fun onOutletSelected(outlet: OutletItem) {
                        selectedOutlet = outlet.iD!!.toInt()
                        selectedOutletText = "${outlet.name} | OutletID: ${outlet.outletID}"
                        binding?.outletText?.text = selectedOutletText
                        activity.setCheckInData(
                            selectedOutlet,
                            latitude,
                            longitude,
                            imageUri
                        )
                    }
                })
            }
        }

        if (bottomSheet.window != null)
            bottomSheet.show()
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
