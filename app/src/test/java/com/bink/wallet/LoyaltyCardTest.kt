package com.bink.wallet

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyViewModel
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
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
class LoyaltyCardTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    lateinit var viewModel: LoyaltyViewModel
    private lateinit var loyaltyWalletRepository: LoyaltyWalletRepository

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        this.loyaltyWalletRepository = mock(LoyaltyWalletRepository::class.java)
        this.viewModel = LoyaltyViewModel(loyaltyWalletRepository)
    }

    @Test
    fun membershipResponse_success() {
        val membershipCardResponse =
            "[ { \"id\":\"12345\", \"membership_plan\":\"112233445566\", \"payment_cards\":[], \"status\":{ \"reason_codes\":[\"X000\"], \"state\":\"authorised\" }, \"bankCard\":{ \"barcode\":\"100000000001111\", \"barcode_type\":0, \"membership_id\":\"100000000001111\", \"colour\":\"#fffffff\" }, \"images\":[ { \"id\":\"367448697676345687f654\", \"url\":\"https://images.bink.com/234jh2kk23.png\", \"type\":0, \"description\":\"BankCard image\", \"encoding\":\"png\" }, { \"id\":\"36717\", \"url\":\"https://images.bink.com/2854333.png\", \"type\":3, \"description\":\"Icon image for bankCard\", \"encoding\":\"png\" }, { \"id\":\"3324153\", \"url\":\"https://images.bink.com/hn_bronze.png\", \"type\":8, \"description\":\"Bronze Tier BankCard\", \"encoding\":\"png\" } ], \"account\":{ \"tier\":1 }, \"balances\":[ { \"value\":0, \"currency\":\"Points\", \"prefix\":\"Reward\", \"suffix\":\"Pts\", \"updated_at\":1516734463 } ], \"vouchers\": [] } ]"
        jsonToMembershipCard(membershipCardResponse)?.isNotEmpty()?.let { assertTrue(it) }
    }

    @Test
    fun membershipResponse_empty() {
        val membershipCardResponse = "[]"
        jsonToMembershipCard(membershipCardResponse)?.isNotEmpty()?.let { assertFalse(it) }
    }

    private fun jsonToMembershipCard(membershipCardResponse: String): List<MembershipCard>? {
        val listType = Types.newParameterizedType(List::class.java, MembershipCard::class.java)
        val adapter: JsonAdapter<List<MembershipCard>> = Moshi.Builder().build().adapter(listType)
        return adapter.fromJson(membershipCardResponse)
    }

    @Test
    fun fetchMembershipCards_success() {
        Mockito.`when`(loyaltyWalletRepository.retrieveMembershipCards(viewModel.membershipCardData))
            .then {
                (
                        MutableLiveData<List<MembershipCard>>().apply {
                            postValue(
                                listOf(
                                    MembershipCard(
                                        "1234",
                                        "plan",
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null
                                    )
                                )
                            )
                        }
                        )

                val observer = mock(Observer::class.java) as Observer<List<MembershipCard>>
                viewModel.membershipCardData.observeForever(observer)
                viewModel.fetchMembershipCards()
                assertNotNull(viewModel.membershipCardData.value)
            }
    }

    @Test
    fun fetchMembershipPlans_success() {
        runBlocking {
            Mockito.`when`(loyaltyWalletRepository.retrieveMembershipPlans(viewModel.membershipPlanData))
                .then {
                    (viewModel.membershipPlanData.apply {
                                postValue(
                                    asList(
                                        MembershipPlan(
                                            "1234",
                                            "plan",
                                            null,
                                            null,
                                            null,
                                            null,
                                            null
                                        )
                                    )
                                )
                            })
                    val observer = mock(Observer::class.java) as Observer<List<MembershipPlan>>
                    viewModel.membershipPlanData.observeForever(observer)
                    runBlocking {
                        viewModel.fetchMembershipPlans()
                    }
                    assertNotNull(viewModel.membershipPlanData.value)
                }
        }
    }
}
