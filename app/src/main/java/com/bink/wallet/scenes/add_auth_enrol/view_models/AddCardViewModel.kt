package com.bink.wallet.scenes.add_auth_enrol.view_models

import com.bink.wallet.R
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import com.bink.wallet.utils.enums.SignUpFormType
import com.bink.wallet.utils.enums.TypeOfField

class AddCardViewModel constructor(loyaltyWalletRepository: LoyaltyWalletRepository) :
    AddAuthViewModel(loyaltyWalletRepository) {

    override fun addItems(membershipPlan: MembershipPlan) {
        super.addItems(membershipPlan)
        membershipPlan.let {
            it.account?.let { account ->
                account.add_fields?.forEach { planField ->
                    planField.typeOfField = TypeOfField.ADD
                    addPlanField(planField)
                }
                account.authorise_fields?.forEach { planField ->
                    planField.typeOfField = TypeOfField.AUTH
                    addPlanField(planField)
                }
                account.plan_documents?.forEach { planDocument ->
                    planDocument.display?.let { display ->
                        if (display.contains(SignUpFormType.ADD_AUTH.type)) {
                            addPlanDocument(planDocument)
                        }
                    }
                }
            }
        }
        mapItems()
    }

    fun handleRequest(
        isRetryJourney: Boolean,
        membershipCardId: String,
        membershipPlan: MembershipPlan
    ) {
        val currentRequest = MembershipCardRequest(
            addRegisterFieldsRequest.value,
            membershipPlan.id
        )
        if (isRetryJourney) {
            updateMembershipCard(membershipCardId, currentRequest)
        } else {
            createMembershipCard(
                currentRequest
            )
        }
    }

    fun retrieveDescriptionText(
        membershipPlan: MembershipPlan,
        isRetryJourney: Boolean
    ): Pair<Int, String>? {
        membershipPlan.let {
            if (isRetryJourney) {
                if (membershipPlan.areTransactionsAvailable()) {
                    membershipPlan.account?.plan_name?.let {
                        return Pair(
                            R.string.log_in_transaction_available,
                            it
                        )
                    }
                } else {
                    membershipPlan.account?.plan_name_card?.let {
                        return Pair(
                            R.string.log_in_transaction_unavailable,
                            it
                        )
                    }
                }
            } else {
                membershipPlan.account?.company_name?.let { companyName ->
                    return Pair(
                        R.string.please_enter_credentials,
                        companyName
                    )
                }
            }
        }
        return null
    }
}