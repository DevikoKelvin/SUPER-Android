package id.erela.surveyproduct.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import id.erela.surveyproduct.R
import id.erela.surveyproduct.adapters.recycler_view.UserListAdapter
import id.erela.surveyproduct.databinding.FragmentUsersBinding
import id.erela.surveyproduct.dialogs.LoadingDialog
import id.erela.surveyproduct.helpers.api.InitSuperAPI
import id.erela.surveyproduct.helpers.customs.CustomToast
import id.erela.surveyproduct.objects.UserListResponse
import id.erela.surveyproduct.objects.UsersSuper
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@SuppressLint("NotifyDataSetChanged")
class UsersFragment(private val context: Context) : Fragment() {
    private var binding: FragmentUsersBinding? = null
    private lateinit var adapter: UserListAdapter
    private val users: ArrayList<UsersSuper> = arrayListOf()
    private var isInitialized = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUsersBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            mainContainerRefresh.setOnRefreshListener {
                callNetwork()
                mainContainerRefresh.isRefreshing = false
            }

            userListRv.layoutManager = LinearLayoutManager(context)
            adapter = UserListAdapter(context, users)
            userListRv.adapter = adapter
            adapter.notifyDataSetChanged()

            /*callNetwork()*/
        }
    }

    @Deprecated("Deprecated in Java")
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            if (!isInitialized)
                callNetwork()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isInitialized = false
    }

    private fun callNetwork() {
        val loadingDialog = LoadingDialog(context)
        if (loadingDialog.window != null)
            loadingDialog.show()
        users.clear()
        binding?.apply {
            try {
                InitSuperAPI.superEndpoint.showAllUsers()
                    .enqueue(object : Callback<UserListResponse> {
                        override fun onResponse(
                            call: Call<UserListResponse>,
                            response: Response<UserListResponse>
                        ) {
                            isInitialized = true
                            loadingDialog.dismiss()
                            if (response.isSuccessful) {
                                if (response.body() != null) {
                                    val result = response.body()
                                    Log.e("Response", result?.data.toString())
                                    for (i in 0 until result?.data!!.size) {
                                        users.add(
                                            UsersSuper(
                                                result.data[i]?.typeName,
                                                result.data[i]?.photoProfile,
                                                result.data[i]?.typeId,
                                                result.data[i]?.createdAt,
                                                result.data[i]?.usermail,
                                                result.data[i]?.typeId,
                                                result.data[i]?.teamName,
                                                result.data[i]?.updatedAt,
                                                result.data[i]?.branchId,
                                                result.data[i]?.branchName,
                                                result.data[i]?.id,
                                                result.data[i]?.fullname,
                                                result.data[i]?.usercode,
                                                result.data[i]?.username
                                            )
                                        )
                                    }
                                    adapter.notifyDataSetChanged()
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

                        override fun onFailure(call: Call<UserListResponse>, throwable: Throwable) {
                            isInitialized = true
                            loadingDialog.dismiss()
                            Log.e("ERROR", "Super API Error Get User Detail")
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
                isInitialized = true
                loadingDialog.dismiss()
                jsonException.printStackTrace()
                Log.e("ERROR", jsonException.toString())
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
}