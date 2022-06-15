package com.bink.wallet.scenes.settings

import android.os.Bundle
import android.view.View
import android.widget.Toolbar
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.DeleteAccountFragmentBinding
import com.bink.wallet.utils.LocalStoreUtils
import com.bink.wallet.utils.getMainActivity
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class DeleteAccountFragment : BaseFragment<DeleteAccountViewModel, DeleteAccountFragmentBinding>() {

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(Toolbar(requireContext()))
            .build()
    }

    override val layoutRes: Int
        get() = R.layout.delete_account_fragment

    override val viewModel: DeleteAccountViewModel by viewModel()

    private val nunitoSans = FontFamily(
        Font(R.font.nunito_sans, FontWeight.Normal),
        Font(R.font.nunito_sans_extrabold, FontWeight.ExtraBold),
        Font(R.font.nunito_sans_light, FontWeight.Light)
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeView.setContent {
            Surface(color = MaterialTheme.colors.background) {
                DeleteAccount()
            }
        }

        viewModel.isLoading.observeNonNull(this) {
            binding.progressSpinner.visibility = if (it) View.VISIBLE else View.GONE
        }

    }

    @Composable
    private fun DeleteAccount() {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.splash_background),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
            )

            if (viewModel.requestComplete.value) {
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(
                        text = if (viewModel.deleteError.value) "Account deletion failed, please contact us" else "Account deletion is successful",
                        fontFamily = nunitoSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .padding(dimensionResource(id = R.dimen.margin_padding_size_medium)),
                        colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.colorPrimary)),
                        onClick = {
                            clearUserDetails()
                        }
                    ) {
                        Text(
                            text = "Ok",
                            fontFamily = nunitoSans,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (viewModel.deleteError.value) {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .padding(dimensionResource(id = R.dimen.margin_padding_size_medium)),
                            colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.colorPrimary)),
                            onClick = {
                                clearUserDetails()
                                contactSupport()
                            }
                        ) {
                            Text(
                                text = "Contact Us",
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
        }
    }

    private fun clearUserDetails() {
        logoutMixpanel()
        LocalStoreUtils.clearPreferences(requireContext())
        try {
            getMainActivity().forceRunApp()
        } catch (e: Exception) {
            getMainActivity().forceRunApp()
        }
    }

    @Preview
    @Composable
    private fun DeleteAccountPreview() {
        Surface(color = MaterialTheme.colors.background) {
            DeleteAccount()
        }
    }
}