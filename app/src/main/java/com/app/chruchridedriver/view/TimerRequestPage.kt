package com.app.chruchridedriver.view


import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
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

        EventBus.getDefault().register(this)

        /* ViewModel Initialization */
        timerRequestPageViewModel = ViewModelProvider(
            this, TimerRequestViewModelFactory(MainRepository())
        )[TimerRequestPageViewModel::class.java]

        mediaPlayer = MediaPlayer.create(applicationContext, R.raw.car_horn)
        mediaPlayer.start()
        mediaPlayer.isLooping = true

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

        requestDetails.add(
            RequestDetails(
                bookingId = 1,
                currentMs = 0,
                isStarted = false,
                pickupLat = 13.1267709,
                pickupLong = 80.1438799,
                dropLat = 13.0500,
                dropLong = 80.2121,
                userName = "Pandiyan",
                userRating = "4.52*",
                estimatedTime = "3 min",
                estimatedMiles = "0.4 mi",
                pickupAddress = "Lorem Ipsum has been the industry's standard dummy text ever since the 1500s",
                dropAddress = "Lorem Ipsum has been the industry's standard dummy text ever since the 1500s"
            )
        )
        timerRequestAdapter.submitList(requestDetails.toList())
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

    override fun delete(id: Int) {
        deleteTimer(id)
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

    private fun deleteTimer(id: Int) {
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
    fun onMessageEvent(message: String?) {
        if (requestDetails.size >= 3) {
            return
        }
        message?.let {
            val myRandomValues = (100..500).random()
            Toast.makeText(this, getString(R.string.new_ride_request_received), Toast.LENGTH_SHORT)
                .show()
            requestDetails.add(
                RequestDetails(
                    bookingId = myRandomValues,
                    currentMs = 0,
                    isStarted = false,
                    pickupLat = 13.1143,
                    pickupLong = 80.1548,
                    dropLat = 13.1322822,
                    dropLong = 80.1510225,
                    userName = "Prakash",
                    userRating = "4.32*",
                    estimatedTime = "5 min",
                    estimatedMiles = "0.7 mi",
                    pickupAddress = "Lorem Ipsum has been the industry's standard dummy text ever since the 1500s",
                    dropAddress = "Lorem Ipsum has been the industry's standard dummy text ever since the 1500s"
                )
            )
            timerRequestAdapter.submitList(requestDetails.toList())
            binding.recycler.smoothSnapToPosition((requestDetails.size - 1))
        }
    }
}