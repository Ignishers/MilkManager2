package `in`.ignishers.milkmanager2.ui.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.ignishers.milkmanager2.database.CustomerRepository
import `in`.ignishers.milkmanager2.database.DailyTransactionRoomDao
import `in`.ignishers.milkmanager2.database.MilkRateRepository
import `in`.ignishers.milkmanager2.database.PaymentRepository
import `in`.ignishers.milkmanager2.models.Customer
import `in`.ignishers.milkmanager2.models.DailyTransaction
import `in`.ignishers.milkmanager2.models.Payment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import `in`.ignishers.milkmanager2.ui.BaseViewModel

class PointOfSaleViewModel(
    private val customerRepository: CustomerRepository,
    private val transactionDao: DailyTransactionRoomDao,
    private val milkRateRepository: MilkRateRepository,
    private val paymentRepository: PaymentRepository
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
    
    private val _milkRate = MutableStateFlow(BigDecimal("60.0"))
    val milkRate: StateFlow<BigDecimal> = _milkRate

    val todayTransactions = MutableStateFlow<List<DailyTransaction>>(emptyList())
    val todayPayments = MutableStateFlow<List<Payment>>(emptyList())
    
    fun loadCustomer(customerId: Long) {
        launchSafe {
            _customer.value = customerRepository.getCustomerById(customerId)
            val rate = milkRateRepository.getLatestRate()?.price ?: BigDecimal("60.0")
            _milkRate.value = rate
            _quantity.value = if (_session.value == "Morning") _customer.value?.defaultQtyMorning ?: BigDecimal.ZERO else _customer.value?.defaultQtyEvening ?: BigDecimal.ZERO
            calculateAmount()
        }
        viewModelScope.launch {
            transactionDao.getTransactionsByDateFlow(customerId, _currentDate.value).collect {
                todayTransactions.value = it
            }
        }
        viewModelScope.launch {
            paymentRepository.getPaymentsByDate(customerId, _currentDate.value).collect {
                todayPayments.value = it
            }
        }
    }
    
    fun setSession(newSession: String) {
        _session.value = newSession
        val cust = _customer.value
        if (cust != null) {
            _quantity.value = if (newSession == "Morning") cust.defaultQtyMorning ?: BigDecimal.ZERO else cust.defaultQtyEvening ?: BigDecimal.ZERO
            calculateAmount()
        }
    }
    
    fun setQuantity(newQuantity: BigDecimal) {
        _quantity.value = newQuantity
        calculateAmount()
    }
    
    private fun calculateAmount() {
        _amount.value = _quantity.value.multiply(_milkRate.value).setScale(2, RoundingMode.HALF_UP)
    }
    
    fun toggleCustomerAutoEntry(enabled: Boolean) {
        launchSafe {
            val cust = _customer.value ?: return@launchSafe
            val custId = cust.id ?: return@launchSafe
            cust.autoEntryEnabled = enabled
            customerRepository.updateAutoEntry(custId, enabled)
            _customer.value = customerRepository.getCustomerById(custId)
        }
    }

    fun addQuickEntry(optionKey: String) {
        launchSafe {
            val cust = _customer.value ?: return@launchSafe
            val rate = _milkRate.value
            val (qty, amt) = when (optionKey) {
                "10rs" -> {
                    val a = BigDecimal("10")
                    val q = if (rate > BigDecimal.ZERO) a.divide(rate, 2, RoundingMode.HALF_UP) else BigDecimal.ZERO
                    q to a
                }
                "20rs" -> {
                    val a = BigDecimal("20")
                    val q = if (rate > BigDecimal.ZERO) a.divide(rate, 2, RoundingMode.HALF_UP) else BigDecimal.ZERO
                    q to a
                }
                "30rs" -> {
                    val a = BigDecimal("30")
                    val q = if (rate > BigDecimal.ZERO) a.divide(rate, 2, RoundingMode.HALF_UP) else BigDecimal.ZERO
                    q to a
                }
                "50rs" -> {
                    val a = BigDecimal("50")
                    val q = if (rate > BigDecimal.ZERO) a.divide(rate, 2, RoundingMode.HALF_UP) else BigDecimal.ZERO
                    q to a
                }
                "250ml" -> {
                    val q = BigDecimal("0.25")
                    val a = q.multiply(rate).setScale(2, RoundingMode.HALF_UP)
                    q to a
                }
                "500ml" -> {
                    val q = BigDecimal("0.50")
                    val a = q.multiply(rate).setScale(2, RoundingMode.HALF_UP)
                    q to a
                }
                "750ml" -> {
                    val q = BigDecimal("0.75")
                    val a = q.multiply(rate).setScale(2, RoundingMode.HALF_UP)
                    q to a
                }
                "1l" -> {
                    val q = BigDecimal("1.00")
                    val a = q.multiply(rate).setScale(2, RoundingMode.HALF_UP)
                    q to a
                }
                else -> return@launchSafe
            }

            val transaction = DailyTransaction(
                customerId = cust.id ?: 0L,
                date = _currentDate.value,
                session = _session.value,
                quantity = qty,
                amount = amt,
                milkType = "Extra",
                timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            )
            transactionDao.insert(transaction)
            cust.currentDue = (cust.currentDue ?: BigDecimal.ZERO).add(amt)
            customerRepository.update(cust)
            _customer.value = customerRepository.getCustomerById(cust.id ?: return@launchSafe)
        }
    }

    fun addManualEntry(
        date: String,
        session: String,
        entryType: String,
        inputValue: BigDecimal
    ) {
        launchSafe {
            val cust = _customer.value ?: return@launchSafe
            val rate = _milkRate.value
            val (qty, amt) = when (entryType) {
                "Litres" -> {
                    val q = inputValue.setScale(2, RoundingMode.HALF_UP)
                    val a = q.multiply(rate).setScale(2, RoundingMode.HALF_UP)
                    q to a
                }
                "ml" -> {
                    val q = inputValue.divide(BigDecimal("1000"), 3, RoundingMode.HALF_UP)
                    val a = q.multiply(rate).setScale(2, RoundingMode.HALF_UP)
                    q to a
                }
                "Rupee" -> {
                    val a = inputValue.setScale(2, RoundingMode.HALF_UP)
                    val q = if (rate > BigDecimal.ZERO) a.divide(rate, 2, RoundingMode.HALF_UP) else BigDecimal.ZERO
                    q to a
                }
                else -> return@launchSafe
            }

            val transaction = DailyTransaction(
                customerId = cust.id ?: 0L,
                date = date,
                session = session,
                quantity = qty,
                amount = amt,
                milkType = "Extra",
                timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            )
            transactionDao.insert(transaction)
            cust.currentDue = (cust.currentDue ?: BigDecimal.ZERO).add(amt)
            customerRepository.update(cust)
            _customer.value = customerRepository.getCustomerById(cust.id ?: return@launchSafe)
        }
    }

    fun addPayment(amount: BigDecimal, mode: String, date: String = _currentDate.value) {
        launchSafe {
            val cust = _customer.value ?: return@launchSafe
            val payment = Payment(
                customerId = cust.id ?: 0L,
                date = date,
                amount = amount,
                paymentMode = mode,
                syncUpdatedAt = System.currentTimeMillis()
            )
            paymentRepository.insert(payment)
            cust.currentDue = (cust.currentDue ?: BigDecimal.ZERO).subtract(amount)
            customerRepository.update(cust)
            _customer.value = customerRepository.getCustomerById(cust.id ?: return@launchSafe)
        }
    }

    fun addDefaultEntry() {
        launchSafe {
            val cust = _customer.value ?: return@launchSafe
            val qty = if (_session.value == "Morning") cust.defaultQtyMorning else cust.defaultQtyEvening
            if (qty == null || qty <= BigDecimal.ZERO) return@launchSafe
            val rate = _milkRate.value
            val amt = qty.multiply(rate).setScale(2, RoundingMode.HALF_UP)

            val transaction = DailyTransaction(
                customerId = cust.id ?: 0L,
                date = _currentDate.value,
                session = _session.value,
                quantity = qty,
                amount = amt,
                milkType = "Regular",
                timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            )
            transactionDao.insert(transaction)
            cust.currentDue = (cust.currentDue ?: BigDecimal.ZERO).add(amt)
            customerRepository.update(cust)
            _customer.value = customerRepository.getCustomerById(cust.id ?: return@launchSafe)
        }
    }

    fun deleteMilkTransaction(transaction: DailyTransaction) {
        launchSafe {
            val cust = _customer.value ?: return@launchSafe
            transactionDao.delete(transaction.transactionId)
            val reversedDue = (cust.currentDue ?: BigDecimal.ZERO).subtract(transaction.amount ?: BigDecimal.ZERO)
            cust.currentDue = reversedDue
            customerRepository.update(cust)
            _customer.value = customerRepository.getCustomerById(cust.id ?: return@launchSafe)
        }
    }

    fun deletePayment(payment: Payment) {
        launchSafe {
            val cust = _customer.value ?: return@launchSafe
            paymentRepository.delete(payment)
            val restoredDue = (cust.currentDue ?: BigDecimal.ZERO).add(payment.amount)
            cust.currentDue = restoredDue
            customerRepository.update(cust)
            _customer.value = customerRepository.getCustomerById(cust.id ?: return@launchSafe)
        }
    }
}
