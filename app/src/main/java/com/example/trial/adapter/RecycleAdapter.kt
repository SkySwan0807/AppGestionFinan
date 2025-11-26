package com.example.trial.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trial.R
import com.example.trial.adapter.ITarget

class RecycleAdapter(
    private val items: List<String>  // Ejemplo simple (puede ser cualquier objeto)
) : RecyclerView.Adapter<RecycleAdapter.ViewHolder>(),
    ITarget<String> {

    // -----------------------
    // Implementación de ITarget
    // -----------------------
    override fun getItemCount(): Int = items.size

    override fun getItem(position: Int): String = items[position]

    // -----------------------
    // Adapter del RecyclerView
    // -----------------------
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtItem: TextView = view.findViewById(R.id.item_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recycle, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txtItem.text = getItem(position)
    }
}
