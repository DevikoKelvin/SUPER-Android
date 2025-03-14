package id.erela.surveyproduct.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import id.erela.surveyproduct.R
import id.erela.surveyproduct.adapters.home_nav.HomeNavPagerAdapter
import id.erela.surveyproduct.databinding.ActivityMainBinding
import id.erela.surveyproduct.databinding.CustomToastBinding
import id.erela.surveyproduct.dialogs.ConfirmationDialog
import id.erela.surveyproduct.helpers.UserDataHelper
import id.erela.surveyproduct.helpers.customs.CustomToast

class MainActivity : AppCompatActivity(), HomeNavPagerAdapter.OnFragmentActionListener {
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

            adapter = HomeNavPagerAdapter(supportFragmentManager, this@MainActivity).also {
                with(it) {
                    onFragmentActionListener(this@MainActivity)
                }
            }

            fragmentsContainer.adapter = adapter

            fragmentsContainer.currentItem = 0
            bottomNavMenu.selectedItemId = R.id.home

            fragmentsContainer.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    when (position) {
                        0 -> {
                            titleBar.alpha = 1f
                            addNewOutletButton.alpha = positionOffset
                            if (positionOffset == 0f)
                                addNewOutletButton.visibility = View.GONE
                            else
                                addNewOutletButton.visibility = View.VISIBLE
                            addNewOutletButton.isEnabled = positionOffset >= 0.90f
                        }

                        1 -> {
                            titleBar.alpha = 1f
                            addNewOutletButton.alpha = 1 - positionOffset
                            addNewOutletButton.visibility = View.VISIBLE
                            addNewOutletButton.isEnabled = positionOffset <= 0.1f
                        }

                        2 -> {
                            titleBar.alpha = 1f
                            addNewOutletButton.alpha = 0f
                            addNewOutletButton.visibility = View.GONE
                        }

                        3 -> {
                            titleBar.alpha = 1 - positionOffset
                            addNewOutletButton.alpha = 0f
                            addNewOutletButton.visibility = View.GONE
                        }

                        4 -> {
                            titleBar.alpha = 0f
                            addNewOutletButton.alpha = 0f
                            addNewOutletButton.visibility = View.GONE
                        }
                    }
                }

                override fun onPageSelected(position: Int) {
                    when (position) {
                        0 -> bottomNavMenu.selectedItemId = R.id.home
                        1 -> bottomNavMenu.selectedItemId = R.id.customers
                        2 -> bottomNavMenu.selectedItemId = R.id.survey
                        3 -> bottomNavMenu.selectedItemId = R.id.history
                        4 -> bottomNavMenu.selectedItemId = R.id.your_profile
                    }
                }

                override fun onPageScrollStateChanged(state: Int) {}
            })

            bottomNavMenu.setOnNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.home -> {
                        fragmentsContainer.currentItem = 0
                        true
                    }

                    R.id.customers -> {
                        fragmentsContainer.currentItem = 1
                        true
                    }

                    R.id.survey -> {
                        fragmentsContainer.currentItem = 2
                        true
                    }

                    R.id.history -> {
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

            addNewOutletButton.setOnClickListener {
                AddOutletActivity.start(this@MainActivity)
            }
        }
    }

    override fun onProfileSignOut() {
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