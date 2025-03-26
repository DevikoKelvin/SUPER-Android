package id.erela.surveyproduct.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.erela.surveyproduct.objects.SurveyAnswer
import id.erela.surveyproduct.repository.SurveyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class SurveyViewModel(private val repository: SurveyRepository) : ViewModel() {
    private val _surveyState = MutableStateFlow<SurveyState>(SurveyState.Idle)
    val surveyState: StateFlow<SurveyState> = _surveyState.asStateFlow()

    fun submitSurvey(
        answers: List<SurveyAnswer>,
        checkInPhoto: String,
        checkOutPhoto: String
    ) {
        viewModelScope.launch {
            _surveyState.value = SurveyState.Loading
            try {
                // Example of creating a photo part
                val checkInPhotoFile = File(checkInPhoto)
                val checkOutPhotoFile = File(checkOutPhoto)
                val photoIn = createMultipartBody(checkInPhotoFile, "PhotoIn")
                val photoOut = createMultipartBody(checkOutPhotoFile, "PhotoOut")
                repository.uploadData(answers, photoIn, photoOut)
                _surveyState.value = SurveyState.Success
            } catch (e: Exception) {
                // Handle exceptions
                e.printStackTrace()
                _surveyState.value = SurveyState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    private fun createMultipartBody(photoFile: File, request: String): MultipartBody.Part {
        val requestBody = photoFile.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(request, photoFile.name, requestBody)
    }
}

sealed class SurveyState {
    data object Idle : SurveyState()
    data object Loading : SurveyState()
    data object Success : SurveyState()
    data class Error(val message: String) : SurveyState()
}