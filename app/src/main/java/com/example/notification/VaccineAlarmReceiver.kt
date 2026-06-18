package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import android.util.Log

class VaccineAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val vaccineId = intent.getIntExtra("VACCINE_ID", 0)
        val vaccineName = intent.getStringExtra("VACCINE_NAME") ?: "Vắc xin định kỳ"
        val vaccineDisease = intent.getStringExtra("VACCINE_DISEASE") ?: ""
        val doseNumber = intent.getIntExtra("VACCINE_DOSE", 1)

        Log.d("VaccineAlarmReceiver", "Received alarm trigger for vaccine: $vaccineName, ID: $vaccineId")

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        // Create launch intent for the app
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = if (launchIntent != null) {
            PendingIntent.getActivity(
                context,
                vaccineId,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            null
        }

        val diseaseText = if (vaccineDisease.isNotBlank()) " (Phòng bệnh: $vaccineDisease)" else ""
        val contentText = "Hôm nay bé có lịch tiêm chủng: $vaccineName $diseaseText. Hãy đem bé đến trung tâm y tế gần nhất."

        val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Lịch Tiêm Chủng Cho Bé Hôm Nay")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .apply {
                if (pendingIntent != null) {
                    setContentIntent(pendingIntent)
                }
            }

        notificationManager.notify(vaccineId, builder.build())
    }
}
