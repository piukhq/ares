package com.bink.wallet.scenes.add_auth_enrol.view_models

import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.add_auth_enrol.FormsUtil
import com.bink.wallet.scenes.loyalty_wallet.wallet.LoyaltyWalletRepository

class GhostCardViewModel(loyaltyWalletRepository: LoyaltyWalletRepository) :
    AddAuthViewModel(loyaltyWalletRepository) {

    override fun addItems(membershipPlan: MembershipPlan, shouldExcludeBarcode: Boolean) {
        super.addItems(membershipPlan, shouldExcludeBarcode)
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
        mapItems(membershipPlan.id)
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
            FormsUtil.getAccount(),
            membershipPlan.id
        )

        checkDetailsToSave(currentRequest)

        ghostMembershipCard(membershipCardId, currentRequest)
    }

    private fun createMembershipCardRequest(
        membershipPlan: MembershipPlan
    ) {
        val currentRequest = MembershipCardRequest(
            FormsUtil.getAccount(),
            membershipPlan.id
        )

        checkDetailsToSave(currentRequest)

        createMembershipCard(
            currentRequest
        )
    }
}