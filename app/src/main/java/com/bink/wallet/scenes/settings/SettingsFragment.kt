package com.bink.wallet.scenes.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.BuildConfig
import com.bink.wallet.R
import com.bink.wallet.databinding.SettingsFragmentBinding
import com.bink.wallet.modal.generic.GenericModalParameters
import com.bink.wallet.model.SettingsItem
import com.bink.wallet.model.SettingsItemType
import com.bink.wallet.utils.*
import com.bink.wallet.utils.FirebaseEvents.SETTINGS_VIEW
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : BaseFragment<SettingsViewModel, SettingsFragmentBinding>() {

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar).shouldDisplayBack(requireActivity())
            .build()
    }

    override val layoutRes: Int
        get() = R.layout.settings_fragment

    override val viewModel: SettingsViewModel by viewModel()

    private val nunitoSans = FontFamily(
        Font(R.font.nunito_sans, FontWeight.Normal),
        Font(R.font.nunito_sans_extrabold, FontWeight.ExtraBold),
        Font(R.font.nunito_sans_light, FontWeight.Light)
    )

    private val showDialog = mutableStateOf(false)

    override fun onResume() {
        super.onResume()
        logScreenView(SETTINGS_VIEW)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.composeView.setContent {
            Surface(color = MaterialTheme.colors.background) {
                SettingsScreen()
            }
        }

        binding.tvSettingsTitle.text = getString(viewModel.getSettingsTitle())
        binding.toolbar.setNavigationIcon(R.drawable.ic_close)

        viewModel.userResponse.observeNonNull(this) {
            setAnalyticsUserId(it.uid)
        }

    }

    @Composable
    fun SettingsScreen() {
        val settingsList = SettingsItemsPopulation.populateItems(LocalContext.current.resources)
        val uid = LocalStoreUtils.getAppSharedPref(LocalStoreUtils.KEY_UID)
        RemoteConfigUtil().beta?.users?.firstOrNull { it.uid == uid }?.let {
            settingsList.add(3,
                SettingsItem(
                    getString(R.string.settings_menu_beta),
                    null,
                    SettingsItemType.BETA,
                    null
                )
            )
        }

        Column(modifier = Modifier.fillMaxHeight()) {
            SettingsList(
                settings = settingsList,
                modifier = Modifier.weight(1f)
            )
        }

        DeleteAccountDialog()
    }

    @Composable
    fun SettingsList(settings: List<SettingsItem>, modifier: Modifier = Modifier) {

        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(dimensionResource(id = R.dimen.margin_padding_size_medium))
        ) {
            items(items = settings) { setting ->

                when (setting.type) {
                    SettingsItemType.HEADER -> {
                        Text(
                            text = setting.title ?: "",
                            fontFamily = nunitoSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 25.sp,
                            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.margin_padding_size_medium_large))
                        )
                    }
                    SettingsItemType.FOOTER -> {
                        VersionNumber()
                    }
                    else -> {
                        val shouldShowBottomDivider = try {
                            val nextSettingType = settings[settings.indexOf(setting) + 1].type
                            nextSettingType != SettingsItemType.HEADER && nextSettingType != SettingsItemType.FOOTER
                        } catch (e: Exception) {
                            false
                        }

                        SettingCell(setting, shouldShowBottomDivider = shouldShowBottomDivider)
                    }
                }
            }
        }
    }

    @Composable
    fun VersionNumber() {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LocalStoreUtils.getAppSharedPref(LocalStoreUtils.KEY_EMAIL)?.let {
                Text(
                    text = it,
                    fontFamily = nunitoSans,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray
                )
            }

            Text(
                text = getString(
                    R.string.settings_build_version,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE
                ), color = Color.Gray
            )
        }
    }

    @Composable
    fun SettingCell(settingsItem: SettingsItem, shouldShowBottomDivider: Boolean) {
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_padding_size_small_medium)))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
            .height(
                dimensionResource(
                    id = R.dimen.settings_box_height
                )
            )
            .clickable {
                settingsItemClick(settingsItem)
            }) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = settingsItem.title ?: "",
                    fontSize = 18.sp,
                    fontFamily = nunitoSans,
                    fontWeight = FontWeight.ExtraBold
                )
                if (!settingsItem.value.isNullOrEmpty()) {
                    Text(
                        text = settingsItem.value,
                        fontFamily = nunitoSans,
                        fontWeight = FontWeight.Light,
                        fontSize = 15.sp
                    )
                }
            }
            Row {
                Image(
                    painter = painterResource(id = R.drawable.ic_right),
                    contentDescription = "Right Chevron"
                )
            }
        }

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_padding_size_small_medium)))

        if (shouldShowBottomDivider) {
            Divider()
        }
    }


    @Composable
    private fun DeleteAccountDialog() {
        if (showDialog.value) {
            Dialog(onDismissRequest = { showDialog.value = false }) {
                Column(
                    modifier = Modifier
                        .background(Color.White)
                        .padding(dimensionResource(id = R.dimen.margin_padding_size_medium))
                ) {
                    Text(
                        text = stringResource(R.string.delete_account_title),
                        modifier = Modifier
                            .padding(bottom = dimensionResource(id = R.dimen.margin_padding_size_small)),
                        fontFamily = nunitoSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = stringResource(R.string.delete_account_body),
                        modifier = Modifier
                            .padding(bottom = dimensionResource(id = R.dimen.margin_padding_size_small)),
                        fontFamily = nunitoSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(65.dp)
                            .padding(bottom = dimensionResource(id = R.dimen.margin_padding_size_small)),
                        colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.colorPrimary)),
                        onClick = {
                            showDialog.value = false
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.cancel_text),
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
                            .height(65.dp)
                            .padding(bottom = dimensionResource(id = R.dimen.margin_padding_size_small)),
                        colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.colorPrimaryDark)),
                        onClick = {
                            showDialog.value = false
                            findNavController().navigate(SettingsFragmentDirections.settingsToDeleteAccount())
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.delete_button_text),
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

    private fun settingsItemClick(item: SettingsItem) {
        when (item.type) {
            SettingsItemType.HEADER -> {
                // these items are to do nothing at all, as they'll never be clickable
            }

            SettingsItemType.RATE_APP -> {
                val appPackageName = requireContext().packageName
                try {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(getString(viewModel.getPlayStoreAppUrl(), appPackageName))
                        )
                    )
                } catch (_: ActivityNotFoundException) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(getString(viewModel.getPlayStoreBrowserUrl(), appPackageName))
                        )
                    )
                }
            }

            SettingsItemType.BETA -> {
                findNavController().navigateIfAdded(
                    this,
                    SettingsFragmentDirections.settingsToBeta()
                )
            }

            SettingsItemType.DEBUG_MENU -> {
                findNavController().navigateIfAdded(
                    this,
                    SettingsFragmentDirections.settingsToDebug()
                )
            }
            SettingsItemType.FAQS -> {
                item.url?.let { url ->
                    findNavController().navigate(
                        SettingsFragmentDirections.actionSettingsScreenToBinkWebFragment(url)
                    )
                }
            }
            SettingsItemType.SECURITY_AND_PRIVACY -> {
                val action =
                    SettingsFragmentDirections.settingsToSecurityAndPrivacy(
                        GenericModalParameters(
                            R.drawable.ic_back,
                            false,
                            getString(R.string.security_and_privacy_title),
                            getString(R.string.security_and_privacy_copy),
                            description2 = getString(R.string.security_and_privacy_copy_2)
                        )
                    )
                findNavController().navigateIfAdded(this, action)
            }
            SettingsItemType.DELETE_ACC -> {
                showDialog.value = true
            }
            SettingsItemType.HOW_IT_WORKS -> {
                val action =
                    SettingsFragmentDirections.settingsToHowItWorks(
                        GenericModalParameters(
                            R.drawable.ic_back,
                            false,
                            getString(R.string.how_it_works_title),
                            getString(R.string.how_it_works_copy)
                        )
                    )
                findNavController().navigateIfAdded(this, action)
            }
            SettingsItemType.WHO_WE_ARE -> {
                findNavController().navigate(
                    SettingsFragmentDirections.settingsToWhoAreWe()
                )
            }
            SettingsItemType.TERMS_AND_CONDITIONS,
            SettingsItemType.PRIVACY_POLICY,
            -> {
                item.url?.let { url ->
                    findNavController().navigate(
                        SettingsFragmentDirections.actionSettingsScreenToBinkWebFragment(url)
                    )
                }
            }

            SettingsItemType.CONTACT_US -> {
                contactSupport()
            }

            SettingsItemType.LOGOUT -> {
                if (UtilFunctions.isNetworkAvailable(requireActivity(), true)) {
                    requireContext().displayModalPopup(
                        getString(R.string.settings_menu_log_out),
                        getString(R.string.log_out_confirmation),
                        okAction = {
                            if (UtilFunctions.isNetworkAvailable(requireContext(), true)) {
                                logMixpanelEvent(MixpanelEvents.LOGOUT)
                                viewModel.logOut()
                            }
                        },
                        buttonText = R.string.settings_menu_log_out,
                        hasNegativeButton = true
                    )
                }
            }

            SettingsItemType.PREFERENCES -> {
                findNavController().navigateIfAdded(
                    this@SettingsFragment,
                    R.id.settings_to_preferences
                )
            }
            SettingsItemType.FOOTER -> {

            }
        }

        viewModel.logOutResponse.observeNonNull(this@SettingsFragment) {
            viewModel.clearData()
        }

        viewModel.logOutErrorResponse.observeNonNull(this@SettingsFragment) {
            viewModel.clearData()
        }

        viewModel.clearDataResponse.observeNonNull(this) {
            clearUserDetails()
        }

        viewModel.clearErrorResponse.observeNonNull(this) {
            clearUserDetails()
        }
    }

    private fun clearUserDetails() {
        viewModel.logOutResponse.removeObservers(this@SettingsFragment)
        logoutMixpanel()
        LocalStoreUtils.clearPreferences()
        try {
            getMainActivity().forceRunApp()
        } catch (e: Exception) {
            getMainActivity().forceRunApp()
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        Surface(color = MaterialTheme.colors.background) {
            SettingsScreen()
        }
    }

}