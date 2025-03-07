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
import id.erela.surveyproduct.objects.Data

class ProfileFragment : Fragment() {
    private var binding: FragmentProfileBinding? = null
    private val userData: Data by lazy {
        UserDataHelper(requireContext()).getData()
    }

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
        binding?.apply {
            Glide.with(requireContext())
                .load(BuildConfig.IMAGE_URL + userData.photoProfile)
                .into(profilePicture)
            fullName.text = userData.fullname
            userType.text = userData.typeName
            userCode.text = userData.usercode
            userName.text = userData.username
            userMail.text = userData.usermail
            teamBranch.text =
                if (userData.teamName == "" && userData.branchName == "") "-"
                else if (userData.teamName == "" && userData.branchName != "") "Branch of ${userData.branchName}"
                else if (userData.teamName != "" && userData.branchName == "") userData.teamName
                else "${userData.teamName} of Branch ${userData.branchName}"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}