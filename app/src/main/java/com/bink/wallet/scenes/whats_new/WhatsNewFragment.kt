package com.bink.wallet.scenes.whats_new

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.fragment.navArgs
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.WhatsNewFragmentBinding
import com.bink.wallet.model.AdHocMessage
import com.bink.wallet.model.NewFeature
import com.bink.wallet.model.NewMerchant
import com.bink.wallet.model.asAnyList
import com.bink.wallet.theme.AppTheme
import com.bink.wallet.theme.White
import com.bink.wallet.utils.ImageViaUrl
import com.bink.wallet.utils.asJetpackColour
import com.bink.wallet.utils.nunitoSans
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
            viewModel.getFeaturedCards(args.membershipPlans)
        }

        binding.composeView.setContent {
            AppTheme(viewModel.theme.value) {
                WhatsNew(viewModel.whatsNew.value?.asAnyList())
            }
        }
    }


    @Preview
    @Composable
    private fun Preview() {
        val whatsNew = arrayListOf<Any>(
            NewMerchant("Hello", "0"),
            NewFeature("You can now add a new profile picture!", "https://firebasestorage.googleapis.com/v0/b/bink-ac226.appspot.com/o/new-features%2F0.jpeg?alt=media&token=0e425696-53ab-44a7-b172-3011d7d33094", "New pic!!!"),
            AdHocMessage("Hello", "", "Title"),
        )
        WhatsNew(whatsNew)
    }

    @Composable
    private fun WhatsNew(whatsNew: ArrayList<Any>?) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.margin_padding_size_medium))) {
            Text(modifier = Modifier.fillMaxWidth(),
                text = "Whats new",
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
                    shape = RoundedCornerShape(12.dp)) {
                    when (newItem) {
                        is NewMerchant -> {
                            NewMerchant(newMerchant = newItem)
                        }
                        is NewFeature -> {
                            NewFeature(newFeature = newItem)
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
    private fun NewMerchant(newMerchant: NewMerchant) {
        BoxWithConstraints(modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(Color.White)
        ) {

            Box(
                modifier = Modifier
                    .size(500.dp)
                    .offset(
                        y = (-80).dp,
                        x = 110.dp,
                    )
                    .rotate(-46f)
                    .clip(RoundedCornerShape(15.dp))
                    .background(newMerchant.secondaryColour?.asJetpackColour() ?: Color.Transparent)
            )

            Box(
                modifier = Modifier
                    .size(500.dp)
                    .offset(
                        y = (-40).dp,
                        x = 140.dp,
                    )
                    .rotate(-23f)
                    .clip(RoundedCornerShape(15.dp))
                    .clip(RoundedCornerShape(15.dp))
                    .background(newMerchant.primaryColour?.asJetpackColour() ?: Color.Transparent)
            )

            Column(modifier = Modifier
                .offset(
                    y = 40.dp,
                    x = 160.dp,
                )) {
                Text(
                    text = newMerchant.merchantName ?: "",
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
                FeatureTitle(title = "New Merchant")
            }

            Row(modifier = Modifier
                .align(Alignment.CenterStart)) {
                Spacer(modifier = Modifier.width(16.dp))
                ImageViaUrl(
                    url = newMerchant.iconUrl ?: "",
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .size(80.dp)
                )
            }

            Image(
                imageVector = Icons.Filled.ArrowForwardIos,
                contentDescription = "Next", colorFilter = ColorFilter.tint(White),
                modifier = Modifier
                    .size(25.dp)
                    .align(Alignment.CenterEnd)
            )
        }
    }

    @Composable
    private fun NewFeature(newFeature: NewFeature) {
        BoxWithConstraints(modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background("#33aeb9".asJetpackColour())
        ) {

            Column(modifier = Modifier
                .align(Alignment.TopCenter)
                .matchParentSize()
                .fillMaxHeight()) {
                FeatureTitle(title = "New Feature")
            }

            Image(
                imageVector = Icons.Filled.ArrowForwardIos,
                contentDescription = "Next", colorFilter = ColorFilter.tint(White),
                modifier = Modifier
                    .size(25.dp)
                    .align(Alignment.CenterEnd)
            )

            Column(modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)) {

                ImageViaUrl(
                    url = newFeature.imageUrl ?: "", modifier = Modifier
                        .size(30.dp)
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
            .height(150.dp)
            .background("#33aeb9".asJetpackColour())
        ) {

            Column(modifier = Modifier
                .align(Alignment.TopCenter)
                .matchParentSize()
                .fillMaxHeight()) {
                FeatureTitle(title = "Ad Hoc Message")
            }

            Column(modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)) {

                ImageViaUrl(
                    url = adHocMessage.imageUrl ?: "", modifier = Modifier
                        .size(30.dp)
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
                .padding(12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (title != "") Color.Gray.copy(alpha = 0.5f) else Color.Transparent)) {
                Text(text = title,
                    fontFamily = nunitoSans,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp))
            }
        }

    }

}