package com.app.chruchridedriver.view

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.app.chruchridedriver.R
import com.app.chruchridedriver.util.CommonUtil
import com.gauravbhola.ripplepulsebackground.RipplePulseLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton


class DriverHomePage : AppCompatActivity() {
    private val cu = CommonUtil()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.driver_home_page)

        /* Hiding ToolBar */
        supportActionBar?.hide()
        val mRipplePulseLayout: RipplePulseLayout = findViewById(R.id.layout_ripplepulse)
        val logout: LinearLayout = findViewById(R.id.logout)
        mRipplePulseLayout.startRippleAnimation()
        logout.setOnClickListener {
            cu.moveToLoginpageWithDataClear(this)
            val moveToLoginPage = Intent(this, LoginPage::class.java)
            startActivity(moveToLoginPage)
            finish()
        }
    }

}