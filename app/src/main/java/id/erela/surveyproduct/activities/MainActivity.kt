package id.erela.surveyproduct.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import androidx.viewpager2.widget.ViewPager2
import app.rive.runtime.kotlin.core.Rive
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import id.erela.surveyproduct.BuildConfig
import id.erela.surveyproduct.R
import id.erela.surveyproduct.adapters.home_nav.HomeNavPagerAdapter
import id.erela.surveyproduct.databinding.ActivityMainBinding
import id.erela.surveyproduct.dialogs.ConfirmationDialog
import id.erela.surveyproduct.fragments.HistoryFragment
import id.erela.surveyproduct.fragments.HomeFragment
import id.erela.surveyproduct.fragments.OutletFragment
import id.erela.surveyproduct.fragments.ProfileFragment
import id.erela.surveyproduct.fragments.StartSurveyFragment
import id.erela.surveyproduct.helpers.SharedPreferencesHelper
import id.erela.surveyproduct.helpers.UserDataHelper
import id.erela.surveyproduct.objects.UsersSuper

class MainActivity : AppCompatActivity(), ProfileFragment.OnProfileButtonActionListener {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val userData: UsersSuper by lazy {
        UserDataHelper(applicationContext).getData()
    }
    private lateinit var adapter: HomeNavPagerAdapter
    private val activityResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val outletFragment = supportFragmentManager.fragments.find { fragment ->
                fragment is OutletFragment
            } as? OutletFragment
            outletFragment?.callNetwork()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        init()
    }

    override fun onResume() {
        super.onResume()
        val startSurveyFragment = supportFragmentManager.fragments.find { fragment ->
            fragment is StartSurveyFragment
        } as? StartSurveyFragment
        startSurveyFragment?.callNetwork()
    }

    @SuppressLint("SetTextI18n")
    private fun init() {
        binding.apply {
            Rive.init(applicationContext)

            bottomNavMenu.itemIconTintList = null

            Glide.with(applicationContext)
                .asBitmap()
                .load(BuildConfig.IMAGE_URL + userData.photoProfile)
                .placeholder(
                    AppCompatResources.getDrawable(
                        applicationContext,
                        R.drawable.profile_icon
                    )
                )
                .error(
                    AppCompatResources.getDrawable(
                        applicationContext,
                        R.drawable.profile_icon
                    )
                )
                .circleCrop()
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        bitmap: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        bottomNavMenu.menu.findItem(R.id.profile)
                            .setIcon(bitmap.toDrawable(applicationContext.resources))
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        bottomNavMenu.menu.findItem(R.id.profile).setIcon(placeholder)
                    }
                })

            versionText.text = if (getString(R.string.language) == "en") "Version ${BuildConfig.VERSION_NAME}.${BuildConfig.VERSION_CODE}_${BuildConfig.BUILD_TIMESTAMP}"
            else "Versi ${BuildConfig.VERSION_NAME}.${BuildConfig.VERSION_CODE}_${BuildConfig.BUILD_TIMESTAMP}"

            onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    binding.apply {
                        if (bottomNavMenu.selectedItemId == R.id.home) {
                            val dialog = ConfirmationDialog(
                                this@MainActivity,
                                if (getString(R.string.language) == "en") "Are you sure want to quit?"
                                else "Apakah yakin ingin keluar?",
                                if (getString(R.string.language) == "en") "Yes" else "Ya"
                            ).also {
                                with(it) {
                                    setConfirmationDialogListener(object :
                                        ConfirmationDialog.ConfirmationDialogListener {
                                        override fun onConfirm() {
                                            finish()
                                            SharedPreferencesHelper.getSharedPreferences(
                                                applicationContext
                                            ).edit {
                                                remove(HistoryFragment.KEY_START)
                                                remove(HistoryFragment.KEY_END)
                                            }
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
                            versionText.alpha = 0f
                            addButton.hide()
                        }

                        1 -> {
                            titleBar.alpha = 1f
                            versionText.alpha = 0f
                            if (addButton.isExtended)
                                addButton.shrink()
                            addButton.text = getString(R.string.add_outlet_title)
                            addButton.backgroundTintList = ColorStateList.valueOf(
                                ContextCompat.getColor(
                                    this@MainActivity, R.color.custom_toast_background_success
                                )
                            )
                            if (!addButton.isShown)
                                addButton.show()
                            addButton.setOnClickListener {
                                activityResultLauncher.launch(
                                    Intent(this@MainActivity, AddOutletActivity::class.java)
                                )
                            }
                        }

                        2 -> {
                            titleBar.alpha = 1f
                            versionText.alpha = 0f
                            if (!addButton.isExtended)
                                addButton.extend()
                            addButton.text = getString(R.string.start_survey_now)
                            addButton.backgroundTintList = ColorStateList.valueOf(
                                "#5899EF".toColorInt()
                            )
                            if (!addButton.isShown)
                                addButton.show()
                            addButton.setOnClickListener {
                                CheckInActivity.start(this@MainActivity)
                            }
                        }

                        3 -> {
                            titleBar.alpha = 1 - positionOffset
                            versionText.alpha = positionOffset
                            addButton.hide()
                        }

                        4 -> {
                            titleBar.alpha = 0f
                            versionText.alpha = 1f
                            addButton.hide()
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
                        addButton.hide()
                    }

                    R.id.outlet -> {
                        fragmentsContainer.setCurrentItem(1, false)
                        if (addButton.isExtended)
                            addButton.shrink()
                        addButton.text = getString(R.string.add_outlet_title)
                        addButton.backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(
                                this@MainActivity, R.color.custom_toast_background_success
                            )
                        )
                        if (!addButton.isShown)
                            addButton.show()
                    }

                    R.id.start_survey -> {
                        fragmentsContainer.setCurrentItem(2, false)
                        if (!addButton.isExtended)
                            addButton.extend()
                        addButton.text = getString(R.string.start_survey_now)
                        addButton.backgroundTintList = ColorStateList.valueOf(
                            "#5899EF".toColorInt()
                        )
                        if (!addButton.isShown)
                            addButton.show()
                    }

                    R.id.history -> {
                        fragmentsContainer.setCurrentItem(3, false)
                        addButton.hide()
                    }

                    R.id.profile -> {
                        fragmentsContainer.setCurrentItem(4, false)
                        addButton.hide()
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
            if (getString(R.string.language) == "en") "Are you sure you want to sign out?"
            else "Apakah Anda yakin ingin keluar?",
            if (getString(R.string.language) == "en") "Yes" else "Ya"
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