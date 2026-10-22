package `in`.ignishers.milkmanager2.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import `in`.ignishers.milkmanager2.receiver.AutoEntryReceiver
import java.util.Calendar

object AutoEntryScheduler {

    const val REQUEST_CODE_MORNING = 1001
    const val REQUEST_CODE_EVENING = 1002

    fun scheduleAll(context: Context) {
        scheduleMorning(context)
        scheduleEvening(context)
    }

    fun cancelAll(context: Context) {
        cancelAlarm(context, REQUEST_CODE_MORNING, AutoEntryReceiver.ACTION_AUTO_ENTRY_MORNING)
        cancelAlarm(context, REQUEST_CODE_EVENING, AutoEntryReceiver.ACTION_AUTO_ENTRY_EVENING)
    }

    fun scheduleMorning(context: Context) {
        // 5:00 AM
        scheduleAlarm(context, 5, 0, AutoEntryReceiver.ACTION_AUTO_ENTRY_MORNING, REQUEST_CODE_MORNING)
    }

    fun scheduleEvening(context: Context) {
        // 4:00 PM (16:00)
        scheduleAlarm(context, 16, 0, AutoEntryReceiver.ACTION_AUTO_ENTRY_EVENING, REQUEST_CODE_EVENING)
    }

    private fun scheduleAlarm(context: Context, hour: Int, minute: Int, action: String, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AutoEntryReceiver::class.java).apply {
            this.action = action
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target.timeInMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target.timeInMillis, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target.timeInMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, target.timeInMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, target.timeInMillis, pendingIntent)
        }
    }

    private fun cancelAlarm(context: Context, requestCode: Int, action: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AutoEntryReceiver::class.java).apply {
            this.action = action
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
