package com.bink.wallet.scenes.loyalty_details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.databinding.DetailVoucherItemBinding
import com.bink.wallet.model.response.membership_card.Voucher
import com.bink.wallet.utils.*
import com.bink.wallet.utils.enums.VoucherStates
import kotlin.math.roundToInt

class LoyaltyCardDetailsVouchersAdapter(
    private val vouchers: List<Voucher>,
    val onClickListener: (Any) -> Unit = {}
) :
    RecyclerView.Adapter<LoyaltyCardDetailsVouchersAdapter.LoyaltyCardDetailsVouchersViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LoyaltyCardDetailsVouchersViewHolder {
        val binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.detail_voucher_item,
            parent,
            false
        ) as DetailVoucherItemBinding
        return LoyaltyCardDetailsVouchersViewHolder(binding, onClickListener)
    }

    override fun getItemCount() = vouchers.size

    override fun onBindViewHolder(holder: LoyaltyCardDetailsVouchersViewHolder, position: Int) =
        holder.bind(vouchers[position])


    class LoyaltyCardDetailsVouchersViewHolder(
        var binding: DetailVoucherItemBinding,
        val onClickListener: (Any) -> Unit = {}
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(thisVoucher: Voucher) {
            with (binding) {
                voucher = thisVoucher
                thisVoucher.burn?.let {
                    if (it.value != null) {
                        title.text = ValueDisplayUtils.displayValue(
                            it.value,
                            it.prefix,
                            it.suffix,
                            it.currency,
                            it.type
                        )
                    }
                }
                root.apply {
                    this.setOnClickListener {
                        onClickListener(thisVoucher)
                    }
                }
            }
            binding.executePendingBindings()
            when (thisVoucher.state) {
                VoucherStates.IN_PROGRESS.state,
                VoucherStates.ISSUED.state -> {
                    if (thisVoucher.earn?.target_value != null &&
                        thisVoucher.earn.target_value != FLOAT_ZERO) {
                        with (thisVoucher.earn) {
                            if (value != null) {
                                binding.spentAmount.text = ValueDisplayUtils.displayValue(
                                    value,
                                    prefix,
                                    suffix,
                                    currency
                                )
                            }
                            val goal = ValueDisplayUtils.displayValue(
                                target_value!!,
                                prefix,
                                suffix,
                                currency
                            )
                            with (binding) {
                                subtitle.text = thisVoucher.subtext.plus(SPACE).plus(goal)
                                progressBar.max =
                                    (thisVoucher.earn.target_value * FLOAT_ONE_HUNDRED).roundToInt()
                                progressBar.progress =
                                    ((thisVoucher.earn.value
                                        ?: FLOAT_ZERO) * FLOAT_ONE_HUNDRED).roundToInt()
                                goalAmount.text = goal
                            }
                        }
                    } else {
                        hideEarnBurnValues()
                    }
                    if (thisVoucher.state == VoucherStates.ISSUED.state) {
                        fillProgressBar()
                    }
                }
                else -> {
                    hideEarnBurnValues()
                    fillProgressBar()
                }
            }
            setProgressDrawable(thisVoucher.state)
        }

        private fun hideEarnBurnValues() {
            with (binding) {
                goalTitle.visibility = View.GONE
                goalAmount.visibility = View.GONE
                spentTitle.visibility = View.GONE
                spentAmount.visibility = View.GONE
            }
        }

        private fun fillProgressBar() {
            with (binding) {
                progressBar.max = INT_ONE_HUNDRED
                progressBar.progress = INT_ONE_HUNDRED
            }
        }

        private fun setProgressDrawable(state: String?) {
            with(binding.progressBar) {
                progressDrawable =
                    ContextCompat.getDrawable(
                        context,
                        when (state) {
                            VoucherStates.ISSUED.state ->
                                R.drawable.loyalty_voucher_progress_earned
                            VoucherStates.REDEEMED.state ->
                                R.drawable.loyalty_voucher_progress_redeemed
                            VoucherStates.EXPIRED.state ->
                                R.drawable.loyalty_voucher_progress_expired
                            else ->
                                R.drawable.loyalty_voucher_progress_bar
                        }
                    )
            }
        }
    }
}