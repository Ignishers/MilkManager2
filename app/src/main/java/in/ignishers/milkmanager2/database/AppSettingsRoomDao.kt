package `in`.ignishers.milkmanager2.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import `in`.ignishers.milkmanager2.models.AppSetting

@Dao
interface AppSettingsRoomDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: AppSetting): Long

    @Query("SELECT setting_value FROM app_settings WHERE setting_key = :key")
    suspend fun getSettingValue(key: String): String?
}
