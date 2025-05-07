package com.example.budgetbuddy.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.budgetbuddy.R
import com.example.budgetbuddy.data.models.BudgetModel
import com.example.budgetbuddy.data.models.TransactionModel
import com.example.budgetbuddy.data.models.TransactionType
import com.example.budgetbuddy.data.preferences.SharedPreferencesManager
import com.example.budgetbuddy.ui.fragments.HomeFragment
import com.example.budgetbuddy.utils.NotificationHelper
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.Toast
import android.app.AlertDialog
import android.os.Environment

class MainActivity : AppCompatActivity() {
    private lateinit var btnAddTransaction: MaterialButton
    private lateinit var btnAddBudget: MaterialButton
    private lateinit var cardBudgetSummary: CardView
    private lateinit var tvBudgetAmount: TextView
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpenses: TextView
    private lateinit var tvRemainingBudget: TextView
    private val budgetModel = BudgetModel()
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private lateinit var notificationHelper: NotificationHelper
    private var isClearingData = false

    private val budgetLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val newBudget = result.data?.getFloatExtra("budget", 0f) ?: 0f
            budgetModel.amount = newBudget
            budgetModel.saveToPreferences(this)
            updateAllSummaryValues()
        }
    }

    private val transactionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val transaction = result.data?.getSerializableExtra("transaction") as? TransactionModel
            val position = result.data?.getIntExtra("position", -1) ?: -1
            
            transaction?.let {
                if (position != -1) {
                    sharedPreferencesManager.updateTransaction(it)
                } else {
                    sharedPreferencesManager.saveTransaction(it)
                }
                
                val homeFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as? HomeFragment
                if (homeFragment != null) {
                    if (position != -1) {
                        homeFragment.updateTransaction(position, it)
                    } else {
                        homeFragment.addNewTransaction(it)
                    }
                } else {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment())
                        .commit()
                }
                updateAllSummaryValues()
            }
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_CODE = 123
        private const val STORAGE_PERMISSION_REQUEST_CODE = 456
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        notificationHelper = NotificationHelper(this)
        sharedPreferencesManager = SharedPreferencesManager(this)
        initializeViews()

        if (intent.getBooleanExtra("clear_data", false)) {
            clearAllData()
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        setupButtonListeners()
        updateAllSummaryValues()
        checkNotificationPermission()
        checkStoragePermissions()
        setupBottomNavigation()
    }

    private fun initializeViews() {
        btnAddTransaction = findViewById(R.id.btnAddTransaction)
        btnAddBudget = findViewById(R.id.btnAddBudget)
        cardBudgetSummary = findViewById(R.id.cardBudgetSummary)
        tvBudgetAmount = findViewById(R.id.tvBudgetAmount)
        tvTotalIncome = findViewById(R.id.tvTotalIncome)
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses)
        tvRemainingBudget = findViewById(R.id.tvRemainingBudget)
    }

    private fun setupButtonListeners() {
        btnAddTransaction.setOnClickListener { 
            showTransactionTypeDialog()
        }
        
        btnAddBudget.setOnClickListener { 
            addBudget() 
        }
    }

    private fun showTransactionTypeDialog() {
        try {
            val intent = Intent(this, Transaction::class.java)
            launchTransactionActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error launching transaction screen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addBudget() {
        try {
            val intent = Intent(this, Budget::class.java)
            budgetLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error launching budget screen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateAllSummaryValues() {
        lifecycleScope.launch {
            try {
                val budget = budgetModel.loadFromPreferences(this@MainActivity)
                val transactions = sharedPreferencesManager.getTransactions()
                
                val totalIncome = transactions
                    .filter { it.type == TransactionType.INCOME }
                    .sumOf { it.amount.toDouble() }
                
                val totalExpenses = transactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount.toDouble() }
                
                val remaining = budget + totalIncome.toFloat() - totalExpenses.toFloat()
                
                if (!isClearingData && !isFinishing && !isDestroyed) {
                    if (remaining < 0f) {
                        notificationHelper.showBudgetExceededAlert(remaining)
                    } else if (remaining < 5000f) {
                        notificationHelper.showBudgetWarning(remaining)
                    }
                }
                
                runOnUiThread {
                    tvBudgetAmount.text = String.format("Rs %.2f", budget)
                    tvTotalIncome.text = String.format("Rs %.2f", totalIncome)
                    tvTotalExpenses.text = String.format("Rs %.2f", totalExpenses)
                    tvRemainingBudget.text = String.format("Rs %.2f", remaining)
                    
                    tvTotalIncome.setTextColor(getColor(android.R.color.holo_green_light))
                    tvTotalExpenses.setTextColor(getColor(android.R.color.holo_red_light))
                    tvRemainingBudget.setTextColor(
                        if (remaining >= 0)
                            getColor(android.R.color.holo_green_light)
                        else
                            getColor(android.R.color.holo_red_light)
                    )
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error updating summary values", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.navigation_home

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_analysis -> {
                    val intent = Intent(this, CategorySummaryActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }
                R.id.navigation_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != 
                PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }
    }

    private fun checkStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                requestManageStoragePermission()
            }
        } else {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != 
                PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    STORAGE_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun requestManageStoragePermission() {
        try {
            val dialogView = layoutInflater.inflate(R.layout.dialog_storage_permission, null)
            
            val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setView(dialogView)
                .create()

            dialogView.findViewById<MaterialButton>(R.id.btnGrantAccess).setOnClickListener {
                dialog.dismiss()
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = android.net.Uri.parse("package:${applicationContext.packageName}")
                startActivityForResult(intent, STORAGE_PERMISSION_REQUEST_CODE)
            }

            dialogView.findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener {
                dialog.dismiss()
            }

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error requesting storage permission", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun updateSummary() {
        updateAllSummaryValues()
    }

    fun onTransactionDeleted() {
        updateAllSummaryValues()
    }

    fun launchTransactionActivity(intent: Intent) {
        transactionLauncher.launch(intent)
    }

    override fun onResume() {
        super.onResume()
        updateAllSummaryValues()
        val homeFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as? HomeFragment
        homeFragment?.loadSavedData()
    }

    private fun clearAllData() {
        try {
            isClearingData = true
            sharedPreferencesManager.clearAllPreferences()
            
            budgetModel.amount = 0f
            budgetModel.saveToPreferences(this)
            
            runOnUiThread {
                tvBudgetAmount.text = "Rs 0.00"
                tvTotalIncome.text = "Rs 0.00"
                tvTotalExpenses.text = "Rs 0.00"
                tvRemainingBudget.text = "Rs 0.00"
            }
            
            val homeFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as? HomeFragment
            homeFragment?.let {
                it.loadSavedData()
            }
            isClearingData = false
        } catch (e: Exception) {
            Toast.makeText(this, "Error clearing data", Toast.LENGTH_SHORT).show()
            isClearingData = false
        }
    }
}
