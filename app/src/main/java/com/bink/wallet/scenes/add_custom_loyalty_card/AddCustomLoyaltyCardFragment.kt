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

        val card = Card(
            barcode = cardNumber,
            null,
            cardNumber,
            ColourPalette.getRandomColour(),
            null,
            storeName
        )
        val membershipId = UUID.randomUUID().toString()

        val membershipCard = MembershipCard(
            id = generateCustomCardId(), "9999", null, null, card, null,
            null, null, null, null, null, null, true
        )

        viewModel.createMembershipCard(membershipCard)
    }

    private fun generateCustomCardId(): String {
        val id = (2000..18000).random() + System.currentTimeMillis().toInt()
        return id.toString()
    }
}