package com.bink.wallet.scenes.add_auth_enrol.view_models

import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import com.bink.wallet.utils.enums.SignUpFormType
import com.bink.wallet.utils.enums.TypeOfField

class GetNewCardViewModel(loyaltyWalletRepository: LoyaltyWalletRepository) :
    AddAuthViewModel(loyaltyWalletRepository) {

    override fun addItems() {
        currentMembershipPlan.value?.let { membershipPlan ->
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
        }
        mapItems()
    }
}