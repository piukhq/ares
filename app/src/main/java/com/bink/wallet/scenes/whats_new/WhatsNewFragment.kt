package com.bink.wallet.scenes.whats_new

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.WhatsNewFragmentBinding
import com.bink.wallet.model.AdHocMessage
import com.bink.wallet.model.NewFeature
import com.bink.wallet.model.NewMerchant
import com.bink.wallet.model.asAnyList
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.theme.AppTheme
import com.bink.wallet.theme.White
import com.bink.wallet.utils.*
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class WhatsNewFragment :
    BaseFragment<WhatsNewViewModel, WhatsNewFragmentBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    private val args by navArgs<WhatsNewFragmentArgs>()
    override val viewModel: WhatsNewViewModel by viewModel()
    override val layoutRes: Int
        get() = R.layout.whats_new_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            viewModel.whatsNew.value = args.whatsNew
            viewModel.getFeaturedCards(args.membershipPlans, args.membershipCards)
        }

        binding.composeView.setContent {
            AppTheme(viewModel.theme.value) {
                WhatsNew(viewModel.whatsNew.value?.asAnyList())
            }
        }
    }

    @Composable
    private fun WhatsNew(whatsNew: ArrayList<Any>?) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.margin_padding_size_medium))) {
            Text(modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.whats_new_title),
                textAlign = TextAlign.Center,
                color = colorResource(id = R.color.blue_accent),
                fontSize = 30.sp,
                fontFamily = nunitoSans,
                fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_padding_size_large)))
            whatsNew?.let {
                NewList(list = it)
            }
        }
    }

    @Composable
    private fun NewList(list: ArrayList<Any>) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.margin_padding_size_large)),
        ) {
            items(items = list) { newItem ->

                Card(modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.whats_new_item_corner))) {
                    when (newItem) {
                        is NewMerchant -> {
                            NewMerchant(newMerchant = newItem) {
                                findNavController().navigate(WhatsNewFragmentDirections.whatsNewToAddJoin(
                                    it,
                                    null,
                                    isFromJoinCard = false,
                                    isRetryJourney = false))
                            }
                        }
                        is NewFeature -> {
                            NewFeature(newFeature = newItem) {
                                newItem.screen?.let { navigate(it) }
                            }
                        }
                        is AdHocMessage -> {
                            AdHocMessage(adHocMessage = newItem)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun NewMerchant(newMerchant: NewMerchant, onClick: (MembershipPlan) -> Unit) {
        BoxWithConstraints(modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.whats_new_item_height))
            .background(Color.White)
            .noRippleClickable {
                newMerchant.membershipPlan?.let { onClick(it) }
            }
        ) {

            Box(
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.new_merchant_box_size))
                    .offset(
                        y = (-80).dp,
                        x = 110.dp,
                    )
                    .rotate(-46f)
                    .clip(RoundedCornerShape(dimensionResource(id = R.dimen.whats_new_item_corner)))
                    .background(newMerchant.membershipPlan?.card
                        ?.getSecondaryColor()
                        ?.asJetpackColour() ?: Color.Transparent)
            )

            Box(
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.new_merchant_box_size))
                    .offset(
                        y = (-40).dp,
                        x = 140.dp,
                    )
                    .rotate(-23f)
                    .clip(RoundedCornerShape(dimensionResource(id = R.dimen.whats_new_item_corner)))
                    .background(newMerchant.membershipPlan?.card?.colour?.asJetpackColour() ?: Color.Transparent)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(
                        y = (50).dp,
                    )
                    .size(dimensionResource(id = R.dimen.new_merchant_corner_box_size))
                    .clip(RoundedCornerShape(dimensionResource(id = R.dimen.whats_new_item_corner)))
                    .background(newMerchant.membershipPlan?.card?.colour?.asJetpackColour() ?: Color.Transparent)
            )

            Column(modifier = Modifier
                .offset(
                    y = 40.dp,
                    x = 160.dp,
                )) {
                Text(
                    text = newMerchant.membershipPlan?.account?.company_name ?: "",
                    fontFamily = nunitoSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.White
                )
                Text(
                    text = "${newMerchant.description}",
                    fontFamily = nunitoSans,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            Column(modifier = Modifier
                .align(Alignment.TopCenter)
                .matchParentSize()
                .fillMaxHeight()) {
                FeatureTitle(title = stringResource(R.string.whats_new_merchant_title))
            }

            Row(modifier = Modifier
                .align(Alignment.CenterStart)) {
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.margin_padding_size_medium)))
                ImageViaUrl(
                    url = getIconTypeFromPlan(newMerchant.membershipPlan) ?: "",
                    modifier = Modifier
                        .clip(RoundedCornerShape(dimensionResource(id = R.dimen.margin_padding_size_small)))
                        .size(dimensionResource(id = R.dimen.merchant_icon_size))
                )
            }

            Image(
                imageVector = Icons.Filled.ArrowForwardIos,
                contentDescription = "Next", colorFilter = ColorFilter.tint(White),
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.navigate_icon_size))
                    .align(Alignment.CenterEnd)
            )
        }
    }

    @Composable
    private fun NewFeature(newFeature: NewFeature, onClick: () -> Unit) {
        BoxWithConstraints(modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.whats_new_item_height))
            .background("#33aeb9".asJetpackColour())
            .clickable {
                onClick()
            }
        ) {

            Column(modifier = Modifier
                .align(Alignment.TopCenter)
                .matchParentSize()
                .fillMaxHeight()) {
                FeatureTitle(title = stringResource(R.string.whats_new_new_feature_title))
            }

            Image(
                imageVector = Icons.Filled.ArrowForwardIos,
                contentDescription = "Next", colorFilter = ColorFilter.tint(White),
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.navigate_icon_size))
                    .align(Alignment.CenterEnd)
            )

            Column(modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = dimensionResource(id = R.dimen.margin_padding_size_medium))) {

                ImageViaUrl(
                    url = newFeature.imageUrl ?: "", modifier = Modifier
                        .size(dimensionResource(id = R.dimen.new_feature_icon_size))
                )

                Text(
                    text = newFeature.title ?: "",
                    fontFamily = nunitoSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.White
                )
                Text(
                    text = newFeature.description ?: "",
                    fontFamily = nunitoSans,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }

    @Composable
    private fun AdHocMessage(adHocMessage: AdHocMessage) {
        BoxWithConstraints(modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.whats_new_item_height))
            .background("#33aeb9".asJetpackColour())
        ) {

            Column(modifier = Modifier
                .align(Alignment.TopCenter)
                .matchParentSize()
                .fillMaxHeight()) {
                FeatureTitle(title = stringResource(R.string.whats_new_ad_hoc_title))
            }

            Column(modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = dimensionResource(id = R.dimen.margin_padding_size_medium))) {

                ImageViaUrl(
                    url = adHocMessage.imageUrl ?: "", modifier = Modifier
                        .size(dimensionResource(id = R.dimen.new_feature_icon_size))
                )

                Text(
                    text = adHocMessage.title ?: "",
                    fontFamily = nunitoSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.White
                )
                Text(
                    text = adHocMessage.description ?: "",
                    fontFamily = nunitoSans,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }

    @Composable
    private fun FeatureTitle(title: String) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize()) {

            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier
                .padding(dimensionResource(id = R.dimen.whats_new_item_corner))
                .clip(RoundedCornerShape(dimensionResource(id = R.dimen.margin_padding_size_small)))
                .background(if (title != "") Color.Gray.copy(alpha = 0.5f) else Color.Transparent)) {
                Text(text = title,
                    fontFamily = nunitoSans,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.whats_new_item_corner), vertical = dimensionResource(id = R.dimen.margin_padding_size_really_small)))
            }
        }

    }

    private fun navigate(screen: Int) {
        when (screen) {
            1 -> {
                //Loyalty Wallet
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            2 -> {
                //Payment Wallet
                findNavController().navigate(WhatsNewFragmentDirections.whatsNewToPaymentWallet())
            }
            3 -> {
                //Browse Brands
                val plansAndCards = viewModel.getPlansAndCards()
                findNavController().navigate(WhatsNewFragmentDirections.whatsNewToBrowseBrands(plansAndCards.first, plansAndCards.second))
            }
            4 -> {
                //Settings
                findNavController().navigate(WhatsNewFragmentDirections.whatsNewToSettingsScreen())
            }
        }
    }

}