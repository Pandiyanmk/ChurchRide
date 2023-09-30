package com.app.chruchridedriver.firebaseService

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.app.chruchridedriver.R
import com.app.chruchridedriver.view.GetStartedPage
import com.app.chruchridedriver.view.TimerRequestPage
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.greenrobot.eventbus.EventBus


class FirebaseMessageReceiver : FirebaseMessagingService() {
    lateinit var notificationChannel: NotificationChannel
    lateinit var notificationManager: NotificationManager
    lateinit var builder: Notification.Builder
    private val channelId = "12345"
    private val description = "Test Notification"
    override fun onNewToken(token: String) {
        val sharedPreference = getSharedPreferences("FCMID", Context.MODE_PRIVATE)
        var editor = sharedPreference.edit()
        editor.putString("Token", token)
        editor.commit()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (remoteMessage.notification != null) {
            showNotification(
                remoteMessage.notification!!.title,
                remoteMessage.notification!!.body,
                remoteMessage.notification!!.tag
            )
        }
    }

    private fun showNotification(
        title: String?, message: String?, type: String?
    ) {
        when (type) {
            "new_register" -> {
                EventBus.getDefault().post("new_register")
            }

            "doc_status" -> {
                EventBus.getDefault().post("doc_status")
            }

            "ride_request" -> {
                if (!TimerRequestPage.isTimerRequestPageActive) {
                    val intent = Intent(this, TimerRequestPage::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                } else {
                    EventBus.getDefault().post("ride_request")
                }
                return
            }
        }
        val intent = Intent(this, GetStartedPage::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        var pendingIntent: PendingIntent? = null

        pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel =
                NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
            builder = Notification.Builder(this, channelId).setContentTitle(
                title
            ).setStyle(
                Notification.BigTextStyle().bigText(message)
            ).setContentText(message).setSmallIcon(R.drawable.baseline_car_crash_24)
                .setContentIntent(pendingIntent)
        }
        builder.notification.flags = builder.notification.flags or Notification.FLAG_AUTO_CANCEL
        notificationManager.notify(12345, builder.build())
    }
}
