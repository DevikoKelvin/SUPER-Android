package id.erela.surveyproduct.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import id.erela.surveyproduct.BuildConfig
import id.erela.surveyproduct.R
import id.erela.surveyproduct.databinding.ActivityDetailOutletBinding
import id.erela.surveyproduct.dialogs.LoadingDialog
import id.erela.surveyproduct.helpers.Generic
import id.erela.surveyproduct.helpers.UserDataHelper
import id.erela.surveyproduct.helpers.api.AppAPI
import id.erela.surveyproduct.helpers.customs.CustomToast
import id.erela.surveyproduct.objects.OutletItem
import id.erela.surveyproduct.objects.OutletResponse
import id.erela.surveyproduct.objects.UsersSuper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class DetailOutletActivity : AppCompatActivity() {
    private val binding: ActivityDetailOutletBinding by lazy {
        ActivityDetailOutletBinding.inflate(layoutInflater)
    }
    private val userData: UsersSuper by lazy {
        UserDataHelper(applicationContext).getData()
    }
    private var id: Int = 0
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private lateinit var dialog: LoadingDialog
    private lateinit var outlet: OutletItem
    private val activityResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            callNetwork()
        }
    }

    companion object {
        private const val OUTLET_ID = "OUTLET_ID"

        fun start(context: Context, id: Int) {
            context.startActivity(
                Intent(
                    context,
                    DetailOutletActivity::class.java
                ).also {
                    with(it) {
                        putExtra(OUTLET_ID, id)
                    }
                }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Mapbox.getInstance(this@DetailOutletActivity)
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
            dialog = LoadingDialog(this@DetailOutletActivity)
            backButton.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            id = intent.getIntExtra(OUTLET_ID, 0)

            mainContainerRefresh.setOnRefreshListener {
                callNetwork()
                mainContainerRefresh.isRefreshing = false
            }

            callNetwork()

            editButton.setOnClickListener {
                activityResultLauncher.launch(
                    Intent(
                        this@DetailOutletActivity,
                        AddOutletActivity::class.java
                    ).also {
                        with(it) {
                            putExtra(AddOutletActivity.DATA, outlet)
                            putExtra(AddOutletActivity.IS_EDIT, true)
                        }
                    }
                )
            }
        }
    }

    private fun callNetwork() {
        binding.apply {
            if (dialog.window != null)
                dialog.show()
            try {
                AppAPI.superEndpoint.showOutletById(id).enqueue(object : Callback<OutletResponse> {
                    override fun onResponse(
                        call: Call<OutletResponse>,
                        response: Response<OutletResponse>
                    ) {
                        dialog.dismiss()
                        if (response.isSuccessful) {
                            if (response.body() != null) {
                                val result = response.body()
                                when (result?.code) {
                                    1 -> {
                                        if (result.data != null)
                                            outlet = result.data
                                        if (outlet.creatorID == userData.iD)
                                            editButton.visibility = View.VISIBLE
                                        else
                                            editButton.visibility = View.GONE

                                        outletName.text = outlet.name
                                        outletID.text = outlet.outletID
                                        address.text = outlet.address
                                        picNumber.text = outlet.picNumber
                                        phone.text = if (!outlet.phoneNumber.toString().isEmpty())
                                            outlet.phoneNumber
                                        else "-"
                                        village.text = outlet.villageName
                                        subDistrict.text = outlet.subDistrictName
                                        cityRegency.text = outlet.cityRegencyName
                                        province.text = outlet.provinceName
                                        latitude = outlet.latitude?.toDouble() ?: 0.toDouble()
                                        longitude = outlet.longitude?.toDouble() ?: 0.toDouble()

                                        setMapPreview()
                                    }

                                    0 -> {
                                        finish()
                                        CustomToast.getInstance(applicationContext)
                                            .setMessage(result.message!!)
                                            .setFontColor(
                                                ContextCompat.getColor(
                                                    applicationContext,
                                                    R.color.custom_toast_font_failed
                                                )
                                            )
                                            .setBackgroundColor(
                                                ContextCompat.getColor(
                                                    applicationContext,
                                                    R.color.custom_toast_background_failed
                                                )
                                            ).show()
                                        Generic.crashReport(Exception("Detail Outlet Error: ${result.message}"))
                                    }
                                }
                            } else {
                                Log.e(
                                    "ERROR (Outlet Detail)",
                                    "Response body is null. ${response.code()}: ${response.message()}"
                                )
                                finish()
                                CustomToast.getInstance(applicationContext)
                                    .setMessage(
                                        if (getString(R.string.language) == "en") "Something went wrong, please try again later"
                                        else "Terjadi kesalahan, silakan coba lagi nanti"
                                    )
                                    .setFontColor(
                                        ContextCompat.getColor(
                                            applicationContext,
                                            R.color.custom_toast_font_failed
                                        )
                                    )
                                    .setBackgroundColor(
                                        ContextCompat.getColor(
                                            applicationContext,
                                            R.color.custom_toast_background_failed
                                        )
                                    ).show()
                                Generic.crashReport(Exception("Detail Outlet Response body is null"))
                            }
                        } else {
                            Log.e(
                                "ERROR (Outlet Detail)",
                                "Response not successful. ${response.code()}: ${response.message()}"
                            )
                            finish()
                            CustomToast.getInstance(applicationContext)
                                .setMessage(
                                    if (getString(R.string.language) == "en") "Something went wrong, please try again later"
                                    else "Terjadi kesalahan, silakan coba lagi nanti"
                                )
                                .setFontColor(
                                    ContextCompat.getColor(
                                        applicationContext,
                                        R.color.custom_toast_font_failed
                                    )
                                )
                                .setBackgroundColor(
                                    ContextCompat.getColor(
                                        applicationContext,
                                        R.color.custom_toast_background_failed
                                    )
                                ).show()
                            Generic.crashReport(Exception("Detail Outlet Response not successful: ${response.code()} - ${response.message()}"))
                        }
                    }

                    override fun onFailure(call: Call<OutletResponse>, throwable: Throwable) {
                        dialog.dismiss()
                        throwable.printStackTrace()
                        Log.e("ERROR (Outlet Detail)", throwable.toString())
                        Generic.crashReport(Exception("Detail Outlet Error: ${throwable.message}"))
                    }

                })
            } catch (jsonException: Exception) {
                dialog.dismiss()
                jsonException.printStackTrace()
                Log.e("ERROR (Outlet Detail)", jsonException.toString())
                finish()
                CustomToast.getInstance(applicationContext)
                    .setMessage(
                        if (getString(R.string.language) == "en") "Something went wrong, please try again later"
                        else "Terjadi kesalahan, silakan coba lagi nanti"
                    )
                    .setFontColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.custom_toast_font_failed
                        )
                    )
                    .setBackgroundColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.custom_toast_background_failed
                        )
                    ).show()
                Generic.crashReport(Exception("Detail Outlet Error: ${jsonException.message}"))
            }
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapPreview.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapPreview.onSaveInstanceState(outState)
    }

    private fun setMapPreview() {
        binding.apply {
            mapPreview.getMapAsync { map ->
                with(map) {
                    setStyle(BuildConfig.MAP_URL + BuildConfig.MAP_API_KEY)
                    map.addOnMapClickListener {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                String.format(
                                    Locale.getDefault(),
                                    "http://maps.google.com/maps?q=loc:$latitude,$longitude"
                                )
                                    .toUri()
                            )
                        )
                        true
                    }
                    uiSettings.apply {
                        isCompassEnabled = false
                        isLogoEnabled = false
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
}
