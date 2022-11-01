package com.bink.wallet.scenes.settings

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.FragmentBetaFeatureBinding
import com.bink.wallet.model.BetaFeature
import com.bink.wallet.utils.FirebaseEvents
import com.bink.wallet.utils.RemoteConfigUtil
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class BetaFeatureFragment : BaseFragment<BetaFeatureViewModel, FragmentBetaFeatureBinding>() {

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar).shouldDisplayBack(requireActivity())
            .build()
    }

    override val layoutRes: Int
        get() = R.layout.fragment_beta_feature

    override val viewModel: BetaFeatureViewModel by viewModel()

    private val nunitoSans = FontFamily(
        Font(R.font.nunito_sans, FontWeight.Normal),
        Font(R.font.nunito_sans_extrabold, FontWeight.ExtraBold),
        Font(R.font.nunito_sans_light, FontWeight.Light)
    )

    private val betaFeatures = mutableStateOf(ArrayList<BetaFeature>())

    override fun onResume() {
        super.onResume()
        logScreenView(FirebaseEvents.SETTINGS_VIEW)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeView.setContent {
            Surface(color = MaterialTheme.colors.background) {
                BetaFeatures()
            }
        }
        getToggledFeatures(RemoteConfigUtil().beta?.features)?.let {
            betaFeatures.value = it
        }

        binding.tvBetaFeaturesTitle.text = getString(R.string.beta_feature_title)
        binding.toolbar.setNavigationIcon(R.drawable.ic_close)

    }

    @Composable
    private fun BetaFeatures() {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(dimensionResource(id = R.dimen.margin_padding_size_medium))
        ) {
            itemsIndexed(items = betaFeatures.value) { index, feature ->

                val isChecked = remember { mutableStateOf(feature.locallyEnabled) }

                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(dimensionResource(id = R.dimen.settings_box_height))
                        .padding(top = dimensionResource(id = R.dimen.margin_padding_size_small_medium), bottom = dimensionResource(id = R.dimen.margin_padding_size_small_medium))) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = feature.title,
                        fontSize = 18.sp,
                        fontFamily = nunitoSans,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Switch(
                        checked = isChecked.value,
                        onCheckedChange = {
                            featureToggled(!feature.locallyEnabled, feature.slug)
                            isChecked.value = !isChecked.value
                        }
                    )
                }

                if (index != betaFeatures.value.size - 1) {
                    Divider()
                }
            }
        }


    }

    private fun featureToggled(checked: Boolean, slug: String) {
        SharedPreferenceManager.setBetaFeatureEnabled(slug, checked)
    }

    private fun getToggledFeatures(features: ArrayList<BetaFeature>?): ArrayList<BetaFeature>? {
        features?.forEachIndexed { index, feature ->
            features[index].locallyEnabled = SharedPreferenceManager.betaFeatureEnabled(feature.slug)
        }

        return features
    }
}