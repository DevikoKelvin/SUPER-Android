package id.erela.surveyproduct.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import id.erela.surveyproduct.R
import id.erela.surveyproduct.databinding.ActivityStartSurveyBinding
import id.erela.surveyproduct.fragments.AnswerFragment
import id.erela.surveyproduct.fragments.CheckInFragment
import id.erela.surveyproduct.fragments.CheckOutFragment
import id.erela.surveyproduct.helpers.SharedPreferencesHelper
import id.erela.surveyproduct.helpers.customs.CustomToast

class StartSurveyActivity : AppCompatActivity() {
    private val binding: ActivityStartSurveyBinding by lazy {
        ActivityStartSurveyBinding.inflate(layoutInflater)
    }
    private var currentFragment: Fragment? = null
    private var fragmentPosition = 1

    companion object {
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

                        2 -> {
                            fragmentPosition--
                            inflateFragment(1)
                        }

                        3 -> {
                            fragmentPosition--
                            inflateFragment(2)
                        }
                    }
                }
            })

            backButton.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            inflateFragment(1)

            previousButton.setOnClickListener {
                if (fragmentPosition == 1) return@setOnClickListener
                fragmentPosition--
                inflateFragment(fragmentPosition)
            }

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
                        handleNextButtonClick()
                        return@setOnClickListener
                    }
                }
                fragmentPosition++
                inflateFragment(fragmentPosition)
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

        binding.apply {
            when (position) {
                1 -> {
                    toolbarTitle.text = getString(R.string.check_in_title)
                    previousButton.isEnabled = false
                    previousButton.alpha = 0.5f
                }

                2 -> {
                    toolbarTitle.text = getString(R.string.survey_data)
                    previousButton.isEnabled = true
                    previousButton.alpha = 1.0f
                    nextButton.isEnabled = true
                    nextButton.alpha = 1.0f
                }

                3 -> {
                    toolbarTitle.text = getString(R.string.check_out_title)
                    nextButton.isEnabled = false
                    nextButton.alpha = 0.5f
                }
            }
        }

        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, fragment)
        }
        currentFragment = fragment
        fragmentPosition = position
    }

    private fun handleNextButtonClick() {
        val answerFragment = currentFragment as? AnswerFragment
        if (answerFragment != null) {
            // Get SharedPreferences to check answers
            val sharedPrefs = SharedPreferencesHelper.getSharedPreferences(this)
            var allQuestionsAnswered = true
            /*// Check each question and subquestion
            for (questionId in AnswerFragment.questionIdArray) {
                // Check subquestions if they exist
                for (subQuestionId in AnswerFragment.subQuestionIdArray) {
                    if (subQuestionId != null) {
                        val hasSubPhoto = sharedPrefs.getString(
                            "${AnswerFragment.ANSWER_PHOTO}_${questionId}_$subQuestionId",
                            null
                        )
                        val hasSubText = sharedPrefs.getString(
                            "${AnswerFragment.ANSWER_TEXT}_${questionId}_$subQuestionId",
                            null
                        )
                        val hasSubCheckbox = sharedPrefs.getBoolean(
                            "${AnswerFragment.ANSWER_CHECKBOX_MULTIPLE}_${questionId}_$subQuestionId",
                            false
                        )

                        if (hasSubPhoto == null && hasSubText.isNullOrBlank() && !hasSubCheckbox) {
                            allQuestionsAnswered = false
                            break
                        }
                    }
                }
                // Check main questions
                val hasPhoto =
                    sharedPrefs.getString("${AnswerFragment.ANSWER_PHOTO}_${questionId}_0", null)
                val hasText =
                    sharedPrefs.getString("${AnswerFragment.ANSWER_TEXT}_${questionId}_0", null)
                val hasCheckbox = sharedPrefs.getBoolean(
                    "${AnswerFragment.ANSWER_CHECKBOX_MULTIPLE}_${questionId}_0",
                    false
                )
                // If none of the answer types exist for this question
                if (hasPhoto == null && hasText.isNullOrBlank() && !hasCheckbox) {
                    allQuestionsAnswered = false
                    break
                }

                if (!allQuestionsAnswered) break
            }*/

            if (allQuestionsAnswered) {
                // All questions are answered, proceed to next fragment
                fragmentPosition++
                inflateFragment(fragmentPosition)
            } else {
                // Show toast message for unanswered questions
                CustomToast(applicationContext)
                    .setMessage("Please answer all questions!")
                    .setFontColor(getColor(R.color.custom_toast_font_failed))
                    .setBackgroundColor(getColor(R.color.custom_toast_background_failed))
                    .show()
            }
        } else {
            // If not in AnswerFragment, just proceed to next fragment
            fragmentPosition++
            inflateFragment(fragmentPosition)
        }
    }
}