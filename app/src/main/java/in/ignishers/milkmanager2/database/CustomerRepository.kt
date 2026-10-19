package `in`.ignishers.milkmanager2.database

import `in`.ignishers.milkmanager2.models.Customer
import kotlinx.coroutines.flow.Flow

class CustomerRepository(private val customerDao: CustomerRoomDao) {
    fun getAllCustomers(): Flow<List<Customer>> = customerDao.getAllCustomers()

    fun getCustomersByRoute(routeId: Long): Flow<List<Customer>> = customerDao.getCustomersByRoute(routeId)
    
    fun getRootCustomers(): Flow<List<Customer>> = customerDao.getRootCustomers()

    suspend fun getCustomerById(id: Long): Customer? = customerDao.getCustomerById(id)

    suspend fun insert(customer: Customer): Long {
        customer.syncIsSynced = 0
        customer.syncUpdatedAt = System.currentTimeMillis()
        return customerDao.insert(customer)
    }

    suspend fun updateSortOrder(id: Long, newOrder: Int) {
        customerDao.updateSortOrder(id, newOrder)
    }

    suspend fun update(customer: Customer) {
        customer.syncIsSynced = 0
        customer.syncUpdatedAt = System.currentTimeMillis()
        customerDao.update(customer)
    }

    suspend fun delete(id: Long) {
        customerDao.delete(id)
    }
}
