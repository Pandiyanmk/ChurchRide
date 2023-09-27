package com.app.chruchridedriver.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.chruchridedriver.R
import com.app.chruchridedriver.data.model.RegisteredDriverX
import com.app.chruchridedriver.interfaces.ClickedAdapterInterface
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import java.text.ParseException
import java.text.SimpleDateFormat

class SearchDriverAdapter(
    private val context: Context,
    private var list: List<RegisteredDriverX>,
    var onLanguageChangeListener: ClickedAdapterInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.registereddriver, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list[position]

        if (holder is MyViewHolder) {
            holder.name.text = item.name
            holder.mobileno.text = item.mobileno
            holder.email.text = item.emailaddress
            holder.date.text = getDate(item.regdate)
            Glide.with(context).load(item.profilepic).placeholder(R.drawable.uploadprofile)
                .into(holder.profile_picture)
            holder.open.setOnClickListener {
                onLanguageChangeListener.selectedValue(item.id)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.name)
        val profile_picture = view.findViewById<CircleImageView>(R.id.profile_picture)
        val mobileno = view.findViewById<TextView>(R.id.mobileno)
        val email = view.findViewById<TextView>(R.id.email)
        val date = view.findViewById<TextView>(R.id.date)
        val open = view.findViewById<ImageView>(R.id.open)
    }

    private fun getDate(date: String): String {
        val input = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
        val output = SimpleDateFormat("MMMM dd, yyyy - hh:mm aa")
        return try {
            output.format(input.parse(date))
        } catch (e: ParseException) {
            e.printStackTrace()
            date
        }
    }
}
