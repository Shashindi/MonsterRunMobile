package com.example.imilipocket.ui.budget

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.imilipocket.data.Category
import com.example.imilipocket.data.PreferenceManager
import com.example.imilipocket.data.Transaction
import java.util.Calendar

class BudgetViewModel(private val preferenceManager: PreferenceManager) : ViewModel() {
    private val _budget = MutableLiveData<Double>()
    val budget: LiveData<Double> = _budget

    private val _categoryBudgets = MutableLiveData<Map<Category, Double>>()
    val categoryBudgets: LiveData<Map<Category, Double>> = _categoryBudgets

    init {
        loadBudget()
        loadCategoryBudgets()
    }

    private fun loadBudget() {
            _budget.value = preferenceManager.getMonthlyBudget()
    }

    private fun loadCategoryBudgets() {
        val budgets = mutableMapOf<Category, Double>()
        Category.values().forEach { category ->
            budgets[category] = preferenceManager.getCategoryBudget(category)
        }
        _categoryBudgets.value = budgets
    }

    fun updateBudget(amount: Double) {
        preferenceManager.setMonthlyBudget(amount)
        _budget.value = amount
    }

    fun updateCategoryBudget(category: Category, amount: Double) {
        preferenceManager.setCategoryBudget(category, amount)
        val currentBudgets = _categoryBudgets.value?.toMutableMap() ?: mutableMapOf()
        currentBudgets[category] = amount
        _categoryBudgets.value = currentBudgets
    }

    fun getCategoryBudgets(): Map<Category, Double> {
        return _categoryBudgets.value ?: emptyMap()
    }

    fun getMonthlyExpenses(): Double {
        val transactions = preferenceManager.getTransactions()
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        return transactions
            .filter { transaction ->
                val transactionCalendar = Calendar.getInstance().apply {
                    timeInMillis = transaction.date
                }
                transactionCalendar.get(Calendar.MONTH) == currentMonth &&
                transactionCalendar.get(Calendar.YEAR) == currentYear
            }
            .sumOf { it.amount }
    }

    class Factory(private val preferenceManager: PreferenceManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return BudgetViewModel(preferenceManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 