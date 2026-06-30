package com.app.chruchridedriver.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.apachat.loadingbutton.core.customViews.CircularProgressButton
import com.app.chruchridedriver.R
import com.app.chruchridedriver.data.model.DriverDetailsData
import com.app.chruchridedriver.data.model.VehiclesDetailsData
import com.app.chruchridedriver.util.CommonUtil


class VehicleDetails : AppCompatActivity() {
    private val cu = CommonUtil()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.vehicle_details_page)

        /* Hiding ToolBar */
        supportActionBar?.hide()

        val driverDetailsData: DriverDetailsData? =
            intent.getSerializableExtra("driverDetails") as DriverDetailsData?

        val typev = findViewById<EditText>(R.id.vehicleTypeEditText)
        val makev = findViewById<EditText>(R.id.vehicleMakeEditText)
        val modelv = findViewById<EditText>(R.id.vehicleModelEditText)
        val yearv = findViewById<EditText>(R.id.vehicleYearEditText)
        val colorv = findViewById<EditText>(R.id.vehicleColorEditText)
        val doors = findViewById<EditText>(R.id.doors)
        val seats = findViewById<EditText>(R.id.seats)
        val backtap = findViewById<ImageView>(R.id.backtap)
        val vehiclenext = findViewById<CircularProgressButton>(R.id.vehiclenext)
        backtap.setOnClickListener {
            finish()
        }
        vehiclenext.setOnClickListener {
            if (typev.text.toString() == "" || makev!!.text.toString() == "" || modelv.text.toString() == "" || yearv.text.toString() == "" || colorv.text.toString() == "" || doors!!.text.toString() == "" || seats.text.toString() == "") {
                displayMessageInAlert(getString(R.string.all_fields_need_to_be_filled))
            } else {
                closeKeyboard()
                val vehicleDetailsData = VehiclesDetailsData(
                    type = typev.text.toString(),
                    make = makev.text.toString(),
                    model = modelv.text.toString(),
                    year = yearv.text.toString(),
                    color = colorv.text.toString(),
                    doors = doors.text.toString(),
                    seats = seats.text.toString()
                )
                val vehicleDetailsIntent = Intent(this, DocumentUpload::class.java)
                vehicleDetailsIntent.putExtra("vehicleDetails", vehicleDetailsData)
                vehicleDetailsIntent.putExtra("driverDetails", driverDetailsData)
                startActivity(vehicleDetailsIntent)
            }
        }
    }

    private fun displayMessageInAlert(message: String) {
        cu.showAlert(message, this)
    }

    private fun closeKeyboard() {
        this.currentFocus?.let { view ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
