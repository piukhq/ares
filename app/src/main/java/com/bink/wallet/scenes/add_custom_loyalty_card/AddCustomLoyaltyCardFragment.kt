package com.bink.wallet.scenes.add_custom_loyalty_card

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.bink.wallet.utils.*
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
    private fun CustomCard() {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = R.drawable.ic_bink_icon),
                contentDescription = "Bink logo",
                alignment = Alignment.Center,
                modifier = Modifier
                    .padding(all = dimensionResource(id = R.dimen.margin_padding_size_medium_large))
                    .size(dimensionResource(id = R.dimen.brand_image_height))
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = dimensionResource(id = R.dimen.margin_padding_size_medium),
                        start = dimensionResource(id = R.dimen.margin_padding_size_medium_large),
                    ), text = stringResource(id = R.string.custom_card_main_header),
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
                        bottom = dimensionResource(id = R.dimen.margin_padding_size_medium_large),
                        start = dimensionResource(id = R.dimen.margin_padding_size_medium_large),
                        end = dimensionResource(id = R.dimen.margin_padding_size_medium_large)
                    ),
                text = stringResource(id = R.string.custom_card_description),
                fontFamily = nunitoSans,
                fontWeight = FontWeight.Light,
                fontSize = 21.sp,
                color = MaterialTheme.colors.onSurface,
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_padding_size_medium)))

            var cardNumber by remember { mutableStateOf("") }
            var storeName by remember { mutableStateOf("") }
            val enabled = remember { mutableStateOf(false) }

            enabled.value = (cardNumber.isNotEmpty() && storeName.isNotEmpty())

            TextField(
                value = cardNumber,
                onValueChange = { cardNumber = it },
                label = { Text(stringResource(id = R.string.custom_card_card_number)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = dimensionResource(id = R.dimen.margin_padding_size_medium_large),
                        end = dimensionResource(id = R.dimen.margin_padding_size_medium_large),
                        bottom = dimensionResource(id = R.dimen.margin_padding_size_small)
                    ),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = MaterialTheme.colors.background,
                    textColor = MaterialTheme.colors.onSurface
                )
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_padding_size_small)))

            TextField(
                value = storeName,
                onValueChange = { storeName = it },
                label = { Text(stringResource(id = R.string.custom_card_store_name)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = dimensionResource(id = R.dimen.margin_padding_size_medium_large),
                        end = dimensionResource(id = R.dimen.margin_padding_size_medium_large)
                    ),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = MaterialTheme.colors.background,
                    textColor = MaterialTheme.colors.onSurface
                )
            )

            Spacer(modifier = Modifier.padding(dimensionResource(id = R.dimen.margin_padding_size_extra_large)))

            GradientButton(
                text = stringResource(id = R.string.custom_card_button_text), modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = dimensionResource(id = R.dimen.continue_button_padding),
                        end = dimensionResource(id = R.dimen.continue_button_padding)
                    ),
                textModifier = Modifier.padding(dimensionResource(id = R.dimen.continue_button_text_padding)),
                onClick = {
                    generateCustomCard(cardNumber, storeName)
                },
                isEnabled = enabled.value
            )
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