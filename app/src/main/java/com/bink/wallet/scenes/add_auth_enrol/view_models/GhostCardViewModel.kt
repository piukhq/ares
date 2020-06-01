package com.bink.wallet.scenes.add_auth_enrol.view_models

import com.bink.wallet.model.request.membership_card.Account
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository

class GhostCardViewModel(loyaltyWalletRepository: LoyaltyWalletRepository) :
    AddAuthViewModel(loyaltyWalletRepository) {

    override fun addItems(membershipPlan: MembershipPlan, shouldExcludeBarcode: Boolean) {
        super.addItems(membershipPlan, shouldExcludeBarcode)
        membershipPlan.account?.add_fields?.map {
            it.typeOfField = com.bink.wallet.utils.enums.TypeOfField.ADD
            addPlanField(it)
        }
        membershipPlan.account?.registration_fields?.map {
            it.typeOfField = com.bink.wallet.utils.enums.TypeOfField.REGISTRATION
            addPlanField(it)
        }
        membershipPlan.account?.plan_documents?.map {
            it.display?.let { display ->
                if (display.contains(com.bink.wallet.utils.enums.SignUpFormType.GHOST.type)) {
                    addPlanDocument(it)
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
        if (isRetryJourney) {
            createGhostCardRequest(membershipCardId, membershipPlan)
        } else {
            createMembershipCardRequest(membershipPlan)
        }
    }

    fun createGhostCardRequest(
        membershipCardId: String,
        membershipPlan: MembershipPlan
    ) {
        val currentRequest = MembershipCardRequest(
            Account(
                null,
                null,
                null,
                addRegisterFieldsRequest.value?.registration_fields,
                null
            ),
            membershipPlan.id
        )
        ghostMembershipCard(membershipCardId, currentRequest)
    }

    private fun createMembershipCardRequest(
        membershipPlan: MembershipPlan
    ) {
        val currentRequest = MembershipCardRequest(
            Account(
                addRegisterFieldsRequest.value?.add_fields,
                null,
                null,
                null,
                null
            ),
            membershipPlan.id
        )
        createMembershipCard(
            currentRequest
        )
    }
}