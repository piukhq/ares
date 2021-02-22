package com.bink.wallet.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bink.wallet.utils.LocalPointScraping.WebScrapeCredentials

@Dao
interface WebScrapeDao {
    @Query("SELECT * FROM webscrape_credentials")
    suspend fun getWebScrapeCredentials(): List<WebScrapeCredentials>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun storeWebScrapeCredentials(webScrapeCredentials: WebScrapeCredentials)

    @Query("DELETE FROM webscrape_credentials WHERE id = :membershipPlanId")
    suspend fun deleteWebScrapeCredentials(membershipPlanId: Int)

}