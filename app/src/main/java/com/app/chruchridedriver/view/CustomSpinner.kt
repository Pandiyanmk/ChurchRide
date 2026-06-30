package com.app.chruchridedriver.view

import android.content.Context
import android.util.AttributeSet
import android.widget.Spinner
import androidx.appcompat.widget.AppCompatSpinner

class CustomSpinner : AppCompatSpinner {
    interface OnSpinnerEventsListener {
        fun onPopupWindowOpened(spinner: Spinner?)
        fun onPopupWindowClosed(spinner: Spinner?)
    }

    private var mListener: OnSpinnerEventsListener? = null
    private var mOpenInitiated = false

    constructor(context: Context?) : super(context!!)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    override fun performClick(): Boolean {
        mOpenInitiated = true
        if (mListener != null) {
            mListener!!.onPopupWindowOpened(this)
        }
        return super.performClick()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasBeenOpened() && hasFocus) {
            performClosedEvent()
        }
    }

    fun setSpinnerEventsListener(
        onSpinnerEventsListener: OnSpinnerEventsListener?
    ) {
        mListener = onSpinnerEventsListener
    }

    private fun performClosedEvent() {
        mOpenInitiated = false
        if (mListener != null) {
            mListener!!.onPopupWindowClosed(this)
        }
    }

    private fun hasBeenOpened(): Boolean {
        return mOpenInitiated
    }
}