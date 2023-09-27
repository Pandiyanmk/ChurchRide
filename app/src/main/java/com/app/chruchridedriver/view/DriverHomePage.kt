package com.app.chruchridedriver.view


import android.Manifest
import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Property
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.apachat.loadingbutton.core.customViews.CircularProgressButton
import com.app.chruchridedriver.R
import com.app.chruchridedriver.location.LocationService
import com.app.chruchridedriver.repository.MainRepository
import com.app.chruchridedriver.util.CommonUtil
import com.app.chruchridedriver.util.DynamicCountdownTimer
import com.app.chruchridedriver.util.DynamicCountdownTimer.DynamicCountdownCallback
import com.app.chruchridedriver.viewModel.DriverHomePageViewModel
import com.app.chruchridedriver.viewModel.DriverHomePageViewModelFactory
import com.gauravbhola.ripplepulsebackground.RipplePulseLayout
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.math.abs
import kotlin.math.sign


class DriverHomePage : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var driverHomePageViewModel: DriverHomePageViewModel
    private val cu = CommonUtil()
    private lateinit var mGoogleMap: GoogleMap
    private var onlinestatus = 0
    var timer: DynamicCountdownTimer? = null
    private var doubleBackToExitPressedOnce = false
    var loader: MaterialProgressBar? = null
    private lateinit var mRipplePulseLayout: RipplePulseLayout
    private lateinit var onandofflayout: FloatingActionButton
    private lateinit var gosettings: FloatingActionButton
    private lateinit var onandofftext: TextView
    private lateinit var onlinetext: TextView
    private lateinit var onlinesubtext: TextView
    private lateinit var menulay: LinearLayout
    private lateinit var locationoff: LinearLayout
    private var carMarker: Marker? = null
    private var bearing = 0f
    private val MY_PERMISSIONS_REQUEST_LOCATION = 99
    private var mapLoaded = false
    private var oldLocation: Location? = null
    private var sendLoaction: Location? = null
    private var updateTime: Long = 10000
    private val BACKGROUND_LOCATION_PERMISSION_CODE = 2
    private val ENABLED_LOCATION_PERMISSION_CODE = 5
    private var contactUs: BottomSheetDialog? = null
    var supportEmail = ""
    var supportAddress = ""
    var supportContactUs = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.driver_home_page)

        /* Hiding ToolBar */
        supportActionBar?.hide()

        /* ViewModel Initialization */
        driverHomePageViewModel = ViewModelProvider(
            this, DriverHomePageViewModelFactory(MainRepository())
        )[DriverHomePageViewModel::class.java]

        contactUs = BottomSheetDialog(this)
        contactUs!!.setCancelable(true)

        mRipplePulseLayout = findViewById(R.id.layout_ripplepulse)
        val logout: FloatingActionButton = findViewById(R.id.logout)
        val mylocation: FloatingActionButton = findViewById(R.id.mylocation)
        val profile: FloatingActionButton = findViewById(R.id.profile)
        val callsupport: FloatingActionButton = findViewById(R.id.callsupport)
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

        getContactUs()

        mRipplePulseLayout.startRippleAnimation()
        logout.setOnClickListener {
            stopLocationService()
            moveToLoginPageWithDataClear()
        }

        onandofflayout.setOnClickListener {
            if (onlinestatus == 0) {
                checkLocationPermission()
            } else {
                goToOffline()
            }
        }

        callsupport.setOnClickListener {
            openContactUs()
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

        driverHomePageViewModel.responseContent.observe(this) { result ->
            if (result.locationUpdatedData.isNotEmpty()) {
                supportEmail = result.locationUpdatedData[0].supportTeamEmail
                supportContactUs = result.locationUpdatedData[0].supportTeamCall
                supportAddress = result.locationUpdatedData[0].supportTeamAddress

                if (result.locationUpdatedData[0].verified == "0") {
                    stopLocationService()
                    movToDocumentUploadStatusPage()
                    return@observe
                }
                if (result.locationUpdatedData[0].fcmid != cu.getFcmId(this)) {
                    stopLocationService()
                    moveToLoginPageWithDataClear()
                    return@observe
                }
                val serverUpdatedTime: Int =
                    result.locationUpdatedData[0].locationUpdateTime.toInt() * 1000
                if (updateTime != serverUpdatedTime.toLong()) {
                    updateTime = serverUpdatedTime.toLong()
                    timer?.updateSeconds(updateTime)
                }
            }


            loader!!.visibility = View.GONE
        }
        driverHomePageViewModel.errorMessage.observe(this) {
            loader!!.visibility = View.GONE
        }
    }

    private fun moveToLoginPageWithDataClear() {
        cu.moveToLoginpageWithDataClear(this)
        val moveToLoginPage = Intent(this, LoginPage::class.java)
        startActivity(moveToLoginPage)
        finish()
    }

    private fun movToDocumentUploadStatusPage() {
        updateLogin(cu.getDriverId(this)!!)
        val driverDocPage = Intent(this, DocumentUploadStatus::class.java)
        driverDocPage.putExtra("driverId", cu.getDriverId(this))
        driverDocPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(driverDocPage)
        finish()
    }

    override fun onMapReady(p0: GoogleMap?) {
        mGoogleMap = p0!!
        mapLoaded = true
        try {
            mGoogleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this, R.raw.church_style
                )
            )
        } catch (_: Exception) {

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.let {
            it.Cancel()
            timer = null
        }
        stopLocationService()
    }

    fun updateLocation() {
        sendLoaction?.let {
            if (cu.isNetworkAvailable(this)) {
                loader!!.visibility = View.VISIBLE
                driverHomePageViewModel.updateCurrentLocation(
                    driverId = cu.getDriverId(this)!!,
                    latitude = "${it.latitude}",
                    longitude = "${it.longitude}",
                    activestatus = "$onlinestatus"
                )
            }
        }
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
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), MY_PERMISSIONS_REQUEST_LOCATION
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ENABLED_LOCATION_PERMISSION_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    goOn()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
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


    private fun goToOffline() {
        onlinestatus = 0
        mRipplePulseLayout.startRippleAnimation()
        onandofftext.text = getString(R.string.go)
        onlinetext.text = getString(R.string.you_re_offline)
        onlinesubtext.visibility = View.VISIBLE
        loader!!.visibility = View.GONE
        timer?.let {
            it.Cancel()
            timer = null
        }
        stopLocationService()
        updateLocation()
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
        checkBackgroundLocationPermission()
    }

    private fun goOn() {
        locationStatusCheck()
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
            sendLoaction = it
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
        val lat = (b.latitude - a.latitude) * fraction + a.latitude
        var lngDelta = b.longitude - a.longitude

        if (abs(x = lngDelta) > 180) {
            lngDelta -= sign(lngDelta) * 360
        }
        val lng = lngDelta * fraction + a.longitude
        return LatLng(lat, lng)
    }


    private fun anima(point: CameraUpdate) {
        if (mapLoaded) {
            mGoogleMap.let {
                runOnUiThread { it.animateCamera(point) }
            }
        }
    }

    private fun updateMarker(location: Location?) {
        if (location == null) {
            return
        }
        if (mapLoaded) {
            if (carMarker == null) {
                oldLocation = location
                val height = 100
                val width = 60
                val bitmap =
                    ContextCompat.getDrawable(this, R.drawable.car_driver) as BitmapDrawable
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

    private fun checkBackgroundLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    goOn()
                } else {
                    askPermissionForBackgroundUsage()
                }
            }
        } else {
            goOn()
        }
    }


    private fun askPermissionForBackgroundUsage() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        ) {
            AlertDialog.Builder(this).setTitle(getString(R.string.permission_needed))
                .setMessage(getString(R.string.background_location_permission_needed_tap_allow_all_time_in_the_next_screen))
                .setPositiveButton(getString(R.string.ok_text)) { _, _ ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ActivityCompat.requestPermissions(
                            this, arrayOf(
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            ), BACKGROUND_LOCATION_PERMISSION_CODE
                        )
                    }
                }.setNegativeButton(getString(R.string.cancel)) { _, _ ->

                }.create().show()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    BACKGROUND_LOCATION_PERMISSION_CODE
                )
            }
        }
    }

    private fun updateLogin(driverId: String) {
        val sharedPreference = getSharedPreferences("LOGIN", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putString("savedId", driverId)
        editor.putString("isLoggedInType", "driver")
        editor.putInt("isLoggedIn", 1)
        editor.putInt("isDoc", 1)
        editor.commit()
    }

    private fun locationStatusCheck() {
        val manager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            enableLocation()
        } else {
            startLocationService()
            onlinestatus = 1
            mRipplePulseLayout.stopRippleAnimation()
            onandofftext.text = getString(R.string.off)
            onlinetext.text = getString(R.string.you_re_online)
            onlinesubtext.visibility = View.GONE
            timer = DynamicCountdownTimer(updateTime, 1000)
            timer!!.setDynamicCountdownCallback(object : DynamicCountdownCallback {
                override fun onTick(l: Long) {
                }

                override fun onFinish() {
                    updateLocation()
                    timer!!.Cancel()
                    timer!!.Start()
                }
            })
            timer!!.Start()
        }
    }

    private fun enableLocation() {
        val locationRequest: LocationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 30 * 1000
        locationRequest.fastestInterval = 5 * 1000
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())
        result.addOnCompleteListener { task ->
            try {
                val response = task.getResult(ApiException::class.java)
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val resolvable = exception as ResolvableApiException
                        resolvable.startResolutionForResult(
                            this, ENABLED_LOCATION_PERMISSION_CODE
                        )
                    } catch (_: SendIntentException) {
                    } catch (_: ClassCastException) {
                    }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {}
                }
            }
        }
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(
            this, getString(R.string.please_click_back_again_to_exit), Toast.LENGTH_SHORT
        ).show()

        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            doubleBackToExitPressedOnce = false
        }, 2000)
    }

    private fun openContactUs() {
        val view = layoutInflater.inflate(R.layout.contactus, null)
        contactUs!!.setContentView(view)
        val phonenumber = view.findViewById(R.id.phonenumber) as TextView
        val email = view.findViewById(R.id.email) as TextView
        val address = view.findViewById(R.id.address) as TextView
        val closebutton = view.findViewById(R.id.closebutton) as CircularProgressButton
        phonenumber.text = supportContactUs
        email.text = supportEmail
        address.text = supportAddress
        phonenumber.paint?.isUnderlineText = true
        closebutton.setOnClickListener {
            contactUs!!.dismiss()
        }
        phonenumber.setOnClickListener {
            cu.dialPad(this, phonenumber.text.toString())
        }
        contactUs!!.show()
    }

    private fun getContactUs() {
        if (cu.isNetworkAvailable(this)) {
            loader!!.visibility = View.VISIBLE
            driverHomePageViewModel.updateCurrentLocation(
                driverId = cu.getDriverId(this)!!,
                latitude = "",
                longitude = "",
                activestatus = "$onlinestatus"
            )
        }
    }
}