package id.erela.surveyproduct.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import id.erela.surveyproduct.R
import id.erela.surveyproduct.databinding.ActivityLoginBinding
import id.erela.surveyproduct.helpers.UserDataHelper
import id.erela.surveyproduct.helpers.api.InitErelaAppAPI
import id.erela.surveyproduct.helpers.customs.CustomToast
import id.erela.surveyproduct.objects.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        init()
    }

    @SuppressLint("SetTextI18n")
    private fun init() {
        binding.apply {
            if (UserDataHelper(this@LoginActivity).isUserDataStored()) {
                startActivity(Intent(this@LoginActivity, MainActivity::class.java)).also {
                    finish()
                }
            }

            usernameField.setText("igun.cantik")
            passwordField.setText("putrikecilayah")

            signInButton.setOnClickListener {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

                if (usernameField.text.isNullOrEmpty() || passwordField.text.isNullOrEmpty()) {
                    loadingBar.visibility = View.GONE
                    CustomToast.getInstance(applicationContext)
                        .setMessage("Please fill in all fields.")
                        .setFontColor(getColor(R.color.custom_toast_font_failed))
                        .setBackgroundColor(getColor(R.color.custom_toast_background_failed))
                        .show()
                    if (usernameField.text.isNullOrEmpty()) {
                        usernameFieldLayout.error = "Username cannot be empty"
                    } else {
                        usernameFieldLayout.error = null
                    }
                    if (passwordField.text.isNullOrEmpty()) {
                        passwordFieldLayout.error = "Password cannot be empty"
                    } else {
                        passwordFieldLayout.error = null
                    }
                } else {
                    checkLogin(usernameField.text.toString(), passwordField.text.toString())
                }
            }
        }
    }

    private fun checkLogin(username: String, password: String) {
        binding.apply {
            usernameFieldLayout.error = null
            passwordFieldLayout.error = null
            loadingBar.visibility = View.VISIBLE

            try {
                InitErelaAppAPI.endpoint.login(username, password)
                    .enqueue(object : Callback<LoginResponse> {
                        override fun onResponse(
                            call: Call<LoginResponse>,
                            response: Response<LoginResponse>
                        ) {
                            loadingBar.visibility = View.GONE
                            if (response.isSuccessful) {
                                if (response.body() != null) {
                                    val result = response.body()
                                    when (result?.error) {
                                        1 -> {
                                            CustomToast.getInstance(applicationContext)
                                                .setMessage(result.message!!)
                                                .setFontColor(
                                                    ContextCompat.getColor(
                                                        this@LoginActivity,
                                                        R.color.custom_toast_font_failed
                                                    )
                                                )
                                                .setBackgroundColor(
                                                    ContextCompat.getColor(
                                                        this@LoginActivity,
                                                        R.color.custom_toast_background_failed
                                                    )
                                                ).show()
                                            usernameField.setText("")
                                            passwordField.setText("")
                                        }

                                        0 -> {
                                            CustomToast.getInstance(applicationContext)
                                                .setMessage("Login Successful!")
                                                .setFontColor(
                                                    ContextCompat.getColor(
                                                        this@LoginActivity,
                                                        R.color.custom_toast_font_success
                                                    )
                                                )
                                                .setBackgroundColor(
                                                    ContextCompat.getColor(
                                                        this@LoginActivity,
                                                        R.color.custom_toast_background_success
                                                    )
                                                ).show()
                                            UserDataHelper(this@LoginActivity)
                                                .storeData(
                                                    result.users?.id ?: 0,
                                                    result.users?.fullname,
                                                    result.users?.usermail,
                                                    result.users?.username,
                                                    result.users?.usercode,
                                                    result.users?.usertype,
                                                    result.users?.userteam,
                                                    result.users?.branch
                                                )
                                            Handler(mainLooper).postDelayed({
                                                startActivity(
                                                    Intent(
                                                        this@LoginActivity,
                                                        MainActivity::class.java
                                                    )
                                                ).also {
                                                    finish()
                                                }
                                            }, 2000)
                                        }
                                    }
                                } else {
                                    Log.e("ERROR", "Response body is null")
                                    Log.e("Response", response.toString())
                                    CustomToast.getInstance(applicationContext)
                                        .setMessage("Something went wrong, please try again.")
                                        .setFontColor(
                                            ContextCompat.getColor(
                                                this@LoginActivity,
                                                R.color.custom_toast_font_failed
                                            )
                                        )
                                        .setBackgroundColor(
                                            ContextCompat.getColor(
                                                this@LoginActivity,
                                                R.color.custom_toast_background_failed
                                            )
                                        ).show()
                                }
                            } else {
                                Log.e("ERROR", "Response not successful")
                                Log.e("Response", response.toString())
                                CustomToast.getInstance(applicationContext)
                                    .setMessage("Something went wrong, please try again.")
                                    .setFontColor(
                                        ContextCompat.getColor(
                                            this@LoginActivity,
                                            R.color.custom_toast_font_failed
                                        )
                                    )
                                    .setBackgroundColor(
                                        ContextCompat.getColor(
                                            this@LoginActivity,
                                            R.color.custom_toast_background_failed
                                        )
                                    ).show()
                            }
                        }

                        override fun onFailure(call: Call<LoginResponse>, throwable: Throwable) {
                            loadingBar.visibility = View.GONE
                            Log.e("ERROR", throwable.toString())
                            throwable.printStackTrace()
                            Snackbar.make(
                                binding.root,
                                "Something went wrong. Please try again!",
                                Snackbar.LENGTH_SHORT
                            ).also {
                                with(it) {
                                    setAction("Retry") {
                                        checkLogin(username, password)
                                    }
                                }
                            }.show()
                        }
                    })
            } catch (exception: Exception) {
                loadingBar.visibility = View.GONE
                Log.e("ERROR", exception.toString())
                Snackbar.make(
                    binding.root,
                    "Something went wrong. Please try again!",
                    Snackbar.LENGTH_SHORT
                ).also {
                    with(it) {
                        setAction("Retry") {
                            checkLogin(username, password)
                        }
                    }
                }.show()
                exception.printStackTrace()
            }
        }
    }
}