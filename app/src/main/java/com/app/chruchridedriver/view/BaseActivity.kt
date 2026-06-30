package com.app.chruchridedriver.view

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale


open class BaseActivity : AppCompatActivity() {
    fun updateLanguage() {
        val sharedPreference = getSharedPreferences("LOGIN", Context.MODE_PRIVATE)
        val isLan = sharedPreference.getString("isLanguage", "en")
        val locale = Locale(isLan)
        Locale.setDefault(locale)
        val config: Configuration = baseContext.resources.configuration
        config.locale = locale
        baseContext.resources.updateConfiguration(
            config, baseContext.resources.displayMetrics
        )
    }
}