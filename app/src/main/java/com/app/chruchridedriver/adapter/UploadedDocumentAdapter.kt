package com.app.chruchridedriver.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.app.chruchridedriver.R
import com.app.chruchridedriver.data.model.UploadedDocumentX
import com.app.chruchridedriver.interfaces.ClickedAdapterInterface

class UploadedDocumentAdapter(
    private val mList: ArrayList<UploadedDocumentX>,
    private val ctx: Context,
    private var onLanguageChangeListener: ClickedAdapterInterface
) : RecyclerView.Adapter<UploadedDocumentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_uploaded_document, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val documentModel = mList[position]
        holder.documentName.text = documentModel.documentName
        if (documentModel.comment.isNotEmpty()) {
            holder.note.visibility = View.VISIBLE
            holder.note.text = "${ctx.getString(R.string.comment)} ${documentModel.comment}"
        } else {
            holder.note.visibility = View.GONE
        }

        if (position == mList.size - 1) {
            holder.lines.visibility = View.GONE
        } else {
            holder.lines.visibility = View.VISIBLE
        }

        if (documentModel.approvedstatus == "2") {
            holder.status.text = ctx.getString(R.string.verified)
            holder.status.setTextColor(Color.parseColor("#000000"))
            holder.overlay.setBackgroundResource(R.drawable.selectedbackgrounddoc)
        } else if (documentModel.approvedstatus == "1") {
            holder.status.text = ctx.getString(R.string.rejected)
            holder.status.setTextColor(Color.parseColor("#ffffff"))
            holder.overlay.setBackgroundResource(R.drawable.rejectedbackgroundcolor)
        } else {
            holder.status.text = ctx.getString(R.string.pending)
            holder.overlay.setBackgroundResource(R.drawable.pendingbackground)
            holder.status.setTextColor(Color.parseColor("#ffffff"))
        }
        holder.status.setOnClickListener {
            onLanguageChangeListener.selectedValue("" + position)
        }
        holder.arrow.setOnClickListener {
            onLanguageChangeListener.selectedValue("" + position)
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val documentName: TextView = itemView.findViewById(R.id.documentName)
        val note: TextView = itemView.findViewById(R.id.note)
        val filechoosen: TextView = itemView.findViewById(R.id.filechoosen)
        val chooseimage: TextView = itemView.findViewById(R.id.chooseimage)
        val tickImage: ImageView = itemView.findViewById(R.id.tickImage)
        val overlay: LinearLayout = itemView.findViewById(R.id.overlay)
        val status: TextView = itemView.findViewById(R.id.status)
        val lines: View = itemView.findViewById(R.id.lines)
        val arrow: ImageView = itemView.findViewById(R.id.arrow)
    }
}
