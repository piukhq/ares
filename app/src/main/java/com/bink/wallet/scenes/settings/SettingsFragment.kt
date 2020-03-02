package com.bink.wallet.scenes.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.BuildConfig
import com.bink.wallet.MainActivity
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.SettingsFragmentBinding
import com.bink.wallet.modal.generic.GenericModalParameters
import com.bink.wallet.model.ListHolder
import com.bink.wallet.model.SettingsItem
import com.bink.wallet.model.SettingsItemType
import com.bink.wallet.utils.FirebaseEvents.SETTINGS_VIEW
import com.bink.wallet.utils.LocalStoreUtils
import com.bink.wallet.utils.UtilFunctions.isNetworkAvailable
import com.bink.wallet.utils.displayModalPopup
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.facebook.login.LoginManager
import org.koin.androidx.viewmodel.ext.android.viewModel


class SettingsFragment :
    BaseFragment<SettingsViewModel, SettingsFragmentBinding>(),
    Observer<ListHolder<SettingsItem>> {

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar).shouldDisplayBack(requireActivity())
            .build()
    }

    override val layoutRes: Int
        get() = R.layout.settings_fragment

    override val viewModel: SettingsViewModel by viewModel()

    override fun onResume() {
        super.onResume()
        logScreenView(SETTINGS_VIEW)
    }

    override fun onChanged(value: ListHolder<SettingsItem>?) {
        binding.settingsContainer.adapter?.let {
            value?.applyChange(it)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.itemsList.value = ListHolder()
        for (item in SettingsItemsPopulation.populateItems(resources)) {
            viewModel.itemsList.addItem(item)
        }

        with(binding.toolbar) {
            title = getString(R.string.settings)
            setNavigationIcon(R.drawable.ic_close)
        }

        val settingsAdapter = SettingsAdapter(
            viewModel.itemsList,
            itemClickListener = { settingsItemClick(it) })

        binding.settingsContainer.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = settingsAdapter
        }
        viewModel.itemsList.observe(this, this)
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
                            Uri.parse(getString(R.string.play_store_market_url, appPackageName))
                        )
                    )
                } catch (_: android.content.ActivityNotFoundException) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(getString(R.string.play_store_browser_url, appPackageName))
                        )
                    )
                }
            }

            SettingsItemType.DEBUG_MENU -> {
                findNavController().navigateIfAdded(
                    this,
                    SettingsFragmentDirections.settingsToDebug()
                )
            }
            SettingsItemType.FAQS ->
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.faq_url))
                    )
                )
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
            SettingsItemType.TERMS_AND_CONDITIONS ->
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.ts_and_cs_url))
                    )
                )

            SettingsItemType.PRIVACY_POLICY ->
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.privacy_policy_url))
                    )
                )

            SettingsItemType.CONTACT_US -> {
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.data = Uri.parse(
                    getString(
                        R.string.contact_us_mailto,
                        getString(R.string.contact_us_email_address)
                    )
                )
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.contact_us_email_subject))
                intent.putExtra(
                    Intent.EXTRA_TEXT, getString(
                        R.string.contact_us_email_message,
                        viewModel.loginData.value?.email,
                        BuildConfig.VERSION_NAME,
                        BuildConfig.VERSION_CODE.toString(),
                        android.os.Build.VERSION.RELEASE,
                        android.os.Build.VERSION.SDK_INT.toString()
                    )
                )
                try {
                    startActivity(
                        Intent.createChooser(
                            intent,
                            getString(R.string.contact_us_select_email_client)
                        )
                    )
                } catch (e: Exception) {
                    requireContext().displayModalPopup(
                        null,
                        getString(R.string.contact_us_no_email_message)
                    )
                }
            }

            SettingsItemType.LOGOUT -> {
                if (isNetworkAvailable(requireActivity(), true)) {
                    LoginManager.getInstance().logOut()
                    requireContext().displayModalPopup(
                        getString(R.string.settings_menu_log_out),
                        getString(R.string.log_out_confirmation),
                        okAction = {
                            if (isNetworkAvailable(requireContext(), true)) {
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
        }

        viewModel.logOutResponse.observeNonNull(this@SettingsFragment) {
            clearUserDetails()
        }

        viewModel.logOutErrorResponse.observeNonNull(this@SettingsFragment) {
            clearUserDetails()
        }
    }

    private fun clearUserDetails() {
        viewModel.logOutResponse.removeObservers(this@SettingsFragment)
        LocalStoreUtils.clearPreferences()
        SharedPreferenceManager.clear()
        try {
            findNavController().navigateIfAdded(
                this@SettingsFragment,
                R.id.settings_to_onboarding
            )
        } catch (e: Exception) {
            (requireActivity() as MainActivity).restartApp()
        }
    }
}