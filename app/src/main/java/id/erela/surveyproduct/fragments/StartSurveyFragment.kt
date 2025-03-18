package id.erela.surveyproduct.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import app.rive.runtime.kotlin.core.Rive
import id.erela.surveyproduct.R
import id.erela.surveyproduct.activities.SurveyDetailActivity
import id.erela.surveyproduct.adapters.recycler_view.CheckInOutAdapter
import id.erela.surveyproduct.databinding.FragmentStartSurveyBinding
import id.erela.surveyproduct.helpers.UserDataHelper
import id.erela.surveyproduct.helpers.api.AppAPI
import id.erela.surveyproduct.helpers.customs.CustomToast
import id.erela.surveyproduct.objects.CheckInOutItem
import id.erela.surveyproduct.objects.CheckInOutListResponse
import id.erela.surveyproduct.objects.UsersSuper
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@SuppressLint("NotifyDataSetChanged")
class StartSurveyFragment(private val context: Context) : Fragment() {
    private var binding: FragmentStartSurveyBinding? = null
    private var isInitialized = false
    private lateinit var adapter: CheckInOutAdapter
    private val checkInOutHistory = ArrayList<CheckInOutItem?>()
    private val userData: UsersSuper by lazy {
        UserDataHelper(context).getData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStartSurveyBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prepareView()
    }

    override fun onResume() {
        super.onResume()

        prepareView()
    }

    @Deprecated("Deprecated in Java")
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        binding?.apply {
            if (isVisibleToUser) {
                prepareView()
                startSurveyButton.isEnabled = true
                if (!isInitialized)
                    callNetwork()
            } else
                startSurveyButton.isEnabled = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun prepareView() {
        binding?.apply {
            if (isInitialized) {
                if (checkInOutHistory.isEmpty()) {
                    emptyAnimation.visibility = View.VISIBLE
                    checkInOutListRv.visibility = View.GONE
                } else {
                    emptyAnimation.visibility = View.GONE
                    checkInOutListRv.visibility = View.VISIBLE
                }
                loadingManager(false)
            } else {
                callNetwork()
            }
            mainContainerRefresh.setOnRefreshListener {
                callNetwork()
                mainContainerRefresh.isRefreshing = false
            }

            adapter = CheckInOutAdapter(checkInOutHistory).also {
                with(it) {
                    setOnTodayTrackingItemClickListener(object :
                        CheckInOutAdapter.OnCheckInOutItemClickListener {
                        override fun onCheckInOutItemClick(item: CheckInOutItem?) {
                            SurveyDetailActivity.start(context, item!!)
                        }
                    })
                }
            }
            checkInOutListRv.adapter = adapter
            checkInOutListRv.layoutManager = LinearLayoutManager(context)
            checkInOutListRv.setHasFixedSize(true)

            startSurveyButton.setOnClickListener { }
        }
    }

    private fun callNetwork() {
        loadingManager(true)
        binding?.apply {
            try {
                AppAPI.superEndpoint.showTodayCheckInOut(userData.iD!!.toInt())
                    .enqueue(object : Callback<CheckInOutListResponse> {
                        override fun onResponse(
                            call: Call<CheckInOutListResponse>,
                            response: Response<CheckInOutListResponse>
                        ) {
                            loadingManager(false)
                            isInitialized = true
                            if (response.isSuccessful) {
                                if (response.body() != null) {
                                    val result = response.body()
                                    when (result?.code) {
                                        1 -> {
                                            checkInOutHistory.clear()
                                            for (item in result.data!!) {
                                                checkInOutHistory.add(item!!)
                                            }
                                            if (checkInOutHistory.isEmpty()) {
                                                emptyAnimation.visibility = View.VISIBLE
                                                checkInOutListRv.visibility = View.GONE
                                            } else {
                                                emptyAnimation.visibility = View.GONE
                                                checkInOutListRv.visibility = View.VISIBLE
                                            }
                                            adapter.notifyDataSetChanged()
                                        }

                                        0 -> {
                                            /*CustomToast.getInstance(context)
                                                .setMessage(result.message!!)
                                                .setFontColor(
                                                    ContextCompat.getColor(
                                                        context,
                                                        R.color.custom_toast_font_failed
                                                    )
                                                )
                                                .setBackgroundColor(
                                                    ContextCompat.getColor(
                                                        context,
                                                        R.color.custom_toast_background_failed
                                                    )
                                                ).show()*/
                                            emptyAnimation.visibility = View.VISIBLE
                                            checkInOutListRv.visibility = View.GONE
                                        }
                                    }
                                } else {
                                    Log.e("ERROR", "Response body is null")
                                    Log.e("Response", response.toString())
                                    /*CustomToast.getInstance(context)
                                        .setMessage("Something went wrong, please try again.")
                                        .setFontColor(
                                            ContextCompat.getColor(
                                                context,
                                                R.color.custom_toast_font_failed
                                            )
                                        )
                                        .setBackgroundColor(
                                            ContextCompat.getColor(
                                                context,
                                                R.color.custom_toast_background_failed
                                            )
                                        ).show()*/
                                    emptyAnimation.visibility = View.VISIBLE
                                    checkInOutListRv.visibility = View.GONE
                                }
                            } else {
                                Log.e("ERROR", "Response not successful")
                                Log.e("Response", response.toString())
                                /*CustomToast.getInstance(context)
                                    .setMessage("Something went wrong, please try again.")
                                    .setFontColor(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.custom_toast_font_failed
                                        )
                                    )
                                    .setBackgroundColor(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.custom_toast_background_failed
                                        )
                                    ).show()*/
                                emptyAnimation.visibility = View.VISIBLE
                                checkInOutListRv.visibility = View.GONE
                            }
                        }

                        override fun onFailure(
                            call: Call<CheckInOutListResponse>,
                            throwable: Throwable
                        ) {
                            isInitialized = false
                            loadingManager(false)
                            Log.e("ERROR", throwable.toString())
                            throwable.printStackTrace()
                            /*CustomToast.getInstance(context)
                                .setMessage("Something went wrong, please try again.")
                                .setFontColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.custom_toast_font_failed
                                    )
                                )
                                .setBackgroundColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.custom_toast_background_failed
                                    )
                                ).show()*/
                            emptyAnimation.visibility = View.VISIBLE
                            checkInOutListRv.visibility = View.GONE
                        }
                    })
            } catch (jsonException: JSONException) {
                isInitialized = false
                loadingManager(false)
                Log.e("ERROR", jsonException.toString())
                jsonException.printStackTrace()
                /*CustomToast.getInstance(context)
                    .setMessage("Something went wrong, please try again.")
                    .setFontColor(
                        ContextCompat.getColor(
                            context,
                            R.color.custom_toast_font_failed
                        )
                    )
                    .setBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.custom_toast_background_failed
                        )
                    ).show()*/
                emptyAnimation.visibility = View.VISIBLE
                checkInOutListRv.visibility = View.GONE
            }
        }
    }

    private fun loadingManager(isLoading: Boolean) {
        binding?.apply {
            if (isLoading) {
                emptyAnimation.visibility = View.GONE
                checkInOutListRv.visibility = View.GONE
                shimmerLayout.apply {
                    visibility = View.VISIBLE
                    startShimmer()
                }
            } else {
                checkInOutListRv.visibility = View.VISIBLE
                shimmerLayout.apply {
                    stopShimmer()
                    visibility = View.GONE
                }
            }
        }
    }
}