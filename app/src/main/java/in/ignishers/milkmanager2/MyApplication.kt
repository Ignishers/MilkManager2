package `in`.ignishers.milkmanager2

import android.app.Application
import `in`.ignishers.milkmanager2.di.appModule
import `in`.ignishers.milkmanager2.utils.CrashLogger
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Setup Global Crash Handler
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            CrashLogger.logException(applicationContext, throwable, "Fatal Crash on Thread: \${thread.name}")
            // Let the system handle the crash after we've logged it
            defaultHandler?.uncaughtException(thread, throwable)
        }

        // Start Koin Dependency Injection
        startKoin {
            androidContext(this@MyApplication)
            modules(appModule)
        }
    }
}
