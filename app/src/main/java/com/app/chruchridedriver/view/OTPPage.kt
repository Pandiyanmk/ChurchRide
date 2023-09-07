package com.app.chruchridedriver.view

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
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
                if (loader!!.visibility == View.INVISIBLE) {
                    startLoader()
                    loginPageViewModel.getLoginResponse(currentMobileNumber)
                }
            } else {
                displayMessageInAlert(getString(R.string.no_internet).uppercase(Locale.getDefault()))
            }
        }

        loginPageViewModel.errorMessage.observe(this) { errorMessage ->
            cu.showAlert(errorMessage, this)
            stopLoader()
        }

        loginPageViewModel.responseContent.observe(this) { result ->
            if (result.data.isNotEmpty()) {
                currentOTP = result.data[0].codeOTP
                otpView.setOTP(currentOTP)
            }
            stopLoader()
        }


        nextButton.setOnClickListener {
            if (otpView.otp!! == currentOTP) {
                closeKeyboard()
                val moveToReset = Intent(this, DriverDetails::class.java)
                moveToReset.putExtra("mobileNumber", currentMobileNumber)
                startActivity(moveToReset)
                finish()
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
}