package com.bink.wallet.scenes.add_auth_enrol.view_models

import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.add_auth_enrol.FormsUtil
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.scenes.loyalty_wallet.wallet.LoyaltyWalletRepository
import com.bink.wallet.utils.enums.SignUpFormType
import com.bink.wallet.utils.enums.TypeOfField

class GetNewCardViewModel(loyaltyWalletRepository: LoyaltyWalletRepository, loginRepository: LoginRepository) :
    AddAuthViewModel(loyaltyWalletRepository, loginRepository) {

    override fun addItems(membershipPlan: MembershipPlan, shouldExcludeBarcode: Boolean) {
        super.addItems(membershipPlan, shouldExcludeBarcode)
        membershipPlan.account?.enrol_fields?.map {
            it.typeOfField = TypeOfField.ENROL
            addPlanField(it)
        }
        membershipPlan.account?.plan_documents?.map {
            it.display?.let { display ->
                if (display.contains(SignUpFormType.ENROL.type)) {
                    addPlanDocument(it)
                }
            }
        }

        mapItems(membershipPlan.id)
    }

    fun handleRequest(
        isRetryJourney: Boolean,
        membershipCardId: String,
        currentMembershipPlan: MembershipPlan
    ) {
        val currentRequest = MembershipCardRequest(
            FormsUtil.getAccount(),
            currentMembershipPlan.id
        )

        checkDetailsToSave(currentRequest)

        if (isRetryJourney) {
            updateMembershipCard(membershipCardId, currentRequest)
        } else {
            createMembershipCard(
                MembershipCardRequest(
                    FormsUtil.getAccount(),
                    currentMembershipPlan.id
                )
            )
        }
    }
}