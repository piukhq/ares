package com.bink.wallet.scenes.add_auth_enrol.view_models

import com.bink.wallet.R
import com.bink.wallet.model.request.membership_card.Account
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.add_auth_enrol.FormsUtil
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.scenes.loyalty_wallet.wallet.LoyaltyWalletRepository
import com.bink.wallet.utils.BARCODE
import com.bink.wallet.utils.CARD_NUMBER
import com.bink.wallet.utils.enums.SignUpFormType
import com.bink.wallet.utils.enums.TypeOfField
import com.bink.wallet.utils.local_point_scraping.WebScrapableManager

class AddCardViewModel constructor(loyaltyWalletRepository: LoyaltyWalletRepository, loginRepository: LoginRepository) :
    AddAuthViewModel(loyaltyWalletRepository,loginRepository) {

    override fun addItems(membershipPlan: MembershipPlan, shouldExcludeBarcode: Boolean) {
        super.addItems(membershipPlan, shouldExcludeBarcode)
        membershipPlan.let { plan ->
            plan.account?.let { account ->
                account.add_fields?.forEach { planField ->
                    if (shouldExcludeBarcode && !planField.common_name.equals(BARCODE)) {
                        //card field
                        planField.typeOfField = TypeOfField.ADD
                        planField.alternativePlanField =
                            account.add_fields.firstOrNull { it.common_name.equals(BARCODE) }
                        addPlanField(planField)
                    } else if (!shouldExcludeBarcode && !planField.common_name.equals(CARD_NUMBER)) {
                        //barcode
                        planField.typeOfField = TypeOfField.ADD
                        planField.alternativePlanField =
                            account.add_fields.firstOrNull { it.common_name.equals(CARD_NUMBER) }
                        addPlanField(planField)
                    }
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
        mapItems(membershipPlan.id)
    }

    fun handleRequest(
        isRetryJourney: Boolean,
        membershipCardId: String,
        membershipPlan: MembershipPlan
    ) {
        val account: Account? = FormsUtil.getAccount()

        val currentRequest = MembershipCardRequest(account, membershipPlan.id)

        checkDetailsToSave(currentRequest)

        val strippedRequest = WebScrapableManager.setUsernameAndPassword(currentRequest)

        if (isRetryJourney) {
            updateMembershipCard(membershipCardId, strippedRequest)
        } else {
            createMembershipCard(
                strippedRequest
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