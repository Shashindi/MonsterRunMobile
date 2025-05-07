package com.example.budgetbuddy.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.budgetbuddy.utils.NotificationHelper

class ExpenseReminderReceiver : BroadcastReceiver() {
    private val TAG = "ExpenseReminderReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        try {
            val notificationHelper = NotificationHelper(context)
            notificationHelper.showDailyReminder()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing daily reminder", e)
        }
    }
} 