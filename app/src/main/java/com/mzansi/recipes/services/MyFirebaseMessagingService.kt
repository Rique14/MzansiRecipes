package com.mzansi.recipes.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mzansi.recipes.MainActivity
import com.mzansi.recipes.R
import com.mzansi.recipes.di.AppModules
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    companion object {
        private const val TAG = "MyFirebaseMsgService"
        const val CHANNEL_ID_SHOPPING = "mzansi_shopping_reminders"
        const val CHANNEL_ID_RECIPES = "mzansi_recipe_alerts"
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Use the existing provider to get the user preferences
        val prefs = AppModules.provideUserPrefs(this)

        serviceScope.launch {
            val notificationsEnabled = prefs.settings.first().notificationsEnabled
            if (!notificationsEnabled) {
                Log.d(TAG, "Notifications are disabled by the user. Skipping notification.")
                return@launch
            }

            Log.d(TAG, "From: ${remoteMessage.from}")

            remoteMessage.data.isNotEmpty().let {
                Log.d(TAG, "Message data payload: " + remoteMessage.data)
            }

            remoteMessage.notification?.let {
                Log.d(TAG, "Message Notification Body: ${it.body}")
                sendNotification(it.title, it.body, remoteMessage.data["type"])
            }
        }
    }

    private fun sendRegistrationToServer(token: String?) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null && token != null) {
            val firestore = FirebaseFirestore.getInstance()
            val userDocRef = firestore.collection("users").document(userId)
            userDocRef.update("fcmToken", token)
                .addOnSuccessListener { Log.d(TAG, "FCM Token updated in Firestore for user $userId") }
                .addOnFailureListener { e -> Log.w(TAG, "Error updating FCM Token in Firestore for user $userId", e) }
        } else {
            Log.d(TAG, "User not logged in or token is null, cannot save FCM token to Firestore.")
        }
    }

    private fun sendNotification(messageTitle: String?, messageBody: String?, notificationType: String?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = when (notificationType) {
            "shopping" -> CHANNEL_ID_SHOPPING
            "recipe" -> CHANNEL_ID_RECIPES
            else -> CHANNEL_ID_RECIPES // Default if type is not specified or unknown
        }

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_pot_icon) // Replace with your app's notification icon
            .setContentTitle(messageTitle ?: "Mzansi Recipe Alert")
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shoppingChannel = NotificationChannel(
                CHANNEL_ID_SHOPPING,
                "Shopping Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(shoppingChannel)

            val recipeChannel = NotificationChannel(
                CHANNEL_ID_RECIPES,
                "Recipe Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(recipeChannel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
        Log.d(TAG, "Notification sent: Title='$messageTitle', Body='$messageBody', Channel='$channelId'")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
