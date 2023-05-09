package com.bink.wallet.scenes.loyalty_wallet.wallet.adapter.viewholders

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bink.wallet.databinding.TakePollViewBinding
import com.bink.wallet.model.PollItem
import com.bink.wallet.scenes.BaseViewHolder
import com.bink.wallet.utils.nunitoSans

class TakePollViewHolder(
    val binding: TakePollViewBinding,
    val onClickListener: (Any) -> Unit = {},
) :
    BaseViewHolder<PollItem>(binding) {

    override fun bind(pollItem: PollItem) {
        with(binding) {
            composeView.setContent {
                TakePollCTA(pollItem) {
                    onClickListener(pollItem)
                }
            }
        }

        binding.root.setOnClickListener {
            onClickListener(pollItem)
        }
    }
}

@Composable
private fun TakePollCTA(pollItem: PollItem, onClick: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick() }) {

        Row(modifier = Modifier.fillMaxWidth()) {
            Image(
                imageVector = Icons.Filled.Leaderboard,
                contentDescription = "Poll", colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier
                    .size(25.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Image(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close", colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier
                    .size(25.dp)
            )
        }

        Text(
            text = "${pollItem.question}",
            fontFamily = nunitoSans,
            fontSize = 18.sp,
            color = Color.White
        )
        Row {
            Text(
                text = "Take Poll",
                fontFamily = nunitoSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
            Image(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = "Take Poll", colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier
                    .size(25.dp)
            )
        }
    }
}