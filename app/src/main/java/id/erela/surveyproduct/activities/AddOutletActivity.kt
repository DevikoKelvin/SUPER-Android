package id.erela.surveyproduct.activities

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import id.erela.surveyproduct.BuildConfig
import id.erela.surveyproduct.R
import id.erela.surveyproduct.databinding.ActivityAddOutletBinding
import id.erela.surveyproduct.dialogs.LoadingDialog
import id.erela.surveyproduct.helpers.PermissionHelper
import id.erela.surveyproduct.helpers.api.AppAPI
import id.erela.surveyproduct.helpers.customs.CustomToast
import id.erela.surveyproduct.objects.OutletCategoryResponse
import id.erela.surveyproduct.objects.ProvinceListResponse
import id.erela.surveyproduct.objects.RegionListResponse
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class AddOutletActivity : AppCompatActivity() {
    private val binding: ActivityAddOutletBinding by lazy {
        ActivityAddOutletBinding.inflate(layoutInflater)
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var selectedType: Int = 0
    private var selectedProvince: Int = 0
    private var selectedCityRegency: Int = 0
    private val cityRegencyList: ArrayList<String> = ArrayList()
    private lateinit var cityRegencyDropdownAdapter: ArrayAdapter<String>
    private var selectedSubDistrict: Int = 0
    private val subDistrictList: ArrayList<String> = ArrayList()
    private lateinit var subDistrictDropdownAdapter: ArrayAdapter<String>
    private var selectedVillage: Long = 0
    private val villageList: ArrayList<String> = ArrayList()
    private lateinit var villageDropdownAdapter: ArrayAdapter<String>
    private var isFormEmpty = arrayOf(
        false,
        false,
        false,
        false,
        false,
        false,
        false
    )
    private lateinit var dialog: LoadingDialog

    companion object {
        fun start(context: Context) {
            context.startActivity(
                Intent(context, AddOutletActivity::class.java)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Mapbox.getInstance(this@AddOutletActivity)
        setContentView(binding.root)

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this@AddOutletActivity)

        init()
    }

    private fun init() {
        binding.apply {
            backButton.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            if (!isLocationEnabled()) {
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
                    )
                    .show()
                finish()
            } else {
                getLastKnownLocation()
            }

            refreshButton.setOnClickListener {
                getLastKnownLocation()
            }

            prepareFormInput()
            getOutletsCategory()

            saveButton.setOnClickListener { }
        }
    }

    private fun prepareFormInput() {
        binding.apply {
            outletNameField.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    charSequence: CharSequence?,
                    textStart: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    charSequence: CharSequence?,
                    textStart: Int,
                    before: Int,
                    count: Int
                ) {
                    if (charSequence!!.isEmpty()) {
                        outletNameFieldLayout.error = "Outlet name is required"
                        isFormEmpty[0] = false
                    } else {
                        outletNameFieldLayout.error = null
                        isFormEmpty[0] = true
                    }
                }

                override fun afterTextChanged(editable: Editable?) {
                }
            })
            addressField.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    charSequence: CharSequence?,
                    textStart: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    charSequence: CharSequence?,
                    textStart: Int,
                    before: Int,
                    count: Int
                ) {
                    if (charSequence!!.isEmpty()) {
                        addressFieldLayout.error = "Address is required"
                        isFormEmpty[6] = false
                    } else {
                        addressFieldLayout.error = null
                        isFormEmpty[6] = true
                    }
                }

                override fun afterTextChanged(editable: Editable?) {
                }
            })
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
                this@AddOutletActivity, PermissionHelper.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this@AddOutletActivity, PermissionHelper.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            PermissionHelper.requestPermission(
                this@AddOutletActivity,
                arrayOf(
                    PermissionHelper.ACCESS_COARSE_LOCATION,
                    PermissionHelper.ACCESS_FINE_LOCATION
                ),
                PermissionHelper.REQUEST_LOCATION_GPS
            )
        } else {
            val dialog = LoadingDialog(this@AddOutletActivity)
            if (dialog.window != null)
                dialog.show()
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    dialog.dismiss()
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude
                        setMapPreview()
                        getFullAddressFromLocation()
                    }
                }
        }
    }

    private fun getFullAddressFromLocation() {
        binding.apply {
            val geocoder = Geocoder(this@AddOutletActivity, Locale.forLanguageTag("id-ID"))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(
                    latitude, longitude, 1
                ) {
                    addressField.setText(it[0].getAddressLine(0) ?: "")
                }
            } else {
                val addresses: List<Address> = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    1
                )!!
                Log.e("Addresses", "Addresses: $addresses")
                if (addressField.text!!.isEmpty())
                    addressField.setText(addresses[0].getAddressLine(0) ?: "")
            }
        }
    }

    private fun getOutletsCategory() {
        binding.apply {
            dialog = LoadingDialog(this@AddOutletActivity)
            if (dialog.window != null)
                dialog.show()
            try {
                AppAPI.superEndpoint.showAllOutletCategories()
                    .enqueue(object : Callback<OutletCategoryResponse> {
                        override fun onResponse(
                            call: Call<OutletCategoryResponse>,
                            response: Response<OutletCategoryResponse>
                        ) {
                            if (response.isSuccessful) {
                                if (response.body() != null) {
                                    val result = response.body()
                                    when (result?.code) {
                                        1 -> {
                                            val data: ArrayList<String> = ArrayList()
                                            data.add("Select Outlet Category")
                                            result.data?.forEach {
                                                data.add(it?.type.toString())
                                            }
                                            val categoryDropdownAdapter = ArrayAdapter(
                                                this@AddOutletActivity,
                                                R.layout.generic_dropdown_item,
                                                R.id.dropdownItemText,
                                                data
                                            )
                                            typeDropdown.adapter = categoryDropdownAdapter
                                            if (selectedType != 0) {
                                                for (i in 0 until result.data!!.size) {
                                                    if (result.data[i]?.iD == selectedType)
                                                        typeDropdown.setSelection(
                                                            categoryDropdownAdapter.getPosition(
                                                                result.data[i]?.type
                                                            )
                                                        )
                                                }
                                            }
                                            typeDropdown.onItemSelectedListener =
                                                object : AdapterView.OnItemSelectedListener {
                                                    override fun onItemSelected(
                                                        adapterView: AdapterView<*>?,
                                                        view: View?,
                                                        position: Int,
                                                        id: Long
                                                    ) {
                                                        selectedType =
                                                            if (position == 0) 0 else result.data!![position - 1]?.iD!!.toInt()
                                                        isFormEmpty[1] = selectedType != 0
                                                        if (selectedType != 0)
                                                            typeDropdownLayout.strokeColor =
                                                                ContextCompat.getColor(
                                                                    this@AddOutletActivity,
                                                                    R.color.form_field_stroke
                                                                )
                                                    }

                                                    override fun onNothingSelected(adapterView: AdapterView<*>?) {}
                                                }
                                            getProvinceList()
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
                                        }
                                    }
                                } else {
                                    Log.e("ERROR (Category)", "Response body is null")
                                    Log.e("Response", response.toString())
                                    finish()
                                    CustomToast.getInstance(applicationContext)
                                        .setMessage("Something went wrong, please try again.")
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
                                }
                            } else {
                                Log.e("ERROR (Category)", "Response not successful")
                                Log.e("Response", response.toString())
                                finish()
                                CustomToast.getInstance(applicationContext)
                                    .setMessage("Something went wrong, please try again.")
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
                            }
                        }

                        override fun onFailure(
                            call: Call<OutletCategoryResponse>,
                            throwable: Throwable
                        ) {
                            dialog.dismiss()
                            throwable.printStackTrace()
                            Log.e("ERROR (Category)", throwable.toString())
                            finish()
                            CustomToast.getInstance(applicationContext)
                                .setMessage("Something went wrong, please try again.")
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
                        }
                    })
            } catch (jsonException: JSONException) {
                dialog.dismiss()
                jsonException.printStackTrace()
                Log.e("ERROR (Category)", jsonException.toString())
                finish()
                CustomToast.getInstance(applicationContext)
                    .setMessage("Something went wrong, please try again.")
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
            }
        }
    }

    private fun getProvinceList() {
        binding.apply {
            try {
                AppAPI.superEndpoint.showAllProvinces()
                    .enqueue(object : Callback<ProvinceListResponse> {
                        override fun onResponse(
                            call: Call<ProvinceListResponse>,
                            response: Response<ProvinceListResponse>
                        ) {
                            dialog.dismiss()
                            if (response.isSuccessful) {
                                if (response.body() != null) {
                                    val result = response.body()
                                    when (result?.code) {
                                        1 -> {
                                            val provinceList: ArrayList<String> = ArrayList()
                                            provinceList.add("Select Province")
                                            cityRegencyList.add("-")
                                            subDistrictList.add("-")
                                            villageList.add("-")
                                            result.data?.forEach { provinceItem ->
                                                provinceList.add(
                                                    provinceItem?.name.toString().lowercase()
                                                        .split(" ")
                                                        .joinToString(" ") { joinedString ->
                                                            joinedString.replaceFirstChar {
                                                                if (it.isLowerCase()) it.titlecase(
                                                                    Locale.ROOT
                                                                ) else it.toString()
                                                            }
                                                        }
                                                )
                                            }
                                            val provinceDropdownAdapter = ArrayAdapter(
                                                this@AddOutletActivity,
                                                R.layout.generic_dropdown_item,
                                                R.id.dropdownItemText,
                                                provinceList
                                            )
                                            provinceDropdown.adapter = provinceDropdownAdapter
                                            cityRegencyDropdownAdapter = ArrayAdapter(
                                                this@AddOutletActivity,
                                                R.layout.generic_dropdown_item,
                                                R.id.dropdownItemText,
                                                cityRegencyList
                                            )
                                            cityRegencyDropdown.adapter = cityRegencyDropdownAdapter
                                            subDistrictDropdownAdapter = ArrayAdapter(
                                                this@AddOutletActivity,
                                                R.layout.generic_dropdown_item,
                                                R.id.dropdownItemText,
                                                subDistrictList
                                            )
                                            subDistrictDropdown.adapter = subDistrictDropdownAdapter
                                            villageDropdownAdapter = ArrayAdapter(
                                                this@AddOutletActivity,
                                                R.layout.generic_dropdown_item,
                                                R.id.dropdownItemText,
                                                villageList
                                            )
                                            villageDropdown.adapter = villageDropdownAdapter
                                            if (selectedProvince != 0) {
                                                for (i in 0 until result.data!!.size) {
                                                    if (result.data[i]?.id == selectedProvince)
                                                        provinceDropdown.setSelection(
                                                            provinceDropdownAdapter.getPosition(
                                                                result.data[i]?.name
                                                            )
                                                        )
                                                }
                                            }
                                            provinceDropdown.onItemSelectedListener =
                                                object : AdapterView.OnItemSelectedListener {
                                                    override fun onItemSelected(
                                                        adapterView: AdapterView<*>?,
                                                        view: View?,
                                                        position: Int,
                                                        id: Long
                                                    ) {
                                                        selectedProvince =
                                                            if (position == 0) 0 else result.data!![position - 1]?.id!!.toInt()
                                                        isFormEmpty[2] = selectedProvince != 0
                                                        if (selectedProvince != 0)
                                                            provinceDropdownLayout.strokeColor =
                                                                ContextCompat.getColor(
                                                                    this@AddOutletActivity,
                                                                    R.color.form_field_stroke
                                                                )
                                                        getRegionList(selectedProvince, null, null)
                                                    }

                                                    override fun onNothingSelected(adapterView: AdapterView<*>?) {
                                                    }
                                                }
                                        }
                                    }
                                } else {
                                    Log.e("ERROR (Province)", "Response body is null")
                                    Log.e("Response", response.toString())
                                    finish()
                                    CustomToast.getInstance(applicationContext)
                                        .setMessage("Something went wrong, please try again.")
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
                                }
                            } else {
                                Log.e("ERROR (Province)", "Response not successful")
                                Log.e("Response", response.toString())
                                finish()
                                CustomToast.getInstance(applicationContext)
                                    .setMessage("Something went wrong, please try again.")
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
                            }
                        }

                        override fun onFailure(
                            call: Call<ProvinceListResponse>,
                            throwable: Throwable
                        ) {
                            dialog.dismiss()
                            throwable.printStackTrace()
                            Log.e("ERROR (Province)", throwable.toString())
                            finish()
                            CustomToast.getInstance(applicationContext)
                                .setMessage("Something went wrong, please try again.")
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
                        }
                    })
            } catch (jsonException: JSONException) {
                dialog.dismiss()
                jsonException.printStackTrace()
                Log.e("ERROR (Province)", jsonException.toString())
                finish()
                CustomToast.getInstance(applicationContext)
                    .setMessage("Something went wrong, please try again.")
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
            }
        }
    }

    private fun getRegionList(provincesId: Int, citiesId: Int?, districtsId: Int?) {
        binding.apply {
            if (dialog.window != null)
                dialog.show()
            try {
                AppAPI.superEndpoint.showRegionList(
                    provincesId, citiesId, districtsId
                ).enqueue(object : Callback<RegionListResponse> {
                    override fun onResponse(
                        call: Call<RegionListResponse>,
                        response: Response<RegionListResponse>
                    ) {
                        dialog.dismiss()
                        if (response.isSuccessful) {
                            if (response.body() != null) {
                                val result = response.body()
                                when (result?.code) {
                                    1 -> {
                                        cityRegencyList.clear()
                                        cityRegencyList.add("Select City/Regency")
                                        result.data?.cities?.forEach { citiesItem ->
                                            cityRegencyList.add(
                                                citiesItem?.name.toString().lowercase()
                                                    .split(" ")
                                                    .joinToString(" ") { joinedString ->
                                                        joinedString.replaceFirstChar {
                                                            if (it.isLowerCase()) it.titlecase(
                                                                Locale.ROOT
                                                            ) else it.toString()
                                                        }
                                                    }
                                            )
                                        }
                                        cityRegencyDropdownAdapter.notifyDataSetChanged()
                                        if (selectedCityRegency != 0) {
                                            for (i in 0 until result.data!!.cities!!.size) {
                                                if (result.data.cities?.get(i)?.id == selectedCityRegency)
                                                    cityRegencyDropdown.setSelection(
                                                        cityRegencyDropdownAdapter.getPosition(
                                                            result.data.cities[i]?.name
                                                        )
                                                    )
                                            }
                                        }
                                        cityRegencyDropdown.onItemSelectedListener =
                                            object : AdapterView.OnItemSelectedListener {
                                                override fun onItemSelected(
                                                    adapterView: AdapterView<*>?,
                                                    view: View?,
                                                    position: Int,
                                                    id: Long
                                                ) {
                                                    selectedCityRegency =
                                                        if (position == 0) 0 else result.data?.cities!![position - 1]?.id!!.toInt()
                                                    isFormEmpty[3] = selectedCityRegency != 0
                                                    if (selectedCityRegency != 0)
                                                        cityRegencyDropdownLayout.strokeColor =
                                                            ContextCompat.getColor(
                                                                this@AddOutletActivity,
                                                                R.color.form_field_stroke
                                                            )
                                                    getRegionList(
                                                        selectedProvince,
                                                        selectedCityRegency,
                                                        null
                                                    )
                                                }

                                                override fun onNothingSelected(adapterView: AdapterView<*>?) {
                                                }
                                            }
                                        if (result.data?.districts != null) {
                                            subDistrictList.clear()
                                            subDistrictList.add("Select Sub-District")
                                            result.data.districts.forEach { districtsItem ->
                                                subDistrictList.add(
                                                    districtsItem?.name.toString().lowercase()
                                                        .split(" ")
                                                        .joinToString(" ") { joinedString ->
                                                            joinedString.replaceFirstChar {
                                                                if (it.isLowerCase()) it.titlecase(
                                                                    Locale.ROOT
                                                                ) else it.toString()
                                                            }
                                                        }
                                                )
                                            }
                                            subDistrictDropdownAdapter.notifyDataSetChanged()
                                            if (selectedSubDistrict != 0) {
                                                for (i in 0 until result.data.districts.size) {
                                                    if (result.data.districts[i]?.id == selectedSubDistrict)
                                                        subDistrictDropdown.setSelection(
                                                            subDistrictDropdownAdapter.getPosition(
                                                                result.data.districts[i]?.name
                                                            )
                                                        )
                                                }
                                            }
                                            subDistrictDropdown.onItemSelectedListener =
                                                object : AdapterView.OnItemSelectedListener {
                                                    override fun onItemSelected(
                                                        adapterView: AdapterView<*>?,
                                                        view: View?,
                                                        position: Int,
                                                        id: Long
                                                    ) {
                                                        selectedSubDistrict =
                                                            if (position == 0) 0 else result.data.districts[position - 1]?.id!!.toInt()
                                                        isFormEmpty[4] = selectedSubDistrict != 0
                                                        if (selectedSubDistrict != 0)
                                                            subDistrictDropdownLayout.strokeColor =
                                                                ContextCompat.getColor(
                                                                    this@AddOutletActivity,
                                                                    R.color.form_field_stroke
                                                                )
                                                        getRegionList(
                                                            selectedProvince,
                                                            selectedCityRegency,
                                                            selectedSubDistrict
                                                        )
                                                    }

                                                    override fun onNothingSelected(adapterView: AdapterView<*>?) {
                                                    }
                                                }
                                        }
                                        if (result.data?.villages != null) {
                                            villageList.clear()
                                            villageList.add("Select Village")
                                            result.data.villages.forEach { villagesItem ->
                                                villageList.add(
                                                    villagesItem?.name.toString().lowercase()
                                                        .split(" ")
                                                        .joinToString(" ") { joinedString ->
                                                            joinedString.replaceFirstChar {
                                                                if (it.isLowerCase()) it.titlecase(
                                                                    Locale.ROOT
                                                                ) else it.toString()
                                                            }
                                                        }
                                                )
                                            }
                                            villageDropdownAdapter.notifyDataSetChanged()
                                            if (selectedVillage != 0L) {
                                                for (i in 0 until result.data.villages.size) {
                                                    if (result.data.villages[i]?.id == selectedVillage)
                                                        villageDropdown.setSelection(
                                                            villageDropdownAdapter.getPosition(
                                                                result.data.villages[i]?.name
                                                            )
                                                        )
                                                }
                                            }
                                            villageDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                                override fun onItemSelected(
                                                    adapterView: AdapterView<*>?,
                                                    view: View?,
                                                    position: Int,
                                                    id: Long
                                                ) {
                                                    selectedVillage =
                                                        if (position == 0) 0 else result.data.villages[position - 1]?.id!!
                                                    isFormEmpty[5] = selectedVillage != 0L
                                                    if (selectedVillage != 0L)
                                                        villageDropdownLayout.strokeColor =
                                                            ContextCompat.getColor(
                                                                this@AddOutletActivity,
                                                                R.color.form_field_stroke
                                                            )
                                                }

                                                override fun onNothingSelected(adapterView: AdapterView<*>?) {
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Log.e("ERROR (Region)", "Response body is null")
                                Log.e("Response", response.toString())
                                finish()
                                CustomToast.getInstance(applicationContext)
                                    .setMessage("Something went wrong, please try again.")
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
                            }
                        } else {
                            Log.e("ERROR (Region)", "Response not successful")
                            Log.e("Response", response.toString())
                            finish()
                            CustomToast.getInstance(applicationContext)
                                .setMessage("Something went wrong, please try again.")
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
                        }
                    }

                    override fun onFailure(
                        call: Call<RegionListResponse>,
                        throwable: Throwable
                    ) {
                        dialog.dismiss()
                        throwable.printStackTrace()
                        Log.e("ERROR (Region)", throwable.toString())
                        finish()
                        CustomToast.getInstance(applicationContext)
                            .setMessage("Something went wrong, please try again.")
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
                    }
                })
            } catch (jsonException: JSONException) {
                dialog.dismiss()
                jsonException.printStackTrace()
                Log.e("ERROR (Province)", jsonException.toString())
                finish()
                CustomToast.getInstance(applicationContext)
                    .setMessage("Something went wrong, please try again.")
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
            }
        }
    }
}