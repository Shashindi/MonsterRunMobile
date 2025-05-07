package com.example.coincount

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

class UpdateBudgetActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_budget)


        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            onBackPressed()
        }

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        val currentMonthTextView = findViewById<TextView>(R.id.currentMonthTextView)
        val budgetEditText = findViewById<EditText>(R.id.budgetEditText)
        val saveButton = findViewById<Button>(R.id.saveButton)

        val currentMonth = getCurrentMonth()
        currentMonthTextView.text = "Current Month: $currentMonth"

        saveButton.setOnClickListener {
            val budget = budgetEditText.text.toString().toFloatOrNull()
            if (budget != null) {
                saveBudget(currentMonth, budget)
                Toast.makeText(this, "Budget saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Please enter a valid budget", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentMonth(): String {
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun saveBudget(month: String, budget: Float) {
        val editor = sharedPreferences.edit()
        editor.putFloat("budget_$month", budget)
        editor.apply()
    }
}