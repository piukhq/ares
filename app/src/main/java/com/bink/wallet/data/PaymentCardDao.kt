package com.bink.wallet.data

import androidx.room.*
import com.bink.wallet.model.response.payment_card.PaymentCard

@Dao
interface PaymentCardDao {
    @Query("SELECT * FROM payment_card")
    suspend fun getAllAsync(): List<PaymentCard>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun storeAll(plans: List<PaymentCard>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePaymentCard(paymentCard: PaymentCard)

    @Query("SELECT * FROM payment_card WHERE id =:paymentCardId")
    suspend fun findPaymentCardById(paymentCardId: String): PaymentCard

    @Query("DELETE FROM payment_card")
    suspend fun deleteAll()
}