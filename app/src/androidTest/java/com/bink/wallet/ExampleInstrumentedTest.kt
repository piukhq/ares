package com.bink.wallet


import androidx.lifecycle.MutableLiveData
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
//import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoyaltyWalletViewTest {

    @Rule
    @JvmField
    val activityRule = ActivityTestRule(MainActivity::class.java, true, true)

    private lateinit var viewModel: LoyaltyViewModel
    private val membershipCards = MutableLiveData<MembershipCard>()
    private val membershipPlans = MutableLiveData<MembershipPlan>()

    @Before
    fun init() {
//        viewModel = mock(LoyaltyViewModel::class.java)


    }
}