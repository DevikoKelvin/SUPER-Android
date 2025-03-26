package id.erela.surveyproduct.viewmodels.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.erela.surveyproduct.repository.SurveyRepository
import id.erela.surveyproduct.viewmodels.SurveyViewModel

class SurveyViewModelFactory(private val surveyRepository: SurveyRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SurveyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SurveyViewModel(surveyRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}