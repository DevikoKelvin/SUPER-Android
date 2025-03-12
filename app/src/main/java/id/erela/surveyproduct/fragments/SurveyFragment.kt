package id.erela.surveyproduct.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import id.erela.surveyproduct.databinding.FragmentSurveyBinding
import id.erela.surveyproduct.helpers.api.AppAPI
import id.erela.surveyproduct.objects.SurveyListResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SurveyFragment : Fragment() {
    private var binding: FragmentSurveyBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSurveyBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
        }
    }

    @Deprecated("Deprecated in Java")
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser)
            callNetwork()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun callNetwork() {
        AppAPI.superEndpoint.showAllSurveys()
            .enqueue(object : Callback<SurveyListResponse> {
                override fun onResponse(
                    call: Call<SurveyListResponse>,
                    response: Response<SurveyListResponse>
                ) {
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            val result = response.body()
                            Log.e("Response", result?.data.toString())
                        }
                    }
                }

                override fun onFailure(call: Call<SurveyListResponse>, throwable: Throwable) {
                    throwable.printStackTrace()
                }
            })
    }
}