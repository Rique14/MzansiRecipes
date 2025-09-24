package com.mzansi.recipes

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.mzansi.recipes.services.MyFirebaseMessagingService // Import your service for channel IDs

class MzansiApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Shopping Reminders Channel
            val shoppingChannel = NotificationChannel(
                MyFirebaseMessagingService.CHANNEL_ID_SHOPPING,
                "Shopping Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for your shopping list items."
                // You can set other properties like light color, vibration pattern, etc.
            }

            // Recipe Alerts Channel
            val recipeChannel = NotificationChannel(
                MyFirebaseMessagingService.CHANNEL_ID_RECIPES,
                "Recipe Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for new recipes or recipe updates."
            }

            // Register the channels with the system
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(shoppingChannel)
            notificationManager.createNotificationChannel(recipeChannel)
        }
    }
}
