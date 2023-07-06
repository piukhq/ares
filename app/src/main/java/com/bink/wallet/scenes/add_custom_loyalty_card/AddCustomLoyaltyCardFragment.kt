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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddCustomCardFragmentBinding
import com.bink.wallet.theme.AppTheme
import com.bink.wallet.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.bink.wallet.utils.toolbar.FragmentToolbar

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

    private val args: AddCustomLoyaltyCardFragmentArgs by navArgs()

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

        val cardNumber = args.cardNumber ?: ""
        viewModel.updateCardNumber(cardNumber)
    }

    @Composable
    private fun CustomCard() {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Header()

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_padding_size_medium)))

            val enabled = remember { mutableStateOf(false) }

            enabled.value = (viewModel.cardNumber.isNotEmpty() && viewModel.storeName.isNotEmpty())

            TextFields()

            Spacer(modifier = Modifier.weight(1f))

            GradientButton(
                text = stringResource(id = R.string.custom_card_button_text), modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = dimensionResource(id = R.dimen.continue_button_padding),
                        end = dimensionResource(id = R.dimen.continue_button_padding)
                    ),
                textModifier = Modifier.padding(dimensionResource(id = R.dimen.continue_button_text_padding)),
                onClick = {
                    viewModel.createMembershipCard()
                },
                isEnabled = enabled.value
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_padding_size_medium)))
        }
    }

    @Composable
    private fun Header() {
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
            textAlign = TextAlign.Start,
            maxLines = 1
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
            fontSize = 18.sp,
            color = MaterialTheme.colors.onSurface,
            textAlign = TextAlign.Start,
            maxLines = 1

        )
    }

    @Composable
    private fun TextFields() {
        val isTextFieldEmpty = viewModel.cardNumber.isEmpty()
        val image =
            if (isTextFieldEmpty) R.drawable.ic_camera else R.drawable.ic_clear_search

        TextField(
            value = viewModel.cardNumber,
            onValueChange = { viewModel.updateCardNumber(it) },
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
            ),
            trailingIcon = {
                IconButton(onClick = {
                    if (!isTextFieldEmpty) viewModel.updateCardNumber("") else findNavController().popBackStack()
                }, content = {
                    Icon(
                        painter = painterResource(id = image),
                        contentDescription = "Trailing icon"
                    )
                })
            },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_padding_size_small)))

        TextField(
            value = viewModel.storeName,
            onValueChange = { viewModel.updateStoreName(it) },
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
            ),
            singleLine = true

        )
    }
}