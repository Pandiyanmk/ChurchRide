package com.app.chruchridedriver.view


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.apachat.loadingbutton.core.customViews.CircularProgressButton
import com.app.chruchridedriver.R
import com.app.chruchridedriver.adapter.LanguageAdapter
import com.app.chruchridedriver.data.model.LanguageCode
import com.app.chruchridedriver.interfaces.ClickedAdapterInterface
import com.app.chruchridedriver.repository.MainRepository
import com.app.chruchridedriver.util.CommonUtil
import com.app.chruchridedriver.viewModel.LoginPageViewModel
import com.app.chruchridedriver.viewModel.LoginPageViewModelFactory
import java.util.Locale


class LoginPage : AppCompatActivity(), CustomSpinner.OnSpinnerEventsListener,
    ClickedAdapterInterface {
    private lateinit var loginPageViewModel: LoginPageViewModel
    private val cu = CommonUtil()
    var name: String? = null
    private var spinnerLang: CustomSpinner? = null
    private var loginButton: CircularProgressButton? = null
    private var adapter: LanguageAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)

        /* Hiding ToolBar */
        supportActionBar?.hide()

        /* ViewModel Initialization */
        loginPageViewModel = ViewModelProvider(
            this, LoginPageViewModelFactory(MainRepository())
        )[LoginPageViewModel::class.java]

        /* Initializing Views */
        val mobileNumber = findViewById<TextView>(R.id.mobile_number)
        loginButton = findViewById(R.id.loginButton)
        val clear = findViewById<ImageView>(R.id.clear)
        spinnerLang = findViewById(R.id.language_spinnear)

        mobileNumber.requestFocus()
        if (mobileNumber.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
        clear.setOnClickListener {
            mobileNumber.text = ""
        }
        mobileNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.isNotEmpty()) {
                    clear.visibility = View.VISIBLE
                } else {
                    clear.visibility = View.INVISIBLE
                }
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int, count: Int
            ) {

            }
        })

        spinnerLang!!.setSpinnerEventsListener(this)

        loginButton!!.setOnClickListener {
            if (mobileNumber.text.toString().isEmpty()) {
                displayMessageInAlert(getString(R.string.please_enter_user_id).uppercase(Locale.ROOT))
            } else {
                startFetch("+1" + mobileNumber.text.toString())
            }
        }
        setLanguageBasedOnCode()

        loginPageViewModel.errorMessage.observe(this) { errorMessage ->
            cu.showAlert(errorMessage, this)
            loginButton!!.revertAnimation()
        }

        loginPageViewModel.responseContent.observe(this) { result ->
            if (result.data.isNotEmpty()) {
                val moveToReset = Intent(this, OTPPage::class.java)
                moveToReset.putExtra("codeOTP", result.data[0].codeOTP)
                moveToReset.putExtra("mobileNumber", result.data[0].mobileNumber)
                startActivity(moveToReset)
            } else {
                cu.showAlert(getString(R.string.invalid_login_details), this)
            }

            loginButton!!.revertAnimation()
        }
    }

    private fun startFetch(mobileNumber: String) {
        if (cu.isNetworkAvailable(this)) {
            loginButton!!.startAnimation()
            loginPageViewModel.getLoginResponse(mobileNumber)
        } else {
            displayMessageInAlert(getString(R.string.no_internet).uppercase(Locale.getDefault()))
        }
    }

    private fun displayMessageInAlert(message: String) {
        cu.showAlert(message, this)
    }

    override fun onPopupWindowOpened(spinner: Spinner?) {
        spinnerLang!!.background = ContextCompat.getDrawable(this, R.drawable.bg_spinner_up)
    }

    override fun onPopupWindowClosed(spinner: Spinner?) {
        spinnerLang!!.background = ContextCompat.getDrawable(this, R.drawable.bg_spinner)
    }

    override fun selectedValue(name: String) {
        val sharedPreference = getSharedPreferences("LOGIN", Context.MODE_PRIVATE)
        val isLan = sharedPreference.getString("isLanguage", "en")

        if (name == "English") {
            if (isLan != "en") {
                updateLanguage("en")
            }
        } else {
            if (isLan != "hi") {
                updateLanguage("hi")
            }
        }

    }

    private fun updateLanguage(code: String) {
        val sharedPreference = getSharedPreferences("LOGIN", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putString("isLanguage", code)
        editor.commit()
        restartAppAgain()
    }

    private fun restartAppAgain() {
        val moveToReset = Intent(this, GetStartedPage::class.java)
        moveToReset.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(moveToReset)
        finishAffinity()
    }

    private fun setLanguageBasedOnCode() {
        val addLanguage: ArrayList<LanguageCode> = ArrayList()
        val english = LanguageCode("English", R.drawable.uscode)
        val french = LanguageCode("Français", R.drawable.france)

        val sharedPreference = getSharedPreferences("LOGIN", Context.MODE_PRIVATE)
        val isLan = sharedPreference.getString("isLanguage", "en")
        if (isLan.equals("en")) {
            addLanguage.add(english)
            addLanguage.add(french)
        } else {
            addLanguage.add(french)
            addLanguage.add(english)
        }

        adapter = LanguageAdapter(this, addLanguage, this)
        spinnerLang!!.adapter = adapter
    }
}