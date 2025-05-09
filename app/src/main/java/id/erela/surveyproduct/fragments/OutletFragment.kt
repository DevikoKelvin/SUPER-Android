package id.erela.surveyproduct.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
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
import java.util.Locale

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
        binding?.apply {
            if (isVisibleToUser) {
                prepareView()
                if (!isInitialized)
                    callNetwork()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
        isInitialized = false
    }

    private fun prepareView() {
        binding?.apply {
            if (isInitialized) {
                if (outletList.isEmpty()) {
                    emptyAnimation.visibility = View.VISIBLE
                    outletListRv.visibility = View.GONE
                } else {
                    outletList.clear()
                    callNetwork()
                    emptyAnimation.visibility = View.GONE
                    outletListRv.visibility = View.VISIBLE
                }
                loadingManager(false)
            } else {
                callNetwork()
            }
            mainContainerRefresh.setOnRefreshListener {
                callNetwork()
                mainContainerRefresh.isRefreshing = false
            }
        }
    }

    fun callNetwork() {
        loadingManager(true)
        binding?.apply {
            searchInput.addTextChangedListener { editable ->
                val searchText = editable.toString().lowercase(Locale.forLanguageTag("id-ID"))
                val filteredList = ArrayList<OutletItem>()
                for (i in 0 until outletList.size) {
                    if (outletList[i].name?.lowercase(Locale.forLanguageTag("id-ID"))
                            ?.indexOf(searchText) != -1 || outletList[i].address?.lowercase(
                            Locale.forLanguageTag(
                                "id-ID"
                            )
                        )?.indexOf(searchText) != -1
                        || outletList[i].cityRegencyName?.lowercase(Locale.forLanguageTag("id-ID"))
                            ?.indexOf(searchText) != -1
                        || outletList[i].outletID?.lowercase(Locale.forLanguageTag("id-ID"))
                            ?.indexOf(searchText) != -1
                    ) {
                        filteredList.add(outletList[i])
                    }
                }

                adapter = OutletAdapter(filteredList, "survey").also {
                    with(it) {
                        setOnOutletItemClickListener(object :
                            OutletAdapter.OnOutletItemClickListener {
                            override fun onOutletForDetailItemClick(
                                id: Int
                            ) {
                                DetailOutletActivity.start(
                                    context,
                                    id
                                )
                            }

                            override fun onOutletForSurveyItemClick(
                                outlet: OutletItem
                            ) {
                            }
                        })
                    }
                }
                outletListRv.adapter = adapter
            }

            try {
                AppAPI.superEndpoint.showAllOutlets()
                    .enqueue(object : Callback<OutletListResponse> {
                        override fun onResponse(
                            call: Call<OutletListResponse>,
                            response: Response<OutletListResponse>
                        ) {
                            loadingManager(false)
                            isInitialized = true
                            if (response.isSuccessful) {
                                if (response.body() != null) {
                                    val result = response.body()
                                    when (result?.code) {
                                        1 -> {
                                            outletList.clear()
                                            result.data?.forEach {
                                                if (it?.status == 1) {
                                                    it.apply {
                                                        outletList.add(
                                                            OutletItem(
                                                                status,
                                                                outletID,
                                                                typeName,
                                                                address,
                                                                village,
                                                                creatorID,
                                                                createdAt,
                                                                latitude,
                                                                creator,
                                                                subDistrictName,
                                                                longitude,
                                                                updatedAt,
                                                                province,
                                                                name,
                                                                provinceName,
                                                                type,
                                                                subDistrict,
                                                                villageName,
                                                                cityRegencyName,
                                                                cityRegency,
                                                                iD
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                            if (outletList.isEmpty()) {
                                                emptyAnimation.visibility = View.VISIBLE
                                                outletListRv.visibility = View.GONE
                                            } else {
                                                emptyAnimation.visibility = View.GONE
                                                outletListRv.visibility = View.VISIBLE
                                            }
                                            outletListRv.layoutManager =
                                                LinearLayoutManager(context)
                                            adapter = OutletAdapter(outletList, "detail").also {
                                                with(it) {
                                                    setOnOutletItemClickListener(object :
                                                        OutletAdapter.OnOutletItemClickListener {
                                                        override fun onOutletForDetailItemClick(
                                                            id: Int
                                                        ) {
                                                            DetailOutletActivity.start(
                                                                context,
                                                                id
                                                            )
                                                        }

                                                        override fun onOutletForSurveyItemClick(
                                                            outlet: OutletItem
                                                        ) {
                                                        }
                                                    })
                                                }
                                            }
                                            outletListRv.adapter = adapter
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
                                            emptyAnimation.visibility = View.VISIBLE
                                            outletListRv.visibility = View.GONE
                                        }
                                    }
                                } else {
                                    Log.e("ERROR", "Response body is null")
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
                                    emptyAnimation.visibility = View.VISIBLE
                                    outletListRv.visibility = View.GONE
                                }
                            } else {
                                Log.e("ERROR", "Response not successful")
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
                                emptyAnimation.visibility = View.VISIBLE
                                outletListRv.visibility = View.GONE
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
                            emptyAnimation.visibility = View.VISIBLE
                            outletListRv.visibility = View.GONE
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
                emptyAnimation.visibility = View.VISIBLE
                outletListRv.visibility = View.GONE
            }
        }
    }

    private fun loadingManager(isLoading: Boolean) {
        binding?.apply {
            if (isLoading) {
                emptyAnimation.visibility = View.GONE
                outletListRv.visibility = View.GONE
                shimmerLayout.apply {
                    visibility = View.VISIBLE
                    startShimmer()
                }
            } else {
                outletListRv.visibility = View.VISIBLE
                shimmerLayout.apply {
                    stopShimmer()
                    visibility = View.GONE
                }
            }
        }
    }
}