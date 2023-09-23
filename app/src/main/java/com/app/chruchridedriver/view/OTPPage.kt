package com.app.chruchridedriver.view

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.apachat.loadingbutton.core.customViews.CircularProgressButton
import com.app.chruchridedriver.R
import com.app.chruchridedriver.repository.MainRepository
import com.app.chruchridedriver.util.CommonUtil
import com.app.chruchridedriver.viewModel.LoginPageViewModel
import com.app.chruchridedriver.viewModel.LoginPageViewModelFactory
import `in`.aabhasjindal.otptextview.OtpTextView
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import java.util.Locale


class OTPPage : AppCompatActivity() {
    private lateinit var loginPageViewModel: LoginPageViewModel
    private val cu = CommonUtil()
    private var currentOTP = ""
    private var currentMobileNumber = ""
    private var loader: MaterialProgressBar? = null
    var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.otp_page)

        /* Hiding ToolBar */
        supportActionBar?.hide()

        /* ViewModel Initialization */
        loginPageViewModel = ViewModelProvider(
            this, LoginPageViewModelFactory(MainRepository())
        )[LoginPageViewModel::class.java]
        currentOTP = "" + intent.getStringExtra("codeOTP")
        val codesent = findViewById<TextView>(R.id.codesent)
        val resendotp = findViewById<TextView>(R.id.resendotp)
        val otptimer = findViewById<TextView>(R.id.otptimer)
        val otpView = findViewById<OtpTextView>(R.id.otp_view)
        val backtap = findViewById<ImageView>(R.id.backtap)
        val nextButton = findViewById<CircularProgressButton>(R.id.nextButton)
        loader = findViewById(R.id.loader)

        currentMobileNumber = "" + intent.getStringExtra("mobileNumber")
        codesent.text = currentMobileNumber
        otpView.requestFocus()
        otpView.setOTP(currentOTP)
        if (otpView.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
        resendotp.paintFlags = resendotp.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        backtap.setOnClickListener {
            closeKeyboard()
            finish()
        }
        resendotp.setOnClickListener {
            if (cu.isNetworkAvailable(this)) {
                if (loader!!.visibility != View.VISIBLE) {
                    startLoader()
                    loginPageViewModel.getLoginResponse(currentMobileNumber)
                }
            } else {
                displayMessageInAlert(getString(R.string.no_internet).uppercase(Locale.getDefault()))
            }
        }

        loginPageViewModel.errorMessage.observe(this) { errorMessage ->
            nextButton!!.revertAnimation()
            cu.showAlert(errorMessage, this)
            stopLoader()
        }
        loginPageViewModel.driverContent.observe(this) { result ->
            nextButton!!.revertAnimation()
            if (result.driverDetails.isNotEmpty()) {
                if (result.driverDetails[0].type == "admin") {
                    updateLogin(result.driverDetails[0].id, "admin")
                    val driverDocPage = Intent(this, DriverSearchPage::class.java)
                    driverDocPage.putExtra("adminId", result.driverDetails[0].id)
                    driverDocPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(driverDocPage)
                    finish()
                } else {
                    if (result.driverDetails[0].verified == "1") {
                        updateHomeLogin(result.driverDetails[0].id, "driver")
                        val driverDocPage = Intent(this, DriverHomePage::class.java)
                        driverDocPage.putExtra("driverId", result.driverDetails[0].id)
                        driverDocPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(driverDocPage)
                        finish()
                    } else {
                        updateLogin(result.driverDetails[0].id, "driver")
                        val driverDocPage = Intent(this, DocumentUploadStatus::class.java)
                        driverDocPage.putExtra("driverId", result.driverDetails[0].id)
                        driverDocPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(driverDocPage)
                        finish()
                    }
                }
            } else {
                val moveToReset = Intent(this, DriverDetails::class.java)
                moveToReset.putExtra("mobileNumber", currentMobileNumber)
                startActivity(moveToReset)
                finish()
            }
        }


        loginPageViewModel.responseContent.observe(this) { result ->
            if (result.data.isNotEmpty()) {
                currentOTP = result.data[0].codeOTP
                otpView.setOTP(currentOTP)
                otptimer.visibility = View.VISIBLE
                resendotp.visibility = View.GONE
                timer = object : CountDownTimer(60000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        otptimer.text = "${millisUntilFinished / 1000}"
                    }

                    override fun onFinish() {
                        resendotp.visibility = View.VISIBLE
                        otptimer.visibility = View.GONE
                        timer = null
                    }
                }
                timer?.start()
            }
            stopLoader()
        }


        nextButton.setOnClickListener {
            if (otpView.otp!! == currentOTP) {
                val sharedPreference = getSharedPreferences("FCMID", Context.MODE_PRIVATE)
                val token = sharedPreference.getString("Token", "")
                nextButton!!.startAnimation()
                loginPageViewModel.getDriverId(currentMobileNumber, token!!)
                closeKeyboard()
            } else {
                cu.showAlert(getString(R.string.invalid_otp_entered), this)
            }
        }
    }

    private fun displayMessageInAlert(message: String) {
        cu.showAlert(message, this)
    }

    private fun startLoader() {
        loader!!.visibility = View.VISIBLE
    }

    private fun stopLoader() {
        loader!!.visibility = View.INVISIBLE
    }

    private fun closeKeyboard() {
        this.currentFocus?.let { view ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.let {
            it.cancel()
            false
        }
    }

    private fun updateLogin(driverId: String, type: String) {
        val sharedPreference = getSharedPreferences("LOGIN", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putString("savedId", driverId)
        editor.putString("isLoggedInType", type)
        editor.putInt("isLoggedIn", 1)
        editor.putInt("isDoc", 1)
        editor.commit()
    }
    private fun updateHomeLogin(driverId: String, type: String) {
        val sharedPreference = getSharedPreferences("LOGIN", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putString("savedId", driverId)
        editor.putString("isLoggedInType", type)
        editor.putInt("isLoggedIn", 1)
        editor.putInt("isDoc", 2)
        editor.commit()
    }
}