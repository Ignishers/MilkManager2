package `in`.ignishers.milkmanager2.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "global_milk_price")
data class MilkRate(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "price_id")
    val id: Int = 0,

    @ColumnInfo(name = "price_per_litre")
    val price: BigDecimal,

    @ColumnInfo(name = "effective_date")
    val effectiveDate: String,

    @ColumnInfo(name = "seller_id")
    val syncSellerId: String? = null,

    @ColumnInfo(name = "is_synced", defaultValue = "0")
    val syncIsSynced: Int? = 0,

    @ColumnInfo(name = "updated_at", defaultValue = "0")
    val syncUpdatedAt: Long? = 0,

    @ColumnInfo(name = "remote_id")
    val remoteId: Long? = null
)
