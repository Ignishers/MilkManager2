package `in`.ignishers.milkmanager2.utils

import android.content.Context
import android.net.Uri
import `in`.ignishers.milkmanager2.database.AppDatabase
import `in`.ignishers.milkmanager2.models.Customer
import `in`.ignishers.milkmanager2.models.DailyTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.math.BigDecimal

class CsvImportManager(private val context: Context, private val db: AppDatabase) {

    suspend fun importCustomersFromCsv(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var count = 0
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readLine()
                    var line: String? = reader.readLine()
                    while (line != null) {
                        val parts = line.split(",")
                        if (parts.size >= 4) {
                            val customer = Customer(0L, parts[1].trim(), parts[2].trim(), parts[3].trim().toLongOrNull() ?: 0L)
                            db.customerDao().insert(customer)
                            count++
                        }
                        line = reader.readLine()
                    }
                }
            }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importTransactionsFromCsv(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var count = 0
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readLine()
                    var line: String? = reader.readLine()
                    while (line != null) {
                        val parts = line.split(",")
                        if (parts.size >= 5) {
                            val transaction = DailyTransaction(
                                customerId = parts[1].trim().toLongOrNull() ?: 0L,
                                date = parts[2].trim(),
                                session = parts[3].trim(),
                                quantity = parts[4].trim().toBigDecimalOrNull() ?: BigDecimal.ZERO,
                                amount = parts[5].trim().toBigDecimalOrNull() ?: BigDecimal.ZERO
                            )
                            db.dailyTransactionDao().insert(transaction)
                            count++
                        }
                        line = reader.readLine()
                    }
                }
            }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
