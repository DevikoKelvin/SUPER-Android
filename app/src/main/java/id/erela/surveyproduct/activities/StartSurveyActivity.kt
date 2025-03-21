package id.erela.surveyproduct.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import id.erela.surveyproduct.R
import id.erela.surveyproduct.databinding.ActivityStartSurveyBinding
import id.erela.surveyproduct.fragments.AnswerFragment
import id.erela.surveyproduct.fragments.CheckInFragment
import id.erela.surveyproduct.helpers.SharedPreferencesHelper

class StartSurveyActivity : AppCompatActivity() {
    private val binding: ActivityStartSurveyBinding by lazy {
        ActivityStartSurveyBinding.inflate(layoutInflater)
    }
    private var currentFragment: Fragment? = null
    private val fragmentData = mutableMapOf<Int, Bundle>()
    private val checkInData = HashMap<String, String>()
    private var fragmentPosition = 1
    private var selectedOutlet = 0
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var imageUri: Uri? = null

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
                saveCurrentFragmentState()
                fragmentPosition--
                inflateFragment(fragmentPosition)
            }

            nextButton.setOnClickListener {
                when (fragmentPosition) {
                    1 -> {
                        saveCheckInData()
                    }

                    3 -> {
                        return@setOnClickListener
                    }
                }
                saveCurrentFragmentState()
                fragmentPosition++
                inflateFragment(fragmentPosition)
            }
        }
    }

    private fun saveCurrentFragmentState() {
        currentFragment?.let { fragment ->
            val bundle = Bundle()
            when (fragment) {
                is CheckInFragment -> fragment.saveState(bundle)
                is AnswerFragment -> fragment.saveState(bundle)
            }
            fragmentData[fragmentPosition] = bundle
        }
    }

    private fun restoreFragmentState(fragment: Fragment, position: Int) {
        fragmentData[position]?.let { bundle ->
            when (fragment) {
                is CheckInFragment -> fragment.restoreState(bundle)
                is AnswerFragment -> fragment.restoreState(bundle)
            }
        }
    }

    private fun saveCheckInData() {
        val set = HashSet<String>().apply {
            add(selectedOutlet.toString())
            add(latitude.toString())
            add(longitude.toString())
            add(imageUri.toString())
        }
        SharedPreferencesHelper.getSharedPreferences(applicationContext).edit {
            putStringSet("check_in_data", set)
        }
        val data = SharedPreferencesHelper.getSharedPreferences(applicationContext)
            .getStringSet("check_in_data", null)?.toList()
        checkInData["outlet"] = data?.get(0).toString()
        checkInData["latitude"] = data?.get(1).toString()
        checkInData["longitude"] = data?.get(2).toString()
        checkInData["image"] = data?.get(3).toString()
    }

    private fun inflateFragment(position: Int) {
        saveCurrentFragmentState()
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

                else -> {
                    nextButton.isEnabled = false
                    nextButton.alpha = 0.5f
                }
            }
        }

        restoreFragmentState(fragment, position)

        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, fragment)
        }
        currentFragment = fragment
        fragmentPosition = position
    }

    fun setCheckInData(outlet: Int, lat: Double, long: Double, image: Uri?) {
        selectedOutlet = outlet
        latitude = lat
        longitude = long
        imageUri = image
    }
}