package com.bink.wallet.scenes.add_auth_enrol.view_models

import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.model.request.membership_card.Account
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.request.membership_card.PlanFieldsRequest
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import com.bink.wallet.utils.BARCODE
import com.bink.wallet.utils.CARD_NUMBER
import com.bink.wallet.utils.LocalPointScraping.WebScrapableManager
import com.bink.wallet.utils.enums.FieldType
import com.bink.wallet.utils.enums.SignUpFormType
import com.bink.wallet.utils.enums.TypeOfField

class AddCardViewModel constructor(loyaltyWalletRepository: LoyaltyWalletRepository) :
    AddAuthViewModel(loyaltyWalletRepository) {

    override fun addItems(membershipPlan: MembershipPlan, shouldExcludeBarcode: Boolean) {
        super.addItems(membershipPlan, shouldExcludeBarcode)
        val addPlans = mutableListOf<PlanField>()
        membershipPlan.let {
            it.account?.let { account ->
                account.add_fields?.forEach { planField ->
                    addPlans.add(planField)
                    if (shouldExcludeBarcode && !planField.common_name.equals(BARCODE)) {
                        planField.typeOfField = TypeOfField.ADD
                        addPlanField(planField)
                    } else if (!shouldExcludeBarcode && !planField.common_name.equals(CARD_NUMBER)) {
                        planField.typeOfField = TypeOfField.ADD
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
        mapItems()
        allAddPlans.value = addPlans
    }

    fun handleRequest(
        isRetryJourney: Boolean,
        membershipCardId: String,
        membershipPlan: MembershipPlan
    ) {
        val account: Account? = when (SharedPreferenceManager.isNowBarcode) {
            true -> getBarcodeFieldRequestAccount(
                addRegisterFieldsRequest.value
            )
            else -> if (SharedPreferenceManager.isNowCardNumber) {
                getCardNumberFieldRequestAccount(addRegisterFieldsRequest.value)

            } else {
                addRegisterFieldsRequest.value
            }
        }

        val currentRequest = MembershipCardRequest(account, membershipPlan.id)
        WebScrapableManager.getCurrentCredentials(currentRequest)

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

    private fun getBarcodeFieldRequestAccount(acc: Account?): Account {

        val addFields = mutableListOf<PlanFieldsRequest>()

        val authorizeFields = acc?.authorise_fields
        val enrolFields = acc?.enrol_fields
        val registerFields = acc?.registration_fields

        val barCodePlanField = allAddPlans.value?.firstOrNull { planField ->
            planField.common_name.equals(
                BARCODE
            )
        }

        val barCodeValue = SharedPreferenceManager.barcodeValue

        if (barCodePlanField != null) {
            val planFieldRequestForBarcode = PlanFieldsRequest(
                barCodePlanField.column,
                barCodeValue,
                isSensitive = barCodePlanField.type == FieldType.SENSITIVE.type
            )
            addFields.add(planFieldRequestForBarcode)
        }


        return Account(addFields, authorizeFields, enrolFields, registerFields)


    }

    private fun getCardNumberFieldRequestAccount(acc: Account?): Account {
        val addFields = mutableListOf<PlanFieldsRequest>()

        val authorizeFields = acc?.authorise_fields
        val enrolFields = acc?.enrol_fields
        val registerFields = acc?.registration_fields

        val cardNumberPlanField = allAddPlans.value?.firstOrNull { planField ->
            planField.common_name.equals(
                CARD_NUMBER
            )
        }

        val cardNumberValue = SharedPreferenceManager.cardNumberValue

        if (cardNumberPlanField != null) {
            val planFieldRequestForCardNumber = PlanFieldsRequest(
                cardNumberPlanField.column,
                cardNumberValue,
                isSensitive = cardNumberPlanField.type == FieldType.SENSITIVE.type
            )

            addFields.add(planFieldRequestForCardNumber)
        }


        return Account(addFields, authorizeFields, enrolFields, registerFields)

    }
}