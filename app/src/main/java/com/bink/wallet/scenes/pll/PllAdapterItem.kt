package com.bink.wallet.scenes.pll

import com.bink.wallet.R
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard

sealed class PllAdapterItem(val id: Int) {
    data class PaymentCardItem(
        var paymentCard: PaymentCard,
        var isSelected: Boolean = false
    ) : PllAdapterItem(R.layout.pll_payment_card_item)

    data class PllBrandHeaderItem(val membershipPlan: MembershipPlan) : PllAdapterItem(R.layout.modal_brand_header)
    data class PllDescriptionItem(val planName: String) : PllAdapterItem(R.layout.item_pll_description)
    object PllTitleItem : PllAdapterItem(R.layout.item_pll_title)
}