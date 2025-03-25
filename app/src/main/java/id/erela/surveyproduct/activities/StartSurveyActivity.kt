package id.erela.surveyproduct.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.gms.common.api.Api
import id.erela.surveyproduct.R
import id.erela.surveyproduct.databinding.ActivityStartSurveyBinding
import id.erela.surveyproduct.fragments.AnswerFragment
import id.erela.surveyproduct.fragments.CheckInFragment
import id.erela.surveyproduct.fragments.CheckOutFragment
import id.erela.surveyproduct.helpers.SharedPreferencesHelper
import id.erela.surveyproduct.helpers.api.AppAPI
import id.erela.surveyproduct.helpers.customs.CustomToast

class StartSurveyActivity : AppCompatActivity() {
    private val binding: ActivityStartSurveyBinding by lazy {
        ActivityStartSurveyBinding.inflate(layoutInflater)
    }
    private var currentFragment: Fragment? = null
    private var fragmentPosition = 1

    companion object {
        const val CHECK_IN_UPLOADED = "CHECK_IN_UPLOADED"
        const val ANSWER_UPLOADED = "ANSWER_UPLOADED"

        fun start(context: Context) {
            context.startActivity(
                Intent(context, StartSurveyActivity::class.java)
            )
        }
    }

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
                    when (fragmentPosition) {
                        1 -> {
                            CheckInFragment.clearCheckInData(this@StartSurveyActivity)
                            AnswerFragment.clearAnswerData(this@StartSurveyActivity)
                            CheckOutFragment.clearCheckInData(this@StartSurveyActivity)
                            finish()
                        }

                        else -> {
                            fragmentPosition--
                            inflateFragment(fragmentPosition)
                        }
                    }
                }
            })

            backButton.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            inflateFragment(1)

            nextButton.setOnClickListener {
                if (fragmentPosition == 3) return@setOnClickListener
                when (currentFragment) {
                    is CheckInFragment -> {
                        val selectedOutlet =
                            SharedPreferencesHelper.getSharedPreferences(this@StartSurveyActivity)
                                .getInt(CheckInFragment.SELECTED_OUTLET, 0)
                        val imageURI =
                            SharedPreferencesHelper.getSharedPreferences(this@StartSurveyActivity)
                                .getString(CheckInFragment.IMAGE_URI, null)?.toUri()
                        if (selectedOutlet == 0 || imageURI == null) {
                            CustomToast(applicationContext).setMessage(
                                if (selectedOutlet == 0) "Please select outlet first!"
                                else "Please take photo first!"
                            ).setFontColor(getColor(R.color.custom_toast_font_failed))
                                .setBackgroundColor(getColor(R.color.custom_toast_background_failed))
                                .show()
                            return@setOnClickListener
                        }
                    }

                    is AnswerFragment -> {
                        if (!AnswerFragment.validateAnswer(this@StartSurveyActivity)) {
                            CustomToast(applicationContext).setMessage(
                                "Please answer all questions!"
                            ).setFontColor(getColor(R.color.custom_toast_font_failed))
                                .setBackgroundColor(getColor(R.color.custom_toast_background_failed))
                                .show()
                            return@setOnClickListener
                        }
                    }
                }
                fragmentPosition++
                inflateFragment(fragmentPosition)
            }

            uploadButton.setOnClickListener {
                if (currentFragment is CheckOutFragment) {
                    (currentFragment as CheckOutFragment)
                }
            }
        }
    }

    private fun inflateFragment(position: Int) {
        val fragment = when (position) {
            1 -> CheckInFragment(this)
            2 -> AnswerFragment()
            3 -> CheckOutFragment(this)
            else -> throw IllegalArgumentException("Invalid position")
        }

        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, fragment)
        }
        currentFragment = fragment
        fragmentPosition = position

        binding.apply {
            when (position) {
                1 -> {
                    toolbarTitle.text = getString(R.string.check_in_title)
                    nextButtonContainer.visibility = View.VISIBLE
                    uploadButtonContainer.visibility = View.GONE
                }

                2 -> {
                    toolbarTitle.text = getString(R.string.survey_data)
                    nextButtonContainer.visibility = View.VISIBLE
                    uploadButtonContainer.visibility = View.GONE
                }

                3 -> {
                    toolbarTitle.text = getString(R.string.check_out_title)
                    nextButtonContainer.visibility = View.GONE
                    uploadButtonContainer.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun uploadData() {
        val sharedPreferences =
            SharedPreferencesHelper.getSharedPreferences(this@StartSurveyActivity)

        if (sharedPreferences.getBoolean(CHECK_IN_UPLOADED, false)) {
            AppAPI.superEndpoint.
        }
    }
}