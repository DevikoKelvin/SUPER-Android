package id.erela.surveyproduct.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import com.bumptech.glide.Glide
import id.erela.surveyproduct.BuildConfig
import id.erela.surveyproduct.R
import id.erela.surveyproduct.databinding.FragmentHomeBinding
import id.erela.surveyproduct.helpers.UserDataHelper
import id.erela.surveyproduct.objects.UsersSuper
import java.time.LocalDateTime

class HomeFragment(private val context: Context) : Fragment() {
    private var binding: FragmentHomeBinding? = null
    private val userData: UsersSuper by lazy {
        UserDataHelper(context).getData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding?.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            Glide.with(context)
                .load(BuildConfig.IMAGE_URL + userData.photoProfile)
                .placeholder(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.blank_profile_icon
                    )
                )
                .into(photoProfile)
            val currentDateTime = LocalDateTime.now()
            greetings.text = "Good ${
                when (currentDateTime.hour) {
                    in 0..11 -> "morning,"
                    in 12..18 -> "afternoon,"
                    else -> "evening,"
                }
            }"
            fullName.text = "${userData.fullname}!"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}