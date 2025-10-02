package id.erela.surveyproduct.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.graphics.Insets
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import id.erela.surveyproduct.BuildConfig
import id.erela.surveyproduct.R
import id.erela.surveyproduct.activities.AnswerActivity.Companion.ANSWER_CHECKBOX_MULTIPLE
import id.erela.surveyproduct.activities.AnswerActivity.Companion.ANSWER_PHOTO
import id.erela.surveyproduct.activities.AnswerActivity.Companion.ANSWER_TEXT
import id.erela.surveyproduct.bottom_sheets.SelectOutletBottomSheet
import id.erela.surveyproduct.databinding.ActivityCheckInBinding
import id.erela.surveyproduct.dialogs.LoadingDialog
import id.erela.surveyproduct.helpers.PermissionHelper
import id.erela.surveyproduct.helpers.SharedPreferencesHelper
import id.erela.surveyproduct.helpers.customs.CustomToast
import id.erela.surveyproduct.objects.OutletItem
import id.erela.surveyproduct.objects.QuestionsItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CheckInActivity : AppCompatActivity() {
    private val binding: ActivityCheckInBinding by lazy {
        ActivityCheckInBinding.inflate(layoutInflater)
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var selectedOutlet = 0
    private var latitude: Double = 0.0
    private var selectedOutletText: String? = null
    private var longitude: Double = 0.0
    private var cameraCaptureFileName: String = ""
    private var imageUri: Uri? = null
    private val sharedPreferences: SharedPreferences by lazy {
        SharedPreferencesHelper.getSharedPreferences(applicationContext)
    }
    private val cameraLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        with(it) {
            binding.apply {
                if (resultCode == RESULT_OK) {
                    sharedPreferences.edit {
                        putString(IMAGE_URI, imageUri.toString())
                    }
                    photoContainer.visibility = View.VISIBLE
                    photoPlaceholder.visibility = View.GONE
                    photoPreview.visibility = View.VISIBLE
                    photoPreview.setImageURI(imageUri)
                } else {
                    imageUri = null
                    sharedPreferences.edit {
                        remove(IMAGE_URI)
                    }
                    photoContainer.visibility = View.GONE
                    photoPlaceholder.visibility = View.VISIBLE
                    photoPreview.visibility = View.GONE
                }
            }
        }
    }

    companion object {
        const val CHECK_IN_ID = "CHECK_IN_ID"
        const val ANSWER_GROUP_ID = "ANSWER_GROUP_ID"
        const val SELECTED_OUTLET = "CHECK_IN_SELECTED_OUTLET"
        const val SELECTED_OUTLET_TEXT = "CHECK_IN_SELECTED_OUTLET_TEXT"
        const val LATITUDE = "CHECK_IN_LATITUDE"
        const val LONGITUDE = "CHECK_IN_LONGITUDE"
        const val IMAGE_URI = "CHECK_IN_IMAGE_URI"
        const val CHECK_IN_UPLOADED = "CHECK_IN_UPLOADED"
        var questionIdArray = ArrayList<Int>()
        var subQuestionIdArray = ArrayList<Int?>()
        val surveyQuestionsList = ArrayList<QuestionsItem>()

        @SuppressLint("StaticFieldLeak")
        var activity: Activity? = null

        fun start(context: Context) {
            context.startActivity(
                Intent(context, CheckInActivity::class.java)
            )
        }

        fun clearCheckInData(context: Context) {
            SharedPreferencesHelper.getSharedPreferences(context).edit {
                remove(CHECK_IN_ID)
                remove(ANSWER_GROUP_ID)
                remove(SELECTED_OUTLET)
                remove(SELECTED_OUTLET_TEXT)
                remove(LATITUDE)
                remove(LONGITUDE)
                remove(IMAGE_URI)
                remove(CHECK_IN_UPLOADED)
            }
        }

        fun clearCheckOutData(context: Context) {
            SharedPreferencesHelper.getSharedPreferences(context).edit {
                remove(CheckOutActivity.IMAGE_URI)
                remove(CheckOutActivity.LATITUDE)
                remove(CheckOutActivity.LONGITUDE)
            }
        }

        fun clearAnswerData(context: Context) {
            SharedPreferencesHelper.getSharedPreferences(context).edit {
                remove(AnswerActivity.ANSWER_UPLOADED)
                surveyQuestionsList.forEach { question ->
                    if (question.subQuestions.isNullOrEmpty()) {
                        when (question.questionType) {
                            "photo" -> {
                                remove("${ANSWER_PHOTO}_${question.iD}_0")
                            }

                            "essay" -> {
                                remove("${ANSWER_TEXT}_${question.iD}_0")
                            }

                            "checkbox" -> {
                                for (i in 0 until question.checkboxOptions?.size!!) {
                                    remove("${ANSWER_CHECKBOX_MULTIPLE}_${question.iD}_0_${i}")
                                }
                            }

                            "multiple" -> {
                                for (i in 0 until question.multipleOptions?.size!!) {
                                    remove("${ANSWER_CHECKBOX_MULTIPLE}_${question.iD}_0_${i}")
                                }
                            }
                        }
                    } else {
                        question.subQuestions.forEach { subQuestion ->
                            when (subQuestion!!.questionType) {
                                "photo" -> {
                                    remove("${ANSWER_PHOTO}_${subQuestion.questionID}_${subQuestion.iD}")
                                }

                                "essay" -> {
                                    remove("${ANSWER_TEXT}_${subQuestion.questionID}_${subQuestion.iD}")
                                }

                                "checkbox" -> {
                                    for (i in 0 until question.checkboxOptions?.size!!) {
                                        remove("${ANSWER_CHECKBOX_MULTIPLE}_${subQuestion.questionID}_${subQuestion.iD}_${i}")
                                    }
                                }

                                "multiple" -> {
                                    for (i in 0 until question.multipleOptions?.size!!) {
                                        remove("${ANSWER_CHECKBOX_MULTIPLE}_${subQuestion.questionID}_${subQuestion.iD}_${i}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Mapbox.getInstance(this@CheckInActivity)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars: Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        init()
    }

    override fun onResume() {
        super.onResume()
        activity = this
    }

    override fun onDestroy() {
        super.onDestroy()
        activity = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionHelper.REQUEST_CODE_CAMERA) {
            if (PermissionHelper.isPermissionGranted(
                    this@CheckInActivity,
                    PermissionHelper.CAMERA
                )
            ) {
                openCamera()
            } else {
                PermissionHelper.requestPermission(
                    this@CheckInActivity,
                    arrayOf(PermissionHelper.CAMERA),
                    PermissionHelper.REQUEST_CODE_CAMERA
                )
            }
        }
        if (requestCode == PermissionHelper.REQUEST_LOCATION_GPS) {
            if (isLocationEnabled()) {
                getLastKnownLocation()
            } else {
                showLocationError()
                getLastKnownLocation()
            }
        }
    }

    private fun init() {
        binding.apply {
            onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    clearCheckInData(this@CheckInActivity)
                    clearAnswerData(this@CheckInActivity)
                    CheckOutActivity.clearCheckOutData(this@CheckInActivity)
                    finish()
                }
            })

            val isCheckInUploaded = sharedPreferences.getBoolean(CHECK_IN_UPLOADED, false)
            if (isCheckInUploaded) {
                AnswerActivity.start(
                    this@CheckInActivity,
                    sharedPreferences.getInt(SELECTED_OUTLET, 0),
                    imageUri,
                    sharedPreferences.getFloat(LATITUDE, 0f).toDouble(),
                    sharedPreferences.getFloat(LONGITUDE, 0f).toDouble()
                )
                finish()
            }

            backButton.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            // Restore outlet text
            selectedOutlet = sharedPreferences.getInt(SELECTED_OUTLET, 0)
            selectedOutletText =
                sharedPreferences.getString(SELECTED_OUTLET_TEXT, null)
            if (selectedOutletText != null) {
                outletText.text = selectedOutletText
            }
            // Restore photo preview if exists
            imageUri = sharedPreferences.getString(IMAGE_URI, null)
                ?.toUri()
            imageUri?.let {
                photoContainer.visibility = View.VISIBLE
                photoPlaceholder.visibility = View.GONE
                photoPreview.visibility = View.VISIBLE
                photoPreview.setImageURI(it)
            }
            // Restore map position if needed
            latitude = sharedPreferences.getFloat(LATITUDE, 0f).toDouble()
            longitude = sharedPreferences.getFloat(LONGITUDE, 0f).toDouble()
            // Initialize location services
            fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this@CheckInActivity)
            // Check and restore location
            if (!isLocationEnabled()) {
                showLocationError()
            } else if (latitude == 0.0 && longitude == 0.0) {
                getLastKnownLocation()
            } else {
                setMapPreview()
            }

            refreshButton.setOnClickListener {
                getLastKnownLocation()
            }

            chooseOutletButton.setOnClickListener {
                showOutletSelector()
            }

            takePhotoButton.setOnClickListener {
                handlePhotoCapture()
            }

            nextButton.setOnClickListener {
                if (selectedOutlet == 0 || imageUri == null) {
                    CustomToast(applicationContext).setMessage(
                        if (selectedOutlet == 0) "Please select outlet first!"
                        else "Please take photo first!"
                    ).setFontColor(getColor(R.color.custom_toast_font_failed))
                        .setBackgroundColor(getColor(R.color.custom_toast_background_failed))
                        .show()
                    return@setOnClickListener
                } else {
                    AnswerActivity.start(
                        this@CheckInActivity,
                        sharedPreferences.getInt(SELECTED_OUTLET, 0),
                        imageUri!!,
                        sharedPreferences.getFloat(LATITUDE, 0f).toDouble(),
                        sharedPreferences.getFloat(LONGITUDE, 0f).toDouble()
                    )
                }
            }
        }
    }

    private fun setMapPreview() {
        binding.apply {
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
            getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun getLastKnownLocation() {
        if (
            ContextCompat.checkSelfPermission(
                this@CheckInActivity, PermissionHelper.ACCESS_COARSE_LOCATION
            ) != PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this@CheckInActivity, PermissionHelper.ACCESS_FINE_LOCATION
            ) != PERMISSION_GRANTED
        ) {
            PermissionHelper.requestPermission(
                this@CheckInActivity,
                arrayOf(
                    PermissionHelper.ACCESS_COARSE_LOCATION,
                    PermissionHelper.ACCESS_FINE_LOCATION
                ),
                PermissionHelper.REQUEST_LOCATION_GPS
            )
        } else {
            val dialog = LoadingDialog(this@CheckInActivity)
            if (dialog.window != null)
                dialog.show()
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    dialog.dismiss()
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude
                        sharedPreferences.edit {
                            putFloat(LATITUDE, latitude.toFloat())
                            putFloat(LONGITUDE, longitude.toFloat())
                        }
                        setMapPreview()
                    }
                }
        }
    }

    private fun openCamera() {
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.forLanguageTag("id-ID")).format(Date())
        cameraCaptureFileName = "Super_CheckIn_Capture_$timeStamp.jpg"
        imageUri = contentResolver.insert(
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

    private fun showLocationError() {
        CustomToast(applicationContext)
            .setMessage("Please turn on your location first!")
            .setBackgroundColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.custom_toast_background_failed
                )
            )
            .setFontColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.custom_toast_font_failed
                )
            ).show()
    }

    private fun showOutletSelector() {
        binding.apply {
            val bottomSheet = SelectOutletBottomSheet(this@CheckInActivity).also {
                with(it) {
                    setOnOutletSelectedListener(object :
                        SelectOutletBottomSheet.OnOutletSelectedListener {
                        @SuppressLint("SetTextI18n")
                        override fun onOutletSelected(outlet: OutletItem) {
                            selectedOutlet = outlet.iD!!.toInt()
                            selectedOutletText = "${outlet.name} | OutletID: ${outlet.outletID}"
                            outletText.text = selectedOutletText
                            SharedPreferencesHelper.getSharedPreferences(context).edit {
                                putInt(SELECTED_OUTLET, selectedOutlet)
                                putString(SELECTED_OUTLET_TEXT, selectedOutletText)
                            }
                        }
                    })
                }
            }

            if (bottomSheet.window != null)
                bottomSheet.show()
        }
    }

    private fun handlePhotoCapture() {
        if (PermissionHelper.isPermissionGranted(
                this@CheckInActivity,
                PermissionHelper.CAMERA
            )
        ) {
            openCamera()
        } else {
            PermissionHelper.requestPermission(
                this@CheckInActivity,
                arrayOf(PermissionHelper.CAMERA),
                PermissionHelper.REQUEST_CODE_CAMERA
            )
        }
    }
}