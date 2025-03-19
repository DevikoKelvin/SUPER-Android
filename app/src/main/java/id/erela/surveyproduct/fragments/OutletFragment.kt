package id.erela.surveyproduct.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import id.erela.surveyproduct.R
import id.erela.surveyproduct.activities.AddOutletActivity
import id.erela.surveyproduct.activities.DetailOutletActivity
import id.erela.surveyproduct.adapters.recycler_view.OutletAdapter
import id.erela.surveyproduct.adapters.recycler_view.OutletDiffUtilCallback
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
    private val activityResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            callNetwork()
        }
    }

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
                addNewOutletButton.isEnabled = true
                if (!isInitialized)
                    callNetwork()
            } else
                addNewOutletButton.isEnabled = false
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

            addNewOutletButton.setOnClickListener {
                activityResultLauncher.launch(
                    Intent(context, AddOutletActivity::class.java)
                )
            }
        }
    }

    private fun callNetwork() {
        loadingManager(true)
        binding?.apply {
            searchInput.addTextChangedListener { editable ->
                val searchText = editable.toString().lowercase(Locale.forLanguageTag("id-ID"))
                val filteredList = if (searchText.isEmpty()) {
                    outletList
                } else {
                    outletList.filter { outletItem ->
                        outletItem.name?.lowercase(Locale.forLanguageTag("id-ID"))
                            ?.indexOf(searchText) != -1
                    }
                }
                val diffCallback = OutletDiffUtilCallback(outletList, filteredList)
                val diffResult = DiffUtil.calculateDiff(diffCallback)

                outletList.clear()
                outletList.addAll(filteredList)
                diffResult.dispatchUpdatesTo(adapter)

                if (editable.toString().isEmpty()) {
                    callNetwork()
                }
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
                                            if (outletList.isEmpty()) {
                                                emptyAnimation.visibility = View.VISIBLE
                                                outletListRv.visibility = View.GONE
                                            } else {
                                                emptyAnimation.visibility = View.GONE
                                                outletListRv.visibility = View.VISIBLE
                                            }
                                            outletListRv.layoutManager =
                                                LinearLayoutManager(context)
                                            adapter = OutletAdapter(outletList).also {
                                                with(it) {
                                                    setOnOutletItemClickListener(object :
                                                        OutletAdapter.OnOutletItemClickListener {
                                                        override fun onOutletItemClick(outlet: OutletItem) {
                                                            DetailOutletActivity.start(
                                                                context,
                                                                outlet
                                                            )
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
                                    emptyAnimation.visibility = View.VISIBLE
                                    outletListRv.visibility = View.GONE
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