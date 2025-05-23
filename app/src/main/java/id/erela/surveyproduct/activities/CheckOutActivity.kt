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
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import id.erela.surveyproduct.BuildConfig
import id.erela.surveyproduct.R
import id.erela.surveyproduct.databinding.ActivityCheckOutBinding
import id.erela.surveyproduct.dialogs.LoadingDialog
import id.erela.surveyproduct.helpers.PermissionHelper
import id.erela.surveyproduct.helpers.SharedPreferencesHelper
import id.erela.surveyproduct.helpers.UserDataHelper
import id.erela.surveyproduct.helpers.api.AppAPI
import id.erela.surveyproduct.helpers.customs.CustomToast
import id.erela.surveyproduct.objects.CheckInResponse
import id.erela.surveyproduct.objects.CheckOutResponse
import id.erela.surveyproduct.objects.InsertAnswerResponse
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
    private val userData: UsersSuper by lazy {
        UserDataHelper(this@CheckOutActivity).getData()
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var cameraCaptureFileName: String = ""
    private var imageUri: Uri? = null
    private var answerGroupId = 0
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
                }
            }
        }
    }

    companion object {
        const val LATITUDE = "CHECK_OUT_LATITUDE"
        const val LONGITUDE = "CHECK_OUT_LONGITUDE"
        const val IMAGE_URI = "CHECK_OUT_IMAGE_URI"

        fun start(
            context: Context,
            outletID: Int,
            photoIn: Uri?,
            latIn: Double,
            longIn: Double,
            answer: ArrayList<SurveyAnswer>
        ) {
            context.startActivity(
                Intent(context, CheckOutActivity::class.java).also {
                    with(it) {
                        putExtra(CheckInActivity.SELECTED_OUTLET, outletID)
                        putExtra(CheckInActivity.IMAGE_URI, photoIn.toString())
                        putExtra(CheckInActivity.LATITUDE, latIn)
                        putExtra(CheckInActivity.LONGITUDE, longIn)
                        putExtra(AnswerActivity.ANSWER_ARRAY, answer)
                    }
                }
            )
        }

        fun clearCheckOutData(context: Context) {
            SharedPreferencesHelper.getSharedPreferences(context).edit {
                remove(IMAGE_URI)
                remove(LATITUDE)
                remove(LONGITUDE)
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

    private fun init() {
        binding.apply {
            backButton.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            imageUri =
                sharedPreferences.getString(IMAGE_URI, null)?.toUri()
            imageUri?.let {
                photoContainer.visibility = View.VISIBLE
                photoPlaceholder.visibility = View.GONE
                photoPreview.visibility = View.VISIBLE
                photoPreview.setImageURI(it)
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

            submitButton.setOnClickListener {
                executeUpload()
            }
        }
    }

    private fun executeUpload() {
        dialog = LoadingDialog(this@CheckOutActivity)

        binding.apply {
            val isCheckInUploaded =
                sharedPreferences.getBoolean(CheckInActivity.CHECK_IN_UPLOADED, false)
            val isSurveyUploaded =
                sharedPreferences.getBoolean(AnswerActivity.ANSWER_UPLOADED, false)

            if (isCheckInUploaded && !isSurveyUploaded) {
                answerGroupId = sharedPreferences.getInt(CheckInActivity.ANSWER_GROUP_ID, 0)
                uploadSurveyData()
            } else if (!isCheckInUploaded && !isSurveyUploaded) {
                uploadCheckIn()
            } else if (isCheckInUploaded && isSurveyUploaded) {
                uploadCheckOut()
            }
        }
    }

    private fun uploadCheckIn() {
        if (dialog.window != null)
            dialog.show()
        val data: MutableMap<String, RequestBody> = mutableMapOf()
        with(data) {
            put("UserID", createPartFromString(userData.iD.toString())!!)
            put(
                "OutletID",
                createPartFromString(
                    sharedPreferences.getInt(
                        CheckInActivity.SELECTED_OUTLET,
                        0
                    ).toString()
                )!!
            )
            put(
                "LatIn",
                createPartFromString(
                    sharedPreferences.getFloat(CheckInActivity.LATITUDE, 0f).toString()
                )!!
            )
            put(
                "LongIn",
                createPartFromString(
                    sharedPreferences.getFloat(CheckInActivity.LONGITUDE, 0f).toString()
                )!!
            )
        }
        val photoCheckIn: MultipartBody.Part? = createMultipartBody(
            intent.getStringExtra(CheckInActivity.IMAGE_URI)!!.toUri(),
            "PhotoIn"
        )!!

        AppAPI.superEndpoint.checkIn(data, photoCheckIn!!)
            .enqueue(object : Callback<CheckInResponse> {
                override fun onResponse(
                    call: Call<CheckInResponse>,
                    response: Response<CheckInResponse>
                ) {
                    dialog.dismiss()
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            val result = response.body()
                            when (result?.code) {
                                1 -> {
                                    CustomToast(applicationContext)
                                        .setMessage("Check In Successfully!")
                                        .setBackgroundColor(
                                            getColor(R.color.custom_toast_background_success)
                                        )
                                        .setFontColor(
                                            getColor(R.color.custom_toast_font_success)
                                        ).show()
                                    CheckInActivity.clearCheckInData(this@CheckOutActivity)
                                    sharedPreferences.edit {
                                        putInt(
                                            CheckInActivity.CHECK_IN_ID,
                                            result.data?.iD!!
                                        )
                                        putInt(
                                            CheckInActivity.ANSWER_GROUP_ID,
                                            result.data.answerGroupID!!
                                        )
                                        putBoolean(
                                            CheckInActivity.CHECK_IN_UPLOADED,
                                            true
                                        )
                                    }
                                    val isCheckInUploaded =
                                        sharedPreferences.getBoolean(
                                            CheckInActivity.CHECK_IN_UPLOADED,
                                            false
                                        )
                                    answerGroupId = result.data?.answerGroupID!!
                                    if (isCheckInUploaded)
                                        uploadSurveyData()
                                }

                                0 -> {
                                    CustomToast(applicationContext)
                                        .setMessage("Check In Failed! ${result.message}")
                                        .setBackgroundColor(
                                            getColor(R.color.custom_toast_background_failed)
                                        )
                                        .setFontColor(
                                            getColor(R.color.custom_toast_font_failed)
                                        ).show()
                                    sharedPreferences.edit {
                                        putBoolean(
                                            CheckInActivity.CHECK_IN_UPLOADED,
                                            false
                                        )
                                    }
                                }
                            }
                        } else {
                            CustomToast(applicationContext)
                                .setMessage("Check In Failed!")
                                .setBackgroundColor(
                                    getColor(R.color.custom_toast_background_failed)
                                )
                                .setFontColor(
                                    getColor(R.color.custom_toast_font_failed)
                                ).show()
                            sharedPreferences.edit {
                                putBoolean(
                                    CheckInActivity.CHECK_IN_UPLOADED,
                                    false
                                )
                            }
                            Log.e("ERROR", "Check In Response body is null")
                        }
                    } else {
                        CustomToast(applicationContext)
                            .setMessage("Check In Failed!")
                            .setBackgroundColor(
                                getColor(R.color.custom_toast_background_failed)
                            )
                            .setFontColor(
                                getColor(R.color.custom_toast_font_failed)
                            ).show()
                        sharedPreferences.edit {
                            putBoolean(
                                CheckInActivity.CHECK_IN_UPLOADED,
                                false
                            )
                        }
                        Log.e(
                            "ERROR",
                            "Check In is not successful. ${response.code()}: ${response.message()}"
                        )
                    }
                }

                override fun onFailure(call: Call<CheckInResponse>, throwable: Throwable) {
                    dialog.dismiss()
                    throwable.printStackTrace()
                    CustomToast(applicationContext)
                        .setMessage("Check In Failed!")
                        .setBackgroundColor(
                            getColor(R.color.custom_toast_background_failed)
                        )
                        .setFontColor(
                            getColor(R.color.custom_toast_font_failed)
                        ).show()
                    sharedPreferences.edit {
                        putBoolean(
                            CheckInActivity.CHECK_IN_UPLOADED,
                            false
                        )
                    }
                    Log.e("ERROR", "Check In Failure. ${throwable.message}")
                }
            })
    }

    private fun uploadSurveyData() {
        if (dialog.window != null)
            dialog.show()
        val answerGroupIdPart = createPartFromString(answerGroupId.toString())
        val answers = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getParcelableArrayListExtra(
                AnswerActivity.ANSWER_ARRAY,
                SurveyAnswer::class.java
            )
        else
            intent.getParcelableArrayListExtra(AnswerActivity.ANSWER_ARRAY)

        try {
            val map = mutableListOf<MultipartBody.Part>()
            with(map) {
                for (i in 0 until answers?.size!!) {
                    add(
                        MultipartBody.Part.createFormData(
                            "Answers[$i][QuestionID]",
                            answers[i].QuestionID.toString()
                        )
                    )
                    if (answers[i].Answer != null)
                        add(
                            MultipartBody.Part.createFormData(
                                "Answers[$i][Answer]",
                                answers[i].Answer!!
                            )
                        )
                    if (answers[i].CheckboxID != null)
                        add(
                            MultipartBody.Part.createFormData(
                                "Answers[$i][CheckboxID]",
                                answers[i].CheckboxID.toString()
                            )
                        )
                    if (answers[i].MultipleID != null)
                        add(
                            MultipartBody.Part.createFormData(
                                "Answers[$i][MultipleID]",
                                answers[i].MultipleID.toString()
                            )
                        )
                    if (answers[i].SubQuestionID != null)
                        add(
                            MultipartBody.Part.createFormData(
                                "Answers[$i][SubQuestionID]",
                                answers[i].SubQuestionID.toString()
                            )
                        )
                    if (answers[i].Photo != null) {
                        add(
                            createMultipartBody(
                                answers[i].Photo!!.toUri(),
                                "Answers[$i][Photo]"
                            )!!
                        )
                    }
                }
            }

            AppAPI.superEndpoint.insertAnswer(
                answerGroupIdPart!!, map
            ).enqueue(object : Callback<InsertAnswerResponse> {
                override fun onResponse(
                    pcall: Call<InsertAnswerResponse>,
                    response: Response<InsertAnswerResponse>
                ) {
                    dialog.dismiss()
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            val result = response.body()
                            when (result?.code) {
                                1 -> {
                                    CustomToast(applicationContext)
                                        .setMessage("Survey Answer Successfully Submitted!")
                                        .setBackgroundColor(
                                            getColor(R.color.custom_toast_background_success)
                                        )
                                        .setFontColor(
                                            getColor(R.color.custom_toast_font_success)
                                        ).show()
                                    sharedPreferences.edit {
                                        putBoolean(
                                            AnswerActivity.ANSWER_UPLOADED,
                                            true
                                        )
                                    }
                                    CheckInActivity.clearAnswerData(this@CheckOutActivity)
                                    uploadCheckOut()
                                }

                                0 -> {
                                    CustomToast(applicationContext)
                                        .setMessage("Survey Answer Submission Failed! ${result.message}")
                                        .setBackgroundColor(
                                            getColor(R.color.custom_toast_background_failed)
                                        )
                                        .setFontColor(
                                            getColor(R.color.custom_toast_font_failed)
                                        ).show()
                                    sharedPreferences.edit {
                                        putBoolean(
                                            AnswerActivity.ANSWER_UPLOADED,
                                            false
                                        )
                                    }
                                }
                            }
                        } else {
                            CustomToast(applicationContext)
                                .setMessage("Survey Answer Submission Failed!")
                                .setBackgroundColor(
                                    getColor(R.color.custom_toast_background_failed)
                                )
                                .setFontColor(
                                    getColor(R.color.custom_toast_font_failed)
                                ).show()
                            sharedPreferences.edit {
                                putBoolean(
                                    AnswerActivity.ANSWER_UPLOADED,
                                    false
                                )
                            }
                            Log.e("ERROR", "Survey Answer Submission response body is null")
                        }
                    } else {
                        CustomToast(applicationContext)
                            .setMessage("Survey Answer Submission Failed!")
                            .setBackgroundColor(
                                getColor(R.color.custom_toast_background_failed)
                            )
                            .setFontColor(
                                getColor(R.color.custom_toast_font_failed)
                            ).show()
                        sharedPreferences.edit {
                            putBoolean(
                                AnswerActivity.ANSWER_UPLOADED,
                                false
                            )
                        }
                        Log.e(
                            "ERROR",
                            "Survey Answer Submission response is not successful. ${response.code()}: ${response.message()}"
                        )
                    }
                }

                override fun onFailure(call: Call<InsertAnswerResponse>, throwable: Throwable) {
                    dialog.dismiss()
                    sharedPreferences.edit {
                        putBoolean(
                            AnswerActivity.ANSWER_UPLOADED,
                            false
                        )
                    }
                    throwable.printStackTrace()
                    Log.e("ERROR", "Survey Answer Submission failure. ${throwable.message}")
                    CustomToast(applicationContext)
                        .setMessage("Survey Answer Submission Failed!")
                        .setBackgroundColor(
                            getColor(R.color.custom_toast_background_failed)
                        )
                        .setFontColor(
                            getColor(R.color.custom_toast_font_failed)
                        ).show()
                }
            })
        } catch (e: Exception) {
            dialog.dismiss()
            sharedPreferences.edit {
                putBoolean(
                    AnswerActivity.ANSWER_UPLOADED,
                    false
                )
            }
            e.printStackTrace()
            Log.e("ERROR", "Survey Answer Submission Exception: ${e.message}")
            CustomToast(applicationContext)
                .setMessage("Survey Answer Submission Failed!")
                .setBackgroundColor(
                    getColor(R.color.custom_toast_background_failed)
                )
                .setFontColor(
                    getColor(R.color.custom_toast_font_failed)
                ).show()
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
        }
        val photoCheckOut = if (imageUri != null)
            createMultipartBody(
                imageUri!!,
                "PhotoOut"
            )
        else null
        (if (photoCheckOut != null) AppAPI.superEndpoint.checkOut(
            data,
            photoCheckOut
        ) else AppAPI.superEndpoint.checkOutNoPhoto(data)).enqueue(
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
                                        .setMessage("Check Out Successfully!")
                                        .setBackgroundColor(
                                            getColor(R.color.custom_toast_background_success)
                                        )
                                        .setFontColor(
                                            getColor(R.color.custom_toast_font_success)
                                        ).show()
                                    if (CheckInActivity.activity != null && AnswerActivity.activity != null) {
                                        CheckInActivity.activity?.finish()
                                        AnswerActivity.activity?.finish()
                                        CheckInActivity.activity = null
                                        AnswerActivity.activity = null
                                        CheckInActivity.clearCheckInData(this@CheckOutActivity)
                                        CheckInActivity.clearAnswerData(this@CheckOutActivity)
                                        CheckInActivity.clearCheckOutData(this@CheckOutActivity)
                                        finish()
                                    }
                                }

                                0 -> {
                                    CustomToast(applicationContext)
                                        .setMessage("Check Out Failed! ${result.message}")
                                        .setBackgroundColor(
                                            getColor(R.color.custom_toast_background_failed)
                                        )
                                        .setFontColor(
                                            getColor(R.color.custom_toast_font_failed)
                                        ).show()
                                }
                            }
                        } else {
                            Log.e("ERROR", "Check Out Response body is null")
                            CustomToast(applicationContext)
                                .setMessage("Check Out Failed!")
                                .setBackgroundColor(
                                    getColor(R.color.custom_toast_background_failed)
                                )
                                .setFontColor(
                                    getColor(R.color.custom_toast_font_failed)
                                ).show()
                        }
                    } else {
                        Log.e(
                            "ERROR",
                            "Check Out Response is not successful. ${response.code()}: ${response.message()}"
                        )
                        CustomToast(applicationContext)
                            .setMessage("Check Out Failed!")
                            .setBackgroundColor(
                                getColor(R.color.custom_toast_background_failed)
                            )
                            .setFontColor(
                                getColor(R.color.custom_toast_font_failed)
                            ).show()
                    }
                }

                override fun onFailure(call: Call<CheckOutResponse>, throwable: Throwable) {
                    dialog.dismiss()
                    throwable.printStackTrace()
                    Log.e("ERROR", "Check Out failure! ${throwable.message}")
                    CustomToast(applicationContext)
                        .setMessage("Check Out Failed!")
                        .setBackgroundColor(
                            getColor(R.color.custom_toast_background_failed)
                        )
                        .setFontColor(
                            getColor(R.color.custom_toast_font_failed)
                        ).show()
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