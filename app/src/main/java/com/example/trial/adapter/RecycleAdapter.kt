package com.example.trial.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trial.R
import com.example.trial.data.local.entities.ExpenseEntity

class RecycleAdapter(
    private var items: List<ExpenseEntity>
) : RecyclerView.Adapter<RecycleAdapter.ViewHolder>(),
    ITarget<ExpenseEntity> {

    override fun getItemCount(): Int = items.size
    override fun getItem(position: Int): ExpenseEntity = items[position]

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.txtTitle)
        val amount: TextView = view.findViewById(R.id.txtAmount)
        val category: TextView = view.findViewById(R.id.txtCategory)
        val date: TextView = view.findViewById(R.id.txtDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val expense = getItem(position)

        holder.title.text = expense.title
        holder.amount.text = "Bs ${expense.amount}"
        holder.category.text = expense.category
        holder.date.text = java.text.SimpleDateFormat("dd/MM/yyyy")
            .format(java.util.Date(expense.date))
    }

    fun updateData(newList: List<ExpenseEntity>) {
        items = newList
        notifyDataSetChanged()
    }
}
