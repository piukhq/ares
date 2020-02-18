package com.bink.wallet.model

import com.bink.wallet.model.response.membership_card.MembershipCard

data class MembershipCardListWrapper(val membershipCards: MutableList<MembershipCard>)