package com.app.chruchridedriver.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.chruchridedriver.R
import com.app.chruchridedriver.data.model.RideDetail
import com.app.chruchridedriver.interfaces.ClickedAdapterInterface

class RideDetailsAdapter(
    private val mList: ArrayList<RideDetail>,
    private val ctx: Context,
    private var onLanguageChangeListener: ClickedAdapterInterface
) : RecyclerView.Adapter<RideDetailsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ride_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val documentModel = mList[position]
        holder.ride_count.text = "${position + 1}"
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val ride_count: TextView = itemView.findViewById(R.id.ride_count)
    }
}
