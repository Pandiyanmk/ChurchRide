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
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import java.util.Locale


class DriverSearchPage : AppCompatActivity(), ClickedAdapterInterface {
    private lateinit var driverSearchPageViewModel: DriverSearchPageViewModel
    private val cu = CommonUtil()
    private var adminId = ""
    private var loader: MaterialProgressBar? = null
    private var adapter: SearchDriverAdapter? = null

    private var unapproved: TextView? = null
    private var approved: TextView? = null
    private var all: TextView? = null

    private var allview: View? = null
    private var approvedview: View? = null
    private var unapprovedview: View? = null

    var sortBy = "3"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.driver_search_page)

        /* Hiding ToolBar */
        supportActionBar?.hide()
        anyChangeMade()/* ViewModel Initialization */
        driverSearchPageViewModel = ViewModelProvider(
            this, DriverSearchPageViewModelFactory(MainRepository())
        )[DriverSearchPageViewModel::class.java]
        adminId = "" + intent.getStringExtra("adminId")
        loader = findViewById(R.id.loader)

        val recentdriver = findViewById<RecyclerView>(R.id.recentdriver)
        val errormessage = findViewById<TextView>(R.id.errormessage)
        val logout = findViewById<ImageView>(R.id.logout)
        val backtap = findViewById<ImageView>(R.id.backtap)
        recentdriver.layoutManager = LinearLayoutManager(this)
        val search = findViewById<ImageView>(R.id.search)

        all = findViewById(R.id.all)
        approved = findViewById(R.id.approved)
        unapproved = findViewById(R.id.unapproved)

        allview = findViewById(R.id.allview)
        approvedview = findViewById(R.id.approvedview)
        unapprovedview = findViewById(R.id.unapprovedview)

        all!!.setOnClickListener {
            if (cu.isNetworkAvailable(this)) {
                if (loader!!.visibility != View.VISIBLE) {
                    updateAllButtonClick()
                    startLoader()
                    sortBy = "3"
                    driverSearchPageViewModel.getRegisteredDriverRecent(sortBy)
                }
            } else {
                displayMessageInAlert(getString(R.string.no_internet).uppercase(Locale.getDefault()))
            }
        }

        approved!!.setOnClickListener {
            if (cu.isNetworkAvailable(this)) {
                if (loader!!.visibility != View.VISIBLE) {
                    updateApprovedButtonClick()
                    startLoader()
                    sortBy = "1"
                    driverSearchPageViewModel.getRegisteredDriverRecent(sortBy)
                }
            } else {
                displayMessageInAlert(getString(R.string.no_internet).uppercase(Locale.getDefault()))
            }
        }
        unapproved!!.setOnClickListener {
            if (cu.isNetworkAvailable(this)) {
                if (loader!!.visibility != View.VISIBLE) {
                    updateUnapprovedButtonClick()
                    startLoader()
                    sortBy = "0"
                    driverSearchPageViewModel.getRegisteredDriverRecent(sortBy)
                }
            } else {
                displayMessageInAlert(getString(R.string.no_internet).uppercase(Locale.getDefault()))
            }
        }

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
                    cu.moveToLoginpageWithDataClear(this)
                    val moveToLoginPage = Intent(this, LoginPage::class.java)
                    startActivity(moveToLoginPage)
                    finish()
                }.show()
        }
        search.setOnClickListener {
            if (cu.isNetworkAvailable(this)) {
                if (loader!!.visibility != View.VISIBLE) {
                    startLoader()
                    driverSearchPageViewModel.getRegisteredDriverRecent(sortBy)
                }
            } else {
                displayMessageInAlert(getString(R.string.no_internet).uppercase(Locale.getDefault()))
            }
        }

        if (cu.isNetworkAvailable(this)) {
            if (loader!!.visibility != View.VISIBLE) {
                startLoader()
                driverSearchPageViewModel.getRegisteredDriverRecent(sortBy)
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
            if (result.registeredDriver.isEmpty()) {
                errormessage.visibility = View.VISIBLE
            } else {
                errormessage.visibility = View.GONE
            }
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




    fun updateAllButtonClick() {
        all!!.setTextColor(resources.getColor(R.color.white))
        approved!!.setTextColor(resources.getColor(R.color.lightgrey))
        unapproved!!.setTextColor(resources.getColor(R.color.lightgrey))

        allview!!.setBackgroundColor(resources.getColor(R.color.white))
        approvedview!!.setBackgroundColor(resources.getColor(R.color.grey))
        unapprovedview!!.setBackgroundColor(resources.getColor(R.color.grey))
    }

    fun updateApprovedButtonClick() {
        all!!.setTextColor(resources.getColor(R.color.lightgrey))
        approved!!.setTextColor(resources.getColor(R.color.white))
        unapproved!!.setTextColor(resources.getColor(R.color.lightgrey))

        allview!!.setBackgroundColor(resources.getColor(R.color.grey))
        approvedview!!.setBackgroundColor(resources.getColor(R.color.white))
        unapprovedview!!.setBackgroundColor(resources.getColor(R.color.grey))
    }

    private fun updateUnapprovedButtonClick() {
        all!!.setTextColor(resources.getColor(R.color.lightgrey))
        approved!!.setTextColor(resources.getColor(R.color.lightgrey))
        unapproved!!.setTextColor(resources.getColor(R.color.white))

        allview!!.setBackgroundColor(resources.getColor(R.color.grey))
        approvedview!!.setBackgroundColor(resources.getColor(R.color.grey))
        unapprovedview!!.setBackgroundColor(resources.getColor(R.color.white))
    }

    private fun anyChangeMade() {
        val sharedPreference = getSharedPreferences("ANYCHANGE", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putInt("isChange", 0)
        editor.commit()
    }

    override fun onResume() {
        super.onResume()
        val sharedPreference = getSharedPreferences("ANYCHANGE", Context.MODE_PRIVATE)
        val isChange = sharedPreference.getInt("isChange", 0)
        anyChangeMade()
        if (isChange == 1) {
            if (cu.isNetworkAvailable(this)) {
                if (loader!!.visibility != View.VISIBLE) {
                    startLoader()
                    driverSearchPageViewModel.getRegisteredDriverRecent(sortBy)
                }
            } else {
                displayMessageInAlert(getString(R.string.no_internet).uppercase(Locale.getDefault()))
            }
        }

    }
}