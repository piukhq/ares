package com.bink.wallet.scenes.add_custom_loyalty_card

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddCustomCardFragmentBinding
import com.bink.wallet.model.response.membership_card.Card
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.theme.AppTheme
import com.bink.wallet.utils.ColourPalette
import com.bink.wallet.utils.MembershipPlanUtils
import com.bink.wallet.utils.nunitoSans
import com.bink.wallet.utils.observeNonNull
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.bink.wallet.utils.toolbar.FragmentToolbar
import java.util.*

class AddCustomLoyaltyCardFragment :
    BaseFragment<AddCustomLoyaltyCardViewModel, AddCustomCardFragmentBinding>() {

    override val layoutRes: Int
        get() = R.layout.add_custom_card_fragment
    override val viewModel: AddCustomLoyaltyCardViewModel by viewModel()

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    lateinit var membershipPlan: MembershipPlan
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.composeView.setContent {
            AppTheme(theme = viewModel.theme.value) {
                CustomCard()
            }
        }
        viewModel.navigateToLcd.observeNonNull(viewLifecycleOwner) {
            findNavController().navigate(
                AddCustomLoyaltyCardFragmentDirections.customCardToLcd(
                    MembershipPlanUtils.getBlankMembershipPlan(),
                    it
                )
            )
        }
    }

    @Composable
    @Preview
    private fun CustomCard() {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = R.drawable.ic_bink_icon),
                contentDescription = "Bink logo",
                alignment = Alignment.Center,
                modifier = Modifier.padding(all = 34.dp)
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = dimensionResource(id = R.dimen.margin_padding_size_medium),
                        start = 24.dp,
                    ), text = "Enter credentials",
                fontFamily = nunitoSans,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp,
                color = MaterialTheme.colors.onSurface,
                textAlign = TextAlign.Start
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 2.dp,
                        bottom = dimensionResource(id = R.dimen.margin_padding_size_medium_large),
                        start = 24.dp,
                        end = 24.dp
                    ), text = "Please enter your credentials below to add this card to your wallet.",
                fontFamily = nunitoSans,
                fontWeight = FontWeight.Light,
                fontSize = 21.sp,
                color = MaterialTheme.colors.onSurface,
                textAlign = TextAlign.Start
            )

            var cardNumber by remember { mutableStateOf("") }
            var storeName by remember { mutableStateOf("") }
            val enabled = remember { mutableStateOf(false) }

            enabled.value = (cardNumber.isNotEmpty() && storeName.isNotEmpty())

            TextField(
                value = cardNumber,
                onValueChange = { cardNumber = it },
                label = { Text("Card number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 8.dp)
            )

            TextField(
                value = storeName,
                onValueChange = { storeName = it },
                label = { Text("Store name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp)
            )

            Button(
                modifier = Modifier
                    .clip(RoundedCornerShape(dimensionResource(id = R.dimen.force_barcode_rounding)))
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 60.dp),
                contentPadding = PaddingValues(),
                onClick = { generateCustomCard(cardNumber, storeName) },
                enabled = enabled.value
            ) {
                Text(text = "Add card")
            }
        }
    }

    private fun generateCustomCard(cardNumber: String, storeName: String) {

        val card = Card(
            barcode = cardNumber,
            null,
            cardNumber,
            ColourPalette.getRandomColour(),
            null,
            storeName
        )

        val membershipCard = MembershipCard(
            id = generateCustomCardId(), "9999", null, null, card, null,
            null, null, null, null, UUID.randomUUID().toString(), null, true
        )

        viewModel.createMembershipCard(membershipCard)
    }

    private fun generateCustomCardId(): String {
        val id = (2000..18000).random() + System.currentTimeMillis().toInt()
        return id.toString()
    }
}