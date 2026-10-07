package com.example.prog_poe_final.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.prog_poe_final.R
import com.example.prog_poe_final.api.ApiClient
import com.example.prog_poe_final.utils.CurrentUserHolder
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")

        val user = CurrentUserHolder.getCurrentUser()
        if (user != null) {
            val body = mapOf("userId" to user.user_id!!, "fcmToken" to token)
            ApiClient.api.updateFcmToken(body)
                .enqueue(object : Callback<Map<String, Any>> {
                    override fun onResponse(
                        call: Call<Map<String, Any>>,
                        response: Response<Map<String, Any>>
                    ) {
                        Log.d("FCM", "Token uploaded: ${response.isSuccessful}")
                    }

                    override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                        Log.e("FCM", "Failed to send token: ${t.message}")
                    }
                })
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Message received: ${remoteMessage.data}")

        // 1) Try `notification` payload
        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "New Message"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: remoteMessage.data["message"]
            ?: "You received a new message"

        showNotification(title, body)
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(title: String, body: String) {
        val channelId = "chat_messages_channel"

        // Android 8+ needs a channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Chat Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for incoming chat messages"
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // change if you have a chat icon
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), notification)
    }
}
