package com.app.chruchridedriver.view

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.app.chruchridedriver.R
import com.bumptech.glide.Glide


class ViewImage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_image)

        val backtap = findViewById<ImageView>(R.id.backtap)
        val heading = findViewById<TextView>(R.id.heading)
        val viewImage = findViewById<ImageView>(R.id.viewImage)
        heading.text = intent.getStringExtra("heading")
        supportActionBar?.hide()

        if (intent.getStringExtra("image")!!.startsWith("http")) {
            Glide.with(this).load(intent.getStringExtra("image"))
                .placeholder(R.drawable.imageplaceholder).into(viewImage)
        } else {
            val myUri = Uri.parse(intent.getStringExtra("image"))
            viewImage?.setImageURI(myUri)
        }

        backtap.setOnClickListener {
            finish()
        }
    }
}
