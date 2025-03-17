package id.erela.surveyproduct.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import id.erela.surveyproduct.BuildConfig
import id.erela.surveyproduct.databinding.ActivityDetailOutletBinding
import id.erela.surveyproduct.objects.OutletItem
import java.util.Locale

class DetailOutletActivity : AppCompatActivity() {
    private val binding: ActivityDetailOutletBinding by lazy {
        ActivityDetailOutletBinding.inflate(layoutInflater)
    }
    private lateinit var outlet: OutletItem
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    companion object {
        private const val DATA = "DATA"

        fun start(context: Context, outlet: OutletItem) {
            context.startActivity(
                Intent(
                    context,
                    DetailOutletActivity::class.java
                ).also {
                    with(it) {
                        putExtra(DATA, outlet)
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

        init()
    }

    private fun init() {
        binding.apply {
            backButton.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            outlet = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                intent.getSerializableExtra(DATA, OutletItem::class.java)!!
            else
                intent.getSerializableExtra(DATA) as OutletItem

            outletName.text = outlet.name
            outletID.text = outlet.outletID
            address.text = outlet.address
            village.text = outlet.village
            subDistrict.text = outlet.subDistrict
            cityRegency.text = outlet.cityRegency
            province.text = outlet.province
            latitude = outlet.latitude!!
            longitude = outlet.longitude!!

            setMapPreview()
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
                                String.format(Locale.getDefault(), "http://maps.google.com/maps?q=loc:$latitude,$longitude")
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
