package com.bink.wallet.scenes.add_join

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddJoinFragmentBinding
import com.bink.wallet.modal.generic.GenericModalParameters
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.FirebaseEvents.STORE_LINK_VIEW
import com.bink.wallet.utils.FirebaseEvents.getFirebaseIdentifier
import com.bink.wallet.utils.enums.CardType
import com.bink.wallet.utils.enums.SignUpFormType
import com.bink.wallet.utils.enums.TypeOfField
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.toolbar.FragmentToolbar
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddJoinFragment : BaseFragment<AddJoinViewModel, AddJoinFragmentBinding>() {

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    override val layoutRes: Int
        get() = R.layout.add_join_fragment

    private val args: AddJoinFragmentArgs by navArgs()

    private var isFromJoinCard = false

    private var isRetryJourney = false

    private var isFromNoReasonCodes = false

    private var membershipCardId: String? = null

    private var currentMembershipPlan: MembershipPlan? = null

    override val viewModel: AddJoinViewModel by viewModel()

    override fun onResume() {
        super.onResume()
        logScreenView(STORE_LINK_VIEW)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        with(args) {
            this@AddJoinFragment.currentMembershipPlan = currentMembershipPlan
            this@AddJoinFragment.isFromJoinCard = isFromJoinCard
            this@AddJoinFragment.isRetryJourney = isRetryJourney
            this@AddJoinFragment.isFromNoReasonCodes = isFromNoReasonCodes
            this@AddJoinFragment.membershipCardId = membershipCardId
        }

        viewModel.fetchLocalPaymentCards()
        runBlocking {
            viewModel.getPaymentCards()
        }

        viewModel.membershipPlan.value = currentMembershipPlan
        binding.item = currentMembershipPlan

        when (currentMembershipPlan?.feature_set?.card_type) {
            CardType.STORE.type -> {
                binding.addJoinViewImage.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_icons_svl_view_inactive
                    )
                )
                binding.addJoinViewDescription.text =
                    getString(R.string.add_join_inactive_view_description)
                binding.addJoinLinkImage.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_icons_svl_link_inactive
                    )
                )
                binding.addJoinLinkDescription.text =
                    getString(R.string.add_join_inactive_link_description)
            }
            CardType.VIEW.type -> {
                binding.addJoinLinkImage.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_icons_svl_link_inactive
                    )
                )
                binding.addJoinLinkDescription.text =
                    getString(R.string.add_join_inactive_link_description)
            }
        }

        binding.addCardButton.visibility =
            if (currentMembershipPlan?.feature_set?.linking_support?.contains(ADD_BUTTON_ENTRY) == true) {
                View.VISIBLE
            } else {
                View.GONE
            }

        binding.closeButton.setOnClickListener {
            if (isFromJoinCard) {
                findNavController().popBackStack()
            } else {
                findNavController().navigateIfAdded(this, R.id.global_to_home)
            }
        }

        binding.addJoinReward.setOnClickListener {
            currentMembershipPlan?.let {
                if (it.account?.plan_description != null) {
                    findNavController().navigateIfAdded(
                        this,
                        AddJoinFragmentDirections.addJoinToBrandHeader(
                            GenericModalParameters(
                                R.drawable.ic_close,
                                true,
                                it.account.plan_name
                                    ?: getString(R.string.plan_description),
                                it.account.plan_description
                            )
                        )
                    )
                } else if (it.account?.plan_name_card != null) {
                    it.account.plan_name?.let { planName ->
                        findNavController().navigateIfAdded(
                            this,
                            AddJoinFragmentDirections.addJoinToBrandHeader(
                                GenericModalParameters(
                                    R.drawable.ic_close,
                                    true,
                                    planName
                                )
                            )
                        )
                    }
                }
            }
        }

        binding.addCardButton.setOnClickListener {
            currentMembershipPlan?.let {
                val action = AddJoinFragmentDirections.addJoinToGhost(
                    SignUpFormType.ADD_AUTH,
                    it,
                    isRetryJourney,
                    membershipCardId,
                    isFromNoReasonCodes
                )
                findNavController().navigateIfAdded(this, action)
            }

            logEvent(getFirebaseIdentifier(STORE_LINK_VIEW, binding.addCardButton.text.toString()))
        }

        binding.getCardButton.setOnClickListener {
            currentMembershipPlan?.let { membershipPlan ->
                val getNewCardNavigationDirections =
                    if (membershipPlan.feature_set?.linking_support != null &&
                        !membershipPlan.feature_set.linking_support.contains(TypeOfField.ENROL.name)
                    ) {
                        val genericModalParameters = GenericModalParameters(
                            R.drawable.ic_back,
                            true,
                            getString(R.string.native_join_unavailable_title),
                            getString(
                                R.string.native_join_unavailable_text_part_1,
                                membershipPlan.account?.company_name
                            ), "", "", "", getString(
                                R.string.native_join_unavailable_text_part_2
                            )
                        )
                        membershipPlan.account?.plan_url?.let {
                            if (it.isNotEmpty()) {
                                genericModalParameters.firstButtonText =
                                    getString(R.string.native_join_unavailable_button_text)
                                genericModalParameters.link =
                                    membershipPlan.account.plan_url
                            }
                        }
                        AddJoinFragmentDirections.addJoinToJoinUnavailable(genericModalParameters)
                    } else if (membershipPlan.has_vouchers == true &&
                        viewModel.paymentCards.value.isNullOrEmpty()
                    ) {
                        AddJoinFragmentDirections.actionAddJoinToPaymentCardNeededFragment(
                            GenericModalParameters(
                                R.drawable.ic_back,
                                false,
                                getString(R.string.native_join_no_payment_cards_title),
                                getString(R.string.native_join_no_payment_cards_description),
                                firstButtonText = getString(R.string.payment_card_needed_button_text)
                            )
                        )
                    } else {
                        AddJoinFragmentDirections.addJoinToGhost(
                            SignUpFormType.ENROL,
                            membershipPlan,
                            isRetryJourney,
                            membershipCardId,
                            isFromNoReasonCodes
                        )
                    }
                findNavController().navigate(getNewCardNavigationDirections)

                logEvent(
                    getFirebaseIdentifier(
                        STORE_LINK_VIEW,
                        binding.getCardButton.text.toString()
                    )
                )
            }
        }
    }

    companion object {
        private const val ADD_BUTTON_ENTRY = "ADD"

    }
}
