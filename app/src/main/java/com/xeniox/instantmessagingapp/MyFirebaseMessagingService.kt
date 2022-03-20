package com.xeniox.instantmessagingapp

import android.R
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFirebaseMessagingService : FirebaseMessagingService() {
    //TODO:
    // THIS FUCKING SHIT ASS FUNCTION ONLY WORKS WHEN THE APP IS IN THE FOREGROUND AND NOT WHEN IT IS IN THE BACKGROUND
    // for some absolutely fucking unknown reason
    // its just completely stupid. This code will not work if the app is in the background - so basically it is useless
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        var notificationTitle: String? = null
        var notificationBody: String? = null

        // Check if message contains a notification payload
        if (remoteMessage.notification != null) {
            val user = FirebaseAuth.getInstance().currentUser?.uid
            val notificationTitleSubstring = notificationTitle?.substring(0, 28)
            Log.d(TAG, "Current user: $notificationTitleSubstring")
            val fromId = notificationTitle?.substring(28, notificationTitle.length)
            Log.d(TAG, "From: $fromId")
            if (FirebaseAuth.getInstance().uid == notificationTitleSubstring) {
                Log.d(TAG, "User is the same: True :)")

                // get username of fromId from the realtime database
                val ref = FirebaseDatabase.getInstance().getReference("/users/$fromId/username")
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        val username = p0.value.toString()
                        val notificationTitleUsername = "$username: $notificationBody"
                        Log.d(TAG, "notificationTitleUsername: $notificationTitleUsername")
                        val notificationIntent = Intent(this@MyFirebaseMessagingService, ConversationsActivity::class.java)
                        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        val pendingIntent = PendingIntent.getActivity(
                            this@MyFirebaseMessagingService,
                            0,
                            notificationIntent,
                            PendingIntent.FLAG_ONE_SHOT
                        )
                        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                        val notificationBuilder = NotificationCompat.Builder(this@MyFirebaseMessagingService, "default")
                            .setSmallIcon(R.drawable.ic_dialog_email)
                            .setContentTitle(notificationTitleUsername)
                            .setContentText(notificationBody)
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent)
                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.notify(1234, notificationBuilder.build())
                    }
                    override fun onCancelled(p0: DatabaseError) {
//                       TODO("Not yet implemented")
                    }
                })
            } else {
                Log.d(TAG, "Notification Title: $notificationTitle")
                Log.d(TAG, "Notification Body: $notificationBody")
                val notificationIntent = Intent(this@MyFirebaseMessagingService, ConversationsActivity::class.java)
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                val pendingIntent = PendingIntent.getActivity(
                    this@MyFirebaseMessagingService,
                    0,
                    notificationIntent,
                    PendingIntent.FLAG_ONE_SHOT
                )
                val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val notificationBuilder = NotificationCompat.Builder(this@MyFirebaseMessagingService, "default")
                    .setSmallIcon(R.drawable.ic_dialog_email)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationBody)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
        }

        // If you want to fire a local notification (that notification on the top of the phone screen)
        // you should fire it from here
        sendLocalNotification(remoteMessage.notification!!.title, remoteMessage.notification!!.body)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun sendLocalNotification(notificationTitle: String?, notificationBody: String?) {
//        val user = FirebaseAuth.getInstance().currentUser?.uid
//        val notificationTitleSubstring = notificationTitle?.substring(0, 28)
//        Log.d(TAG, "Current user: $notificationTitleSubstring")
//        val fromId = notificationTitle?.substring(28, notificationTitle.length)
//        Log.d(TAG, "From: $fromId")
//        if (FirebaseAuth.getInstance().uid == notificationTitleSubstring) {
//            Log.d(TAG, "User is the same: True :)")
//
//            // get username of fromId from the realtime database
//            val ref = FirebaseDatabase.getInstance().getReference("/users/$fromId/username")
//            ref.addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(p0: DataSnapshot) {
//                    val username = p0.value.toString()
//                    val notificationTitleUsername = "$username: $notificationBody"
//                    Log.d(TAG, "notificationTitleUsername: $notificationTitleUsername")
//                    val notificationIntent = Intent(this@MyFirebaseMessagingService, ConversationsActivity::class.java)
//                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                    val pendingIntent = PendingIntent.getActivity(
//                        this@MyFirebaseMessagingService,
//                        0,
//                        notificationIntent,
//                        PendingIntent.FLAG_ONE_SHOT
//                    )
//                    val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//                    val notificationBuilder = NotificationCompat.Builder(this@MyFirebaseMessagingService, "default")
//                        .setSmallIcon(R.drawable.ic_dialog_email)
//                        .setContentTitle(notificationTitleUsername)
//                        .setContentText(notificationBody)
//                        .setAutoCancel(true)
//                        .setSound(defaultSoundUri)
//                        .setContentIntent(pendingIntent)
//                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//                    notificationManager.notify(1234, notificationBuilder.build())
//                }
//                override fun onCancelled(p0: DatabaseError) {
////                       TODO("Not yet implemented")
//                }
//            })
//        } else {
//            Log.d(TAG, "Notification Title: $notificationTitle")
//        }
    }

    companion object {
        private const val TAG = "FirebaseMessagingService"
    }
}