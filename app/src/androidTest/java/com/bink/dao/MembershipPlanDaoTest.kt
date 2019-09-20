package com.bink.dao

import androidx.room.Room
import androidx.test.InstrumentationRegistry
import com.bink.wallet.data.BinkDatabase
import com.bink.wallet.data.MembershipPlanDao
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.hamcrest.CoreMatchers.`is` as Is

@RunWith(JUnit4::class)
class MembershipPlanDaoTest {
    companion object {
        var plan1 = MembershipPlan("123")
        var plan2 = MembershipPlan("321")
    }

    lateinit var database: BinkDatabase
    lateinit var plansDB: MembershipPlanDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getContext(),
            BinkDatabase::class.java
        ).build()
        plansDB = database.membershipPlanDao()
    }

    @After
    fun close() {
        database.close()
    }

    @Test
    fun insertCards() {

        val plansList = ArrayList<MembershipPlan>()

        plansList.add(plan1)
        plansList.add(plan2)

        runBlocking {
            plansDB.storeAll(plansList)

            assertThat(plansDB.getAllAsync().size, Is(2))
        }
    }

    @Test
    fun getPlan() {
        val plansList = ArrayList<MembershipPlan>()

        plansList.add(plan1)
        plansList.add(plan2)

        runBlocking {
            plansDB.storeAll(plansList)

            assertThat(plansDB.getAllAsync().size, Is(2))

            assertThat(plansDB.getPlanById("123").id, Is("123"))
        }

    }
}