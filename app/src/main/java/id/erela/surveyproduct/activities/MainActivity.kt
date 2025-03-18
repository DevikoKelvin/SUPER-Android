package id.erela.surveyproduct.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import app.rive.runtime.kotlin.core.Rive
import id.erela.surveyproduct.R
import id.erela.surveyproduct.adapters.home_nav.HomeNavPagerAdapter
import id.erela.surveyproduct.databinding.ActivityMainBinding
import id.erela.surveyproduct.dialogs.ConfirmationDialog
import id.erela.surveyproduct.fragments.HistoryFragment
import id.erela.surveyproduct.fragments.HomeFragment
import id.erela.surveyproduct.fragments.OutletFragment
import id.erela.surveyproduct.fragments.ProfileFragment
import id.erela.surveyproduct.fragments.StartSurveyFragment
import id.erela.surveyproduct.helpers.UserDataHelper

class MainActivity : AppCompatActivity(), ProfileFragment.OnProfileButtonActionListener {
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
            Rive.init(applicationContext)
            onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    binding.apply {
                        if (bottomNavMenu.selectedItemId == R.id.home) {
                            val dialog = ConfirmationDialog(
                                this@MainActivity,
                                "Are you sure want to quit?",
                                "Yes"
                            ).also {
                                with(it) {
                                    setConfirmationDialogListener(object :
                                        ConfirmationDialog.ConfirmationDialogListener {
                                        override fun onConfirm() {
                                            finish()
                                        }
                                    })
                                }
                            }

                            if (dialog.window != null)
                                dialog.show()
                        } else {
                            bottomNavMenu.selectedItemId = R.id.home
                        }
                    }
                }
            })

            val fragmentList = listOf(
                HomeFragment(this@MainActivity),
                OutletFragment(this@MainActivity),
                StartSurveyFragment(this@MainActivity),
                HistoryFragment(this@MainActivity),
                ProfileFragment().also {
                    with(it) {
                        setOnProfileButtonActionListener(this@MainActivity)
                    }
                }
            )

            adapter = HomeNavPagerAdapter(fragmentList, supportFragmentManager, lifecycle)

            fragmentsContainer.adapter = adapter
            fragmentsContainer.setCurrentItem(0, true)
            bottomNavMenu.menu.findItem(R.id.home).isChecked = true

            fragmentsContainer.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                    when (position) {
                        0 -> {
                            titleBar.alpha = 1f
                            /*addNewOutletButton.alpha = positionOffset
                            if (positionOffset == 0f)
                                addNewOutletButton.visibility = View.GONE
                            else
                                addNewOutletButton.visibility = View.VISIBLE
                            addNewOutletButton.isEnabled = positionOffset >= 0.90f*/
                        }

                        1 -> {
                            titleBar.alpha = 1f
                            /*addNewOutletButton.alpha = 1 - positionOffset
                            addNewOutletButton.visibility = View.VISIBLE
                            addNewOutletButton.isEnabled = positionOffset <= 0.1f*/
                        }

                        2 -> {
                            titleBar.alpha = 1f
                            /*addNewOutletButton.alpha = 0f
                            addNewOutletButton.visibility = View.GONE*/
                        }

                        3 -> {
                            titleBar.alpha = 1 - positionOffset
                            /*addNewOutletButton.alpha = 0f
                            addNewOutletButton.visibility = View.GONE*/
                        }

                        4 -> {
                            titleBar.alpha = 0f
                            /*addNewOutletButton.alpha = 0f
                            addNewOutletButton.visibility = View.GONE*/
                        }
                    }
                }

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    when (position) {
                        0 -> bottomNavMenu.menu.findItem(R.id.home).isChecked = true
                        1 -> bottomNavMenu.menu.findItem(R.id.outlet).isChecked = true
                        2 -> bottomNavMenu.menu.findItem(R.id.start_survey).isChecked = true
                        3 -> bottomNavMenu.menu.findItem(R.id.history).isChecked = true
                        4 -> bottomNavMenu.menu.findItem(R.id.profile).isChecked = true
                    }
                }
            })

            bottomNavMenu.setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.home -> {
                        fragmentsContainer.setCurrentItem(0, false)
                    }

                    R.id.outlet -> {
                        fragmentsContainer.setCurrentItem(1, false)
                    }

                    R.id.start_survey -> {
                        fragmentsContainer.setCurrentItem(2, false)
                    }

                    R.id.history -> {
                        fragmentsContainer.setCurrentItem(3, false)
                    }

                    R.id.profile -> {
                        fragmentsContainer.setCurrentItem(4, false)
                    }
                }
                false
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onSignOut() {
        val dialog = ConfirmationDialog(
            this@MainActivity,
            "Are you sure you want to sign out?",
            "Yes"
        ).also {
            with(it) {
                setConfirmationDialogListener(object :
                    ConfirmationDialog.ConfirmationDialogListener {
                    override fun onConfirm() {
                        UserDataHelper(this@MainActivity).purgeUserData()
                        startActivity(
                            Intent(
                                this@MainActivity,
                                LoginActivity::class.java
                            )
                        )
                        finish()
                    }
                })
            }
        }

        if (dialog.window != null)
            dialog.show()
    }
}