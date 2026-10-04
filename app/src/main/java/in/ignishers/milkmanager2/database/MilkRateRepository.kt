package `in`.ignishers.milkmanager2.database

import `in`.ignishers.milkmanager2.models.MilkRate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MilkRateRepository(private val dao: MilkRateRoomDao) {

    suspend fun insert(rate: MilkRate): Long = withContext(Dispatchers.IO) {
        dao.insert(rate)
    }

    suspend fun getLatestRate(): MilkRate? = withContext(Dispatchers.IO) {
        dao.getLatestRate()
    }
    
    suspend fun deleteAll(): Int = withContext(Dispatchers.IO) {
        dao.deleteAll()
    }
}
