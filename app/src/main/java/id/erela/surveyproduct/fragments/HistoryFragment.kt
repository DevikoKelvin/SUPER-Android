package id.erela.surveyproduct.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import id.erela.surveyproduct.activities.SurveyDetailActivity
import id.erela.surveyproduct.adapters.recycler_view.CheckInOutAdapter
import id.erela.surveyproduct.bottom_sheets.FilterHistoryBottomSheet
import id.erela.surveyproduct.databinding.FragmentHistoryBinding
import id.erela.surveyproduct.helpers.UserDataHelper
import id.erela.surveyproduct.helpers.api.AppAPI
import id.erela.surveyproduct.objects.CheckInOutItem
import id.erela.surveyproduct.objects.CheckInOutListResponse
import id.erela.surveyproduct.objects.UsersSuper
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@SuppressLint("NotifyDataSetChanged")
class HistoryFragment(private val context: Context) : Fragment() {
    private var binding: FragmentHistoryBinding? = null
    private var isInitialized = false
    private lateinit var adapter: CheckInOutAdapter
    private val checkInOutHistory = ArrayList<CheckInOutItem?>()
    private val userData: UsersSuper by lazy {
        UserDataHelper(context).getData()
    }
    private var start = ""
    private var end = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prepareView()
    }

    override fun onResume() {
        super.onResume()

        prepareView()
    }

    @Deprecated(
        "Deprecated in Java", ReplaceWith(
            "super.setUserVisibleHint(isVisibleToUser)",
            "androidx.fragment.app.Fragment"
        )
    )
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            prepareView()
            if (!isInitialized)
                callNetwork(start, end)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isInitialized = false
    }

    private fun prepareView() {
        binding?.apply {
            val startCalendar = Calendar.getInstance()
            startCalendar.set(Calendar.DAY_OF_MONTH, 1)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.forLanguageTag("id-ID"))
            start = dateFormat.format(startCalendar.time)
            val endCalendar = Calendar.getInstance()
            end = dateFormat.format(endCalendar.time)

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
                callNetwork(start, end)
            }
            mainContainerRefresh.setOnRefreshListener {
                callNetwork(start, end)
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

            filterButton.setOnClickListener {
                val bottomSheet =
                    FilterHistoryBottomSheet(context, start, end, parentFragmentManager).also {
                        with(it) {
                            setOnFilterOkListener(object :
                                FilterHistoryBottomSheet.OnFilterOkListener {
                                override fun onFilterOk(start: String, end: String) {
                                    this@HistoryFragment.start = start
                                    this@HistoryFragment.end = end
                                    callNetwork(start, end)
                                }
                            })
                        }
                    }

                if (bottomSheet.window != null)
                    bottomSheet.show()
            }
        }
    }

    private fun callNetwork(start: String, end: String) {
        loadingManager(true)
        binding?.apply {
            try {
                AppAPI.superEndpoint.showAllCheckInOut(userData.iD!!.toInt(), start, end)
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
                                            emptyAnimation.visibility = View.VISIBLE
                                            checkInOutListRv.visibility = View.GONE
                                        }
                                    }
                                } else {
                                    Log.e("ERROR", "Response body is null")
                                    Log.e("Response", response.toString())
                                    emptyAnimation.visibility = View.VISIBLE
                                    checkInOutListRv.visibility = View.GONE
                                }
                            } else {
                                Log.e("ERROR", "Response not successful")
                                Log.e("Response", response.toString())
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
                            emptyAnimation.visibility = View.VISIBLE
                            checkInOutListRv.visibility = View.GONE
                        }
                    })
            } catch (jsonException: JSONException) {
                isInitialized = false
                loadingManager(false)
                Log.e("ERROR", jsonException.toString())
                jsonException.printStackTrace()
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