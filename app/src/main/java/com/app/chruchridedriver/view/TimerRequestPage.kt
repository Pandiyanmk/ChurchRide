package com.app.chruchridedriver.view


import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
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
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RequestTimerPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* Hiding ToolBar */
        supportActionBar?.hide()

        /* ViewModel Initialization */
        timerRequestPageViewModel = ViewModelProvider(
            this, TimerRequestViewModelFactory(MainRepository())
        )[TimerRequestPageViewModel::class.java]

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.recycler.apply {
            layoutManager =
                LinearLayoutManager(this@TimerRequestPage, LinearLayoutManager.HORIZONTAL, false)
            adapter = timerRequestAdapter
            val snapHelper: SnapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(binding.recycler)
        }

        binding.cancel.setOnClickListener {
            val newPosition = binding.requestcounts.text.toString().toInt() - 1
            deleteTimer(requestDetails[newPosition].id)
        }

        requestDetails.add(RequestDetails(1, 0, false))
        timerRequestAdapter.submitList(requestDetails.toList())

        Handler().postDelayed({
            requestDetails.add(RequestDetails(2, 0, false))
            timerRequestAdapter.submitList(requestDetails.toList())
            binding.recycler.smoothSnapToPosition((requestDetails.size - 1))
        }, 6000)

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
                }
            }
        })
    }

    private fun getCurrentItem(): Int {
        return (binding.recycler.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
    }

    private fun setCurrentItem(position: Int, smooth: Boolean) {
        if (smooth) binding.recycler.smoothScrollToPosition(position) else binding.recycler.scrollToPosition(
            position
        )
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
        if (carMarker == null) {
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
            carMarker!!.rotation = bearing
            val point = CameraUpdateFactory.newLatLngZoom(
                cu.getDriverLocation(this), 18f
            )
            anima(point)
        }
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
        requestDetails.remove(requestDetails.find { it.id == id })
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
        }
    }
}