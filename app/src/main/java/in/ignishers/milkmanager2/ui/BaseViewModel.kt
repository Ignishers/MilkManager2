package `in`.ignishers.milkmanager2.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.ignishers.milkmanager2.utils.CrashLogger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class BaseViewModel : ViewModel(), KoinComponent {
    
    // Injecting context safely for crash logging
    private val context: Context by inject()

    protected val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        CrashLogger.logException(context, exception, "Coroutine Exception in \${this.javaClass.simpleName}")
    }

    protected fun launchSafe(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(exceptionHandler) {
            block()
        }
    }
}
