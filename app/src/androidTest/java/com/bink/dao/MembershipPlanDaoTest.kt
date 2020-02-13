package com.bink.dao

import androidx.room.Room
import androidx.test.InstrumentationRegistry
import com.bink.wallet.data.BinkDatabase
import com.bink.wallet.data.MembershipPlanDao
import com.bink.wallet.model.response.membership_plan.MembershipPlan
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
class MembershipPlanDaoTest {
//    lateinit var database: BinkDatabase
//    lateinit var plansDB: MembershipPlanDao
//
//    @Before
//    fun setup() {
//        database = Room.inMemoryDatabaseBuilder(
//            InstrumentationRegistry.getContext(),
//            BinkDatabase::class.java
//        ).build()
//        plansDB = database.membershipPlanDao()
//    }
//
//    @After
//    fun close() {
//        database.close()
//    }
//
//    @Test
//    fun insertCards() {
//        getPlansFromJon()?.let {
//            runBlocking {
//                plansDB.storeAll(it)
//                assertEquals(plansDB.getAllAsync().size, 6)
//            }
//        }
//    }
//
//    @Test
//    fun getPlan() {
//        getPlansFromJon()?.let {
//            runBlocking {
//                plansDB.storeAll(it)
//                assertEquals(plansDB.getAllAsync().size, 6)
//                assertEquals(plansDB.getPlanById("194").id, "194")
//            }
//        }
//    }
//
//    private fun getPlansFromJon(): List<MembershipPlan>? {
//        val moshi = Moshi.Builder().build()
//        val listType = Types.newParameterizedType(List::class.java, MembershipPlan::class.java)
//        val adapter: JsonAdapter<List<MembershipPlan>> = moshi.adapter(listType)
//        val json =
//            InstrumentationRegistry.getContext().resources.assets.open("membershipPlans.json")
//                .bufferedReader().use {
//                    it.readText()
//                }
//        adapter.fromJson(json)?.let {
//            return it
//        }
//        return null
//    }
}