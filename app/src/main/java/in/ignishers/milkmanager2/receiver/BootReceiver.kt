package `in`.ignishers.milkmanager2.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import `in`.ignishers.milkmanager2.database.AppDatabase
import `in`.ignishers.milkmanager2.scheduler.AutoEntryManager
import `in`.ignishers.milkmanager2.scheduler.AutoEntryScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val isMasterEnabled = db.appSettingsDao().getSettingValue("auto_entry_enabled") == "true"
                    if (isMasterEnabled) {
                        AutoEntryScheduler.scheduleAll(context)
                        AutoEntryManager.checkAndRunPendingAutoEntries(context)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
