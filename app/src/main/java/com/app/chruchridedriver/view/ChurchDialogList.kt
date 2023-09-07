package com.app.chruchridedriver.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.chruchridedriver.R
import com.app.chruchridedriver.adapter.ChurchAdapter
import com.app.chruchridedriver.data.model.Church
import com.app.chruchridedriver.interfaces.ClickedAdapterInterface

abstract class ChurchDialogList(
    context: Context,
    private var list: ArrayList<Church>,
    var onLanguageChangeListener: ClickedAdapterInterface
) : Dialog(context) {

    private var adapter: ChurchAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState ?: Bundle())
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_list, null)
        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView(view)
    }

    private fun setUpRecyclerView(view: View) {
        view.findViewById<RecyclerView>(R.id.rvList).layoutManager = LinearLayoutManager(context)
        adapter = ChurchAdapter(context, list, onLanguageChangeListener)
        view.findViewById<RecyclerView>(R.id.rvList).adapter = adapter
    }
}
