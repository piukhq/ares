package com.bink.wallet.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bink.wallet.model.response.membership_card.MembershipCard


@Dao
interface MembershipCardDao {
    @Query("SELECT * FROM membership_card")
    suspend fun getAllAsync(): List<MembershipCard>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun storeAll(membershipCards: List<MembershipCard>?)

    @Query("DELETE FROM membership_card WHERE id = :membershipCardId")
    suspend fun deleteCard(membershipCardId: String)

    @Query("DELETE FROM membership_card ")
    suspend fun deleteAllCards()
}