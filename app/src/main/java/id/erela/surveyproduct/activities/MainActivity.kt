package id.erela.surveyproduct.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import id.erela.surveyproduct.R
import id.erela.surveyproduct.adapters.view_pager.HomeNavPagerAdapter
import id.erela.surveyproduct.databinding.ActivityMainBinding
import id.erela.surveyproduct.helpers.UserDataHelper

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var adapter: HomeNavPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        init()
    }

    private fun init() {
        binding.apply {
            adapter = HomeNavPagerAdapter(supportFragmentManager, this@MainActivity)

            fragmentsContainer.adapter = adapter

            fragmentsContainer.currentItem = 2
            bottomNavMenu.selectedItemId = R.id.home

            fragmentsContainer.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    when (position) {
                        3 -> {
                            titleBar.alpha = 1 - positionOffset
                            Log.e("MainActivity", "Position Offset: $positionOffset")
                            Log.e("MainActivity", "Position Offset Px: $positionOffsetPixels")
                        }

                        4 -> {
                            titleBar.alpha = 0f
                        }

                        else -> {
                            titleBar.alpha = 1f
                        }
                    }
                }

                override fun onPageSelected(position: Int) {
                    when (position) {
                        0 -> bottomNavMenu.selectedItemId = R.id.outlet
                        1 -> bottomNavMenu.selectedItemId = R.id.survey
                        2 -> bottomNavMenu.selectedItemId = R.id.home
                        3 -> bottomNavMenu.selectedItemId = R.id.users
                        4 -> bottomNavMenu.selectedItemId = R.id.your_profile
                    }
                }

                override fun onPageScrollStateChanged(state: Int) {}
            })

            bottomNavMenu.setOnNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.outlet -> {
                        fragmentsContainer.currentItem = 0
                        true
                    }

                    R.id.survey -> {
                        fragmentsContainer.currentItem = 1
                        true
                    }

                    R.id.home -> {
                        fragmentsContainer.currentItem = 2
                        true
                    }

                    R.id.users -> {
                        fragmentsContainer.currentItem = 3
                        true
                    }

                    R.id.your_profile -> {
                        fragmentsContainer.currentItem = 4
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    @Deprecated(
        "This method has been deprecated in favor of using the\n" +
                "{@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n" +
                "The OnBackPressedDispatcher controls how back button events are dispatched\n" +
                "to one or more {@link OnBackPressedCallback} objects."
    )
    override fun onBackPressed() {
        binding.apply {
            if (bottomNavMenu.selectedItemId == R.id.home) {
                finish()
                UserDataHelper(this@MainActivity).purgeUserData()
            } else {
                bottomNavMenu.selectedItemId = R.id.home
            }
        }
    }
}