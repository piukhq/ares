package com.bink.wallet.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bink.wallet.model.BannerDisplay

@Dao
interface BannersDisplayDao {
    @Query("SELECT * FROM dismissed_banner")
    suspend fun getDismissedBanners(): List<BannerDisplay>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBannerAsDismissed(merchantId: BannerDisplay)

    @Query("DELETE FROM dismissed_banner")
    suspend fun clearDismissedBannerList()

}