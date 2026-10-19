package `in`.ignishers.milkmanager2.database

import androidx.room.TypeConverter
import java.math.BigDecimal

class BigDecimalConverter {
    @TypeConverter
    fun fromDouble(value: Double?): BigDecimal? {
        return value?.let { BigDecimal.valueOf(it) }
    }

    @TypeConverter
    fun toDouble(bigDecimal: BigDecimal?): Double? {
        return bigDecimal?.toDouble()
    }
}
