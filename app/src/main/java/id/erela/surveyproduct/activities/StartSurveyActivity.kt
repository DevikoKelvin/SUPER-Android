package id.erela.surveyproduct.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import id.erela.surveyproduct.R
import id.erela.surveyproduct.databinding.ActivityStartSurveyBinding
import id.erela.surveyproduct.fragments.CheckInFragment
import id.erela.surveyproduct.helpers.SharedPreferencesHelper

class StartSurveyActivity : AppCompatActivity() {
    private val binding: ActivityStartSurveyBinding by lazy {
        ActivityStartSurveyBinding.inflate(layoutInflater)
    }
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
                fragmentPosition--
                inflateFragment(fragmentPosition)
            }

            nextButton.setOnClickListener {
                if (fragmentPosition == 1) {
                    val set: HashSet<String> = HashSet()
                    set.add(selectedOutlet.toString())
                    set.add(latitude.toString())
                    set.add(longitude.toString())
                    set.add(imageUri.toString())
                    SharedPreferencesHelper.getSharedPreferences(applicationContext).edit {
                        with(it) {
                            apply {
                                putStringSet("check_in_data", set)
                            }
                        }
                    }
                    val data = SharedPreferencesHelper.getSharedPreferences(applicationContext)
                        .getStringSet("check_in_data", null)?.toList()
                    Log.e("Check in Data", data.toString())
                    checkInData["outlet"] = data?.get(0).toString()
                    checkInData["latitude"] = data?.get(3).toString()
                    checkInData["longitude"] = data?.get(2).toString()
                    checkInData["image"] = data?.get(1).toString()
                    Log.e("Check in Data", checkInData.toString())
                }
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
            val currentFragment: Fragment? =
                supportFragmentManager.findFragmentById(binding.fragmentContainer.id)

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

    fun setCheckInData(outlet: Int, lat: Double, long: Double, image: Uri?) {
        selectedOutlet = outlet
        latitude = lat
        longitude = long
        imageUri = image
    }
}