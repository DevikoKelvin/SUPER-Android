package id.erela.surveyproduct.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import id.erela.surveyproduct.R
import id.erela.surveyproduct.fragments.CheckInFragment
import id.erela.surveyproduct.databinding.ActivityStartSurveyBinding

class StartSurveyActivity : AppCompatActivity() {
    private val binding: ActivityStartSurveyBinding by lazy {
        ActivityStartSurveyBinding.inflate(layoutInflater)
    }
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
            backButton.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            inflateFragment(1)

            previousButton.setOnClickListener {
                if (fragmentPosition == 1) {
                    return@setOnClickListener
                }
                fragmentPosition--
                inflateFragment(fragmentPosition)
            }

            nextButton.setOnClickListener {
                if (fragmentPosition == 3) {
                    return@setOnClickListener
                }
                fragmentPosition++
                inflateFragment(fragmentPosition)
            }
        }
    }

    private fun inflateFragment(position: Int) {
        binding.apply {
            val currentFragment: Fragment? = supportFragmentManager.findFragmentById(binding.fragmentContainer.id)

            when (position) {
                1 -> {
                    toolbarTitle.text = getString(R.string.check_in_title)
                    previousButton.visibility = View.GONE
                    if (currentFragment !is CheckInFragment) {
                        supportFragmentManager.commit {
                            replace(
                                binding.fragmentContainer.id,
                                CheckInFragment(this@StartSurveyActivity)
                            )
                            setReorderingAllowed(true)
                        }
                    }
                }

                2 -> {
                    toolbarTitle.text = getString(R.string.start_survey_title)
                    previousButton.visibility = View.VISIBLE
                    nextButton.visibility = View.VISIBLE
                }

                3 -> {
                    toolbarTitle.text = getString(R.string.check_out_title)
                    nextButton.visibility = View.GONE
                }
            }
        }
    }
}