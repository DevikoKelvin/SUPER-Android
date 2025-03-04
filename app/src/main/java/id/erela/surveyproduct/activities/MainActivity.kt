package id.erela.surveyproduct.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import id.erela.surveyproduct.R
import id.erela.surveyproduct.databinding.ActivityMainBinding
import id.erela.surveyproduct.fragments.HomeFragment
import id.erela.surveyproduct.helpers.UserDataHelper

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/

        init()
    }

    private fun init() {
        binding.apply {
            Log.e("UserData", UserDataHelper(this@MainActivity).getData().toString())

            loadFragments(HomeFragment())
            bottomNavMenu.selectedItemId = R.id.home

            bottomNavMenu.setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.outlet -> {
                        true
                    }
                    R.id.survey -> {
                        true
                    }
                    R.id.home -> {
                        loadFragments(HomeFragment())
                        true
                    }
                    R.id.users -> {
                        true
                    }
                    R.id.your_profile -> {
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
        }
    }

    private fun loadFragments(fragment: Fragment) {
        binding.apply {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(fragmentsContainer.id, fragment)
            transaction.commit()
        }
    }

    @SuppressLint("MissingSuperCall")
    @Deprecated(
        "This method has been deprecated in favor of using the\n" +
                "{@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n" +
                "The OnBackPressedDispatcher controls how back button events are dispatched\n" +
                "to one or more {@link OnBackPressedCallback} objects."
    )
    override fun onBackPressed() {
        binding.apply {
            if (bottomNavMenu.selectedItemId == R.id.home) {
                finish()
                UserDataHelper(this@MainActivity).purgeUserData()
            } else {
                bottomNavMenu.selectedItemId = R.id.home
            }
        }
    }
}