package com.example.coincount

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView

class SummaryActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var incomeBarChart: BarChart
    private lateinit var expenseBarChart: BarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        incomeBarChart = findViewById(R.id.incomeBarChart)
        expenseBarChart = findViewById(R.id.expenseBarChart)

        setupBarCharts()
        updateChartData()
    }

    private fun setupBarCharts() {
        // Common configuration for both charts
        val charts = listOf(incomeBarChart, expenseBarChart)

        charts.forEach { chart ->
            chart.apply {
                description.isEnabled = false
                setDrawGridBackground(false)
                setPinchZoom(false)
                setScaleEnabled(false)
                legend.isEnabled = true

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    setDrawGridLines(false)
                    textSize = 12f
                    labelRotationAngle = 45f
                }

                axisLeft.apply {
                    setDrawGridLines(true)
                    axisMinimum = 0f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return "Rs.%.2f".format(value)
                        }
                    }
                }

                axisRight.isEnabled = false
            }
        }
    }

    private fun updateChartData() {
        val transactions = sharedPreferences.getStringSet("transactions", mutableSetOf()) ?: mutableSetOf()
        val (incomeCategoryData, expenseCategoryData) = processTransactions(transactions)

        // Update Income Chart
        updateCategoryChart(incomeBarChart, incomeCategoryData, Color.GREEN, "Income by Category")

        // Update Expense Chart
        updateCategoryChart(expenseBarChart, expenseCategoryData, Color.RED, "Expense by Category")
    }

    private fun updateCategoryChart(
        chart: BarChart,
        data: Map<String, Float>,
        color: Int,
        label: String
    ) {
        val entries = ArrayList<BarEntry>()
        val categories = ArrayList<String>()

        data.entries.forEachIndexed { index, entry ->
            entries.add(BarEntry(index.toFloat(), entry.value))
            categories.add(entry.key)
        }

        val dataSet = BarDataSet(entries, label).apply {
            this.color = color
            valueTextColor = Color.BLACK
            valueTextSize = 12f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "Rs.%.2f".format(value)
                }
            }
        }

        val barData = BarData(dataSet).apply {
            barWidth = 0.9f
        }

        chart.apply {
            this.data = barData
            xAxis.valueFormatter = IndexAxisValueFormatter(categories)
            setFitBars(true)
            invalidate()
        }
    }

    private fun processTransactions(transactions: Set<String>): Pair<Map<String, Float>, Map<String, Float>> {
        val incomeCategoryMap = mutableMapOf<String, Float>()
        val expenseCategoryMap = mutableMapOf<String, Float>()

        transactions.forEach { transaction ->
            val parts = transaction.split("|")
            if (parts.size == 5) {
                val type = parts[0]
                val category = parts[1]
                val amount = parts[3].toFloatOrNull() ?: 0f

                when (type) {
                    "Income" -> {
                        incomeCategoryMap[category] = (incomeCategoryMap[category] ?: 0f) + amount
                    }
                    "Expense" -> {
                        expenseCategoryMap[category] = (expenseCategoryMap[category] ?: 0f) + amount
                    }
                }
            }
        }

        return Pair(
            incomeCategoryMap.toSortedMap(),
            expenseCategoryMap.toSortedMap()
        )

    }
}