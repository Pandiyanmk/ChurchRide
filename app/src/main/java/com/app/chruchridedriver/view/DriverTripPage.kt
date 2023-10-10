package com.app.chruchridedriver.view


import android.Manifest
import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
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
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.chruchridedriver.R
import com.app.chruchridedriver.adapter.RideDetailsAdapter
import com.app.chruchridedriver.data.model.RideDetail
import com.app.chruchridedriver.interfaces.ClickedAdapterInterface
import com.app.chruchridedriver.location.LocationService
import com.app.chruchridedriver.repository.MainRepository
import com.app.chruchridedriver.util.CommonUtil
import com.app.chruchridedriver.util.DynamicCountdownTimer
import com.app.chruchridedriver.util.DynamicCountdownTimer.DynamicCountdownCallback
import com.app.chruchridedriver.viewModel.DriverHomePageViewModel
import com.app.chruchridedriver.viewModel.DriverHomePageViewModelFactory
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
import com.google.android.gms.maps.model.LatLngBounds
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


class DriverTripPage : AppCompatActivity(), OnMapReadyCallback, ClickedAdapterInterface {
    private lateinit var driverHomePageViewModel: DriverHomePageViewModel
    private val cu = CommonUtil()
    private lateinit var mGoogleMap: GoogleMap
    private var onlinestatus = 0
    var timer: DynamicCountdownTimer? = null
    private var doubleBackToExitPressedOnce = false
    var loader: MaterialProgressBar? = null
    private lateinit var gosettings: FloatingActionButton
    private lateinit var gotooverlay: FloatingActionButton
    private lateinit var backpermissionclick: FloatingActionButton
    private lateinit var menulay: LinearLayout
    private lateinit var rideData: RecyclerView
    private lateinit var locationoff: LinearLayout
    private lateinit var alertlayout: LinearLayout
    private lateinit var backgroundLayout: LinearLayout
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
    private var imageLoader: Dialog? = null
    private var rideDetail: ArrayList<RideDetail>? = null
    private var rideDetailsAdapter: RideDetailsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.driver_trip_page)

        /* Hiding ToolBar */
        supportActionBar?.hide()

        EventBus.getDefault().register(this)

        /* ViewModel Initialization */
        driverHomePageViewModel = ViewModelProvider(
            this, DriverHomePageViewModelFactory(MainRepository())
        )[DriverHomePageViewModel::class.java]

        contactUs = BottomSheetDialog(this)
        contactUs!!.setCancelable(true)

        onlinestatus = cu.getOnlineStatus(this)


        val mylocation: FloatingActionButton = findViewById(R.id.mylocation)
        menulay = findViewById(R.id.menulay)
        rideData = findViewById(R.id.rideData)
        loader = findViewById(R.id.loader)
        locationoff = findViewById(R.id.locationoff)
        alertlayout = findViewById(R.id.alertlayout)
        backgroundLayout = findViewById(R.id.backgroundLayout)
        gosettings = findViewById(R.id.gosettings)
        gotooverlay = findViewById(R.id.gotooverlay)
        backpermissionclick = findViewById(R.id.backpermissionclick)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mylocation.setOnClickListener {
            if (mapLoaded) {
                sendLoaction?.let {
                    val cameraPosition =
                        CameraPosition.Builder().target(LatLng(it.latitude, it.longitude)).zoom(18f)
                            .build()
                    mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                }
            }
        }

        driverHomePageViewModel.responseContent.observe(this) { result ->
            loader!!.visibility = View.GONE
            if (result.locationUpdatedData.isNotEmpty()) {
                supportEmail = result.locationUpdatedData[0].supportTeamEmail
                supportContactUs = result.locationUpdatedData[0].supportTeamCall
                supportAddress = result.locationUpdatedData[0].supportTeamAddress

                if (result.locationUpdatedData[0].isRideStatus == "0") {
                    cu.clearRideStatus(this)
                    stopLocationService()
                    val driverDocPage = Intent(this, DriverHomePage::class.java)
                    driverDocPage.putExtra("driverId", cu.getDriverId(this))
                    driverDocPage.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivity(driverDocPage)
                    finish()
                }
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
                    if (onlinestatus == 1) {
                        timer?.updateSeconds(updateTime)
                    }
                }
            }
        }
        driverHomePageViewModel.rideDetailsResponseContent.observe(this) { result ->
            dismissImageLoader()
            if (result.rideDetails.isNotEmpty()) {
                rideData.layoutManager = LinearLayoutManager(this)
                rideDetail = result.rideDetails as ArrayList<RideDetail>
                rideDetailsAdapter = RideDetailsAdapter(rideDetail!!, this, this)
                rideData.adapter = rideDetailsAdapter
                addRideMarker(rideDetail!!)
            } else {
                clearAndMoveToHomePage()
            }
        }
        driverHomePageViewModel.rideerrorMessage.observe(this) {
            dismissImageLoader()
            cu.getDriverId(this)?.let {
                imageLoader()
                driverHomePageViewModel.getRideDetails(it)
            }
        }
        driverHomePageViewModel.errorMessage.observe(this) {
            loader!!.visibility = View.GONE
        }

        gotooverlay.setOnClickListener {
            checkOverlayPermission()
        }
        backpermissionclick.setOnClickListener {
            checkBackgroundLocationPermission()
        }
        Handler().postDelayed({
            startLocationServiceWithCheck()
        }, 1000)
        checkLocationPermission()
        cu.saveRideStatus(this, 1)
        cu.getDriverId(this)?.let {
            imageLoader()
            driverHomePageViewModel.getRideDetails(it)

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
        EventBus.getDefault().unregister(this)
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
                    activestatus = "1"
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!cu.hasLocationPermission(this)) {
            cu.defaultToast(this, getString(R.string.missing_location_permission), Gravity.CENTER)
        }
        if (!cu.hasBackgroundPermission(this)) {
            backgroundLayout.visibility = View.VISIBLE
        } else {
            backgroundLayout.visibility = View.GONE
        }
        if (!Settings.canDrawOverlays(this)) {
            alertlayout.visibility = View.VISIBLE
        } else {
            alertlayout.visibility = View.GONE
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(location: Location?) {
        if (sendLoaction == null) {
            location?.let {
                sendLoaction = it
                updateLocation()
            }
        }
        location?.let {
            sendLoaction = it
            updateMarker(it)
            cu.saveLocation(this, it.latitude, it.longitude)
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

    private fun addRideMarker(rideDetails: ArrayList<RideDetail>) {
        val hashMapMarker: HashMap<String, Marker> = HashMap()
        val height = 100
        val width = 60
        val bitmap = ContextCompat.getDrawable(this, R.drawable.car_driver) as BitmapDrawable
        val b = bitmap.bitmap
        val smallMarker = Bitmap.createScaledBitmap(b, width, height, false)
        val markerOptions = MarkerOptions()

        val builder = LatLngBounds.Builder()


        for (i in 0 until rideDetails.size) {
            val bitmaps: Bitmap = getBitmapMarker("${i + 1}")!!
            val car = BitmapDescriptorFactory.fromBitmap(bitmaps)
            markerOptions.icon(car)
            markerOptions.anchor(
                0.5f, 0.5f
            )
            markerOptions.flat(true)
            markerOptions.position(
                LatLng(
                    rideDetails[i].pickpup_lat.toDouble(), rideDetails[i].pickup_long.toDouble()
                )
            )
            val marker: Marker = mGoogleMap.addMarker(markerOptions)
            hashMapMarker[rideDetails[i].ride_id] = marker
            builder.include(
                LatLng(
                    rideDetails[i].pickpup_lat.toDouble(), rideDetails[i].pickup_long.toDouble()
                )
            )
        }

        builder.include(
            cu.getDriverLocation(this)
        )
        val point = CameraUpdateFactory.newLatLngBounds(builder.build(), 48)
        anima(point)

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
            timer = DynamicCountdownTimer(updateTime, 1000)
            timer!!.setDynamicCountdownCallback(object : DynamicCountdownCallback {
                override fun onTick() {
                }

                override fun onFinish() {
                    updateLocation()
                    timer!!.Cancel()
                    timer!!.Start()
                    startLocationServiceWithCheck()
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
        cu.defaultToast(this, getString(R.string.please_click_back_again_to_exit), Gravity.CENTER)

        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            doubleBackToExitPressedOnce = false
        }, 2000)
    }

    private fun displayMessageInAlert(message: String) {
        cu.showAlert(message, this)
    }

    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, 134)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun <T> Context.isServiceRunning(service: Class<T>): Boolean {
        return (getSystemService(ACTIVITY_SERVICE) as ActivityManager).getRunningServices(Integer.MAX_VALUE)
            .any { it -> it.service.className == service.name }
    }

    private fun startLocationServiceWithCheck() {
        val manager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            stopLocationService()
            enableLocation()
        } else {
            if (cu.hasLocationPermission(this)) {
                if (!this.isServiceRunning(LocationService::class.java)) {
                    startLocationService()
                }
            }
        }
    }

    private fun imageLoader() {
        imageLoader?.let {
            it.show()
            return
        }
        imageLoader = Dialog(this)
        imageLoader!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = layoutInflater.inflate(R.layout.acceptingride, null)
        imageLoader!!.setContentView(view)
        imageLoader!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        imageLoader!!.setCanceledOnTouchOutside(false)
        imageLoader!!.setCancelable(false)
        imageLoader!!.show()
    }

    private fun dismissImageLoader() {
        imageLoader?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }

    }

    private fun clearAndMoveToHomePage() {
        cu.clearRideStatus(this)
        stopLocationService()
        val driverDocPage = Intent(this, DriverHomePage::class.java)
        driverDocPage.putExtra("driverId", cu.getDriverId(this))
        driverDocPage.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(driverDocPage)
        finish()
    }

    override fun selectedValue(name: String?) {
    }

    private fun getBitmapMarker(number: String): Bitmap? {
        val markerLayout: View = layoutInflater.inflate(R.layout.markerlayout, null)

        val markerRating = markerLayout.findViewById<View>(R.id.marker_text) as TextView
        markerRating.text = number

        markerLayout.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        markerLayout.layout(0, 0, markerLayout.measuredWidth, markerLayout.measuredHeight)

        val bitmap = Bitmap.createBitmap(
            markerLayout.measuredWidth,
            markerLayout.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        markerLayout.draw(canvas)
        return bitmap
    }
}