package `in`.ignishers.milkmanager2.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import `in`.ignishers.milkmanager2.models.MilkRate

@Dao
interface MilkRateRoomDao {
    @Insert
    suspend fun insert(rate: MilkRate): Long

    @Query("SELECT * FROM global_milk_price ORDER BY price_id DESC LIMIT 1")
    suspend fun getLatestRate(): MilkRate?
    
    @Query("DELETE FROM global_milk_price")
    suspend fun deleteAll(): Int
}
