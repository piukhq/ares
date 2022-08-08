package com.bink.wallet.scenes.loyalty_wallet.barcode

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
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
        var barcode = MembershipPlanUtils.loadBarcode(requireContext(), BarcodeWrapper(membershipCard))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.margin_padding_size_large))
                .verticalScroll(rememberScrollState())
        ) {
            if (barcode == null && viewModel.forceShowBarcode.value) {
                barcode = MembershipPlanUtils.createBarcode(requireContext(), membershipCard)
            }
            Header(membershipPlan, barcode)

            if (!membershipCard.card?.membership_id.isNullOrEmpty()) {
                MembershipNumber(membershipPlan.account?.plan_name_card ?: "", membershipCard.card?.membership_id!!)
            }

            if (!membershipCard.card?.barcode.isNullOrEmpty() && membershipCard.card?.barcode != membershipCard.card?.membership_id) {
                BarcodeNumber(membershipCard.card?.barcode!!)
            }

            if (barcode == null) {
                ShowBarcodeButton()
            }

            ReportIssueButton()
        }
    }

    @Composable
    fun Header(membershipPlan: MembershipPlan, barcode: Bitmap?) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
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
    fun MembershipNumber(title: String, text: String) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.margin_padding_size_medium), bottom = dimensionResource(id = R.dimen.margin_padding_size_medium)), text = getString(R.string.membership_number_title, title),
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
    fun ShowBarcodeButton() {
        Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.margin_padding_size_medium)))

        Button(
            modifier = Modifier.clip(RoundedCornerShape(dimensionResource(id = R.dimen.force_barcode_rounding))),
            contentPadding = PaddingValues(),
            onClick = {
                viewModel.forceShowBarcode.value = true
                setMixpanelProperty(
                    MixpanelEvents.FORCE_BARCODE,
                    "true"
                )
                viewModel.setBarcodePreference()
                Toast.makeText(requireContext(), getString(R.string.preferences_updated), Toast.LENGTH_LONG).show()
            },
        ) {
            Box(
                modifier = Modifier
                    .background(Brush.horizontalGradient(listOf(Color(0xFF3D908F), Color(0xFF194B53)))),
                contentAlignment = Alignment.Center,
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                        .padding(start = dimensionResource(id = R.dimen.margin_padding_size_medium), end = dimensionResource(id = R.dimen.margin_padding_size_medium), top = dimensionResource(id = R.dimen.margin_padding_size_medium), bottom = dimensionResource(id = R.dimen.margin_padding_size_small))
                    ) {
                        Text(
                            text = getString(R.string.show_barcode_title),
                            fontFamily = nunitoSans,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .weight(1f))

                        Image(painterResource(R.drawable.ic_barcode), contentDescription = "Barcode", modifier = Modifier.size(dimensionResource(id = R.dimen.barcode_icon_size)))
                    }

                    val clickHere = stringResource(id = R.string.click_here)
                    val body = stringResource(id = R.string.show_barcode_body)
                    val start = body.indexOf(clickHere)
                    val spanStyles = listOf(
                        AnnotatedString.Range(SpanStyle(fontWeight = FontWeight.Bold),
                            start = start,
                            end = start + clickHere.length
                        )
                    )

                    Text(
                        text = AnnotatedString(text = body, spanStyles = spanStyles),
                        fontFamily = nunitoSans,
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = dimensionResource(id = R.dimen.margin_padding_size_medium), end = dimensionResource(id = R.dimen.margin_padding_size_medium), bottom = dimensionResource(id = R.dimen.margin_padding_size_medium))
                    )
                }
            }
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
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.margin_padding_size_large))
                .clickable {
                    showDialog.value = true
                },
            text = stringResource(R.string.report_issue),
            fontFamily = nunitoSans,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
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

        if (issue == getString(R.string.barcode_issue_other)) {
            contactSupport()
        }
    }

}

