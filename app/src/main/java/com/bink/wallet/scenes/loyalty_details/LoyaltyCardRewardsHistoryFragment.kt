package com.bink.wallet.scenes.loyalty_details

import android.os.Bundle
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.LoyaltyCardRewardsHistoryBinding
import com.bink.wallet.model.response.membership_card.Burn
import com.bink.wallet.model.response.membership_card.Earn
import com.bink.wallet.model.response.membership_card.Voucher
import com.bink.wallet.utils.ValueDisplayUtils
import com.bink.wallet.utils.bindings.STAMP
import com.bink.wallet.utils.dateFormatTransactionTime
import com.bink.wallet.utils.enums.VoucherStates
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.nunitoSans
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoyaltyCardRewardsHistoryFragment :
    BaseFragment<LoyaltyCardRewardsHistoryViewModel, LoyaltyCardRewardsHistoryBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    override val viewModel: LoyaltyCardRewardsHistoryViewModel by viewModel()
    override val layoutRes: Int
        get() = R.layout.loyalty_card_rewards_history

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let {
            viewModel.membershipPlan.value = LoyaltyCardDetailsFragmentArgs.fromBundle(it).membershipPlan
            viewModel.membershipCard.value = LoyaltyCardDetailsFragmentArgs.fromBundle(it).membershipCard
        }

        binding.title.text = viewModel.membershipPlan.value?.account?.company_name

        binding.composeView.setContent {
            LoyaltyCardRewardsHistory(vouchers = viewModel.getFilteredVouchers())
        }
    }

    @Composable
    private fun LoyaltyCardRewardsHistory(vouchers: List<Voucher>?) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = getString(R.string.rewards_history),
                fontSize = 25.sp,
                fontFamily = nunitoSans,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = getString(R.string.your_past_rewards),
                fontSize = 18.sp,
                fontFamily = nunitoSans,
            )

            if (vouchers.isNullOrEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items = vouchers) { voucher ->
                        Voucher(voucher = voucher)
                    }
                }
            }
        }
    }

    @Composable
    private fun EmptyState() {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.margin_padding_size_medium)), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(R.drawable.ic_empty_wallet), contentDescription = "Empty Wallet", modifier = Modifier.size(dimensionResource(id = R.dimen.rewards_history_image_size)))
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_padding_size_small)))
            Text(
                text = getString(R.string.no_rewards),
                fontSize = 18.sp,
                fontFamily = nunitoSans,
                textAlign = TextAlign.Center
            )

        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    private fun Voucher(voucher: Voucher) {
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_padding_size_small)))
        Card(modifier = Modifier.fillMaxWidth(), onClick = { viewVoucherDetails(voucher) }) {
            Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.margin_padding_size_medium))) {
                Text(
                    text = getVoucherTitle(voucher.burn),
                    fontSize = 18.sp,
                    fontFamily = nunitoSans,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.blue_accent)
                )
                Text(
                    text = getVoucherSubTitle(voucher.earn),
                    fontSize = 14.sp,
                    fontFamily = nunitoSans,
                    fontWeight = FontWeight.Light
                )

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_padding_size_small)))
                Text(
                    text = getVoucherHeadline(voucher),
                    fontSize = 25.sp,
                    fontFamily = nunitoSans,
                    fontWeight = FontWeight.ExtraBold
                )
                DisplayVoucherCount(voucher = voucher)
                val dateToDisplay =
                    if (voucher.state == VoucherStates.REDEEMED.state) voucher.date_redeemed else voucher.expiry_date
                dateToDisplay?.let {
                    Text(
                        text = setTimestamp(dateToDisplay, getString(R.string.voucher_entry_date)),
                        fontSize = 14.sp,
                        fontFamily = nunitoSans,
                        fontWeight = FontWeight.Light
                    )
                }
            }

        }
    }

    @Composable
    private fun DisplayVoucherCount(voucher: Voucher) {
        val colour = if (voucher.state == VoucherStates.REDEEMED.state) {
            colorResource(id = R.color.voucher_redeemed_background)
        } else {
            colorResource(id = R.color.blue_inactive)
        }

        LazyRow(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.margin_padding_size_medium), bottom = dimensionResource(id = R.dimen.margin_padding_size_medium))) {
            voucher.earn?.target_value?.toInt()?.let {
                items(it) {
                    Row(
                        Modifier
                            .padding(start = dimensionResource(id = R.dimen.voucher_circle_start_padding))
                            .size(dimensionResource(id = R.dimen.voucher_circle_size))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height

                            drawCircle(
                                color = colour,
                                center = Offset(x = canvasWidth / 2, y = canvasHeight / 2),
                                radius = size.minDimension / 2,
                                style = Stroke(18F)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.margin_padding_size_small_medium)))
                }
            }
        }

    }

    private fun getVoucherTitle(voucherBurn: Burn?): String {
        return getString(
            R.string.voucher_stamp_title,
            ValueDisplayUtils.displayValue(
                voucherBurn?.value,
                voucherBurn?.prefix,
                voucherBurn?.suffix,
                null
            )
        )
    }

    private fun getVoucherSubTitle(voucherEarn: Earn?): String {
        return getString(
            R.string.voucher_stamp_subtext,
            voucherEarn?.target_value?.toInt(),
            voucherEarn?.suffix
        )
    }

    private fun getVoucherHeadline(voucher: Voucher): String {
        voucher.state?.let { state ->
            return when (state) {
                VoucherStates.REDEEMED.state,
                VoucherStates.EXPIRED.state,
                VoucherStates.CANCELLED.state
                -> state.capitalize()
                VoucherStates.ISSUED.state -> getString(R.string.earned).capitalize()
                else ->
                    if (voucher.earn?.type == STAMP)
                        getString(
                            R.string.voucher_stamp_in_progress_headline,
                            ValueDisplayUtils.displayFormattedHeadline(voucher.earn)
                        ) else getString(
                        R.string.voucher_accumulator_in_progress_headline,
                        ValueDisplayUtils.displayFormattedHeadline(voucher.earn)
                    )
            }
        }

        return ""
    }

    private fun viewVoucherDetails(voucher: Voucher) {
        val directions = viewModel.membershipPlan.value?.let { membershipPlan ->
            LoyaltyCardRewardsHistoryFragmentDirections.historyToVoucher(
                membershipPlan, voucher
            )
        }
        if (directions != null) {
            findNavController().navigateIfAdded(this, directions)
        }
    }

    private fun setTimestamp(timeStamp: Long, format: String = "%s", shortMonth: Boolean = false): String {
        return String.format(format, dateFormatTransactionTime(timeStamp, shortMonth))
    }
}