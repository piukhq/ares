package com.bink.wallet.scenes.onboarding

import android.os.Bundle
import android.view.View
import android.widget.Toolbar
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.OnboardingFragmentBinding
import com.bink.wallet.model.OnboardingItem
import com.bink.wallet.utils.FirebaseEvents.ONBOARDING_JOURNEY_REGISTER
import com.bink.wallet.utils.FirebaseEvents.ONBOARDING_START
import com.bink.wallet.utils.FirebaseEvents.ONBOARDING_VIEW
import com.bink.wallet.utils.FirebaseEvents.getFirebaseIdentifier
import com.bink.wallet.utils.GradientButton
import com.bink.wallet.utils.ONBOARDING_SCROLL_DURATION_SECONDS
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.nunitoSans
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class OnboardingFragment : BaseFragment<OnboardingViewModel, OnboardingFragmentBinding>() {

    override val layoutRes: Int
        get() = R.layout.onboarding_fragment

    override val viewModel: OnboardingViewModel by viewModel()

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(Toolbar(requireContext()))
            .build()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logScreenView(ONBOARDING_VIEW)

        viewModel.clearWallets()

        val onboardingSteps = arrayListOf(
            OnboardingItem(R.drawable.ic_onboarding_page1,
                getString(R.string.page_1_title),
                getString(R.string.page_1_description)
            ),

            OnboardingItem(R.drawable.ic_onboarding_page2,
                getString(R.string.page_2_title),
                getString(R.string.page_2_description)
            ),

            OnboardingItem(R.drawable.ic_onboarding_page3,
                getString(R.string.page_3_title),
                getString(R.string.page_3_description)
            )
        )

        binding.composeView.setContent {
            Surface(color = Color.White) {
                OnboardingScreen(onboardingSteps = onboardingSteps)
            }
        }

    }

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    private fun OnboardingScreen(onboardingSteps: ArrayList<OnboardingItem>) {
        val pagerState = rememberPagerState()
        var key by remember { mutableStateOf(false) }

        LaunchedEffect(key1 = key) {
            launch {
                delay(ONBOARDING_SCROLL_DURATION_SECONDS)
                with(pagerState) {
                    val target = if (currentPage < pageCount - 1) currentPage + 1 else 0
                    animateScrollToPage(target)
                    key = !key
                }
            }
        }

        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {

            Spacer(Modifier.height(dimensionResource(id = R.dimen.margin_padding_size_large)))

            HorizontalPager(
                count = onboardingSteps.size,
                state = pagerState
            ) { index ->
                OnboardingPage(
                    onboardingItem = onboardingSteps[index],
                    modifier = Modifier
                        .weight(1f)
                        .padding(dimensionResource(id = R.dimen.margin_padding_size_medium))
                )
            }

            HorizontalPagerIndicator(
                pagerState = pagerState,
                activeColor = Color.Black,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(dimensionResource(id = R.dimen.margin_padding_size_medium)),
            )

            Spacer(modifier = Modifier.weight(1f))

            GradientButton(text = getString(R.string.continue_with_email), modifier = Modifier
                .fillMaxWidth()
                .padding(start = dimensionResource(id = R.dimen.margin_padding_size_large), end = dimensionResource(id = R.dimen.margin_padding_size_large)), onClick = {
                navigateToNextScreen()
            })

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_padding_size_large)))

        }
    }

    @Composable
    private fun OnboardingPage(onboardingItem: OnboardingItem, modifier: Modifier) {
        Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Image(
                painter = painterResource(onboardingItem.image),
                contentDescription = "Onboarding",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(dimensionResource(id = R.dimen.margin_padding_size_medium))
            )

            Text(
                text = onboardingItem.title,
                fontFamily = nunitoSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = onboardingItem.desc,
                fontFamily = nunitoSans,
                fontWeight = FontWeight.Light,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

        }

    }

    private fun navigateToNextScreen() {
        if (findNavController().currentDestination?.id == R.id.onboarding_fragment) {
            findNavController().navigateIfAdded(
                this,
                OnboardingFragmentDirections.onboardingToContinueWithEmail()
            )
        }
        logEvent(
            getFirebaseIdentifier(
                ONBOARDING_VIEW,
                "Continue with email"
            )
        )
        //ONBOARDING START FOR REGISTER
        logEvent(ONBOARDING_START, getOnboardingStartMap(ONBOARDING_JOURNEY_REGISTER))
    }


}

