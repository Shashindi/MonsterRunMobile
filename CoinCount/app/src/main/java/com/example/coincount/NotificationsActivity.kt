package com.example.coincount


import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.coincount.AddTransactionActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class NotificationsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var notificationsListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        notificationsListView = findViewById(R.id.notificationsListView)
        val clearAllButton = findViewById<Button>(R.id.clearAllButton)

        val notifications = sharedPreferences.getStringSet("notifications", mutableSetOf())?.toMutableList() ?: mutableListOf()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, notifications)
        notificationsListView.adapter = adapter

        clearAllButton.setOnClickListener {
            clearAllNotifications()
        }

        // Bottom Navigation Bar
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.navigation_notifications

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.navigation_add_transaction -> {
                    startActivity(Intent(this, AddTransactionActivity::class.java))
                    true
                }
                R.id.navigation_view_transactions -> {
                    startActivity(Intent(this, ViewTransactionsActivity::class.java))
                    true
                }
                R.id.navigation_notifications -> true
                R.id.navigation_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun clearAllNotifications() {
        val editor = sharedPreferences.edit()
        editor.remove("notifications")
        editor.apply()

        adapter.clear()
        adapter.notifyDataSetChanged()
    }
}