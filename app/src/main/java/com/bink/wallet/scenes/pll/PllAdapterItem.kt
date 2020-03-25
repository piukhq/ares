package com.bink.wallet.scenes.pll

import com.bink.wallet.R
import com.bink.wallet.model.response.payment_card.PllPaymentCardWrapper

sealed class PllAdapterItem(val id: Int?) {
    data class PaymentCardWrapperItem(val pllPaymentCardWrapper: PllPaymentCardWrapper) :
        PllAdapterItem(R.layout.pll_payment_card_item)
    object PllBrandHeaderItem : PllAdapterItem(R.layout.modal_brand_header)
    object PllTitleItem : PllAdapterItem(R.layout.item_pll_title)
    object PllDescriptionItem : PllAdapterItem(R.layout.item_pll_description)
}