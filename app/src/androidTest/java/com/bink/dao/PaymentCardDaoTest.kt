package com.bink.dao

import androidx.room.Room
import androidx.test.InstrumentationRegistry
import com.bink.wallet.data.BinkDatabase
import com.bink.wallet.data.PaymentCardDao
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class PaymentCardDaoTest {
    lateinit var database: BinkDatabase
    lateinit var plansDB: PaymentCardDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getContext(),
            BinkDatabase::class.java
        ).build()
        plansDB = database.paymentCardDao()
    }

    @After
    fun close() {
        database.close()
    }

    @Test
    fun insertCards() {
        val cardsList = getCardsFromJon()
        runBlocking {
            plansDB.storeAll(cardsList)
            Assert.assertEquals(plansDB.getAllAsync().size, 6)
        }
    }


    private fun getCardsFromJon(): List<PaymentCard> {
        val moshi = Moshi.Builder().build()
        val listType = Types.newParameterizedType(List::class.java, PaymentCard::class.java)
        val adapter: JsonAdapter<List<PaymentCard>> = moshi.adapter(listType)
        val json =
            InstrumentationRegistry.getContext().resources.assets.open("paymentCard.json")
                .bufferedReader().use {
                    it.readText()
                }
        return adapter.fromJson(json)!!
    }
}