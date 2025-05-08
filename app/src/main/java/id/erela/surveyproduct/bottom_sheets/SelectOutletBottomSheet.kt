package id.erela.surveyproduct.bottom_sheets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import id.erela.surveyproduct.R
import id.erela.surveyproduct.adapters.recycler_view.OutletAdapter
import id.erela.surveyproduct.databinding.BsSelectOutletBinding
import id.erela.surveyproduct.helpers.api.AppAPI
import id.erela.surveyproduct.helpers.customs.CustomToast
import id.erela.surveyproduct.objects.OutletItem
import id.erela.surveyproduct.objects.OutletListResponse
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class SelectOutletBottomSheet(context: Context) : BottomSheetDialog(context) {
    private val binding: BsSelectOutletBinding by lazy {
        BsSelectOutletBinding.inflate(layoutInflater)
    }
    private var outletList = ArrayList<OutletItem>()
    private lateinit var adapter: OutletAdapter
    private lateinit var onOutletSelectedListener: OnOutletSelectedListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        setCancelable(true)

        init()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun init() {
        binding.apply {
            loadingManager(true)
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
                            }

                            override fun onOutletForSurveyItemClick(
                                outlet: OutletItem
                            ) {
                                onOutletSelectedListener.onOutletSelected(
                                    outlet
                                )
                                dismiss()
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
                                            if (outletList.isEmpty())
                                                outletListRv.visibility = View.GONE
                                            else
                                                outletListRv.visibility = View.VISIBLE

                                            outletListRv.layoutManager =
                                                LinearLayoutManager(context)
                                            adapter = OutletAdapter(outletList, "survey").also {
                                                with(it) {
                                                    setOnOutletItemClickListener(object :
                                                        OutletAdapter.OnOutletItemClickListener {
                                                        override fun onOutletForDetailItemClick(
                                                            id: Int
                                                        ) {
                                                        }

                                                        override fun onOutletForSurveyItemClick(
                                                            outlet: OutletItem
                                                        ) {
                                                            onOutletSelectedListener.onOutletSelected(
                                                                outlet
                                                            )
                                                            dismiss()
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
                                outletListRv.visibility = View.GONE
                            }
                        }

                        override fun onFailure(
                            call: Call<OutletListResponse>,
                            throwable: Throwable
                        ) {
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
                            outletListRv.visibility = View.GONE
                        }
                    })
            } catch (jsonException: JSONException) {
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
                outletListRv.visibility = View.GONE
            }
        }
    }

    private fun loadingManager(isLoading: Boolean) {
        binding.apply {
            if (isLoading) {
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

    fun setOnOutletSelectedListener(onOutletSelectedListener: OnOutletSelectedListener) {
        this.onOutletSelectedListener = onOutletSelectedListener
    }

    interface OnOutletSelectedListener {
        fun onOutletSelected(outlet: OutletItem)
    }
}