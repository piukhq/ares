package com.bink.dao

import androidx.room.Room
import androidx.test.InstrumentationRegistry
import com.bink.wallet.data.BinkDatabase
import com.bink.wallet.data.MembershipCardDao
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


//@RunWith(JUnit4::class)
class MembershipCardDaoTest {

//    lateinit var database: BinkDatabase
//    lateinit var cardsDB: MembershipCardDao
//
//    @Before
//    fun setup() {
//        database = Room.inMemoryDatabaseBuilder(
//            InstrumentationRegistry.getContext(),
//            BinkDatabase::class.java
//        ).build()
//        cardsDB = database.membershipCardDao()
//    }
//
//    @After
//    fun close() {
//        database.close()
//    }
//
//    @Test
//    fun insertCards() {
//        getCardsFromJon()?.let {
//            runBlocking {
//                cardsDB.storeAll(it)
//                assertEquals(cardsDB.getAllAsync().size, 4)
//            }
//        }
//    }
//
//    @Test
//    fun removeCard() {
//        getCardsFromJon()?.let {
//            runBlocking {
//                cardsDB.storeAll(it)
//
//                assertEquals(cardsDB.getAllAsync().size, 4)
//
//                cardsDB.deleteCard("14338")
//
//                assertEquals(cardsDB.getAllAsync().size, 3)
//            }
//        }
//    }
//
//    @Test
//    fun removeCards() {
//        getCardsFromJon()?.let {
//            runBlocking {
//                cardsDB.storeAll(it)
//
//                assertEquals(cardsDB.getAllAsync().size, 4)
//
//                cardsDB.deleteAllCards()
//
//                assertEquals(cardsDB.getAllAsync().size, 0)
//            }
//        }
//    }
//
//    private fun getCardsFromJon(): List<MembershipCard>? {
//        val moshi = Moshi.Builder().build()
//        val listType = Types.newParameterizedType(List::class.java, MembershipCard::class.java)
//        val adapter: JsonAdapter<List<MembershipCard>> = moshi.adapter(listType)
//        val json =
//            InstrumentationRegistry.getContext().resources.assets.open("membershipCards.json")
//                .bufferedReader().use {
//                    it.readText()
//                }
//        adapter.fromJson(json)?.let {
//            return it
//        }
//        return null
//    }
}