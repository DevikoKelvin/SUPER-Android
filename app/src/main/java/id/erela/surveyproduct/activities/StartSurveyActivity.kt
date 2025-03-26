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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import id.erela.surveyproduct.R
import id.erela.surveyproduct.databinding.ActivityStartSurveyBinding
import id.erela.surveyproduct.dialogs.LoadingDialog
import id.erela.surveyproduct.fragments.AnswerFragment
import id.erela.surveyproduct.fragments.CheckInFragment
import id.erela.surveyproduct.fragments.CheckOutFragment
import id.erela.surveyproduct.helpers.SharedPreferencesHelper
import id.erela.surveyproduct.helpers.customs.CustomToast
import id.erela.surveyproduct.objects.SurveyAnswer
import id.erela.surveyproduct.repository.SurveyRepository
import id.erela.surveyproduct.viewmodels.SurveyState
import id.erela.surveyproduct.viewmodels.SurveyViewModel
import id.erela.surveyproduct.viewmodels.factories.SurveyViewModelFactory
import kotlinx.coroutines.launch

class StartSurveyActivity : AppCompatActivity() {
    private val binding: ActivityStartSurveyBinding by lazy {
        ActivityStartSurveyBinding.inflate(layoutInflater)
    }
    private var currentFragment: Fragment? = null
    private var fragmentPosition = 1
    private lateinit var viewModel: SurveyViewModel

    companion object {
        const val CHECK_IN_UPLOADED = "CHECK_IN_UPLOADED"
        const val ANSWER_UPLOADED = "ANSWER_UPLOADED"
        var answerData: ArrayList<SurveyAnswer> = ArrayList()

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
            val repository = SurveyRepository(this@StartSurveyActivity, this@StartSurveyActivity)
            viewModel = ViewModelProvider(
                this@StartSurveyActivity,
                SurveyViewModelFactory(repository)
            )[SurveyViewModel::class.java]

            lifecycleScope.launch {
                val dialog = LoadingDialog(this@StartSurveyActivity)
                viewModel.surveyState.collect { state ->
                    when (state) {
                        is SurveyState.Idle -> {
                        }

                        is SurveyState.Loading -> {
                            if (dialog.window != null)
                                dialog.show()
                        }

                        is SurveyState.Success -> {
                            dialog.dismiss()
                            CustomToast(applicationContext)
                                .setMessage("Survey Successfully Submitted!")
                                .setBackgroundColor(
                                    getColor(R.color.custom_toast_background_success)
                                )
                                .setFontColor(
                                    getColor(R.color.custom_toast_font_success)
                                ).show()
                            finish()
                        }

                        is SurveyState.Error -> {
                            dialog.dismiss()
                            CustomToast(applicationContext)
                                .setMessage("Survey Submission Failed!")
                                .setBackgroundColor(
                                    getColor(R.color.custom_toast_background_failed)
                                )
                                .setFontColor(
                                    getColor(R.color.custom_toast_font_failed)
                                ).show()
                        }
                    }
                }
            }

            uploadButton.setOnClickListener {
                if (fragmentPosition == 3) {
                    AnswerFragment.uploadData(
                        answerData,
                        this@StartSurveyActivity,
                        viewModel
                    )
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
}