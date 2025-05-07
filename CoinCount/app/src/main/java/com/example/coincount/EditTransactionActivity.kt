package com.example.coincount


import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditTransactionActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var originalTransaction: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_transaction)

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            onBackPressed()
        }

        NotificationUtils.createNotificationChannel(this)

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        val typeSpinner = findViewById<Spinner>(R.id.typeSpinner)
        val categorySpinner = findViewById<Spinner>(R.id.categorySpinner)
        val descriptionEditText = findViewById<EditText>(R.id.descriptionEditText)
        val amountEditText = findViewById<EditText>(R.id.amountEditText)
        val saveButton = findViewById<Button>(R.id.saveButton)

        originalTransaction = intent.getStringExtra("transaction") ?: return
        val parts = originalTransaction.split("|")
        val originalType = parts[0]
        val originalCategory = parts[1]
        val originalDescription = parts[2]
        val originalAmount = parts[3].toFloat()

        val typeAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.transaction_types,
            android.R.layout.simple_spinner_item
        )
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = typeAdapter

        typeSpinner.setSelection(if (originalType == "Income") 0 else 1)
        updateCategorySpinner(originalType, categorySpinner)
        categorySpinner.setSelection(getCategoryIndex(categorySpinner, originalCategory))
        descriptionEditText.setText(originalDescription)
        amountEditText.setText(originalAmount.toString())

        typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedType = parent.getItemAtPosition(position).toString()
                updateCategorySpinner(selectedType, categorySpinner)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        saveButton.setOnClickListener {
            val type = typeSpinner.selectedItem.toString()
            val category = categorySpinner.selectedItem.toString()
            val description = descriptionEditText.text.toString()
            val amount = amountEditText.text.toString().toFloatOrNull()

            if (amount != null) {
                updateTransaction(type, category, description, amount)
                updateBudget(originalType, -originalAmount)
                updateBudget(type, amount)
                sendTransactionNotification(type, category, amount)
                Toast.makeText(this, "Transaction updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateCategorySpinner(type: String, categorySpinner: Spinner) {
        val categoryArray = if (type == "Income") {
            R.array.income_categories
        } else {
            R.array.expense_categories
        }

        val categoryAdapter = ArrayAdapter.createFromResource(
            this,
            categoryArray,
            android.R.layout.simple_spinner_item
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter
    }

    private fun getCategoryIndex(categorySpinner: Spinner, category: String): Int {
        val adapter = categorySpinner.adapter as ArrayAdapter<String>
        return adapter.getPosition(category)
    }

    private fun updateTransaction(type: String, category: String, description: String, amount: Float) {
        val transactions = sharedPreferences.getStringSet("transactions", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        transactions.remove(originalTransaction)
        val updatedTransaction = "$type|$category|$description|$amount|${getCurrentDate()}"
        transactions.add(updatedTransaction)
        sharedPreferences.edit().putStringSet("transactions", transactions).apply()
    }

    private fun updateBudget(type: String, amount: Float) {
        val currentMonth = getCurrentMonth()
        val currentBudget = sharedPreferences.getFloat("budget_$currentMonth", 0f)
        val updatedBudget = if (type == "Income") currentBudget + amount else currentBudget - amount
        sharedPreferences.edit().putFloat("budget_$currentMonth", updatedBudget).apply()
    }

    private fun getCurrentMonth(): String {
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun sendTransactionNotification(type: String, category: String, amount: Float) {
        val title = "Transaction Updated"
        val message = "$type of $amount in $category"
        NotificationUtils.sendNotification(this, title, message, (System.currentTimeMillis() % Int.MAX_VALUE).toInt())

        saveNotification(title, message)
    }

    private fun saveNotification(title: String, message: String) {
        val notifications = sharedPreferences.getStringSet("notifications", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        val notification = "$title|$message|${getCurrentDate()}"
        notifications.add(notification)
        sharedPreferences.edit().putStringSet("notifications", notifications).apply()
    }
}