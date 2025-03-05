package id.erela.surveyproduct.adapters.view_pager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import id.erela.surveyproduct.fragments.HomeFragment
import id.erela.surveyproduct.fragments.OutletFragment
import id.erela.surveyproduct.fragments.ProfileFragment
import id.erela.surveyproduct.fragments.SurveyFragment
import id.erela.surveyproduct.fragments.UsersFragment

class HomeNavPagerAdapter(fragmentManager: FragmentManager): FragmentPagerAdapter(fragmentManager) {
    override fun getCount(): Int = 5

    override fun getItem(position: Int): Fragment = when (position) {
        0 -> OutletFragment()
        1 -> SurveyFragment()
        2 -> HomeFragment()
        3 -> UsersFragment()
        4 -> ProfileFragment()
        else -> throw IllegalArgumentException("Invalid position: $position")
    }
}