package com.bink.wallet.scenes.polls

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.fragment.navArgs
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.FragmentPollsBinding
import com.bink.wallet.model.PollItem
import com.bink.wallet.theme.AppTheme
import com.bink.wallet.utils.getFormattedEndDate
import com.bink.wallet.utils.nunitoSans
import com.bink.wallet.utils.toolbar.FragmentToolbar
import kotlinx.coroutines.delay
import org.koin.androidx.viewmodel.ext.android.viewModel

class PollsFragment : BaseFragment<PollsViewModel, FragmentPollsBinding>() {

    private val args by navArgs<PollsFragmentArgs>()

    override val layoutRes: Int
        get() = R.layout.fragment_polls
    override val viewModel by viewModel<PollsViewModel>()

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.poll.value = args.poll

        binding.composeView.setContent {
            AppTheme(viewModel.theme.value) {
                viewModel.poll.value?.let { poll ->
                    PollScreen(poll = poll)
                }
            }
        }
    }

    @Composable
    private fun PollScreen(poll: PollItem) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(id = R.dimen.margin_padding_size_large))
                .verticalScroll(rememberScrollState())
        ) {
            poll.closeTime?.let { closeTime ->
                CloseTime(closeTime = closeTime)

                Text(
                    text = poll.question,
                    fontFamily = nunitoSans,
                    color = MaterialTheme.colors.onSurface,
                    fontSize = 26.sp)

                poll.answers?.let {
                    Answers(it)
                }

                SubmitPoll()
            }

        }
    }

    @Composable
    private fun CloseTime(closeTime: Int) {
        var endDate by remember { mutableStateOf(closeTime.getFormattedEndDate()) }

        LaunchedEffect(Unit) {
            while (true) {
                delay(1000)
                endDate = closeTime.getFormattedEndDate()
            }
        }

        Text(
            text = "This poll expires in $endDate",
            fontFamily = nunitoSans,
            fontWeight = FontWeight.Bold,
            color = colorResource(id = R.color.blue_accent),
            fontSize = 16.sp)
    }

    @Composable
    private fun Answers(answers: List<String>) {
        Column {
            answers.forEach { answer ->
                AnswerRow(answer, false) {

                }
            }
        }
    }

    @Composable
    private fun AnswerRow(answer: String, isSelected: Boolean, answerSelected: (String) -> Unit) {
        val backgroundColour = if (isSelected) colorResource(id = R.color.blue_accent) else colorResource(id = R.color.blue_accent).copy(0.2f)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(backgroundColour)) {

            RadioButton(
                selected = (isSelected),
                onClick = { answerSelected(answer) }
            )

            Text(
                text = answer,
                fontFamily = nunitoSans,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onSurface,
                fontSize = 16.sp)

        }
    }


    @Composable
    private fun SubmitPoll() {
        Button(
            modifier = Modifier
                .clip(RoundedCornerShape(dimensionResource(id = R.dimen.margin_padding_size_small)))
                .fillMaxWidth(),
            contentPadding = PaddingValues(),
            onClick = {
                //Submit Poll
            },
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(colorResource(id = R.color.blue_accent))) {
                Text(
                    text = stringResource(R.string.submit_poll),
                    fontFamily = nunitoSans,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                )
            }

        }
    }

}