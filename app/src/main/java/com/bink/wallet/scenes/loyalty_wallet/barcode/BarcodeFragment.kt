package com.bink.wallet.scenes.loyalty_wallet.barcode

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.BarcodeFragmentBinding
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.*
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel

class BarcodeFragment : BaseFragment<BarcodeViewModel, BarcodeFragmentBinding>() {

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar).shouldDisplayBack(requireActivity())
            .build()
    }

    override val viewModel: BarcodeViewModel by viewModel()
    override val layoutRes: Int
        get() = R.layout.barcode_fragment

    private val showDialog = mutableStateOf(false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            BarcodeFragmentArgs.fromBundle(it).apply {
                viewModel.companyName.value = currentMembershipPlan.account?.company_name
                binding.title.text = currentMembershipPlan.account?.company_name
                binding.composeView.setContent {
                    BarcodeScreen(membershipCard, currentMembershipPlan)
                }
            }
        }

    }

    @Composable
    fun BarcodeScreen(membershipCard: MembershipCard, membershipPlan: MembershipPlan) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.margin_padding_size_large))
                .verticalScroll(rememberScrollState())
        ) {
            Header(membershipCard, membershipPlan)

            if (!membershipCard.card?.membership_id.isNullOrEmpty()) {
                MembershipNumber(membershipCard.card?.membership_id!!)
            }

            if (!membershipCard.card?.barcode.isNullOrEmpty() && membershipCard.card?.barcode != membershipCard.card?.membership_id) {
                BarcodeNumber(membershipCard.card?.barcode!!)
            }

            ReportIssueButton()
        }
    }

    @Composable
    fun Header(membershipCard: MembershipCard, membershipPlan: MembershipPlan) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            val barcode = MembershipPlanUtils.loadBarcode(requireContext(), BarcodeWrapper(membershipCard))
            if (barcode == null) {
                getIconTypeFromPlan(membershipPlan)?.let { url ->
                    ImageViaUrl(
                        url = url, modifier = Modifier
                            .clip(RectangleShape)
                            .width(dimensionResource(id = R.dimen.barcode_brand_image_size))
                            .height(dimensionResource(id = R.dimen.barcode_brand_image_size))
                    )
                }
            } else {
                Image(bitmap = barcode.asImageBitmap(), contentDescription = "Barcode")
            }

            val text = if (barcode == null) getString(R.string.barcode_description_no_barcode) else getString(R.string.barcode_description)
            Text(
                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.margin_padding_size_medium)), text = text,
                fontFamily = nunitoSans,
                fontWeight = FontWeight.Light,
                fontSize = 21.sp
            )
        }
    }

    @Composable
    fun MembershipNumber(text: String) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.margin_padding_size_medium), bottom = dimensionResource(id = R.dimen.margin_padding_size_medium)), text = getString(R.string.membership_number_title),
                fontFamily = nunitoSans,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp
            )

            NumberRow(text = text)
        }
    }

    @Composable
    fun BarcodeNumber(text: String) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.margin_padding_size_medium), bottom = dimensionResource(id = R.dimen.margin_padding_size_medium)), text = getString(R.string.barcode_text),
                fontFamily = nunitoSans,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp
            )

            NumberRow(text = text)
        }
    }

    @Composable
    fun NumberRow(text: String) {
        val textAsList = text.split("").filter { it != " " && it != "" }
        GridItems(
            data = textAsList,
            columnCount = 7
        ) { itemData, index ->
            val backgroundColour = if (index % 2 == 0) Color.Gray else Color.LightGray
            Column(
                modifier = Modifier
                    .background(backgroundColour)
                    .widthIn(min = dimensionResource(id = R.dimen.high_vis_width))
                    .height(dimensionResource(id = R.dimen.high_vis_height)), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row {
                    Text(
                        text = itemData,
                        Modifier.padding(
                            top = dimensionResource(id = R.dimen.high_vis_top_margin),
                            bottom = dimensionResource(id = R.dimen.high_vis_margin),
                            start = dimensionResource(id = R.dimen.high_vis_margin),
                            end = dimensionResource(id = R.dimen.high_vis_margin)
                        ),
                        fontFamily = nunitoSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 40.sp
                    )
                }
                Column(verticalArrangement = Arrangement.Bottom) {
                    Text(
                        text = (index + 1).toString(),
                        fontFamily = nunitoSans,
                        fontWeight = FontWeight.Light,
                        fontSize = 8.sp
                    )
                }
            }
        }

    }

    @Composable
    fun ReportIssueButton() {
        GradientButton(
            text = "Report Issue",
            modifier = Modifier
                .fillMaxWidth(),
            onClick = {
                showDialog.value = true
            }
        )

        if (showDialog.value) {
            IssueDialog()
        }
    }

    @Composable
    fun IssueDialog() {
        val issueCardNumber = stringResource(R.string.barcode_issue_card_number)
        val issueWontScan = stringResource(R.string.barcode_issue_wont_scan)
        val issueOther = stringResource(R.string.barcode_issue_other)

        Dialog(onDismissRequest = { showDialog.value = false }) {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .padding(dimensionResource(id = R.dimen.margin_padding_size_medium))
            ) {
                Text(
                    text = stringResource(R.string.barcode_issue_title),
                    modifier = Modifier
                        .padding(bottom = dimensionResource(id = R.dimen.margin_padding_size_small)),
                    fontFamily = nunitoSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(id = R.dimen.issue_button_height))
                        .padding(bottom = dimensionResource(id = R.dimen.margin_padding_size_small)),
                    colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.colorPrimary)),
                    onClick = {
                        issueEvent(issueCardNumber)
                        showDialog.value = false
                    }
                ) {
                    Text(
                        text = issueCardNumber,
                        fontFamily = nunitoSans,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(id = R.dimen.issue_button_height))
                        .padding(bottom = dimensionResource(id = R.dimen.margin_padding_size_small)),
                    colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.colorPrimaryDark)),
                    onClick = {
                        issueEvent(issueWontScan)
                        showDialog.value = false
                    }
                ) {
                    Text(
                        text = issueWontScan,
                        fontFamily = nunitoSans,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(id = R.dimen.issue_button_height)),
                    colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.colorPrimary)),
                    onClick = {
                        issueEvent(issueOther)
                        showDialog.value = false
                    }
                ) {
                    Text(
                        text = issueOther,
                        fontFamily = nunitoSans,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

    }

    private fun issueEvent(issue: String) {
        logMixpanelEvent(
            MixpanelEvents.BARCODE_ISSUE,
            JSONObject()
                .put(MixpanelEvents.LPS_REASON, issue)
                .put(
                    MixpanelEvents.BRAND_NAME, viewModel.companyName.value
                )
        )
    }

}

