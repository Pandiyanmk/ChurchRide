package com.app.chruchridedriver.view


import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.app.chruchridedriver.R
import com.app.chruchridedriver.adapter.TimerRequestAdapter
import com.app.chruchridedriver.data.model.RequestDetails
import com.app.chruchridedriver.databinding.RequestTimerPageBinding
import com.app.chruchridedriver.interfaces.RequestListener
import com.app.chruchridedriver.repository.MainRepository
import com.app.chruchridedriver.util.CommonUtil
import com.app.chruchridedriver.viewModel.TimerRequestPageViewModel
import com.app.chruchridedriver.viewModel.TimerRequestViewModelFactory
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import org.json.JSONTokener


class TimerRequestPage : AppCompatActivity(), RequestListener, OnMapReadyCallback {
    private lateinit var timerRequestPageViewModel: TimerRequestPageViewModel
    private val cu = CommonUtil()
    private lateinit var binding: RequestTimerPageBinding
    private val timerRequestAdapter = TimerRequestAdapter(this)
    private val requestDetails = mutableListOf<RequestDetails>()
    private lateinit var mGoogleMap: GoogleMap
    private var carMarker: Marker? = null
    private var bearing = 0f
    var currentPos = 1
    lateinit var imageLoader: Dialog

    private lateinit var mediaPlayer: MediaPlayer

    companion object {
        var isTimerRequestPageActive = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RequestTimerPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* Hiding ToolBar */
        supportActionBar?.hide()

        isTimerRequestPageActive = true

        val bookingDetails = intent.getStringExtra("bookingDetails")
        if (bookingDetails != null) {
            splitBookingDetails(bookingDetails)
        }

        EventBus.getDefault().register(this)

        /* ViewModel Initialization */
        timerRequestPageViewModel = ViewModelProvider(
            this, TimerRequestViewModelFactory(MainRepository())
        )[TimerRequestPageViewModel::class.java]

        mediaPlayer = MediaPlayer.create(applicationContext, R.raw.car_horn)
        mediaPlayer.start()
        mediaPlayer.isLooping = true

        binding.currentlocation.setOnClickListener {
            plotMarker()
        }

        timerRequestPageViewModel.errorMessage.observe(this) { errorMessage ->
            imageLoader?.let {
                if (imageLoader.isShowing) {
                    imageLoader.dismiss()
                }
            }
            cu.defaultToast(this, errorMessage)
        }

        timerRequestPageViewModel.responseContent.observe(this) { result ->
            imageLoader?.let {
                if (imageLoader.isShowing) {
                    imageLoader.dismiss()
                }
            }
            if (result.status.isNotEmpty()) {
                finish()
            } else {
                cu.defaultToast(this, getString(R.string.failed_try_again))
            }
        }

        binding.recycler.apply {
            layoutManager =
                LinearLayoutManager(this@TimerRequestPage, LinearLayoutManager.HORIZONTAL, false)
            adapter = timerRequestAdapter
            val snapHelper: SnapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(binding.recycler)
        }

        binding.cancel.setOnClickListener {
            val newPosition = binding.requestcounts.text.toString().toInt() - 1
            deleteTimer(requestDetails[newPosition].bookingId)
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.requestcount.text = "" + currentPos + "/" + requestDetails.size
        binding.requestcounts.text = "" + (currentPos)
        binding.recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val position: Int = getCurrentItem()
                    currentPos = position
                    binding.requestcount.text = "" + (currentPos + 1) + "/" + requestDetails.size
                    binding.requestcounts.text = "" + (currentPos + 1)
                    plotMarker()
                }
            }
        })
    }

    private fun getCurrentItem(): Int {
        return (binding.recycler.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
    }

    override fun onMapReady(p0: GoogleMap?) {
        mGoogleMap = p0!!
        try {
            mGoogleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this, R.raw.church_style
                )
            )
            plotMarker()
        } catch (_: Exception) {

        }
    }

    override fun start(id: Int) {
        TODO("Not yet implemented")
    }

    override fun stop(id: Int, currentMs: Long) {
        TODO("Not yet implemented")
    }

    override fun reset(id: Int) {
        TODO("Not yet implemented")
    }

    override fun delete(id: String) {
        deleteTimer(id)
    }

    override fun accept(id: String) {
        acceptRide(id)
    }

    private fun acceptRide(bookingId: String) {
        imageLoader = Dialog(this)
        imageLoader.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = layoutInflater.inflate(R.layout.acceptingride, null)
        imageLoader.setContentView(view)
        imageLoader.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        imageLoader.setCanceledOnTouchOutside(false)
        imageLoader.setCancelable(false)
        imageLoader.show()
        timerRequestPageViewModel.getAcceptResponse(bookingId, cu.getDriverId(this)!!)
    }

    private fun plotMarker() {
        val newPosition = binding.requestcounts.text.toString().toInt() - 1
        mGoogleMap.clear()

        val height = 100
        val width = 60
        val bitmap = ContextCompat.getDrawable(this, R.drawable.car_driver) as BitmapDrawable
        val b = bitmap.bitmap
        val smallMarker = Bitmap.createScaledBitmap(b, width, height, false)
        val markerOptions = MarkerOptions()
        val car = BitmapDescriptorFactory.fromBitmap(smallMarker)
        markerOptions.icon(car)
        markerOptions.anchor(
            0.5f, 0.5f
        )
        markerOptions.flat(true)
        markerOptions.position(cu.getDriverLocation(this))
        carMarker = mGoogleMap.addMarker(markerOptions)


        mGoogleMap.addMarker(
            returnMarkerOptions(
                requestDetails[newPosition].pickupLat,
                requestDetails[newPosition].pickupLong,
                R.drawable.ic_rider_location_marker
            )
        )

        carMarker!!.rotation = bearing

        val builder = LatLngBounds.Builder()
        builder.include(
            LatLng(
                requestDetails[newPosition].pickupLat, requestDetails[newPosition].pickupLong
            )
        )
        builder.include(
            cu.getDriverLocation(this)
        )
        val point = CameraUpdateFactory.newLatLngBounds(builder.build(), 48)
        anima(point)
    }

    private fun anima(point: CameraUpdate) {
        mGoogleMap.let {
            runOnUiThread { it.animateCamera(point) }
        }
    }

    private fun RecyclerView.smoothSnapToPosition(
        position: Int, snapMode: Int = LinearSmoothScroller.SNAP_TO_START
    ) {
        val smoothScroller = object : LinearSmoothScroller(this.context) {
            override fun getVerticalSnapPreference(): Int = snapMode
            override fun getHorizontalSnapPreference(): Int = snapMode
        }
        smoothScroller.targetPosition = position
        layoutManager?.startSmoothScroll(smoothScroller)
    }

    private fun deleteTimer(id: String) {
        requestDetails.remove(requestDetails.find { it.bookingId == id })
        timerRequestAdapter.submitList(requestDetails.toList())
        if (requestDetails.size == 1) {
            binding.requestcount.text = "1/" + requestDetails.size
            binding.requestcounts.text = "1"
        } else {
            binding.requestcount.text = "" + (currentPos) + "/" + requestDetails.size
            binding.requestcounts.text = "" + (currentPos)
        }
        if (requestDetails.size == 0) {
            finish()
        } else {
            plotMarker()
        }
    }

    private fun returnMarkerOptions(
        latitude: Double, longitude: Double, drawable: Int
    ): MarkerOptions {
        var height1 = 100
        var width1 = 75
        val bitmap1 = ContextCompat.getDrawable(
            this, drawable
        ) as BitmapDrawable
        val b1 = bitmap1.bitmap
        val smallMarker1 = Bitmap.createScaledBitmap(b1, width1, height1, false)
        val markerOptions1 = MarkerOptions()
        val pickup1 = BitmapDescriptorFactory.fromBitmap(smallMarker1)
        markerOptions1.icon(pickup1)
        markerOptions1.anchor(
            0.5f, 0.5f
        )
        markerOptions1.flat(true)
        val pickupLatLong = LatLng(
            latitude, longitude
        )
        markerOptions1.position(pickupLatLong)
        return markerOptions1
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        isTimerRequestPageActive = false
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(message: String) {
        if (requestDetails.size >= 3) {
            return
        }
        splitBookingDetails(message)
    }

    private fun splitBookingDetails(bookingData: String) {
        val jsonObject = JSONTokener(bookingData).nextValue() as JSONObject
        try {
            val jsonArray = jsonObject.getJSONArray("driverFcmIds")
            val estTime = jsonObject.getJSONArray("estTime")
            var distanceTime = ""
            var distanceMiles = ""
            for (i in 0 until estTime.length()) {
                val driver = estTime.getJSONObject(i).getString("driverid")
                if (driver == cu.getDriverId(this)) {
                    distanceTime = estTime.getJSONObject(i).getString("distanceTime")
                    distanceMiles = estTime.getJSONObject(i).getString("distanceKM")
                }
            }
            for (i in 0 until jsonArray.length()) {
                val id = jsonArray.getJSONObject(i).getString("ride_id")
                val pickUpLat = jsonArray.getJSONObject(i).getString("pickpup_lat")
                val pickupLong = jsonArray.getJSONObject(i).getString("pickup_long")
                val pickupAddress = jsonArray.getJSONObject(i).getString("pickup_address")
                val dropLat = jsonArray.getJSONObject(i).getString("drop_lat")
                val dropLong = jsonArray.getJSONObject(i).getString("drop_long")
                val dropAddress = jsonArray.getJSONObject(i).getString("drop_address")
                val bookingTime = jsonArray.getJSONObject(i).getString("booking_time")
                if (requestDetails.size != 0) {
                    cu.defaultToast(this, getString(R.string.new_ride_request_received))
                }
                requestDetails.add(
                    RequestDetails(
                        bookingId = id,
                        currentMs = 0,
                        isStarted = false,
                        pickupLat = pickUpLat.toDouble(),
                        pickupLong = pickupLong.toDouble(),
                        dropLat = dropLat.toDouble(),
                        dropLong = dropLong.toDouble(),
                        userName = "Prakash",
                        userRating = "4.32*",
                        estimatedTime = distanceTime,
                        estimatedMiles = distanceMiles,
                        pickupAddress = pickupAddress,
                        dropAddress = dropAddress
                    )
                )
                timerRequestAdapter.submitList(requestDetails.toList())
                binding.recycler.smoothSnapToPosition((requestDetails.size - 1))
                plotMarker()
            }
        } catch (e: Exception) {
        }

    }
}