package com.bink.dao

import androidx.room.Room
import androidx.test.InstrumentationRegistry
import com.bink.wallet.data.BinkDatabase
import com.bink.wallet.data.MembershipCardDao
import com.bink.wallet.model.response.membership_card.MembershipCard
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.hamcrest.CoreMatchers.`is` as Is

@RunWith(JUnit4::class)
class MembershipCardDaoTest {
    companion object {
        var card1 = MembershipCard("123")
        var card2 = MembershipCard("321")
    }

    lateinit var database: BinkDatabase
    lateinit var cardsDB: MembershipCardDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getContext(),
            BinkDatabase::class.java
        ).build()
        cardsDB = database.membershipCardDao()
    }

    @After
    fun close() {
        database.close()
    }

    @Test
    fun insertCards() {

        val cardsList = ArrayList<MembershipCard>()

        cardsList.add(card1)
        cardsList.add(card2)

        runBlocking {
            cardsDB.storeAll(cardsList)

            assertThat(cardsDB.getAllAsync().size, Is(2))
        }
    }

    @Test
    fun removeCard() {
        val cardsList = ArrayList<MembershipCard>()

        cardsList.add(card1)
        cardsList.add(card2)

        runBlocking {
            cardsDB.storeAll(cardsList)

            assertThat(cardsDB.getAllAsync().size, Is(2))

            cardsDB.deleteCard("123")

            assertThat(cardsDB.getAllAsync().size, Is(1))
        }

    }

    @Test
    fun removeCards() {
        val cardsList = ArrayList<MembershipCard>()

        cardsList.add(card1)
        cardsList.add(card2)

        runBlocking {
            cardsDB.storeAll(cardsList)

            assertThat(cardsDB.getAllAsync().size, Is(2))

            cardsDB.deleteAllCards()

            assertThat(cardsDB.getAllAsync().size, Is(0))
        }

    }
}