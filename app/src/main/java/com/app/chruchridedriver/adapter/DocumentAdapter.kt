package com.app.chruchridedriver.adapter

import android.content.Context
import android.content.Intent
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
import com.app.chruchridedriver.data.model.Document
import com.app.chruchridedriver.interfaces.ClickedAdapterInterface
import com.app.chruchridedriver.view.ViewImage

class DocumentAdapter(
    private val mList: ArrayList<Document>,
    private val ctx: Context,
    private var onLanguageChangeListener: ClickedAdapterInterface
) : RecyclerView.Adapter<DocumentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_document, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val documentModel = mList[position]
        holder.documentName.text = documentModel.name
        holder.note.text = "${ctx.getString(R.string.note)} ${documentModel.content}"
        if (documentModel.uploaded == 1) {
            holder.filechoosen.text = ctx.getString(R.string.image_added)
            holder.tickImage.visibility = View.VISIBLE
            holder.filechoosen.setTextColor(Color.parseColor("#304A27"))
            holder.overlay.setBackgroundResource(R.drawable.selectedbackgrounddoc)
        } else {
            holder.filechoosen.text = ctx.getString(R.string.no_image_choosen)
            holder.tickImage.visibility = View.GONE
            holder.overlay.setBackgroundResource(R.drawable.edittextclickable)
            holder.filechoosen.setTextColor(Color.parseColor("#A1494343"))
        }
        holder.chooseimage.setOnClickListener {
            onLanguageChangeListener.selectedValue("" + position)
        }
        holder.filechoosen.setOnClickListener {
            if (documentModel.uploaded == 1) {
                val viewImageIntent = Intent(ctx, ViewImage::class.java)
                viewImageIntent.putExtra("image", documentModel.pathOfImage.toString())
                viewImageIntent.putExtra("heading", documentModel.name)
                ctx.startActivity(viewImageIntent)
            } else {
                Toast.makeText(ctx, ctx.getString(R.string.no_image_choosen), Toast.LENGTH_SHORT)
                    .show()
            }
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
    }
}
