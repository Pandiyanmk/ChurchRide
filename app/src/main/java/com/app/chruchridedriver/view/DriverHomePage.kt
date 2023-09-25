package com.app.chruchridedriver.view


import android.Manifest
import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.provider.Settings
import android.util.Property
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.app.chruchridedriver.R
import com.app.chruchridedriver.location.LocationService
import com.app.chruchridedriver.util.CommonUtil
import com.gauravbhola.ripplepulsebackground.RipplePulseLayout
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class DriverHomePage : AppCompatActivity(), OnMapReadyCallback {
    private val cu = CommonUtil()
    lateinit var mGoogleMap: GoogleMap
    var onlinestatus = 0
    var timer: CountDownTimer? = null
    var loader: MaterialProgressBar? = null
    lateinit var mRipplePulseLayout: RipplePulseLayout
    lateinit var onandofflayout: FloatingActionButton
    lateinit var gosettings: FloatingActionButton
    lateinit var onandofftext: TextView
    lateinit var onlinetext: TextView
    lateinit var onlinesubtext: TextView
    lateinit var menulay: LinearLayout
    lateinit var locationoff: LinearLayout
    var carMarker: Marker? = null
    var bearing = 0f
    val MY_PERMISSIONS_REQUEST_LOCATION = 99
    var mapLoaded = false
    var oldLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.driver_home_page)

        /* Hiding ToolBar */
        supportActionBar?.hide()

        mRipplePulseLayout = findViewById(R.id.layout_ripplepulse)
        val logout: FloatingActionButton = findViewById(R.id.logout)
        val mylocation: FloatingActionButton = findViewById(R.id.mylocation)
        val profile: FloatingActionButton = findViewById(R.id.profile)
        onandofflayout = findViewById(R.id.onandofflayout)
        onandofftext = findViewById(R.id.onandofftext)
        onlinetext = findViewById(R.id.onlinetext)
        onlinesubtext = findViewById(R.id.onlinesubtext)
        menulay = findViewById(R.id.menulay)
        loader = findViewById(R.id.loader)
        locationoff = findViewById(R.id.locationoff)
        gosettings = findViewById(R.id.gosettings)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mRipplePulseLayout.startRippleAnimation()
        logout.setOnClickListener {
            stopLocationService()
            cu.moveToLoginpageWithDataClear(this)
            val moveToLoginPage = Intent(this, LoginPage::class.java)
            startActivity(moveToLoginPage)
            finish()
        }

        onandofflayout.setOnClickListener {
            if (onlinestatus == 0) {
                checkLocationPermission()
            } else {
                goToOffline()
            }
        }

        mylocation.setOnClickListener {
            if (mapLoaded) {
                oldLocation?.let {
                    val cameraPosition =
                        CameraPosition.Builder().target(LatLng(it.latitude, it.longitude)).zoom(18f)
                            .build()
                    mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                }
            }
        }

        profile.setOnClickListener {
            val driverDocPage = Intent(this, DriverProfilePage::class.java)
            driverDocPage.putExtra("driverId", cu.getDriverId(this))
            startActivity(driverDocPage)
        }
    }

    override fun onMapReady(p0: GoogleMap?) {
        mGoogleMap = p0!!
        mapLoaded = true
        try {
            val success: Boolean = mGoogleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this, R.raw.church_style
                )
            )
        } catch (e: Exception) {

        }
    }

    private fun addCarMarker(location: Location) {
        val height = 100
        val width = 60
        val bitmap = resources.getDrawable(R.drawable.car_driver) as BitmapDrawable
        val b = bitmap.bitmap
        val smallMarker = Bitmap.createScaledBitmap(b, width, height, false)

        val latLng = LatLng(location.latitude, location.longitude)
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
        mGoogleMap.clear()
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        mGoogleMap.addMarker(markerOptions)
        val cameraPosition =
            CameraPosition.Builder().target(LatLng(latLng.latitude, latLng.longitude)).zoom(18f)
                .build()
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.let {
            it.cancel()
            timer = null
        }
        stopLocationService()
    }

    fun updateLocation() {
        val handler = Handler()
        handler.postDelayed(
            Runnable { loader!!.visibility = View.GONE }, 2000
        )
    }

    override fun onResume() {
        super.onResume()
        if (!cu.hasLocationPermission(this)) {
            goToOffline()
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            goOnline()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val permissionGranted = ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                if (permissionGranted) {
                    goOnline()
                } else {
                    showSnackBarForLocationSettings()
                }
            }
        }
    }


    private fun goToOffline() {
        onlinestatus = 0
        mRipplePulseLayout.startRippleAnimation()
        onandofftext.text = getString(R.string.go)
        onlinetext.text = getString(R.string.you_re_offline)
        onlinesubtext.text = getString(R.string.click_go_to_make_you_online)
        onlinesubtext.visibility = View.VISIBLE
        loader!!.visibility = View.GONE
        timer?.let {
            it.cancel()
            timer = null
        }
        stopLocationService()
    }

    private fun startLocationService() {
        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_START
            startService(this)
        }
    }

    private fun stopLocationService() {
        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
            startService(this)
        }
    }

    private fun goOnline() {
        startLocationService()
        onlinestatus = 1
        mRipplePulseLayout.stopRippleAnimation()
        onandofftext.text = getString(R.string.off)
        onlinetext.text = getString(R.string.you_re_online)
        onlinesubtext.visibility = View.GONE
        timer = object : CountDownTimer(6000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                loader!!.visibility = View.VISIBLE
                updateLocation()
                timer!!.start()
            }
        }
        timer!!.start()
    }

    private fun showSnackBarForLocationSettings() {
        locationoff.visibility = View.VISIBLE
        gosettings.setOnClickListener {
            locationoff.visibility = View.GONE
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(location: Location?) {
        location?.let {
            updateMarker(it)
        }
    }


    private fun animateMarkerToICS(marker: Marker, finalPosition: LatLng) {
        val typeEvaluator = TypeEvaluator<LatLng> { fraction, startValue, endValue ->
            interpolate(
                fraction, startValue, endValue
            )
        }
        val property = Property.of(
            Marker::class.java, LatLng::class.java, "position"
        )
        val animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition)
        animator.duration = 3000
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {}
            override fun onAnimationEnd(animator: Animator) {

            }

            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationRepeat(animator: Animator) {}
        })
        animator.start()
    }

    private fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng {
        // function to calculate the in between values of old latlng and new latlng.
        // To get more accurate tracking(Car will always be in the road even when the latlng falls away from road), use roads api from Google apis.
        // As it has quota limits I didn't have used that method.
        val lat = (b.latitude - a.latitude) * fraction + a.latitude
        var lngDelta = b.longitude - a.longitude

        // Take the shortest path across the 180th meridian.
        if (Math.abs(lngDelta) > 180) {
            lngDelta -= Math.signum(lngDelta) * 360
        }
        val lng = lngDelta * fraction + a.longitude
        return LatLng(lat, lng)
    }


    private fun anima(point: CameraUpdate) {
        if (mapLoaded) {
            mGoogleMap?.let {
                runOnUiThread { it.animateCamera(point) }
            }
        }
    }

    private fun updateMarker(location: Location?) {
        if (location == null) {
            return
        }
        if (mGoogleMap != null && mapLoaded) {
            if (carMarker == null) {
                oldLocation = location
                val height = 100
                val width = 60
                val bitmap = resources.getDrawable(R.drawable.car_driver) as BitmapDrawable
                val b = bitmap.bitmap
                val smallMarker = Bitmap.createScaledBitmap(b, width, height, false)
                val markerOptions = MarkerOptions()
                val car = BitmapDescriptorFactory.fromBitmap(smallMarker)
                markerOptions.icon(car)
                markerOptions.anchor(
                    0.5f, 0.5f
                )
                markerOptions.flat(true)
                markerOptions.position(LatLng(location.latitude, location.longitude))
                carMarker = mGoogleMap.addMarker(markerOptions)
                bearing = if (location.hasBearing()) {
                    location.bearing
                } else {
                    0f
                }
                carMarker!!.rotation = bearing
                val point = CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        location.latitude, location.longitude
                    ), 18f
                )
                anima(point)
            } else {
                bearing = if (location.hasBearing()) {
                    location.bearing
                } else {
                    oldLocation!!.bearingTo(location)
                }
                carMarker!!.rotation = bearing
                val point = CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        location.latitude, location.longitude
                    ), mGoogleMap.cameraPosition.zoom
                )
                anima(point)
                animateMarkerToICS(
                    carMarker!!, LatLng(location.latitude, location.longitude)
                )
            }
        }
    }

}