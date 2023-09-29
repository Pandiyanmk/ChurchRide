package com.app.chruchridedriver.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.app.chruchridedriver.data.model.RequestDetails
import com.app.chruchridedriver.databinding.RequestTimerItemBinding
import com.app.chruchridedriver.interfaces.RequestListener

class TimerRequestAdapter(
    private val listener: RequestListener
) : ListAdapter<RequestDetails, RequestTimerViewHolder>(itemComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestTimerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RequestTimerItemBinding.inflate(layoutInflater, parent, false)
        return RequestTimerViewHolder(binding, listener, binding.root.context.resources)
    }

    override fun onBindViewHolder(holder: RequestTimerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private companion object {

        private val itemComparator = object : DiffUtil.ItemCallback<RequestDetails>() {

            override fun areItemsTheSame(
                oldItem: RequestDetails, newItem: RequestDetails
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: RequestDetails, newItem: RequestDetails
            ): Boolean {
                return oldItem.currentMs == newItem.currentMs && oldItem.isStarted == newItem.isStarted
            }

            override fun getChangePayload(oldItem: RequestDetails, newItem: RequestDetails) = Any()
        }
    }
}