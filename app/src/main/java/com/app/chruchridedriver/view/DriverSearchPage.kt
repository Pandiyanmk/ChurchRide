package com.app.chruchridedriver.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.chruchridedriver.R
import com.app.chruchridedriver.adapter.SearchDriverAdapter
import com.app.chruchridedriver.interfaces.ClickedAdapterInterface
import com.app.chruchridedriver.repository.MainRepository
import com.app.chruchridedriver.util.CommonUtil
import com.app.chruchridedriver.viewModel.DriverSearchPageViewModel
import com.app.chruchridedriver.viewModel.DriverSearchPageViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import java.util.Locale


class DriverSearchPage : AppCompatActivity(), ClickedAdapterInterface {
    private lateinit var driverSearchPageViewModel: DriverSearchPageViewModel
    private val cu = CommonUtil()
    private var adminId = ""
    private var loader: MaterialProgressBar? = null
    private var adapter: SearchDriverAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.driver_search_page)

        /* Hiding ToolBar */
        supportActionBar?.hide()

        /* ViewModel Initialization */
        driverSearchPageViewModel = ViewModelProvider(
            this, DriverSearchPageViewModelFactory(MainRepository())
        )[DriverSearchPageViewModel::class.java]
        adminId = "" + intent.getStringExtra("adminId")
        loader = findViewById(R.id.loader)

        val mobileNumber = findViewById<TextView>(R.id.mobile_number)
        val recentdriver = findViewById<RecyclerView>(R.id.recentdriver)
        val logout = findViewById<ImageView>(R.id.logout)
        val backtap = findViewById<ImageView>(R.id.backtap)
        recentdriver.layoutManager = LinearLayoutManager(this)
        val search = findViewById<FloatingActionButton>(R.id.search)
        backtap.setOnClickListener {
            finish()
        }
        logout.setOnClickListener {
            MaterialAlertDialogBuilder(
                this, R.style.AlertDialogTheme
            ).setTitle(getString(R.string.are_you_sure_want_to_logout))
                .setNeutralButton(getString(R.string.cancel)) { _, _ ->
                    // Respond to neutral button press
                }.setPositiveButton(getString(R.string.logout)) { _, _ ->
                    moveToLoginpageWithDataClear()
                    val moveToLoginPage = Intent(this, LoginPage::class.java)
                    startActivity(moveToLoginPage)
                    finish()
                }.show()
        }
        search.setOnClickListener {
            if (mobileNumber.text.isEmpty()) {
                displayMessageInAlert(getString(R.string.please_enter_user_id).uppercase(Locale.ROOT))
            } else {
                if (cu.isNetworkAvailable(this)) {
                    if (loader!!.visibility != View.VISIBLE) {
                        startLoader()
                        driverSearchPageViewModel.getRegisteredDriverRecent()
                    }
                } else {
                    displayMessageInAlert(getString(R.string.no_internet).uppercase(Locale.getDefault()))
                }
            }
        }

        if (cu.isNetworkAvailable(this)) {
            if (loader!!.visibility != View.VISIBLE) {
                startLoader()
                driverSearchPageViewModel.getRegisteredDriverRecent()
            }
        } else {
            displayMessageInAlert(getString(R.string.no_internet).uppercase(Locale.getDefault()))
        }

        driverSearchPageViewModel.errorMessage.observe(this) { errorMessage ->
            cu.showAlert(errorMessage, this)
            stopLoader()
        }

        driverSearchPageViewModel.responseContent.observe(this) { result ->
            stopLoader()
            adapter = SearchDriverAdapter(this, result.registeredDriver, this)
            recentdriver.adapter = adapter
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

    override fun selectedValue(id: String?) {
        closeKeyboard()
        val driverDocPage = Intent(this, AdminDocumentUploadStatus::class.java)
        driverDocPage.putExtra("driverId", id)
        startActivity(driverDocPage)
    }


    private fun moveToLoginpageWithDataClear() {
        val sharedPreference = getSharedPreferences("LOGIN", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putString("savedId", "")
        editor.putString("isLoggedInType", "")
        editor.putInt("isLoggedIn", 0)
        editor.putInt("isDoc", 0)
        editor.commit()
    }
}