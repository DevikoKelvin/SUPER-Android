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
import id.erela.surveyproduct.R
import id.erela.surveyproduct.activities.DetailOutletActivity
import id.erela.surveyproduct.adapters.recycler_view.OutletAdapter
import id.erela.surveyproduct.databinding.FragmentOutletBinding
import id.erela.surveyproduct.helpers.api.AppAPI
import id.erela.surveyproduct.helpers.customs.CustomToast
import id.erela.surveyproduct.objects.OutletItem
import id.erela.surveyproduct.objects.OutletListResponse
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@SuppressLint("NotifyDataSetChanged")
class OutletFragment(private val context: Context) : Fragment() {
    private var binding: FragmentOutletBinding? = null
    private lateinit var adapter: OutletAdapter
    private var outletList = ArrayList<OutletItem>()
    private var isInitialized = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOutletBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            if (isInitialized)
                loadingManager(false)
            mainContainerRefresh.setOnRefreshListener {
                callNetwork()
                mainContainerRefresh.isRefreshing = false
            }

            outletListRv.layoutManager = LinearLayoutManager(context)
            adapter = OutletAdapter(outletList).also {
                with(it) {
                    setOnOutletItemClickListener(object : OutletAdapter.OnOutletItemClickListener {
                        override fun onOutletItemClick(outlet: OutletItem) {
                            DetailOutletActivity.start(context, outlet)
                        }
                    })
                }
            }
            outletListRv.adapter = adapter
            adapter.notifyDataSetChanged()
        }
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
            if (!isInitialized)
                callNetwork()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
        isInitialized = false
    }

    private fun callNetwork() {
        loadingManager(true)
        outletList.clear()
        binding?.apply {
            try {
                AppAPI.superEndpoint.showAllOutlets()
                    .enqueue(object : Callback<OutletListResponse> {
                        override fun onResponse(
                            call: Call<OutletListResponse>,
                            response: Response<OutletListResponse>
                        ) {
                            isInitialized = true
                            loadingManager(false)
                            if (response.isSuccessful) {
                                if (response.body() != null) {
                                    val result = response.body()
                                    when (result?.code) {
                                        1 -> {
                                            result.data?.forEach {
                                                outletList.add(
                                                    OutletItem(
                                                        it?.outletID,
                                                        it?.address,
                                                        it?.village,
                                                        it?.createdAt,
                                                        it?.latitude,
                                                        it?.longitude,
                                                        it?.updatedAt,
                                                        it?.province,
                                                        it?.name,
                                                        it?.type,
                                                        it?.subDistrict,
                                                        it?.cityRegency,
                                                        it?.iD
                                                    )
                                                )
                                            }
                                            adapter.notifyDataSetChanged()
                                        }

                                        0 -> {
                                            CustomToast.getInstance(context)
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
                                                ).show()
                                        }
                                    }
                                } else {
                                    Log.e("ERROR", "Response body is null")
                                    Log.e("Response", response.toString())
                                    CustomToast.getInstance(context)
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
                                        ).show()
                                }
                            } else {
                                Log.e("ERROR", "Response not successful")
                                Log.e("Response", response.toString())
                                CustomToast.getInstance(context)
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
                                    ).show()
                            }
                        }

                        override fun onFailure(
                            call: Call<OutletListResponse>,
                            throwable: Throwable
                        ) {
                            isInitialized = false
                            loadingManager(false)
                            Log.e("ERROR", throwable.toString())
                            throwable.printStackTrace()
                            CustomToast.getInstance(context)
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
                                ).show()
                        }
                    })
            } catch (jsonException: JSONException) {
                isInitialized = false
                loadingManager(false)
                Log.e("ERROR", jsonException.toString())
                jsonException.printStackTrace()
                CustomToast.getInstance(context)
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
                    ).show()
            }
        }
    }

    private fun loadingManager(isLoading: Boolean) {
        binding?.apply {
            if (isLoading) {
                shimmerLayout.apply {
                    visibility = View.VISIBLE
                    startShimmer()
                }
            } else {
                shimmerLayout.apply {
                    stopShimmer()
                    visibility = View.GONE
                }
            }
        }
    }
}