package id.erela.surveyproduct.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import id.erela.surveyproduct.R
import id.erela.surveyproduct.databinding.ActivityStartSurveyBinding
import id.erela.surveyproduct.fragments.AnswerFragment
import id.erela.surveyproduct.fragments.CheckInFragment

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
                if (fragmentPosition == 1)
                    return@setOnClickListener
                fragmentPosition--
                inflateFragment(fragmentPosition)
            }

            nextButton.setOnClickListener {
                if (fragmentPosition == 3)
                    return@setOnClickListener
                fragmentPosition++
                inflateFragment(fragmentPosition)
            }
        }
    }

    private fun inflateFragment(position: Int) {
        val fragment = when (position) {
            1 -> CheckInFragment(this)
            2 -> AnswerFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }

        binding.apply {
            when (position) {
                1 -> {
                    previousButton.isEnabled = false
                    previousButton.alpha = 0.5f
                }

                2 -> {
                    previousButton.isEnabled = true
                    previousButton.alpha = 1.0f
                    nextButton.isEnabled = true
                    nextButton.alpha = 1.0f
                }

                3 -> {
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
}