package `in`.ignishers.milkmanager2.scheduler

import android.content.Context
import `in`.ignishers.milkmanager2.database.AppDatabase
import `in`.ignishers.milkmanager2.models.DailyTransaction
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object AutoEntryManager {

    suspend fun executeAutoEntry(
        context: Context,
        session: String,
        date: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    ): Int {
        val db = AppDatabase.getDatabase(context)
        val isMasterEnabled = db.appSettingsDao().getSettingValue("auto_entry_enabled") == "true"
        if (!isMasterEnabled) return 0

        val milkRate = db.milkRateDao().getLatestRate()?.price ?: BigDecimal("60.0")
        val allCustomers = db.customerDao().getAllCustomersList()
        var processedCount = 0

        for (cust in allCustomers) {
            if (!cust.autoEntryEnabled) continue

            val qty = if (session.equals("Morning", ignoreCase = true)) {
                cust.defaultQtyMorning
            } else {
                cust.defaultQtyEvening
            }

            if (qty != null && qty > BigDecimal.ZERO) {
                val customerId = cust.id ?: continue
                val hasEntry = db.dailyTransactionDao().hasTodayEntry(customerId, date, session)
                if (!hasEntry) {
                    val amount = qty.multiply(milkRate)
                    val transaction = DailyTransaction(
                        customerId = customerId,
                        date = date,
                        session = session,
                        quantity = qty,
                        amount = amount,
                        milkType = "Regular",
                        timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                    )
                    db.dailyTransactionDao().insert(transaction)

                    cust.currentDue = (cust.currentDue ?: BigDecimal.ZERO).add(amount)
                    cust.syncUpdatedAt = System.currentTimeMillis()
                    cust.syncIsSynced = 0
                    db.customerDao().update(cust)
                    processedCount++
                }
            }
        }
        return processedCount
    }

    suspend fun checkAndRunPendingAutoEntries(context: Context): Int {
        val db = AppDatabase.getDatabase(context)
        val isMasterEnabled = db.appSettingsDao().getSettingValue("auto_entry_enabled") == "true"
        if (!isMasterEnabled) return 0

        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        var count = 0

        // Morning auto entry is scheduled for 5:00 AM
        if (hour >= 5) {
            count += executeAutoEntry(context, "Morning", today)
        }
        // Evening auto entry is scheduled for 4:00 PM (16:00)
        if (hour >= 16) {
            count += executeAutoEntry(context, "Evening", today)
        }
        return count
    }
}
