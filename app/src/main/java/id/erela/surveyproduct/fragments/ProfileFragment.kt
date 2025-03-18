package id.erela.surveyproduct.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import id.erela.surveyproduct.BuildConfig
import id.erela.surveyproduct.databinding.FragmentProfileBinding
import id.erela.surveyproduct.helpers.UserDataHelper
import id.erela.surveyproduct.objects.UsersSuper

class ProfileFragment : Fragment() {
    private var binding: FragmentProfileBinding? = null
    private val userUsersSuper: UsersSuper by lazy {
        UserDataHelper(requireContext()).getData()
    }
    private lateinit var onProfileButtonActionListener: OnProfileButtonActionListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prepareView()
    }

    override fun onResume() {
        super.onResume()

        prepareView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun prepareView() {
        binding?.apply {
            signOutButton.setOnClickListener {
                onProfileButtonActionListener.onSignOut()
            }
            Glide.with(requireContext())
                .load(BuildConfig.IMAGE_URL + userUsersSuper.photoProfile)
                .into(profilePicture)
            fullName.text = userUsersSuper.fullName
            userType.text = userUsersSuper.typeName
            userCode.text = userUsersSuper.userCode
            userName.text = userUsersSuper.userName
            userMail.text = userUsersSuper.userMail
            teamBranch.text =
                if (userUsersSuper.teamName == "" && userUsersSuper.branchName == "") "-"
                else if (userUsersSuper.teamName == "" && userUsersSuper.branchName != "") "Branch of ${userUsersSuper.branchName}"
                else if (userUsersSuper.teamName != "" && userUsersSuper.branchName == "") userUsersSuper.teamName
                else "${userUsersSuper.teamName} of Branch ${userUsersSuper.branchName}"
        }
    }

    fun setOnProfileButtonActionListener(onProfileButtonActionListener: OnProfileButtonActionListener) {
        this.onProfileButtonActionListener = onProfileButtonActionListener
    }

    interface OnProfileButtonActionListener {
        fun onSignOut()
    }
}