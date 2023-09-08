package com.app.chruchridedriver.view

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.app.chruchridedriver.R


class ViewImage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_image)

        /* Hiding ToolBar */
        supportActionBar?.hide()
        val myUri = Uri.parse(intent.getStringExtra("image"))

        val backtap = findViewById<ImageView>(R.id.backtap)
        val heading = findViewById<TextView>(R.id.heading)
        val viewImage = findViewById<ImageView>(R.id.viewImage)
        heading.text = intent.getStringExtra("heading")
        viewImage?.setImageURI(myUri)
        backtap.setOnClickListener {
            finish()
        }
    }
}
