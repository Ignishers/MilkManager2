package `in`.ignishers.milkmanager2.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import `in`.ignishers.milkmanager2.models.Customer
import `in`.ignishers.milkmanager2.models.RouteGroup
import `in`.ignishers.milkmanager2.models.DailyTransaction
import `in`.ignishers.milkmanager2.models.MilkRate
import `in`.ignishers.milkmanager2.models.AppSetting
import `in`.ignishers.milkmanager2.models.Payment

@Database(entities = [Customer::class, RouteGroup::class, DailyTransaction::class, MilkRate::class, AppSetting::class, Payment::class], version = 1, exportSchema = false)
@TypeConverters(BigDecimalConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerRoomDao
    abstract fun routeGroupDao(): RouteGroupRoomDao
    abstract fun dailyTransactionDao(): DailyTransactionRoomDao
    abstract fun milkRateDao(): MilkRateRoomDao
    abstract fun appSettingsDao(): AppSettingsRoomDao
    abstract fun paymentDao(): PaymentRoomDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "milky_mist_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
