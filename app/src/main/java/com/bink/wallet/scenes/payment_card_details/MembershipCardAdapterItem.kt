package com.bink.wallet.scenes.payment_card_details

import com.bink.wallet.model.response.membership_card.MembershipCard

data class MembershipCardAdapterItem(
    val membershipCard: MembershipCard,
    var isChangeable: Boolean = true
)