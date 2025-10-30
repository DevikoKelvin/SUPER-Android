package id.erela.surveyproduct.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import id.erela.surveyproduct.R
import id.erela.surveyproduct.databinding.ActivityLoginBinding
import id.erela.surveyproduct.helpers.Generic
import id.erela.surveyproduct.helpers.UserDataHelper
import id.erela.surveyproduct.helpers.api.AppAPI
import id.erela.surveyproduct.helpers.customs.CustomToast
import id.erela.surveyproduct.objects.LoginResponse
import id.erela.surveyproduct.objects.UserDetailResponse
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

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars: Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        init()
    }

    override fun dispatchTouchEvent(motionEvent: MotionEvent): Boolean {
        if (motionEvent.action == MotionEvent.ACTION_DOWN) {
            val view: View? = currentFocus
            if (view is TextInputEditText || view is EditText) {
                val rect = Rect()
                view.getGlobalVisibleRect(rect)
                if (!rect.contains(motionEvent.rawX.toInt(), motionEvent.rawY.toInt())) {
                    view.clearFocus()
                    val inputMethodManager =
                        getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(motionEvent)
    }
    @SuppressLint("SetTextI18n")
    private fun init() {
        binding.apply {
            if (UserDataHelper(this@LoginActivity).isUserDataStored()) {
                startActivity(Intent(this@LoginActivity, MainActivity::class.java)).also {
                    finish()
                }
            }

            signInButton.setOnClickListener {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

                if (usernameField.text.isNullOrEmpty() || passwordField.text.isNullOrEmpty()) {
                    loadingBar.visibility = View.GONE
                    CustomToast.getInstance(applicationContext)
                        .setMessage(
                            if (getString(R.string.language) == "en") "Please fill in all fields."
                            else "Harap isi semua kolom."
                        )
                        .setFontColor(getColor(R.color.custom_toast_font_failed))
                        .setBackgroundColor(getColor(R.color.custom_toast_background_failed))
                        .show()
                    if (usernameField.text.isNullOrEmpty()) {
                        usernameFieldLayout.error =
                            if (getString(R.string.language) == "en") "Username cannot be empty" else "Username tidak boleh kosong"
                    } else {
                        usernameFieldLayout.error = null
                    }
                    if (passwordField.text.isNullOrEmpty()) {
                        passwordFieldLayout.error =
                            if (getString(R.string.language) == "en") "Password cannot be empty" else "Kata sandi tidak boleh kosong"
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
                AppAPI.erelaEndpoint.login(username, password)
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
                                            Generic.crashReport(Exception("Login Error: ${result.message}"))
                                        }

                                        0 -> {
                                            loadingBar.visibility = View.VISIBLE
                                            AppAPI.superEndpoint.getUserByUsername(
                                                result.usersErela?.username
                                            ).enqueue(object : Callback<UserDetailResponse> {
                                                override fun onResponse(
                                                    call1: Call<UserDetailResponse>,
                                                    response1: Response<UserDetailResponse>
                                                ) {
                                                    loadingBar.visibility = View.GONE
                                                    if (response1.isSuccessful) {
                                                        if (response1.body() != null) {
                                                            val result1 = response1.body()
                                                            when (result1?.code) {
                                                                1 -> {
                                                                    CustomToast.getInstance(
                                                                        applicationContext
                                                                    )
                                                                        .setMessage(
                                                                            if (getString(R.string.language) == "en") "Login Successful!"
                                                                            else "Berhasil Masuk!"
                                                                        )
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
                                                                            result1.usersSuper?.iD,
                                                                            result1.usersSuper?.fullName,
                                                                            result1.usersSuper?.userMail,
                                                                            result1.usersSuper?.userName,
                                                                            result1.usersSuper?.photoProfile,
                                                                            result1.usersSuper?.userCode,
                                                                            result1.usersSuper?.typeID,
                                                                            result1.usersSuper?.typeName,
                                                                            result1.usersSuper?.teamID,
                                                                            result1.usersSuper?.teamName,
                                                                            result1.usersSuper?.branchID,
                                                                            result1.usersSuper?.branchName
                                                                        )
                                                                    Handler(mainLooper).postDelayed(
                                                                        {
                                                                            startActivity(
                                                                                Intent(
                                                                                    this@LoginActivity,
                                                                                    MainActivity::class.java
                                                                                )
                                                                            ).also {
                                                                                finish()
                                                                            }
                                                                        },
                                                                        2000
                                                                    )
                                                                }

                                                                0 -> {
                                                                    CustomToast.getInstance(
                                                                        applicationContext
                                                                    )
                                                                        .setMessage(result1.message!!)
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
                                                        } else {
                                                            Log.e("ERROR", "Response body is null")
                                                            CustomToast.getInstance(
                                                                applicationContext
                                                            )
                                                                .setMessage(
                                                                    if (getString(R.string.language) == "en") "Something went wrong, please try again later"
                                                                    else "Terjadi kesalahan, silakan coba lagi nanti"
                                                                )
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
                                                        CustomToast.getInstance(applicationContext)
                                                            .setMessage(
                                                                if (getString(R.string.language) == "en") "Something went wrong, please try again later"
                                                                else "Terjadi kesalahan, silakan coba lagi nanti"
                                                            )
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

                                                override fun onFailure(
                                                    call1: Call<UserDetailResponse>,
                                                    throwable: Throwable
                                                ) {
                                                    loadingBar.visibility = View.GONE
                                                    Log.e(
                                                        "ERROR",
                                                        "Super API Error Get User Detail"
                                                    )
                                                    Log.e("ERROR", throwable.toString())
                                                    throwable.printStackTrace()
                                                    Snackbar.make(
                                                        binding.root,
                                                        if (getString(R.string.language) == "en") "Something went wrong, please try again later"
                                                        else "Terjadi kesalahan, silakan coba lagi nanti",
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
                                        }
                                    }
                                } else {
                                    Log.e("ERROR", "Response body is null")
                                    CustomToast.getInstance(applicationContext)
                                        .setMessage(
                                            if (getString(R.string.language) == "en") "Something went wrong, please try again later"
                                            else "Terjadi kesalahan, silakan coba lagi nanti"
                                        )
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
                                CustomToast.getInstance(applicationContext)
                                    .setMessage(
                                        if (getString(R.string.language) == "en") "Something went wrong, please try again later"
                                        else "Terjadi kesalahan, silakan coba lagi nanti"
                                    )
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
                                if (getString(R.string.language) == "en") "Something went wrong, please try again later"
                                else "Terjadi kesalahan, silakan coba lagi nanti",
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
                    if (getString(R.string.language) == "en") "Something went wrong, please try again later"
                    else "Terjadi kesalahan, silakan coba lagi nanti",
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