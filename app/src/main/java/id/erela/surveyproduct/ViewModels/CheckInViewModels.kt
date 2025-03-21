package id.erela.surveyproduct.ViewModels

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CheckInViewModels : ViewModel() {
    private val _selectedOutlet = MutableStateFlow(0)
    val selectedOutlet: StateFlow<Int> = _selectedOutlet.asStateFlow()
    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri: StateFlow<Uri?> = _imageUri.asStateFlow()
    private val _location = MutableStateFlow(LatLng(0.0, 0.0))
    val location: StateFlow<LatLng> = _location.asStateFlow()

    fun updateSelectedOutlet(outlet: Int) {
        _selectedOutlet.value = outlet
    }

    fun updateImageUri(uri: Uri?) {
        _imageUri.value = uri
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        _location.value = LatLng(latitude, longitude)
    }
}