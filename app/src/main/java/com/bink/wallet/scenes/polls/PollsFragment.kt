package com.bink.wallet.scenes.polls

import android.os.Bundle
import android.view.View
import androidx.compose.animation.AnimatedVisibility
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
import com.bink.wallet.model.PollResultSummary
import com.bink.wallet.theme.AppTheme
import com.bink.wallet.utils.getFormattedEndDate
import com.bink.wallet.utils.nunitoSans
import com.bink.wallet.utils.toolbar.FragmentToolbar
import kotlinx.coroutines.delay
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.DecimalFormat

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
                    PollScreen(poll = poll,
                        answerSelected = {
                            viewModel.answerSelected(it)
                        }, submitAnswer = {
                            viewModel.submitAnswer()
                        })
                }
            }
        }
    }

    @Composable
    private fun PollScreen(poll: PollItem, answerSelected: (String) -> Unit, submitAnswer: () -> Unit) {
        val answerUiState by viewModel.selectedAnswerUiState.collectAsState()
        val resultUiState by viewModel.answerResultUiState.collectAsState()
        val userHasAnswered = resultUiState.pollResultSummary.isNotEmpty()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(id = R.dimen.margin_padding_size_large))
                .verticalScroll(rememberScrollState())
        ) {
            poll.closeTime?.let { closeTime ->

                if (!userHasAnswered) {
                    CloseTime(closeTime = closeTime)

                    Text(
                        text = poll.question,
                        fontFamily = nunitoSans,
                        color = MaterialTheme.colors.onSurface,
                        fontSize = 26.sp)

                    poll.answers?.let {
                        Spacer(modifier = Modifier.height(16.dp))
                        Answers(answers = it, answerUiState = answerUiState, answerSelected = answerSelected)
                    }

                    SubmitPoll(answerUiState = answerUiState, submitAnswer = submitAnswer)
                }

                AnimatedVisibility(visible = userHasAnswered) {
                    Column {
                        Text(
                            text = "Thank you for your response!",
                            fontFamily = nunitoSans,
                            color = MaterialTheme.colors.onSurface,
                            fontSize = 26.sp)

                        Text(
                            text = poll.question,
                            fontFamily = nunitoSans,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(id = R.color.blue_accent),
                            fontSize = 16.sp)

                        Spacer(modifier = Modifier.height(16.dp))
                        PollResults(results = resultUiState.pollResultSummary)
                    }
                }

                AnimatedVisibility(visible = answerUiState.error.isNotEmpty()) {
                    Text(
                        text = answerUiState.error,
                        fontFamily = nunitoSans,
                        color = Color.Red,
                        fontSize = 26.sp)
                }
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
    private fun Answers(answers: List<String>, answerUiState: AnswerUiState, answerSelected: (String) -> Unit) {
        Column {
            answers.forEach { answer ->
                AnswerRow(answer, answerUiState.selectedAnswer == answer, answerSelected)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    @Composable
    private fun PollResults(results: MutableList<PollResultSummary>) {
        Column {
            results.forEach { result ->
                ResultRow(result)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    @Composable
    private fun AnswerRow(answer: String, isSelected: Boolean, answerSelected: (String) -> Unit) {
        val backgroundColour = if (isSelected) colorResource(id = R.color.blue_light) else colorResource(id = R.color.blue_light).copy(0.2f)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.margin_padding_size_small)))
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
    private fun ResultRow(result: PollResultSummary) {
        BoxWithConstraints(modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)) {
            Row(modifier = Modifier
                .clip(RoundedCornerShape(dimensionResource(id = R.dimen.margin_padding_size_small)))
                .fillMaxWidth()
                .height(70.dp)
                .background(colorResource(id = R.color.blue_light).copy(0.2f))
                .align(Alignment.CenterEnd)) {}

            Row(modifier = Modifier
                .clip(RoundedCornerShape(dimensionResource(id = R.dimen.margin_padding_size_small)))
                .fillMaxWidth(result.answerPercentage / 100)
                .height(70.dp)
                .background(colorResource(id = R.color.blue_light))
                .align(Alignment.CenterStart)) {}

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .align(Alignment.Center)) {

                RadioButton(
                    selected = result.isUsersAnswer,
                    onClick = { }
                )

                Text(
                    text = result.answer,
                    fontFamily = nunitoSans,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface,
                    fontSize = 16.sp)

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "${DecimalFormat("#.##").format(result.answerPercentage)}%",
                    fontFamily = nunitoSans,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.blue_accent),
                    fontSize = 16.sp)

                Spacer(modifier = Modifier.width(16.dp))
            }
        }
    }

    @Composable
    private fun SubmitPoll(answerUiState: AnswerUiState, submitAnswer: () -> Unit) {
        val isAnswerSelected = answerUiState.selectedAnswer.isNotEmpty()
        val backgroundColour = if (isAnswerSelected) colorResource(id = R.color.blue_light) else colorResource(id = R.color.blue_light).copy(0.2f)
        Button(
            modifier = Modifier
                .clip(RoundedCornerShape(dimensionResource(id = R.dimen.margin_padding_size_small)))
                .fillMaxWidth(),
            contentPadding = PaddingValues(),
            enabled = isAnswerSelected,
            onClick = {
                submitAnswer()
            },
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(backgroundColour)) {
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