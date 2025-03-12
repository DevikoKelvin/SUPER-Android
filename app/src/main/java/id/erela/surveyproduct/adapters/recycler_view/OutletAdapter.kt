package id.erela.surveyproduct.adapters.recycler_view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.erela.surveyproduct.BuildConfig
import id.erela.surveyproduct.R
import id.erela.surveyproduct.databinding.ListItemOutletBinding
import id.erela.surveyproduct.objects.OutletItem
import id.erela.surveyproduct.objects.UsersSuper

class OutletAdapter(private val context: Context, private val outlets: ArrayList<OutletItem>) :
    RecyclerView.Adapter<OutletAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ListItemOutletBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ListItemOutletBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ).root
    )

    override fun getItemCount(): Int = outlets.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = outlets[position]

        with(holder) {
            binding.apply {
                if (item.photoProfile != null)
                    Glide.with(context)
                        .load(BuildConfig.IMAGE_URL + item.photoProfile)
                        .placeholder(
                            AppCompatResources.getDrawable(
                                context,
                                R.drawable.blank_profile_icon
                            )
                        )
                        .into(photoProfile)
                else
                    photoProfile.setImageDrawable(
                        AppCompatResources.getDrawable(
                            context,
                            R.drawable.blank_profile_icon
                        )
                    )
                fullName.text = item.fullname
                userCode.text = item.usercode
                userType.text = item.typeName
                if (item.teamName == null || item.branchName == null)
                    userTeamBranch.visibility = View.GONE
                else {
                    userTeamBranch.visibility = View.VISIBLE
                    userTeamBranch.text = "${item.teamName} of Branch ${item.branchName}"
                }
            }
        }
    }
}