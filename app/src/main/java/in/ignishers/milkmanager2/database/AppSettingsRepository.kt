package `in`.ignishers.milkmanager2.database

import `in`.ignishers.milkmanager2.models.AppSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppSettingsRepository(private val dao: AppSettingsRoomDao) {

    suspend fun isAutoEntryEnabled(): Boolean {
        return dao.getSettingValue(KEY_AUTO_ENTRY) == "true"
    }

    fun isAutoEntryEnabledFlow(): Flow<Boolean> {
        return dao.getSettingValueFlow(KEY_AUTO_ENTRY).map { it == "true" }
    }

    suspend fun setAutoEntryEnabled(enabled: Boolean) {
        dao.insertSetting(AppSetting(KEY_AUTO_ENTRY, enabled.toString()))
    }

    companion object {
        const val KEY_AUTO_ENTRY = "auto_entry_enabled"
    }
}
