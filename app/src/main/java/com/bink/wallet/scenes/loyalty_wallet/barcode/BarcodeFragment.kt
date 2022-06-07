package com.bink.wallet.scenes.loyalty_wallet.barcode

import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.BarcodeFragmentBinding
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.utils.BarcodeWrapper
import com.bink.wallet.utils.MembershipPlanUtils
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class BarcodeFragment : BaseFragment<BarcodeViewModel, BarcodeFragmentBinding>() {

    private val nunitoSans = FontFamily(
        Font(R.font.nunito_sans, FontWeight.Normal),
        Font(R.font.nunito_sans_extrabold, FontWeight.ExtraBold),
        Font(R.font.nunito_sans_light, FontWeight.Light)
    )

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar).shouldDisplayBack(requireActivity())
            .build()
    }

    override val viewModel: BarcodeViewModel by viewModel()
    override val layoutRes: Int
        get() = R.layout.barcode_fragment

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let {
            BarcodeFragmentArgs.fromBundle(it).apply {
                viewModel.membershipPlan.value = currentMembershipPlan
                viewModel.membershipCard.value = membershipCard
                viewModel.barcode.value = BarcodeWrapper(membershipCard)

                viewModel.isBarcodeAvailable.set(!membershipCard.card?.barcode.isNullOrEmpty())
                viewModel.isCardNumberAvailable.set(!membershipCard.card?.membership_id.isNullOrEmpty())

                membershipCard.card?.let { card ->
                    if (viewModel.isCardNumberAvailable.get()) {
                        viewModel.cardNumber.set(card.membership_id)
                    }
                    if (viewModel.isBarcodeAvailable.get()) {
                        viewModel.barcodeNumber.set(card.barcode)
                    }
                }

                binding.title.text = currentMembershipPlan.account?.company_name
                binding.composeView.setContent {
                    BarcodeScreen(membershipCard)
                }

            }
        }

    }

    @Composable
    fun BarcodeScreen(membershipCard: MembershipCard) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .padding(36.dp)
        ) {
            //Header(membershipCard)
            if (!membershipCard.card?.membership_id.isNullOrEmpty()) {
                MembershipNumber(membershipCard.card?.membership_id!!)
            }
            if (!membershipCard.card?.barcode.isNullOrEmpty()) {
                BarcodeNumber(membershipCard.card?.barcode!!)
            }
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
        Column(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()) {
            Text(
                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp), text = getString(R.string.membership_number_title),
                fontFamily = nunitoSans,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp
            )

            NumberRow(text = text.split(""))
        }
    }

    @Composable
    fun BarcodeNumber(text: String) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()) {
            Text(
                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp), text = getString(R.string.membership_number_title),
                fontFamily = nunitoSans,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp
            )

            NumberRow(text = text.split(""))
        }
    }

    @Composable
    fun NumberRow(text: List<String>) {
        LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 41.dp)) {
            itemsIndexed(items = text) { index, char ->
                val backgroundColour = if (index % 2 == 0) Color.Gray else Color.LightGray
                Column(modifier = Modifier
                    .background(backgroundColour)
                    .width(41.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row {
                        Text(
                            text = char,
                            Modifier.padding(top = 10.dp, bottom = 5.dp, start = 5.dp, end = 5.dp),
                            fontFamily = nunitoSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 40.sp
                        )
                    }
                    Column(verticalArrangement = Arrangement.Bottom) {
                        Text(
                            text = index.toString(),
                            fontFamily = nunitoSans,
                            fontWeight = FontWeight.Light,
                            fontSize = 8.sp
                        )
                    }
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        MembershipNumber("ABC123!@£")
    }

}