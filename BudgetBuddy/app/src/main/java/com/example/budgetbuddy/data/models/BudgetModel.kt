package com.example.budgetbuddy.data.models

import android.content.Context
import android.content.SharedPreferences

// Data class to represent Budget
data class BudgetModel(
    var amount: Float = 0f  // Store the budget amount
) {
    companion object {
        private const val PREF_NAME = "BudgetBuddyPreferences"
        private const val KEY_BUDGET = "monthly_budget"
    }

    // Save the budget to SharedPreferences
    fun saveToPreferences(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat(KEY_BUDGET, amount)
        editor.apply()
    }

    // Load the budget from SharedPreferences
    fun loadFromPreferences(context: Context): Float {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getFloat(KEY_BUDGET, 0f)
    }
}
