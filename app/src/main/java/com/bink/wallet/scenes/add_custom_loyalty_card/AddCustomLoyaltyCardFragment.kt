package com.bink.wallet.scenes.add_custom_loyalty_card

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddCustomCardFragmentBinding
import com.bink.wallet.model.response.membership_card.Card
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.theme.AppTheme
import com.bink.wallet.utils.ColourPalette
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.google.zxing.BarcodeFormat
import java.util.*

class AddCustomLoyaltyCardFragment :
    BaseFragment<AddCustomLoyaltyCardViewModel, AddCustomCardFragmentBinding>() {

    override val layoutRes: Int
        get() = R.layout.add_custom_card_fragment
    override val viewModel: AddCustomLoyaltyCardViewModel by viewModel()

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .build()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.composeView.setContent {
            AppTheme(theme = viewModel.theme.value) {
                CustomCard()
            }
        }
    }

    @Composable
    private fun CustomCard() {
        Column() {
            Image(
                painter = painterResource(id = R.drawable.ic_bink_icon),
                contentDescription = "Bink logo",
                modifier = Modifier.padding(start = 80.dp)
            )
            var cardNumber by remember { mutableStateOf("") }
            var storeName by remember { mutableStateOf("") }
            val enabled = remember { mutableStateOf(false) }

            enabled.value = (cardNumber.isNotEmpty() && storeName.isNotEmpty())

            TextField(
                value = cardNumber,
                onValueChange = { cardNumber = it },
                label = { Text("Card number") }
            )

            TextField(
                value = storeName,
                onValueChange = { storeName = it },
                label = { Text("Store name") }
            )

            Button(
                onClick = { generateCustomCard(cardNumber, storeName) },
                enabled = enabled.value
            ) {

            }
        }
    }

    private fun generateCustomCard(cardNumber: String, storeName: String) {

        val card = Card(barcode = cardNumber, 1, cardNumber, ColourPalette.getRandomColour(), null)
        val membershipId = UUID.randomUUID().toString()

        val membershipCard = MembershipCard(id = membershipId, "9999", null, null, card, null,
            null, null, null, null,null)

        viewModel.createMembershipCard(membershipCard)
    }
}