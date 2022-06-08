package com.bink.wallet.scenes.loyalty_wallet.barcode

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.BarcodeFragmentBinding
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.utils.*
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class BarcodeFragment : BaseFragment<BarcodeViewModel, BarcodeFragmentBinding>() {

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar).shouldDisplayBack(requireActivity())
            .build()
    }

    override val viewModel: BarcodeViewModel by viewModel()
    override val layoutRes: Int
        get() = R.layout.barcode_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            BarcodeFragmentArgs.fromBundle(it).apply {
                viewModel.membershipPlan.value = currentMembershipPlan
                viewModel.membershipCard.value = membershipCard

                binding.title.text = currentMembershipPlan.account?.company_name
                binding.composeView.setContent {
                    BarcodeScreen(membershipCard)
                }

            }
        }

    }

    @Composable
    fun BarcodeScreen(membershipCard: MembershipCard) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Header(membershipCard)
            if (!membershipCard.card?.membership_id.isNullOrEmpty()) {
                MembershipNumber(membershipCard.card?.membership_id!!)
            }
            if (!membershipCard.card?.barcode.isNullOrEmpty() && membershipCard.card?.barcode != membershipCard.card?.membership_id) {
                BarcodeNumber(membershipCard.card?.barcode!!)
            }
            GradientButton(
                text = "Report Issue",
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {

                }
            )
        }
    }

    @Composable
    fun Header(membershipCard: MembershipCard) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            MembershipPlanUtils.loadBarcode(requireContext(), BarcodeWrapper(membershipCard))?.let {
                Image(bitmap = it.asImageBitmap(), contentDescription = "Barcode")
            }
            Text(
                modifier = Modifier.padding(top = 16.dp), text = getString(R.string.barcode_description),
                fontFamily = nunitoSans,
                fontWeight = FontWeight.Light,
                fontSize = 21.sp
            )
        }
    }

    @Composable
    fun MembershipNumber(text: String) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp), text = getString(R.string.membership_number_title),
                fontFamily = nunitoSans,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp
            )

            NumberRow(text = text)
        }
    }

    @Composable
    fun BarcodeNumber(text: String) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp), text = getString(R.string.barcode_text),
                fontFamily = nunitoSans,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp
            )

            NumberRow(text = text)
        }
    }

    @Composable
    fun NumberRow(text: String) {
        val textAsList = text.split("").filter { it != " " && it != "" }
        GridItems(
            data = textAsList,
            columnCount = 7
        ) { itemData, index ->
            val backgroundColour = if (index % 2 == 0) Color.Gray else Color.LightGray
            Column(
                modifier = Modifier
                    .background(backgroundColour)
                    .widthIn(min = 46.dp)
                    .height(86.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row {
                    Text(
                        text = itemData,
                        Modifier.padding(top = 10.dp, bottom = 5.dp, start = 5.dp, end = 5.dp),
                        fontFamily = nunitoSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 40.sp
                    )
                }
                Column(verticalArrangement = Arrangement.Bottom) {
                    Text(
                        text = (index + 1).toString(),
                        fontFamily = nunitoSans,
                        fontWeight = FontWeight.Light,
                        fontSize = 8.sp
                    )
                }
            }
        }

    }

}