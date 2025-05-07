package com.example.coincount


import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.CalendarView
import android.widget.ProgressBar
import android.widget.TextView
import com.example.coincount.AddTransactionActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        setupUI()
        setupNavigationListeners()
        updateBudgetDisplay()
    }

    private fun setupUI() {
        // Welcome Message
        val welcomeTextView = findViewById<TextView>(R.id.welcomeTextView)
        welcomeTextView.text = "Welcome to CoinCount"

        // Current Month
        val currentMonthTextView = findViewById<TextView>(R.id.currentMonthTextView)
        val currentMonth = getCurrentMonth()
        currentMonthTextView.text = currentMonth

        // Calendar
        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        calendarView.apply {
            setOnDateChangeListener { _, year, month, dayOfMonth ->
                // Handle date selection if needed
            }
        }

        // Remaining Days
        val remainingDaysTextView = findViewById<TextView>(R.id.remainingDaysTextView)
        val remainingDays = getRemainingDaysInMonth()
        remainingDaysTextView.text = "$remainingDays days remaining this month"

        // Setup Action Cards
        setupActionCards()
    }

    private fun setupActionCards() {
        val cards = listOf(
            findViewById<CardView>(R.id.updateBudgetCard),
            findViewById<CardView>(R.id.addTransactionCard),
            findViewById<CardView>(R.id.viewTransactionsCard),
            findViewById<CardView>(R.id.viewNotificationsCard)
        )

        val activities = listOf(
            UpdateBudgetActivity::class.java,
            AddTransactionActivity::class.java,
            ViewTransactionsActivity::class.java,
            NotificationsActivity::class.java
        )

        cards.forEachIndexed { index, card ->
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.mint_green))
            card.setOnClickListener {
                startActivity(Intent(this, activities[index]))
            }
        }
    }

    private fun setupNavigationListeners() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.navigation_home

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_add_transaction -> {
                    startActivity(Intent(this, AddTransactionActivity::class.java))
                    true
                }
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

    private fun updateBudgetDisplay() {
        val currentMonth = getCurrentMonth()
        val budget = getBudget(currentMonth)
        val budgetTextView = findViewById<TextView>(R.id.budgetTextView)
        val circularBudgetProgress = findViewById<ProgressBar>(R.id.circularBudgetProgress)

        if (budget != null) {
            val totalSpent = getTotalSpent()
            val remainingBudget = budget - totalSpent
            budgetTextView.text = String.format("Rs.%.2f\nRemaining", remainingBudget)
            updateBudgetProgressBar(budget, circularBudgetProgress)
        } else {
            budgetTextView.text = "No budget\nset"
            circularBudgetProgress.progress = 0
        }
    }

    private fun updateBudgetProgressBar(budget: Float, progressBar: ProgressBar) {
        val totalSpent = getTotalSpent()
        val remainingBudget = budget - totalSpent
        val progress = ((remainingBudget / budget) * 100).toInt().coerceIn(0, 100)

        progressBar.progress = progress

        val color = when {
            progress <= 25 -> ContextCompat.getColor(this, R.color.deep_red)
            progress <= 50 -> ContextCompat.getColor(this, R.color.coral_pink)
            else -> ContextCompat.getColor(this, R.color.mint_green)
        }
        progressBar.progressDrawable.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
    }

    private fun getCurrentMonth(): String {
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun getRemainingDaysInMonth(): Int {
        val calendar = Calendar.getInstance()
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        return daysInMonth - currentDay
    }

    private fun getBudget(month: String): Float? {
        return sharedPreferences.getFloat("budget_$month", -1f).takeIf { it != -1f }
    }

    private fun getTotalSpent(): Float {
        return sharedPreferences.getStringSet("transactions", mutableSetOf())
            ?.filter { it.split("|")[0] == "Expense" }
            ?.sumOf { it.split("|")[3].toFloat().toDouble() }
            ?.toFloat()
            ?: 0f
    }

    override fun onResume() {
        super.onResume()
        updateBudgetDisplay()
    }
}