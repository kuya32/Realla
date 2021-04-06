package com.macode.realla.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.macode.realla.R
import com.macode.realla.activities.LogInActivity
import com.macode.realla.activities.MainActivity
import com.macode.realla.firebase.FireStoreClass

class MyFirebaseMessagingService: FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

    private var fireStoreClass: FireStoreClass = FireStoreClass()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.i(TAG, "FROM: ${remoteMessage.from}")

        remoteMessage.data.isNotEmpty().let {
            Log.i(TAG, "Message data Payload: ${remoteMessage.data}")

            val title = remoteMessage.data["title"]!!
            val message = remoteMessage.data["message"]!!

            sendNotification(title, message)
        }

        remoteMessage.notification?.let {
            Log.i(TAG, "Message Notfication Body: ${it.body}")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        val sharedPreferences = this.getSharedPreferences("reallaPref", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString("fcmToken", token)
        editor.apply()
    }

    private fun sendNotification(title: String, message: String) {
        val intent = if (fireStoreClass.getCurrentUserID().isNotEmpty()) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, LogInActivity::class.java)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val channelID = "reallaNotifcationChannelID"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelID, "Channel Realla Title", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0, notificationBuilder.build())
    }
}