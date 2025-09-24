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

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMsgService"
        const val CHANNEL_ID_SHOPPING = "mzansi_shopping_reminders"
        const val CHANNEL_ID_RECIPES = "mzansi_recipe_alerts"
    }

    /**
     * Called when a new FCM registration token is generated for the device.
     * This token is used to send messages to this specific device.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        // TODO: Send this token to your server and associate it with the logged-in user.
        // For now, let's try to save it to the current user's Firestore document if they are logged in.
        sendRegistrationToServer(token)
    }

    /**
     * Called when a message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            // Handle data payload here. For example, determine the type of alert.
            // You could use a specific key in the data payload to decide which channel to use.
            val notificationType = remoteMessage.data["type"] ?: "general"
            val channelId = when (notificationType) {
                "shopping" -> CHANNEL_ID_SHOPPING
                "recipe" -> CHANNEL_ID_RECIPES
                else -> CHANNEL_ID_RECIPES // Default channel
            }
            // You can also get title and body from data payload if not using notification payload
             val dataTitle = remoteMessage.data["title"]
             val dataBody = remoteMessage.data["body"]
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            sendNotification(it.title, it.body, remoteMessage.data["type"])
        }
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
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

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageTitle FCM message title received.
     * @param messageBody FCM message body received.
     * @param notificationType Type of notification to determine channel.
     */
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

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channels if they don't exist (idempotent)
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
}
