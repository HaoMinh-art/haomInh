package com.example.notification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.data.VaccineRecordEntity
import java.util.Calendar

class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "vaccine_reminders"
        const val CHANNEL_NAME = "Nhắc Lịch Tiêm Chủng"
        const val CHANNEL_DESC = "Thông báo nhắc nhở ngày hẹn tiêm chủng cho trẻ em"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleVaccineReminder(record: VaccineRecordEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        
        // Schedule reminder for 8:00 AM on the scheduled day
        val cal = Calendar.getInstance()
        cal.timeInMillis = record.scheduledDate
        cal.set(Calendar.HOUR_OF_DAY, 8)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)

        val triggerTime = cal.timeInMillis
        val currentTime = System.currentTimeMillis()

        if (triggerTime <= currentTime) {
            Log.d("NotificationHelper", "Scheduled time is in the past for ${record.vaccineName}. Skipping alarm registration.")
            return
        }

        val intent = Intent(context, VaccineAlarmReceiver::class.java).apply {
            putExtra("VACCINE_ID", record.id)
            putExtra("VACCINE_NAME", record.vaccineName)
            putExtra("VACCINE_DISEASE", record.diseasePrevented)
            putExtra("VACCINE_DOSE", record.doseNumber)
        }

        // Use record.id as request code to have unique intents per vaccine record
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            record.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
            Log.d("NotificationHelper", "Successfully scheduled alarm for ${record.vaccineName} at ${cal.time}")
        } catch (e: SecurityException) {
            // In Android 12+, we need SCHEDULE_EXACT_ALARM permission, fallback to standard set
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            Log.d("NotificationHelper", "Exact alarm security exception, fallback to normal alarm for ${record.vaccineName}")
        }
    }

    fun cancelVaccineReminder(record: VaccineRecordEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, VaccineAlarmReceiver::class.java)
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            record.id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("NotificationHelper", "Cancelled alarm for vaccine record ID: ${record.id}")
        }
    }

    fun triggerImmediateNotification(title: String, message: String) {
        val intent = Intent(context, context.javaClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Using standard system alarm drawables
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
