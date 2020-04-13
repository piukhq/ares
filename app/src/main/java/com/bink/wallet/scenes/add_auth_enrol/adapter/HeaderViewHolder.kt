package com.bink.wallet.scenes.add_auth_enrol.adapter

import com.bink.wallet.databinding.AddAuthHeaderItemBinding
import com.bink.wallet.model.response.membership_plan.MembershipPlan

class HeaderViewHolder(
    val binding: AddAuthHeaderItemBinding, private val headerTitle: String?,
    private val headerDescription: String?
) :
    BaseAddAuthViewHolder<MembershipPlan>(binding) {

    var navigateToHeader: () -> Unit = {}

    override fun bind(item: MembershipPlan) {
        super.bind(item)
        binding.membershipPlan = item
        binding.titleAddAuthText.text = headerTitle
        binding.descriptionAddAuth.text = headerDescription
        binding.addJoinReward.setOnClickListener {
            navigateToHeader()
        }
    }
}