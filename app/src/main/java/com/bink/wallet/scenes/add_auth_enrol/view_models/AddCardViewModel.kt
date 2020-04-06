package com.bink.wallet.scenes.add_auth_enrol.view_models

import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import com.bink.wallet.utils.enums.SignUpFormType
import com.bink.wallet.utils.enums.TypeOfField

class AddCardViewModel constructor(loyaltyWalletRepository: LoyaltyWalletRepository) :
    AddAuthViewModel(loyaltyWalletRepository) {

    override fun addItems() {
        currentMembershipPlan.value?.let {
            it.account?.let { account ->
                account.add_fields?.map { planField ->
                    planField.typeOfField = TypeOfField.ADD
                    addPlanField(planField)
                }
                account.authorise_fields?.map { planField ->
                    planField.typeOfField = TypeOfField.AUTH
                    addPlanField(planField)
                }
                account.plan_documents?.map { planDocument ->
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
}