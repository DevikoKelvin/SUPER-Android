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
import id.erela.surveyproduct.adapters.recycler_view.OutletAdapter
import id.erela.surveyproduct.databinding.FragmentHistoryBinding
import id.erela.surveyproduct.dialogs.LoadingDialog
import id.erela.surveyproduct.helpers.customs.CustomToast
import id.erela.surveyproduct.objects.UsersSuper
import org.json.JSONException

@SuppressLint("NotifyDataSetChanged")
class HistoryFragment(private val context: Context) : Fragment() {
    private var binding: FragmentHistoryBinding? = null
    private lateinit var adapter: OutletAdapter
    private var isInitialized = false

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

        binding?.apply {
            mainContainerRefresh.setOnRefreshListener {
                /*callNetwork()*/
                mainContainerRefresh.isRefreshing = false
            }
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
        /*if (isVisibleToUser) {
            if (!isInitialized)
                callNetwork()
        }*/
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