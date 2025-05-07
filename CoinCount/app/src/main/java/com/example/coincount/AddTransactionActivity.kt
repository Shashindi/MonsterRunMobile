package com.example.coincount

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        NotificationUtils.createNotificationChannel(this)

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        val typeSpinner = findViewById<Spinner>(R.id.typeSpinner)
        val categorySpinner = findViewById<Spinner>(R.id.categorySpinner)
        val descriptionEditText = findViewById<EditText>(R.id.descriptionEditText)
        val amountEditText = findViewById<EditText>(R.id.amountEditText)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val viewSummaryButton = findViewById<Button>(R.id.viewSummaryButton)


        val typeAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.transaction_types,
            android.R.layout.simple_spinner_item
        )
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = typeAdapter

        typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedType = parent.getItemAtPosition(position).toString()
                updateCategorySpinner(selectedType, categorySpinner)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        viewSummaryButton.setOnClickListener {
            startActivity(Intent(this, SummaryActivity::class.java))
        }


        saveButton.setOnClickListener {
            val type = typeSpinner.selectedItem.toString()
            val category = categorySpinner.selectedItem.toString()
            val description = descriptionEditText.text.toString()
            val amount = amountEditText.text.toString().toFloatOrNull()

            if (amount != null) {
                saveTransaction(type, category, description, amount)
                updateBudget(type, amount)
                sendTransactionNotification(type, category, amount)
                Toast.makeText(this, "Transaction saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            }
        }

        // Bottom Navigation Bar
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.navigation_add_transaction

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.navigation_add_transaction -> true
                R.id.navigation_view_transactions -> {
                    startActivity(Intent(this, ViewTransactionsActivity::class.java))
                    true
                }
                R.id.navigation_notifications -> {
                    startActivity(Intent(this, NotificationsActivity::class.java))
                    true
                }
                R.id.navigation_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
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

    private fun saveTransaction(type: String, category: String, description: String, amount: Float) {
        val transactions = sharedPreferences.getStringSet("transactions", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        val transaction = "$type|$category|$description|$amount|${getCurrentDate()}"
        transactions.add(transaction)
        sharedPreferences.edit().putStringSet("transactions", transactions).apply()
    }

    private fun updateBudget(type: String, amount: Float) {
        val currentMonth = getCurrentMonth()
        val currentBudget = sharedPreferences.getFloat("budget_$currentMonth", 0f)
        if (type == "Income") {
            val updatedBudget = currentBudget + amount
            sharedPreferences.edit().putFloat("budget_$currentMonth", updatedBudget).apply()
        }
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
        val title = "New Transaction"
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