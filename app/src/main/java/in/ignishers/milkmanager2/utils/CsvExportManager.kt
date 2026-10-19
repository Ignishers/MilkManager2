package `in`.ignishers.milkmanager2.utils

import android.content.Context
import android.net.Uri
import `in`.ignishers.milkmanager2.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.io.PrintWriter

class CsvExportManager(private val context: Context, private val db: AppDatabase) {

    suspend fun exportCustomersToCsv(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val customers = db.customerDao().getAllCustomers().first()
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                PrintWriter(OutputStreamWriter(outputStream)).use { writer ->
                    writer.println("ID,Name,Mobile,RouteGroupID")
                    for (c in customers) {
                        writer.println("${c.id},${c.name ?: ""},${c.mobile ?: ""},${c.routeGroupId}")
                    }
                }
            }
            Result.success(customers.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportTransactionsToCsv(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val transactions = db.dailyTransactionDao().getAllTransactions().first()
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                PrintWriter(OutputStreamWriter(outputStream)).use { writer ->
                    writer.println("TransactionID,CustomerID,Date,Session,Quantity,Amount,PaymentMode")
                    for (t in transactions) {
                        writer.println("${t.transactionId},${t.customerId},${t.date ?: ""},${t.session ?: ""},${t.quantity},${t.amount}")
                    }
                }
            }
            Result.success(transactions.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
