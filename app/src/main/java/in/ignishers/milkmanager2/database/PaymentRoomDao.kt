package `in`.ignishers.milkmanager2.database

import androidx.room.*
import `in`.ignishers.milkmanager2.models.Payment
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

@Dao
interface PaymentRoomDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: Payment): Long

    @Update
    suspend fun update(payment: Payment): Int

    @Delete
    suspend fun delete(payment: Payment): Int

    @Query("SELECT * FROM payment WHERE customer_id_fk = :customerId ORDER BY payment_date DESC")
    fun getPaymentsForCustomer(customerId: Long): Flow<List<Payment>>

    @Query("SELECT * FROM payment WHERE customer_id_fk = :customerId AND payment_date = :date ORDER BY payment_id DESC")
    fun getPaymentsByDate(customerId: Long, date: String): Flow<List<Payment>>

    @Query("SELECT SUM(payment_amount) FROM payment")
    suspend fun getTotalCollectedLifetime(): BigDecimal?
}
