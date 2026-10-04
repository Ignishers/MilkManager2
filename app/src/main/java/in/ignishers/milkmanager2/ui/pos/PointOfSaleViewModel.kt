package `in`.ignishers.milkmanager2.ui.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.ignishers.milkmanager2.database.CustomerRepository
import `in`.ignishers.milkmanager2.database.DailyTransactionRoomDao
import `in`.ignishers.milkmanager2.models.Customer
import `in`.ignishers.milkmanager2.models.DailyTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import `in`.ignishers.milkmanager2.ui.BaseViewModel

class PointOfSaleViewModel(
    private val customerRepository: CustomerRepository,
    private val transactionDao: DailyTransactionRoomDao
) : BaseViewModel() {
    
    private val _customer = MutableStateFlow<Customer?>(null)
    val customer: StateFlow<Customer?> = _customer
    
    private val _currentDate = MutableStateFlow(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
    val currentDate: StateFlow<String> = _currentDate
    
    private val _session = MutableStateFlow("Morning")
    val session: StateFlow<String> = _session
    
    private val _quantity = MutableStateFlow(BigDecimal.ZERO)
    val quantity: StateFlow<BigDecimal> = _quantity
    
    private val _amount = MutableStateFlow(BigDecimal.ZERO)
    val amount: StateFlow<BigDecimal> = _amount
    
    private val _milkRate = MutableStateFlow(BigDecimal("60.0")) // Default rate
    val milkRate: StateFlow<BigDecimal> = _milkRate
    
    fun loadCustomer(customerId: Long) {
        launchSafe {
            _customer.value = customerRepository.getCustomerById(customerId)
            _quantity.value = if (_session.value == "Morning") _customer.value?.defaultQtyMorning else _customer.value?.defaultQtyEvening ?: BigDecimal.ZERO
            calculateAmount()
        }
    }
    
    fun setSession(newSession: String) {
        _session.value = newSession
    }
    
    fun setQuantity(newQuantity: BigDecimal) {
        _quantity.value = newQuantity
        calculateAmount()
    }
    
    fun incrementQuantity() {
        _quantity.value = _quantity.value.add(BigDecimal("0.5"))
        calculateAmount()
    }
    
    fun decrementQuantity() {
        if (_quantity.value > BigDecimal.ZERO) {
            val newQty = _quantity.value.subtract(BigDecimal("0.5"))
            _quantity.value = if (newQty < BigDecimal.ZERO) BigDecimal.ZERO else newQty
            calculateAmount()
        }
    }
    
    private fun calculateAmount() {
        _amount.value = _quantity.value.multiply(_milkRate.value)
    }
    
    fun saveEntry(onSuccess: () -> Unit) {
        launchSafe {
            val cust = _customer.value ?: return@launchSafe
            val transaction = DailyTransaction(
                customerId = cust.id ?: 0L,
                date = _currentDate.value,
                session = _session.value,
                quantity = _quantity.value,
                amount = _amount.value
            )
            transactionDao.insert(transaction)
            onSuccess()
        }
    }
}
