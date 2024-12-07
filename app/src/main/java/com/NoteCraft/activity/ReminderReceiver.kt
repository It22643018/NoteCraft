package com.kikunote.activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kikunote.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ReminderReceiver", "Alarm triggered! Displaying notification.")

        // Retrieve task title and content from the intent
        val taskTitle = intent.getStringExtra("TASK_TITLE") ?: "Reminder"
        val taskContent = intent.getStringExtra("TASK_CONTENT") ?: "TIME UP!"
        Log.d("ReminderReceiver", "Received Task Title: $taskTitle, Task Content: $taskContent")

        // Create Notification Channel for Android 8.0 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "reminderChannel"
            val channelName = "Reminder Notifications"
            val channelDescription = "Channel for reminder notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification
        val builder = NotificationCompat.Builder(context, "reminderChannel")
            .setSmallIcon(R.drawable.ic_notification)  // Replace with your notification icon
            .setContentTitle(taskTitle)  // Use the task title from the intent
            .setContentText(taskContent)  // Use the task content from the intent
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // Show the notification
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(1001, builder.build())  // Notification ID: 1001
    }
}
