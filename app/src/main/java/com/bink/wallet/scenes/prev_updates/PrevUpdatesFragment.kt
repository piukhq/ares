package com.bink.wallet.scenes.prev_updates

import android.os.Bundle
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.PrevUpdatesFragmentBinding
import com.bink.wallet.model.ReleaseData
import com.bink.wallet.model.ReleaseNotes
import com.bink.wallet.model.Releases
import com.bink.wallet.theme.AppTheme
import com.bink.wallet.utils.noRippleClickable
import com.bink.wallet.utils.nunitoSans
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class PrevUpdatesFragment : BaseFragment<PrevUpdatesViewModel, PrevUpdatesFragmentBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    override val viewModel: PrevUpdatesViewModel by viewModel()
    override val layoutRes: Int
        get() = R.layout.prev_updates_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeView.setContent {
            AppTheme(viewModel.theme.value) {
                PrevUpdates()
            }
        }
    }

    @Composable
    private fun PrevUpdates() {
        val answerUiState by viewModel.previousUpdatesUiState.collectAsState()

        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.margin_padding_size_medium))) {
            Text(
                text = stringResource(R.string.prev_updates_title),
                color = MaterialTheme.colors.onSurface,
                fontSize = 30.sp,
                fontFamily = nunitoSans,
                fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_padding_size_medium)))

            AnimatedVisibility(visible = answerUiState.releaseNotes != null) {
                answerUiState.releaseNotes?.let { ReleaseList(releases = it) }
            }
        }

    }

    @Composable
    private fun ReleaseList(releases: Releases) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.margin_padding_size_medium)),
        ) {
            items(items = releases.releases.reversed()) { release ->
                ReleaseItem(release = release)
            }
        }
    }

    @Composable
    private fun ReleaseItem(release: ReleaseData) {
        Card(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.prev_updates_corner))) {

            var expanded by remember { mutableStateOf(false) }
            val rotation by animateFloatAsState(targetValue = if (expanded) 90f else 0f)

            Column(modifier = Modifier
                .padding(dimensionResource(id = R.dimen.margin_padding_size_medium))) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                    .noRippleClickable { expanded = !expanded }) {
                    Box(modifier = Modifier
                        .clip(RoundedCornerShape(dimensionResource(id = R.dimen.prev_settings_icon_corner)))
                        .background(colorResource(id = R.color.colorPrimaryDark))) {
                        Image(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "",
                            colorFilter = ColorFilter.tint(colorResource(id = R.color.colorPrimary)),
                            modifier = Modifier
                                .size(dimensionResource(id = R.dimen.poll_cta_image_size))
                                .padding(dimensionResource(id = R.dimen.margin_padding_size_really_small))
                        )
                    }

                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.margin_padding_size_small)))

                    Text(
                        text = release.release_title,
                        color = MaterialTheme.colors.onSurface,
                        fontSize = 18.sp,
                        fontFamily = nunitoSans,
                        fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.weight(1f))

                    Image(
                        imageVector = Icons.Filled.ArrowForwardIos,
                        contentDescription = "Open notes", colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface),
                        modifier = Modifier
                            .size(dimensionResource(id = R.dimen.poll_icon_size))
                            .rotate(rotation)
                    )

                }

                AnimatedVisibility(visible = expanded) {
                    ReleaseNotes(releaseNotes = release.release_notes)
                }
            }

        }
    }

    @Composable
    private fun ReleaseNotes(releaseNotes: List<ReleaseNotes>) {
        Column(
            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.margin_padding_size_medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.margin_padding_size_medium))) {
            releaseNotes.forEach { releaseNote ->
                Text(
                    text = releaseNote.heading,
                    color = MaterialTheme.colors.onSurface,
                    fontSize = 22.sp,
                    fontFamily = nunitoSans,
                    fontWeight = FontWeight.Bold)

                releaseNote.bullet_points.forEach { point ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.margin_padding_size_small)))

                        Image(
                            imageVector = Icons.Filled.RadioButtonChecked,
                            contentDescription = "Bullet", colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface),
                            modifier = Modifier
                                .size(dimensionResource(id = R.dimen.poll_bullet_size))
                        )

                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.margin_padding_size_small)))

                        Text(
                            text = point,
                            color = MaterialTheme.colors.onSurface,
                            fontSize = 16.sp,
                            fontFamily = nunitoSans)
                    }
                }

            }
        }
    }
}