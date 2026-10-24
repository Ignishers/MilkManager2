package `in`.ignishers.milkmanager2.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import `in`.ignishers.milkmanager2.scheduler.AutoEntryManager
import `in`.ignishers.milkmanager2.scheduler.AutoEntryScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AutoEntryReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val session = when (action) {
            ACTION_AUTO_ENTRY_MORNING -> "Morning"
            ACTION_AUTO_ENTRY_EVENING -> "Evening"
            else -> return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AutoEntryManager.executeAutoEntry(context, session)
                // Reschedule for next day
                if (session == "Morning") {
                    AutoEntryScheduler.scheduleMorning(context)
                } else {
                    AutoEntryScheduler.scheduleEvening(context)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_AUTO_ENTRY_MORNING = "in.ignishers.milkmanager2.AUTO_ENTRY_MORNING"
        const val ACTION_AUTO_ENTRY_EVENING = "in.ignishers.milkmanager2.AUTO_ENTRY_EVENING"
    }
}
