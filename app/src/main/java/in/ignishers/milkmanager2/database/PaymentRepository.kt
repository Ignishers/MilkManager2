package `in`.ignishers.milkmanager2.database

import `in`.ignishers.milkmanager2.models.Payment
import kotlinx.coroutines.flow.Flow

class PaymentRepository(private val paymentDao: PaymentRoomDao) {
    fun getPaymentsForCustomer(customerId: Long): Flow<List<Payment>> = paymentDao.getPaymentsForCustomer(customerId)

    fun getPaymentsByDate(customerId: Long, date: String): Flow<List<Payment>> = paymentDao.getPaymentsByDate(customerId, date)

    suspend fun insert(payment: Payment): Long {
        return paymentDao.insert(payment)
    }

    suspend fun update(payment: Payment) {
        paymentDao.update(payment)
    }

    suspend fun delete(payment: Payment) {
        paymentDao.delete(payment)
    }
}
