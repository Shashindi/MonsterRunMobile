package com.example.budgetbuddy.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.budgetbuddy.data.models.CategoryModel
import com.example.budgetbuddy.data.models.TransactionModel
import com.example.budgetbuddy.data.models.TransactionType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPreferencesManager(private val context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREF_NAME,
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    // Currency Preferences
    fun saveCurrencyType(currency: String) {
        sharedPreferences.edit().putString(KEY_CURRENCY, currency).apply()
    }

    fun getCurrencyType(): String {
        return sharedPreferences.getString(KEY_CURRENCY, "Rs") ?: "Rs"
    }

    // Budget Preferences
    fun saveMonthlyBudget(budget: Float) {
        sharedPreferences.edit().putFloat(KEY_MONTHLY_BUDGET, budget).apply()
    }

    fun getMonthlyBudget(): Float {
        return sharedPreferences.getFloat(KEY_MONTHLY_BUDGET, 0f)
    }

    // Category Preferences
    fun saveCategory(category: CategoryModel) {
        val categories = getCategories()
        val index = categories.indexOfFirst { it.type == category.type }
        if (index != -1) {
            categories[index] = category
        } else {
            categories.add(category)
        }
        saveCategories(categories)
    }

    fun getCategories(): MutableList<CategoryModel> {
        val categoriesJson = sharedPreferences.getString(KEY_CATEGORIES, "[]")
        val type = object : TypeToken<MutableList<CategoryModel>>() {}.type
        return gson.fromJson(categoriesJson, type) ?: mutableListOf()
    }

    private fun saveCategories(categories: List<CategoryModel>) {
        val json = gson.toJson(categories)
        sharedPreferences.edit().putString(KEY_CATEGORIES, json).apply()
    }

    // Transaction Preferences
    fun saveTransaction(transaction: TransactionModel) {
        val transactions = getTransactions().toMutableList()
        transactions.add(transaction)
        saveTransactions(transactions)
    }

    fun updateTransaction(transaction: TransactionModel) {
        val transactions = getTransactions().toMutableList()
        val index = transactions.indexOfFirst { it.id == transaction.id }
        if (index != -1) {
            transactions[index] = transaction
            saveTransactions(transactions)
        }
    }

    fun deleteTransaction(transaction: TransactionModel) {
        val transactions = getTransactions().toMutableList()
        transactions.removeAll { it.id == transaction.id }
        saveTransactions(transactions)
    }

    fun getTransactions(): List<TransactionModel> {
        val json = sharedPreferences.getString(KEY_TRANSACTIONS, null)
        return if (json != null) {
            val type = object : TypeToken<List<TransactionModel>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun saveTransactions(transactions: List<TransactionModel>) {
        val json = gson.toJson(transactions)
        sharedPreferences.edit().putString(KEY_TRANSACTIONS, json).apply()
    }

    // Budget Warning System
    fun getTotalExpenses(): Float {
        return getTransactions()
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount.toDouble() }
            .toFloat()
    }

    fun getBudgetWarningThreshold(): Float {
        return getMonthlyBudget() * 0.8f
    }

    fun isBudgetWarning(): Boolean {
        return getTotalExpenses() >= getBudgetWarningThreshold()
    }

    // Passcode management
    fun savePasscode(passcode: String) {
        sharedPreferences.edit().putString(KEY_PASSCODE, passcode).apply()
    }

    fun verifyPasscode(passcode: String): Boolean {
        val savedPasscode = sharedPreferences.getString(KEY_PASSCODE, null)
        return savedPasscode == passcode
    }

    fun hasPasscode(): Boolean {
        return sharedPreferences.contains(KEY_PASSCODE)
    }

    // Clear all preferences
    fun clearAllPreferences() {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        private const val PREF_NAME = "BudgetBuddyPreferences"
        private const val KEY_CURRENCY = "currency_type"
        private const val KEY_MONTHLY_BUDGET = "monthly_budget"
        private const val KEY_CATEGORIES = "categories"
        private const val KEY_TRANSACTIONS = "transactions"
        private const val KEY_PASSCODE = "passcode"
    }
} 