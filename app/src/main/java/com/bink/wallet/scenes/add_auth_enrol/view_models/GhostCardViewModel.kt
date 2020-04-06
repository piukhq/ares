package com.bink.wallet.scenes.add_auth_enrol.view_models

import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository

class GhostCardViewModel(loyaltyWalletRepository: LoyaltyWalletRepository)
    : AddAuthViewModel(loyaltyWalletRepository) {

    override fun addItems() {
        currentMembershipPlan.value?.let { membershipPlan ->
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
        }
        mapItems()
    }
}