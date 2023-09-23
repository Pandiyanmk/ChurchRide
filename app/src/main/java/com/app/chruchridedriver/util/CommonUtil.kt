package com.app.chruchridedriver.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.app.chruchridedriver.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CommonUtil {

    /* Show Message As An Alert Dialog */
    fun showAlert(message: String, context: Context?) {
        val builder = context?.let { MaterialAlertDialogBuilder(it, R.style.AlertDialogTheme) }
        builder?.let {
            it.setMessage(message)
            it.setPositiveButton(context!!.getString(R.string.ok_text), null)

            val dialog = it.create()
            dialog.show()
        }
    }

    fun moveToLoginpageWithDataClear(ctx: Context) {
        val sharedPreference = ctx.getSharedPreferences("LOGIN", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putString("savedId", "")
        editor.putString("isLoggedInType", "")
        editor.putInt("isLoggedIn", 0)
        editor.putInt("isDoc", 0)
        editor.commit()
    }

    /* Function For Checking Network Availability */
    fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }

                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }

                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        }
        return false
    }
}