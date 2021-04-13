package com.bink.wallet.scenes.loyalty_wallet.adapter.viewholders

import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import com.bink.wallet.R
import com.bink.wallet.databinding.CardItemBinding
import com.bink.wallet.databinding.LoyaltyWalletItemBinding
import com.bink.wallet.model.LoyaltyWalletItem
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.scenes.BaseViewHolder
import com.bink.wallet.utils.ColorUtil
import com.bink.wallet.utils.VOUCHER_EARN_TYPE_STAMPS
import com.bink.wallet.utils.bindings.setVoucherCollectedProgress
import com.bink.wallet.utils.displayVoucherEarnAndTarget
import com.bink.wallet.utils.enums.MembershipCardStatus
import com.bink.wallet.utils.enums.VoucherStates
import com.bink.wallet.utils.formatBalance

class LoyaltyWalletViewHolder(
    val binding: LoyaltyWalletItemBinding,
    val onClickListener: (Any) -> Unit = {},
    val membershipPlans: ArrayList<MembershipPlan>,
    val paymentCards: MutableList<PaymentCard>?
) :
    BaseViewHolder<MembershipCard>(binding) {

    override fun bind(item: MembershipCard) {
        val cardBinding = binding.cardItem
        if (!membershipPlans.isNullOrEmpty()) {

            membershipPlans.firstOrNull { it.id == item.membership_plan }?.let { membershipPlan ->

                paymentCards?.let {
                    val loyaltyItem = LoyaltyWalletItem(item, membershipPlan, it)
                    bindCardToLoyaltyItem(loyaltyItem, binding)
                }

                membershipPlan.card?.let { membershipPlanCard ->
                    item.card?.let {
                        it.secondary_colour = membershipPlanCard.secondary_colour
                    }
                }

                bindVouchersToDisplay(cardBinding, membershipPlan, item)
            }

        }

        with(cardBinding.cardView) {
            setFirstColor(Color.parseColor(item.card?.getSecondaryColor()))
            setSecondColor(Color.parseColor(item.card?.colour))
        }

        bindSecondaryColorChanges(cardBinding, Color.parseColor(item.card?.colour))
    }

    fun bindVouchersToDisplay(
        cardBinding: CardItemBinding,
        currentMembershipPlan: MembershipPlan,
        item: MembershipCard
    ) {
        with(cardBinding) {
            plan = currentMembershipPlan
            item.plan = plan
            mainLayout.setOnClickListener { onClickListener(item) }

            if (item.status?.state == MembershipCardStatus.AUTHORISED.status) {
                cardLogin.visibility = View.GONE
                valueWrapper.visibility = View.VISIBLE
                if (!item.vouchers.isNullOrEmpty()) {
                    item.vouchers?.firstOrNull { it.state == VoucherStates.IN_PROGRESS.state }?.let { voucher ->

                        if (voucher.earn?.type == VOUCHER_EARN_TYPE_STAMPS) {
                            loyaltyValue.setVoucherCollectedProgress(voucher.earn)
                        } else {
                            loyaltyValue.text =
                                root.context.displayVoucherEarnAndTarget(voucher)
                        }

                        loyaltyValueExtra.text = if (voucher.earn?.type == VOUCHER_EARN_TYPE_STAMPS) root.context.getString(
                            R.string.earned
                        ) else root.context.getString(
                            R.string.spent
                        )

                    }
                } else if (!item.balances.isNullOrEmpty()) {

                    item.balances?.firstOrNull()?.let { balance ->
                        when (balance.prefix != null) {
                            true ->
                                loyaltyValue.text = balance.formatBalance()

                            else -> {
                                loyaltyValue.text = balance.value
                                loyaltyValueExtra.text = balance.suffix
                            }
                        }
                    }

                }
            }
        }
    }

    private fun bindCardToLoyaltyItem(
        loyaltyItem: LoyaltyWalletItem,
        binding: LoyaltyWalletItemBinding
    ) {
        with(binding.cardItem) {
            loyaltyItem.apply {
                loyaltyValue.text =
                    if (shouldShowPoints()) {
                        retrievePointsText()
                    } else {
                        retrieveAuthStatusText()?.let {
                            root.context.getString(it)
                        }
                    }

                loyaltyValueExtra.text = retrieveAuthSuffix()

                if (!shouldShowLinkStatus()) {
                    linkStatusText.visibility = View.GONE
                } else {
                    retrieveLinkStatusText()?.let {
                        linkStatusText.visibility = View.VISIBLE
                        linkStatusText.text = root.context.getString(it)
                    }
                }

                if (shouldShowLinkImages()) {
                    linkStatusImg.visibility = View.VISIBLE
                    retrieveLinkImage()?.let {
                        linkStatusImg.setImageDrawable(
                            root.context.getDrawable(it)
                        )
                    }
                } else {
                    linkStatusImg.visibility = View.GONE
                }

            }
        }
    }

    private fun bindSecondaryColorChanges(binding: CardItemBinding, primaryColor: Int) {
        val textColor: Int

        if (ColorUtil.isColorLight(primaryColor, ColorUtil.LIGHT_THRESHOLD_TEXT)) {
            textColor = android.R.color.black
        } else {
            textColor = android.R.color.white
        }

        binding.companyName.setTextColor(binding.root.context.getColor(textColor))
        binding.loyaltyValue.setTextColor(binding.root.context.getColor(textColor))
        binding.loyaltyValueExtra.setTextColor(binding.root.context.getColor(textColor))
        binding.linkStatusText.setTextColor(binding.root.context.getColor(textColor))
        binding.linkStatusImg.setColorFilter(
            ContextCompat.getColor(
                binding.root.context,
                textColor
            ), android.graphics.PorterDuff.Mode.MULTIPLY
        )
    }
}
