package com.app.chruchridedriver.adapter

import android.content.res.Resources
import android.os.CountDownTimer
import androidx.recyclerview.widget.RecyclerView
import com.app.chruchridedriver.data.model.RequestDetails
import com.app.chruchridedriver.databinding.RequestTimerItemBinding
import com.app.chruchridedriver.interfaces.RequestListener

class RequestTimerViewHolder(
    private val binding: RequestTimerItemBinding,
    private val listener: RequestListener,
    private val resources: Resources,
    private val requestTime: Int = 40
) : RecyclerView.ViewHolder(binding.root) {

    private var timer: CountDownTimer? = null

    fun bind(stopwatch: RequestDetails) {
        binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
        binding.name.text = stopwatch.userName
        binding.rating.text = stopwatch.userRating
        binding.pickup.text = stopwatch.pickupAddress
        binding.drop.text = stopwatch.dropAddress
        binding.estimatedtimes.text = "${stopwatch.estimatedTime} - ${stopwatch.estimatedMiles}"

        if (stopwatch.isStarted) {
        } else {
            startTimer(stopwatch)
        }
    }

    private fun startTimer(stopwatch: RequestDetails) {
        timer?.cancel()
        timer = getCountDownTimer(stopwatch)
        timer?.start()
    }

    private fun stopTimer(stopwatch: RequestDetails) {
        timer?.cancel()
    }

    private fun getCountDownTimer(stopwatch: RequestDetails): CountDownTimer {
        return object : CountDownTimer(PERIOD, UNIT_TEN_MS) {
            val interval = UNIT_TEN_MS

            override fun onTick(millisUntilFinished: Long) {
                stopwatch.currentMs += interval
                var time = stopwatch.currentMs.displayTime()
                if (stopwatch.currentMs.displayTime() == "00") {
                    time = time.replace("00", "0")
                } else if (stopwatch.currentMs.displayTime().startsWith("0")) {
                    time = time.replace("0", "")
                }

                val ti = requestTime - time.toInt()
                if (ti == 0) {
                    stopTimer(stopwatch)
                    listener.delete(stopwatch.bookingId)
                }

                binding.stopwatchTimer.text = "" + ti
            }

            override fun onFinish() {
                binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
            }
        }
    }

    private fun Long.displayTime(): String {
        if (this <= 0L) {
            return START_TIME
        }
        val s = this / 1000 % 60

        return "${displaySlot(s)}"
    }

    private fun displaySlot(count: Long): String {
        return if (count / 10L > 0) {
            "$count"
        } else {
            "0$count"
        }
    }

    private companion object {

        private const val START_TIME = "00:00:00:00"
        private const val UNIT_TEN_MS = 10L
        private const val PERIOD = 1000L * 60L * 60L * 24L // Day
    }
}