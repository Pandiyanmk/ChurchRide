package com.app.chruchridedriver.view

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.app.chruchridedriver.R
import com.app.chruchridedriver.util.CommonUtil
import java.util.Locale


class GetStartedPage : AppCompatActivity() {
    var name: String? = null
    val cu: CommonUtil = CommonUtil()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)/* Hiding ToolBar */
        supportActionBar?.hide()

        val sharedPreference = getSharedPreferences("LOGIN", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreference.getInt("isLoggedIn", 0)
        val isDoc = sharedPreference.getInt("isDoc", 0)
        val isLoggedInType = sharedPreference.getString("isLoggedInType", "")
        val savedId = sharedPreference.getString("savedId", "")

        val isLan = sharedPreference.getString("isLanguage", "en")
        updateLanguage(isLan)

        if (isLoggedIn == 1 && isDoc != 0) {
            if (isLoggedInType == "driver" && isDoc == 1) {
                val moveToAboutPage = Intent(this, DocumentUploadStatus::class.java)
                moveToAboutPage.putExtra("driverId", savedId)
                startActivity(moveToAboutPage)
                finish()
            } else if (isLoggedInType == "driver" && isDoc == 2) {
                if (cu.getIsRideStatus(this) == 0) {
                    val driverDocPage = Intent(this, DriverHomePage::class.java)
                    driverDocPage.putExtra("driverId", savedId)
                    driverDocPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(driverDocPage)
                    finish()
                } else {
                    val driverDocPage = Intent(this, DriverTripPage::class.java)
                    driverDocPage.putExtra("driverId", savedId)
                    driverDocPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(driverDocPage)
                    finish()
                }
            } else {
                val moveToAboutPage = Intent(this, DriverSearchPage::class.java)
                moveToAboutPage.putExtra("adminId", savedId)
                startActivity(moveToAboutPage)
                finish()
            }
        } else {
            setContentView(R.layout.launch_page)
            Handler().postDelayed({
                val moveToAboutPage = Intent(this, LoginPage::class.java)
                startActivity(moveToAboutPage)
                finish()
            }, 2000)
        }

    }

    private fun updateLanguage(code: String?) {
        val locale = Locale(code)
        Locale.setDefault(locale)
        val config: Configuration = baseContext.resources.configuration
        config.locale = locale
        baseContext.resources.updateConfiguration(
            config, baseContext.resources.displayMetrics
        )
    }
}