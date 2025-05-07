package com.example.imilipocket.ui.budget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.imilipocket.R
import com.example.imilipocket.data.Category
import com.google.android.material.textfield.TextInputEditText
import java.text.NumberFormat
import java.util.Locale

class CategoryBudgetAdapter(
    private var budgets: Map<Category, Double>,
    private val onBudgetUpdate: (Category, Double) -> Unit
) : RecyclerView.Adapter<CategoryBudgetAdapter.CategoryBudgetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryBudgetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_budget, parent, false)
        return CategoryBudgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryBudgetViewHolder, position: Int) {
        val category = budgets.keys.elementAt(position)
        val budget = budgets[category] ?: 0.0
        holder.bind(category, budget)
    }

    override fun getItemCount() = budgets.size

    fun updateBudgets(newBudgets: Map<Category, Double>) {
        budgets = newBudgets
        notifyDataSetChanged()
    }

    inner class CategoryBudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val etBudget: TextInputEditText = itemView.findViewById(R.id.etBudget)

        fun bind(category: Category, budget: Double) {
            tvCategory.text = category.name
            etBudget.setText(budget.toString())

            etBudget.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    try {
                        val newBudget = etBudget.text.toString().toDouble()
                        onBudgetUpdate(category, newBudget)
                    } catch (e: NumberFormatException) {
                        // Invalid input, keep the old value
                        etBudget.setText(budget.toString())
                    }
                }
            }
        }
    }
} 