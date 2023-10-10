package com.app.chruchridedriver.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.view.Gravity
import androidx.core.content.ContextCompat
import com.app.chruchridedriver.R
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shashank.sony.fancytoastlib.FancyToast


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
        clearOnlineStatus(ctx)
        clearRideStatus(ctx)
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

    fun getDriverId(ctx: Context): String? {
        val sharedPreference = ctx.getSharedPreferences("LOGIN", Context.MODE_PRIVATE)
        return sharedPreference.getString("savedId", "")
    }

    fun getFcmId(ctx: Context): String? {
        val sharedPreference = ctx.getSharedPreferences("FCMID", Context.MODE_PRIVATE)
        return sharedPreference.getString("Token", "")
    }

    fun hasLocationPermission(ctx: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasBackgroundPermission(ctx: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun dialPad(ctx: Context, phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phoneNumber")
        ctx.startActivity(intent)
    }

    fun saveLocation(ctx: Context, latitude: Double, longitude: Double) {
        val sharedPreference = ctx.getSharedPreferences("LOCATION", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putString("latitude", "" + latitude)
        editor.putString("longitude", "" + longitude)
        editor.commit()
    }

    fun getDriverLocation(ctx: Context): LatLng {
        val sharedPreference = ctx.getSharedPreferences("LOCATION", Context.MODE_PRIVATE)
        val latitude = sharedPreference.getString("latitude", "0.0")
        val longitude = sharedPreference.getString("longitude", "0.0")
        return LatLng(latitude!!.toDouble(), longitude!!.toDouble())
    }

    fun saveOnlineStatus(ctx: Context, status: Int) {
        val sharedPreference = ctx.getSharedPreferences("ONLINESTATUS", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putInt("status", status)
        editor.commit()
    }

    fun saveRideStatus(ctx: Context, status: Int) {
        val sharedPreference = ctx.getSharedPreferences("ISRIDE", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putInt("isRide", status)
        editor.commit()
    }

    fun getIsRideStatus(ctx: Context): Int {
        val sharedPreference = ctx.getSharedPreferences("ISRIDE", Context.MODE_PRIVATE)
        return sharedPreference.getInt("isRide", 0)
    }

     fun clearRideStatus(ctx: Context) {
        val sharedPreference = ctx.getSharedPreferences("ISRIDE", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putInt("isRide", 0)
        editor.commit()
    }

    private fun clearOnlineStatus(ctx: Context) {
        val sharedPreference = ctx.getSharedPreferences("ONLINESTATUS", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putInt("status", 0)
        editor.commit()
    }


    fun getOnlineStatus(ctx: Context): Int {
        val sharedPreference = ctx.getSharedPreferences("ONLINESTATUS", Context.MODE_PRIVATE)
        return sharedPreference.getInt("status", 0)
    }

    fun defaultToast(ctx: Context, message: String,gravity: Int = Gravity.CENTER) {
        val to = FancyToast.makeText(
            ctx, message.uppercase(), FancyToast.LENGTH_SHORT, FancyToast.INFO, false
        )
        to.setGravity(gravity, 0, 0)
        to.show()
    }
}