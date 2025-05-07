package com.example.budgetbuddy.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetbuddy.R
import com.example.budgetbuddy.data.models.TransactionModel
import com.example.budgetbuddy.data.models.CategoryModel
import com.example.budgetbuddy.data.models.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.app.DatePickerDialog
import java.util.Calendar
import com.google.android.material.textfield.TextInputEditText
import java.util.UUID

class Transaction : AppCompatActivity() {
    private var editingTransaction: TransactionModel? = null
    private val calendar = Calendar.getInstance()
    private var position: Int = -1
    private var transactionId: Long = System.currentTimeMillis()
    private var transactionType: TransactionType = TransactionType.INCOME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)
        
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        
        initializeViews()
        setupButtonListeners()
    }

    private fun initializeViews() {
        val etTitle = findViewById<TextInputEditText>(R.id.etTitle)
        val etAmount = findViewById<TextInputEditText>(R.id.etAmount)
        val spinnerCategory = findViewById<AutoCompleteTextView>(R.id.spinnerCategory)
        val etDate = findViewById<TextInputEditText>(R.id.etDate)
        val radioGroup = findViewById<com.google.android.material.button.MaterialButtonToggleGroup>(R.id.radioGroupTransactionType)

        title = "Add Transaction"
        supportActionBar?.title = "Add Transaction"

        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        var currentDate = dateFormat.format(calendar.time)
        etDate.setText(currentDate)

        etDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                R.style.CustomDatePickerDialog,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    etDate.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        radioGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                transactionType = when (checkedId) {
                    R.id.radioIncome -> TransactionType.INCOME
                    R.id.radioExpense -> TransactionType.EXPENSE
                    else -> TransactionType.INCOME
                }
                
                val categories = if (transactionType == TransactionType.INCOME) {
                    resources.getStringArray(R.array.income_categories)
                } else {
                    resources.getStringArray(R.array.expense_categories)
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
                spinnerCategory.setAdapter(adapter)
                spinnerCategory.setText("Select Category", false)
            }
        }

        radioGroup.check(R.id.radioIncome)

        editingTransaction = intent.getSerializableExtra("transaction") as? TransactionModel
        if (editingTransaction != null) {
            title = "Edit Transaction"
            supportActionBar?.title = "Update Transaction"
            
            etTitle.setText(editingTransaction?.title)
            etAmount.setText(editingTransaction?.amount.toString())
            
            calendar.timeInMillis = editingTransaction?.date ?: System.currentTimeMillis()
            currentDate = dateFormat.format(calendar.time)
            etDate.setText(currentDate)
            
            when (editingTransaction?.type) {
                TransactionType.INCOME -> radioGroup.check(R.id.radioIncome)
                TransactionType.EXPENSE -> radioGroup.check(R.id.radioExpense)
                null -> radioGroup.check(R.id.radioIncome)
            }
            
            transactionId = editingTransaction?.id ?: System.currentTimeMillis()
            transactionType = editingTransaction?.type ?: TransactionType.INCOME

            spinnerCategory.setText(editingTransaction?.category?.displayName)
        }

        val initialCategories = resources.getStringArray(R.array.income_categories)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, initialCategories)
        spinnerCategory.setAdapter(adapter)

        position = intent.getIntExtra("position", -1)
    }

    private fun setupButtonListeners() {
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSaveTransaction)
            .setOnClickListener {
                saveTransaction()
            }
    }

    private fun saveTransaction() {
        try {
            val amount = findViewById<TextInputEditText>(R.id.etAmount).text.toString().toFloatOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return
            }

            val description = findViewById<TextInputEditText>(R.id.etTitle).text.toString()
            if (description.isEmpty()) {
                Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show()
                return
            }

            val selectedCategoryText = findViewById<AutoCompleteTextView>(R.id.spinnerCategory).text.toString()

            val category = if (transactionType == TransactionType.INCOME) {
                when (selectedCategoryText) {
                    "Salary" -> CategoryModel.Category.SALARY
                    "Business" -> CategoryModel.Category.BUSINESS
                    "Investments" -> CategoryModel.Category.INVESTMENTS
                    "Gifts" -> CategoryModel.Category.GIFTS
                    "Freelance" -> CategoryModel.Category.FREELANCE
                    "Rental Income" -> CategoryModel.Category.RENTAL_INCOME
                    "Other" -> CategoryModel.Category.OTHER
                    else -> CategoryModel.Category.OTHER
                }
            } else {
                when (selectedCategoryText) {
                    "Food & Dining" -> CategoryModel.Category.FOOD
                    "Transportation" -> CategoryModel.Category.TRANSPORTATION
                    "Housing" -> CategoryModel.Category.HOUSING
                    "Utilities" -> CategoryModel.Category.UTILITIES
                    "Education" -> CategoryModel.Category.EDUCATION
                    "Entertainment" -> CategoryModel.Category.ENTERTAINMENT
                    "Health & Medical" -> CategoryModel.Category.HEALTH
                    "Shopping" -> CategoryModel.Category.SHOPPING
                    "Travel" -> CategoryModel.Category.TRAVEL
                    "Personal Care" -> CategoryModel.Category.PERSONAL_CARE
                    "Other" -> CategoryModel.Category.OTHER
                    else -> CategoryModel.Category.OTHER
                }
            }

            val transaction = TransactionModel(
                id = transactionId,
                amount = amount,
                title = description,
                category = category,
                type = transactionType,
                date = calendar.timeInMillis
            )

            val resultIntent = Intent()
            resultIntent.putExtra("transaction", transaction)
            if (position != -1) {
                resultIntent.putExtra("position", position)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving transaction", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        super.onBackPressed()
    }

    override fun onSupportNavigateUp(): Boolean {
        setResult(RESULT_CANCELED)
        onBackPressed()
        return true
    }
}
