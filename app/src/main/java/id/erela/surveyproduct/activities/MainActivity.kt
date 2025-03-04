package id.erela.surveyproduct.activities

import android.os.Bundle
import android.util.Log
import android.window.OnBackInvokedDispatcher
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import id.erela.surveyproduct.R
import id.erela.surveyproduct.databinding.ActivityMainBinding
import id.erela.surveyproduct.helpers.UserDataHelper

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
    }

    private fun init() {
        binding.apply {
            Log.e("UserData", UserDataHelper(this@MainActivity).getData().toString())
        }
    }

    @Deprecated(
        "This method has been deprecated in favor of using the\n" +
                "{@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n" +
                "The OnBackPressedDispatcher controls how back button events are dispatched\n" +
                "to one or more {@link OnBackPressedCallback} objects."
    )
    override fun onBackPressed() {
        super.onBackPressed()
        UserDataHelper(this@MainActivity).purgeUserData()
    }
}