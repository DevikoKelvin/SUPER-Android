package id.erela.surveyproduct.adapters.home_nav

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import id.erela.surveyproduct.fragments.HomeFragment
import id.erela.surveyproduct.fragments.OutletFragment
import id.erela.surveyproduct.fragments.ProfileFragment
import id.erela.surveyproduct.fragments.SurveyFragment
import id.erela.surveyproduct.fragments.HistoryFragment

class HomeNavPagerAdapter(fragmentManager: FragmentManager, private val context: Context) :
    FragmentPagerAdapter(fragmentManager) {
    private lateinit var onFragmentActionListener: OnFragmentActionListener

    override fun getCount(): Int = 5

    override fun getItem(position: Int): Fragment = when (position) {
        0 -> HomeFragment(context)
        1 -> OutletFragment()
        2 -> SurveyFragment()
        3 -> HistoryFragment(context)
        4 -> ProfileFragment().also {
            with(it) {
                setOnProfileButtonActionListener(object :
                    ProfileFragment.OnProfileButtonActionListener {
                    override fun onSignOut() {
                        onFragmentActionListener.onProfileSignOut()
                    }
                })
            }
        }

        else -> throw IllegalArgumentException("Invalid position: $position")
    }

    fun onFragmentActionListener(onFragmentActionListener: OnFragmentActionListener) {
        this.onFragmentActionListener = onFragmentActionListener
    }

    interface OnFragmentActionListener {
        fun onProfileSignOut()
    }
}