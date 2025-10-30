package id.erela.surveyproduct.activities

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
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
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import id.erela.surveyproduct.BuildConfig
import id.erela.surveyproduct.R
import id.erela.surveyproduct.activities.CheckInActivity.Companion.CHECK_IN_UPLOADED
import id.erela.surveyproduct.databinding.ActivityCheckOutBinding
import id.erela.surveyproduct.dialogs.LoadingDialog
import id.erela.surveyproduct.helpers.Generic
import id.erela.surveyproduct.helpers.PermissionHelper
import id.erela.surveyproduct.helpers.SharedPreferencesHelper
import id.erela.surveyproduct.helpers.UserDataHelper
import id.erela.surveyproduct.helpers.api.AppAPI
import id.erela.surveyproduct.helpers.customs.CustomToast
import id.erela.surveyproduct.objects.CheckInResponse
import id.erela.surveyproduct.objects.CheckOutResponse
import id.erela.surveyproduct.objects.InsertAnswerResponse
import id.erela.surveyproduct.objects.IsAlready15MinutesResponse
import id.erela.surveyproduct.objects.SurveyAnswer
import id.erela.surveyproduct.objects.UsersSuper
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CheckOutActivity : AppCompatActivity() {
    private val binding: ActivityCheckOutBinding by lazy {
        ActivityCheckOutBinding.inflate(layoutInflater)
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var cameraCaptureFileName: String = ""
    private var imageUri: Uri? = null
    private var rewardImageUri: Uri? = null
    private var rewardProofImageUri: Uri? = null
    private lateinit var dialog: LoadingDialog
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
    private val rewardPhotoLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        with(it) {
            binding.apply {
                if (resultCode == RESULT_OK) {
                    sharedPreferences.edit {
                        putString(REWARD_URI, rewardImageUri.toString())
                    }
                    rewardPhotoContainer.visibility = View.VISIBLE
                    rewardPhotoPlaceholder.visibility = View.GONE
                    rewardPhotoPreview.visibility = View.VISIBLE
                    rewardPhotoPreview.setImageURI(rewardImageUri)
                } else {
                    rewardImageUri = null
                    sharedPreferences.edit {
                        remove(REWARD_URI)
                    }
                    rewardPhotoContainer.visibility = View.GONE
                    rewardPhotoPlaceholder.visibility = View.VISIBLE
                    rewardPhotoPreview.visibility = View.GONE
                }
            }
        }
    }
    private val rewardProofPhotoLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            with(it) {
                binding.apply {
                    if (resultCode == RESULT_OK) {
                        sharedPreferences.edit {
                            putString(REWARD_PROOF_URI, rewardProofImageUri.toString())
                        }
                        rewardProofPhotoContainer.visibility = View.VISIBLE
                        rewardProofPhotoPlaceholder.visibility = View.GONE
                        rewardProofPhotoPreview.visibility = View.VISIBLE
                        rewardProofPhotoPreview.setImageURI(rewardProofImageUri)
                    } else {
                        rewardProofImageUri = null
                        sharedPreferences.edit {
                            remove(REWARD_PROOF_URI)
                        }
                        rewardProofPhotoContainer.visibility = View.GONE
                        rewardProofPhotoPlaceholder.visibility = View.VISIBLE
                        rewardProofPhotoPreview.visibility = View.GONE
                    }
                }
            }
        }

    companion object {
        const val LATITUDE = "CHECK_OUT_LATITUDE"
        const val LONGITUDE = "CHECK_OUT_LONGITUDE"
        const val IMAGE_URI = "CHECK_OUT_IMAGE_URI"
        const val REWARD_URI = "REWARD_IMAGE_URI"
        const val REWARD_PROOF_URI = "REWARD_PROOF_IMAGE_URI"
        const val NOTE_REWARD = "NOTE_REWARD"

        fun start(
            context: Context,
        ) {
            context.startActivity(
                Intent(context, CheckOutActivity::class.java)
            )
        }

        fun clearCheckOutData(context: Context) {
            SharedPreferencesHelper.getSharedPreferences(context).edit {
                remove(IMAGE_URI)
                remove(LATITUDE)
                remove(LONGITUDE)
                remove(REWARD_URI)
                remove(REWARD_PROOF_URI)
                remove(NOTE_REWARD)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Mapbox.getInstance(this@CheckOutActivity)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars: Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        init()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionHelper.REQUEST_CODE_CAMERA) {
            if (PermissionHelper.isPermissionGranted(
                    this@CheckOutActivity,
                    PermissionHelper.CAMERA
                )
            ) {
                // Determine which camera action was requested and open the appropriate camera
                when (cameraCaptureFileName.substringBeforeLast("_")) {
                    "Super_CheckOut_Reward_Capture" -> openRewardPhotoCamera()
                    "Super_CheckOut_RewardProof_Capture" -> openRewardProofCamera()
                    else -> openCamera() // Fallback to original if no specific reward capture name
                }
            } else {
                PermissionHelper.requestPermission(
                    this@CheckOutActivity,
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
            prepareFormInput()

            backButton.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            if (sharedPreferences.getString(REWARD_URI, null) != null ||
                sharedPreferences.getString(REWARD_PROOF_URI, null) != null
            ) {
                rewardYes.isChecked = true
            }

            if (rewardYes.isChecked) {
                rewardYesContainer.visibility = View.VISIBLE
                rewardNoContainer.visibility = View.GONE
            }

            if (rewardNo.isChecked) {
                rewardNoContainer.visibility = View.VISIBLE
                rewardYesContainer.visibility = View.GONE
            }

            rewardYes.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    rewardYesContainer.visibility = View.VISIBLE
                    rewardNoContainer.visibility = View.GONE

                    noteNoRewardField.setText("")
                }
            }

            rewardNo.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    rewardNoContainer.visibility = View.VISIBLE
                    rewardYesContainer.visibility = View.GONE
                    SharedPreferencesHelper.getSharedPreferences(this@CheckOutActivity).edit {
                        remove(REWARD_URI)
                        remove(REWARD_PROOF_URI)
                    }
                    rewardImageUri = null
                    rewardProofImageUri = null

                    rewardPhotoContainer.visibility = View.GONE
                    rewardPhotoPlaceholder.visibility = View.VISIBLE
                    rewardPhotoPreview.visibility = View.GONE

                    rewardProofPhotoContainer.visibility = View.GONE
                    rewardProofPhotoPlaceholder.visibility = View.VISIBLE
                    rewardProofPhotoPreview.visibility = View.GONE
                }
            }
            // Restore imageUri for CheckOut photo
            imageUri =
                sharedPreferences.getString(IMAGE_URI, null)?.toUri()
            imageUri?.let {
                photoContainer.visibility = View.VISIBLE
                photoPlaceholder.visibility = View.GONE
                photoPreview.visibility = View.VISIBLE
                photoPreview.setImageURI(it)
            }
            // Restore rewardImageUri for Reward Photo
            rewardImageUri =
                sharedPreferences.getString(REWARD_URI, null)?.toUri()
            rewardImageUri?.let {
                rewardPhotoContainer.visibility = View.VISIBLE
                rewardPhotoPlaceholder.visibility = View.GONE
                rewardPhotoPreview.visibility = View.VISIBLE
                rewardPhotoPreview.setImageURI(it)
            }
            // Restore rewardProofImageUri for Reward Proof Photo
            rewardProofImageUri =
                sharedPreferences.getString(REWARD_PROOF_URI, null)?.toUri()
            rewardProofImageUri?.let {
                rewardProofPhotoContainer.visibility = View.VISIBLE
                rewardProofPhotoPlaceholder.visibility = View.GONE
                rewardProofPhotoPreview.visibility = View.VISIBLE
                rewardProofPhotoPreview.setImageURI(it)
            }

            latitude = sharedPreferences.getFloat(LATITUDE, 0f).toDouble()
            longitude = sharedPreferences.getFloat(LONGITUDE, 0f).toDouble()
            fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this@CheckOutActivity)
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

            takePhotoButton.setOnClickListener {
                handlePhotoCapture()
            }

            takePhotoRewardButton.setOnClickListener {
                handleRewardPhotoCapture()
            }

            takePhotoRewardProofButton.setOnClickListener {
                handleRewardProofPhotoCapture()
            }

            submitButton.setOnClickListener {
                executeUpload()
            }
        }
    }

    private fun prepareFormInput() {
        binding.apply {
            noteNoRewardField.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    if (s!!.isEmpty()) {
                        noteNoRewardLayout.error =
                            if (getString(R.string.language) == "en") "Outlet name is required" else "Nama outlet wajib diisi"

                    } else {
                        noteNoRewardLayout.error = null
                        sharedPreferences.edit {
                            putString(NOTE_REWARD, s.toString())
                        }
                    }
                }
            })
        }
    }

    private fun executeUpload() {
        dialog = LoadingDialog(this@CheckOutActivity)

        binding.apply {
            val isRewardYesSelected = rewardYes.isChecked

            if (isRewardYesSelected) {
                if (rewardImageUri == null) {
                    CustomToast(applicationContext)
                        .setMessage(
                            if (getString(R.string.language) == "en") "Please take reward photo first!"
                            else "Mohon ambil foto hadiah/kupingan terlebih dahulu!"
                        )
                        .setFontColor(getColor(R.color.custom_toast_font_failed))
                        .setBackgroundColor(getColor(R.color.custom_toast_background_failed))
                        .show()
                    return
                }
                if (rewardProofImageUri == null) {
                    CustomToast(applicationContext)
                        .setMessage(
                            if (getString(R.string.language) == "en") "Please take reward proof photo first!"
                            else "Mohon ambil foto bukti penyerahan terlebih dahulu!"
                        )
                        .setFontColor(getColor(R.color.custom_toast_font_failed))
                        .setBackgroundColor(getColor(R.color.custom_toast_background_failed))
                        .show()
                    return
                }
            } else { // rewardNo is selected
                if (noteNoRewardField.text.isNullOrBlank()) {
                    CustomToast(applicationContext)
                        .setMessage(
                            if (getString(R.string.language) == "en") "Please fill the note for no reward!"
                            else "Silakan isi catatan!"
                        )
                        .setFontColor(getColor(R.color.custom_toast_font_failed))
                        .setBackgroundColor(getColor(R.color.custom_toast_background_failed))
                        .show()
                    return
                }
            }

            AppAPI.superEndpoint.isAlready15Minutes(
                sharedPreferences.getInt(
                    CheckInActivity.CHECK_IN_ID,
                    0
                )
            ).enqueue(object : Callback<IsAlready15MinutesResponse> {
                override fun onResponse(
                    call: Call<IsAlready15MinutesResponse?>,
                    response: Response<IsAlready15MinutesResponse?>
                ) {
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            val result = response.body()
                            when (result?.code) {
                                1 -> {
                                    CustomToast.getInstance(this@CheckOutActivity)
                                        .setMessage(
                                            if (getString(R.string.language) == "en") "Passed! You have checked in more than 15 minutes ago"
                                            else "Lulus! Kamu telah check-in lebih dari 15 menit yang lalu."
                                        )
                                        .setBackgroundColor(
                                            getColor(R.color.custom_toast_background_success)
                                        )
                                        .setFontColor(
                                            getColor(R.color.custom_toast_font_success)
                                        ).show()
                                    uploadCheckOut()
                                }

                                else -> {
                                    CustomToast(applicationContext)
                                        .setMessage(
                                            if (getString(R.string.language) == "en") {
                                                if (result?.data?.remaining!! > 1) {
                                                    "Sorry, you\'re still ${result.data.timeDiff} minutes away from check in. Please wait another ${result.data.remaining} minutes."
                                                } else {
                                                    "Sorry, you\'re still ${result.data.timeDiff} minutes away from check in. Please wait in a minute."
                                                }
                                            } else
                                                "Maaf, Anda masih ${result?.data?.timeDiff} menit semenjak check in. Mohon tunggu ${result?.data?.remaining} menit lagi."
                                        )
                                        .setBackgroundColor(
                                            getColor(R.color.custom_toast_background_failed)
                                        )
                                        .setFontColor(
                                            getColor(R.color.custom_toast_font_failed)
                                        ).show()
                                    Generic.crashReport(Exception("15 Minutes Check Response. ${result?.message}"))
                                }
                            }
                        } else {
                            Log.e("ERROR", "15 Minutes Check Response body is null")
                            CustomToast(applicationContext)
                                .setMessage(
                                    if (getString(R.string.language) == "en") "Check Out Failed!"
                                    else "Check Out Gagal!"
                                )
                                .setBackgroundColor(
                                    getColor(R.color.custom_toast_background_failed)
                                )
                                .setFontColor(
                                    getColor(R.color.custom_toast_font_failed)
                                ).show()
                            Generic.crashReport(Exception("15 Minutes Check Response body is null"))
                        }
                    } else {
                        Log.e(
                            "ERROR",
                            "15 Minutes Check Response is not successful. ${response.code()}: ${response.message()}"
                        )
                        CustomToast(applicationContext)
                            .setMessage(
                                if (getString(R.string.language) == "en") "Check Out Failed!"
                                else "Check Out Gagal!"
                            )
                            .setBackgroundColor(
                                getColor(R.color.custom_toast_background_failed)
                            )
                            .setFontColor(
                                getColor(R.color.custom_toast_font_failed)
                            ).show()
                        Generic.crashReport(Exception("15 Minutes Check Response is not successful. ${response.code()}: ${response.message()}"))
                    }
                }

                override fun onFailure(
                    call: Call<IsAlready15MinutesResponse?>,
                    throwable: Throwable
                ) {
                    dialog.dismiss()
                    throwable.printStackTrace()
                    Log.e("ERROR", "15 Minutes Check failure! ${throwable.message}")
                    CustomToast(applicationContext)
                        .setMessage(
                            if (getString(R.string.language) == "en") "Check Out Failed!"
                            else "Check Out Gagal!"
                        )
                        .setBackgroundColor(
                            getColor(R.color.custom_toast_background_failed)
                        )
                        .setFontColor(
                            getColor(R.color.custom_toast_font_failed)
                        ).show()
                    Generic.crashReport(Exception("15 Minutes Check failure! ${throwable.message}"))
                }
            })
        }
    }

    private fun uploadCheckOut() {
        if (dialog.window != null)
            dialog.show()
        val data: MutableMap<String, RequestBody> = mutableMapOf()
        with(data) {
            put(
                "ID",
                createPartFromString(
                    sharedPreferences.getInt(CheckInActivity.CHECK_IN_ID, 0).toString()
                )!!
            )
            put(
                "LatOut",
                createPartFromString(
                    sharedPreferences.getFloat(LATITUDE, 0f).toString()
                )!!
            )
            put(
                "LongOut",
                createPartFromString(
                    sharedPreferences.getFloat(LONGITUDE, 0f).toString()
                )!!
            )
            put(
                "Note",
                createPartFromString(
                    sharedPreferences.getString(NOTE_REWARD, null).toString()
                )!!
            )
        }
        val photoCheckOut = if (imageUri != null)
            createMultipartBody(
                imageUri!!,
                "PhotoOut"
            )
        else null
        val photoReward = if (rewardImageUri != null)
            createMultipartBody(
                rewardImageUri!!,
                "RewardPhoto"
            )
        else null
        val photoRewardProof = if (rewardProofImageUri != null)
            createMultipartBody(
                rewardProofImageUri!!,
                "RewardProofPhoto"
            )
        else null

        (if (photoCheckOut != null || (photoReward != null && photoRewardProof != null))
            AppAPI.superEndpoint.checkOut(data, photoCheckOut, photoReward, photoRewardProof)
        else AppAPI.superEndpoint.checkOutNoPhoto(data)).enqueue(
            object : Callback<CheckOutResponse> {
                override fun onResponse(
                    call: Call<CheckOutResponse>,
                    response: Response<CheckOutResponse>
                ) {
                    dialog.dismiss()
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            val result = response.body()
                            when (result?.code) {
                                1 -> {
                                    CustomToast(applicationContext)
                                        .setMessage(
                                            if (getString(R.string.language) == "en") "Check Out Successfully!"
                                            else "Check Out Berhasil!"
                                        )
                                        .setBackgroundColor(
                                            getColor(R.color.custom_toast_background_success)
                                        )
                                        .setFontColor(
                                            getColor(R.color.custom_toast_font_success)
                                        ).show()
                                    CheckInActivity.clearCheckInData(this@CheckOutActivity)
                                    CheckInActivity.clearAnswerData(this@CheckOutActivity)
                                    clearCheckOutData(this@CheckOutActivity)
                                    finish()
                                }

                                0 -> {
                                    CustomToast(applicationContext)
                                        .setMessage(
                                            if (getString(R.string.language) == "en") "Check Out Failed! ${result.message}"
                                            else "Check Out Gagal! ${result.message}"
                                        )
                                        .setBackgroundColor(
                                            getColor(R.color.custom_toast_background_failed)
                                        )
                                        .setFontColor(
                                            getColor(R.color.custom_toast_font_failed)
                                        ).show()
                                    Generic.crashReport(Exception("Check Out Response. ${result.message}"))
                                }
                            }
                        } else {
                            Log.e("ERROR", "Check Out Response body is null")
                            CustomToast(applicationContext)
                                .setMessage(
                                    if (getString(R.string.language) == "en") "Check Out Failed!"
                                    else "Check Out Gagal!"
                                )
                                .setBackgroundColor(
                                    getColor(R.color.custom_toast_background_failed)
                                )
                                .setFontColor(
                                    getColor(R.color.custom_toast_font_failed)
                                ).show()
                            Generic.crashReport(Exception("Check Out Response body is null"))
                        }
                    } else {
                        Log.e(
                            "ERROR",
                            "Check Out Response is not successful. ${response.code()}: ${response.message()}"
                        )
                        CustomToast(applicationContext)
                            .setMessage(
                                if (getString(R.string.language) == "en") "Check Out Failed!"
                                else "Check Out Gagal!"
                            )
                            .setBackgroundColor(
                                getColor(R.color.custom_toast_background_failed)
                            )
                            .setFontColor(
                                getColor(R.color.custom_toast_font_failed)
                            ).show()
                        Generic.crashReport(Exception("Check Out Response is not successful. ${response.code()}: ${response.message()}"))
                    }
                }

                override fun onFailure(call: Call<CheckOutResponse>, throwable: Throwable) {
                    dialog.dismiss()
                    throwable.printStackTrace()
                    Log.e("ERROR", "Check Out failure! ${throwable.message}")
                    CustomToast(applicationContext)
                        .setMessage(
                            if (getString(R.string.language) == "en") "Check Out Failed!"
                            else "Check Out Gagal!"
                        )
                        .setBackgroundColor(
                            getColor(R.color.custom_toast_background_failed)
                        )
                        .setFontColor(
                            getColor(R.color.custom_toast_font_failed)
                        ).show()
                    Generic.crashReport(Exception("Check Out failure! ${throwable.message}"))
                }
            })
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
                        com.mapbox.mapboxsdk.camera.CameraPosition.Builder().target(
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
                this@CheckOutActivity, PermissionHelper.ACCESS_COARSE_LOCATION
            ) != PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this@CheckOutActivity, PermissionHelper.ACCESS_FINE_LOCATION
            ) != PERMISSION_GRANTED
        ) {
            PermissionHelper.requestPermission(
                this@CheckOutActivity,
                arrayOf(
                    PermissionHelper.ACCESS_COARSE_LOCATION,
                    PermissionHelper.ACCESS_FINE_LOCATION
                ),
                PermissionHelper.REQUEST_LOCATION_GPS
            )
        } else {
            val dialog = LoadingDialog(this@CheckOutActivity)
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

    private fun showLocationError() {
        CustomToast(applicationContext)
            .setMessage(
                if (getString(R.string.language) == "en") "Please turn on your location first!"
                else "Harap aktifkan lokasi terlebih dahulu!"
            )
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

    private fun openCamera() {
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.forLanguageTag("id-ID")).format(Date())
        cameraCaptureFileName = "Super_CheckOut_Capture_$timeStamp.jpg"
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

    private fun openRewardPhotoCamera() {
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.forLanguageTag("id-ID")).format(Date())
        cameraCaptureFileName = "Super_CheckOut_Reward_Capture_$timeStamp.jpg"
        rewardImageUri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().also {
                with(it) {
                    put(MediaStore.Images.Media.TITLE, cameraCaptureFileName)
                    put(MediaStore.Images.Media.DESCRIPTION, "Reward Image capture by camera")
                }
            }
        )!!

        rewardPhotoLauncher.launch(
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
                with(it) {
                    putExtra(MediaStore.EXTRA_OUTPUT, rewardImageUri)
                }
            }
        )
    }

    private fun openRewardProofCamera() {
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.forLanguageTag("id-ID")).format(Date())
        cameraCaptureFileName = "Super_CheckOut_RewardProof_Capture_$timeStamp.jpg"
        rewardProofImageUri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().also {
                with(it) {
                    put(MediaStore.Images.Media.TITLE, cameraCaptureFileName)
                    put(MediaStore.Images.Media.DESCRIPTION, "Reward Proof Image capture by camera")
                }
            }
        )!!

        rewardProofPhotoLauncher.launch(
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
                with(it) {
                    putExtra(MediaStore.EXTRA_OUTPUT, rewardProofImageUri)
                }
            }
        )
    }

    private fun handlePhotoCapture() {
        if (PermissionHelper.isPermissionGranted(
                this@CheckOutActivity,
                PermissionHelper.CAMERA
            )
        ) {
            openCamera()
        } else {
            PermissionHelper.requestPermission(
                this@CheckOutActivity,
                arrayOf(PermissionHelper.CAMERA),
                PermissionHelper.REQUEST_CODE_CAMERA
            )
        }
    }

    private fun handleRewardPhotoCapture() {
        if (PermissionHelper.isPermissionGranted(
                this@CheckOutActivity,
                PermissionHelper.CAMERA
            )
        ) {
            openRewardPhotoCamera()
        } else {
            PermissionHelper.requestPermission(
                this@CheckOutActivity,
                arrayOf(PermissionHelper.CAMERA),
                PermissionHelper.REQUEST_CODE_CAMERA
            )
        }
    }

    private fun handleRewardProofPhotoCapture() {
        if (PermissionHelper.isPermissionGranted(
                this@CheckOutActivity,
                PermissionHelper.CAMERA
            )
        ) {
            openRewardProofCamera()
        } else {
            PermissionHelper.requestPermission(
                this@CheckOutActivity,
                arrayOf(PermissionHelper.CAMERA),
                PermissionHelper.REQUEST_CODE_CAMERA
            )
        }
    }

    private fun createPartFromString(stringData: String?): RequestBody? {
        return stringData?.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    private fun createMultipartBody(uri: Uri, name: String): MultipartBody.Part? {
        return try {
            val file = File(getRealPathFromURI(uri)!!)
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(name, file.name, requestBody)
        } catch (e: Exception) {
            Log.e("createMultipartBody", "Error creating MultipartBody.Part", e)
            null
        }
    }

    private fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (displayNameIndex != -1) {
                val fileName = cursor.getString(displayNameIndex)
                cursor.close()
                return fileName
            }
        }
        cursor?.close()
        return null
    }

    private fun getRealPathFromURI(uri: Uri): String? {
        val contentResolver = contentResolver
        val fileName = getFileName(contentResolver!!, uri)

        if (fileName != null) {
            val file = File(cacheDir, fileName)
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(file)
                val buffer = ByteArray(4 * 1024)
                var read: Int

                while (inputStream!!.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                return file.absolutePath
            } catch (e: IOException) {
                Log.e("getRealPathFromURI", "Error: ${e.message}")
            }
        }

        return null
    }
}
