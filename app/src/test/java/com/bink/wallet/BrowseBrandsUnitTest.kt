package com.bink.wallet

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bink.wallet.scenes.browse_brands.BrowseBrandsRepository
import com.bink.wallet.scenes.browse_brands.BrowseBrandsViewModel
import com.bink.wallet.scenes.browse_brands.model.MembershipPlan
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import java.util.Arrays.asList


@RunWith(JUnit4::class)
class BrowseBrandsUnitTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    lateinit var viewModel: BrowseBrandsViewModel
    private lateinit var browseBrandsRepository: BrowseBrandsRepository

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        this.browseBrandsRepository = mock(BrowseBrandsRepository::class.java)
        this.viewModel = BrowseBrandsViewModel(browseBrandsRepository)
    }

    @Test
    fun fetchMembershipPlans_success() {
        Mockito.`when`(browseBrandsRepository.fetchMembershipPlans()).thenReturn(
            MutableLiveData<List<MembershipPlan>>().apply {
                postValue(
                    asList(
                        MembershipPlan(
                            1234,
                            "plan",
                            null,
                            null,
                            null,
                            null
                        )
                    )
                )
            }
        )

        val observer = mock(Observer::class.java) as Observer<List<MembershipPlan>>
        viewModel.membershipPlanData.observeForever(observer)
        viewModel.fetchMembershipPlans()
        assertNotNull(viewModel.membershipPlanData.value)
    }

}