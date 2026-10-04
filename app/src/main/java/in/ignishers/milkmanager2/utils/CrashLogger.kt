package `in`.ignishers.milkmanager2.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CrashLogger {
    private const val TAG = "MilkManagerCrash"

    fun logException(context: Context, exception: Throwable, customMessage: String? = null) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val logFileName = "crash_$timeStamp.log"
        
        Log.e(TAG, "Exception caught: ${customMessage ?: exception.message}", exception)

        try {
            val crashesDir = File(context.filesDir, "crashes")
            if (!crashesDir.exists()) {
                crashesDir.mkdirs()
            }

            val logFile = File(crashesDir, logFileName)
            FileWriter(logFile, true).use { writer ->
                val printWriter = PrintWriter(writer)
                printWriter.println("=== CRASH LOG ===")
                val readableTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                printWriter.println("Time: $readableTime")
                if (customMessage != null) {
                    printWriter.println("Message: $customMessage")
                }
                printWriter.println("Stacktrace:")
                exception.printStackTrace(printWriter)
                printWriter.println("=================")
            }
            Log.d(TAG, "Crash saved to: ${logFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save crash log", e)
        }
    }
}
