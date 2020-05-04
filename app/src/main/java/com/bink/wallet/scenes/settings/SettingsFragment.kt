package com.bink.wallet.scenes.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.MainActivity
import com.bink.wallet.R
import com.bink.wallet.databinding.SettingsFragmentBinding
import com.bink.wallet.modal.generic.GenericModalParameters
import com.bink.wallet.model.ListHolder
import com.bink.wallet.model.SettingsItem
import com.bink.wallet.model.SettingsItemType
import com.bink.wallet.utils.*
import com.bink.wallet.utils.FirebaseEvents.SETTINGS_VIEW
import com.bink.wallet.utils.UtilFunctions.isNetworkAvailable
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import zendesk.core.AnonymousIdentity
import zendesk.core.Zendesk
import zendesk.support.guide.HelpCenterActivity
import zendesk.support.requestlist.RequestListActivity
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.bink.wallet.model.auth.User

class SettingsFragment :
    BaseFragment<SettingsViewModel, SettingsFragmentBinding>(),
    Observer<ListHolder<SettingsItem>> {

    private var shouldShowDetailsRequiredPrompt = false

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
        binding.tvSettingsTitle.text = getString(viewModel.getSettingsTitle())
        binding.toolbar.setNavigationIcon(R.drawable.ic_close)

        val settingsAdapter = SettingsAdapter(
            viewModel.itemsList,
            itemClickListener = { settingsItemClick(it) })

        binding.settingsContainer.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = settingsAdapter
        }
        viewModel.itemsList.observe(this, this)
        setZendeskIdentity()
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
                } catch (_: android.content.ActivityNotFoundException) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(getString(viewModel.getPlayStoreBrowserUrl(), appPackageName))
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
                if (shouldShowDetailsRequiredPrompt) {
                    showDetailsDialog(false)
                } else {
                    HelpCenterActivity.builder()
                        .show(requireActivity())
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
            SettingsItemType.TERMS_AND_CONDITIONS,
            SettingsItemType.PRIVACY_POLICY ->
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(item.url)
                    )
                )

            SettingsItemType.CONTACT_US -> {
                if (shouldShowDetailsRequiredPrompt) {
                    showDetailsDialog(true)
                } else {
                    RequestListActivity.builder()
                        .show(requireActivity())
                }
            }

            SettingsItemType.LOGOUT -> {
                if (isNetworkAvailable(requireActivity(), true)) {
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
        LocalStoreUtils.clearPreferences(requireContext())
        try {
            startActivity(
                Intent(requireContext(), MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    .apply {
                        putSessionHandlerNavigationDestination(
                            SESSION_HANDLER_DESTINATION_ONBOARDING
                        )
                    }
            )
        } catch (e: Exception) {
            (requireActivity() as MainActivity).forceRunApp()
        }
    }

    private fun setZendeskIdentity() {
        var userEmail = ""
        var userFirstName = ""
        var userSecondName = ""
        LocalStoreUtils.getAppSharedPref(LocalStoreUtils.KEY_EMAIL)?.let { safeEmail ->
            userEmail = safeEmail
        }

        LocalStoreUtils.getAppSharedPref(LocalStoreUtils.KEY_FIRST_NAME)?.let { safeFirstName ->
            userFirstName = safeFirstName
        }

        LocalStoreUtils.getAppSharedPref(LocalStoreUtils.KEY_SECOND_NAME)?.let { safeSecondName ->
            userSecondName = safeSecondName
        }

        Log.e("ConnorDebug", "first: " + userFirstName)
        Log.e("ConnorDebug", "second: " + userSecondName)

        if (userFirstName.isEmpty() || userSecondName.isEmpty()) {
            shouldShowDetailsRequiredPrompt = true
        } else {
            setZendeskIdentity(userEmail, userFirstName, userSecondName)
        }
    }

    private fun showDetailsDialog(isContactClick: Boolean) {
        val dialog: AlertDialog
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(getString(R.string.zendesk_user_details_prompt_title))
        val container = layoutInflater.inflate(R.layout.layout_zendesk_user_details, null)
        val etFirstName = container.findViewById<EditText>(R.id.et_first_name)
        val etSecondName = container.findViewById<EditText>(R.id.et_last_name)
        builder.setView(container)
            .setPositiveButton(
                getString(R.string.zendesk_user_details_prompt_cta)
                , null
            )
            .setNegativeButton(getString(android.R.string.cancel), { dialog, id ->
                dialog.cancel()
            })
        dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener {
                if (etFirstName.text.isNotEmpty() && etSecondName.text.isNotEmpty()) {
                    //todo these fields need to come from the dialog
                    var userEmail = ""
                    LocalStoreUtils.getAppSharedPref(LocalStoreUtils.KEY_EMAIL)?.let { safeEmail ->
                        userEmail = safeEmail
                    }
                    setZendeskIdentity(
                        userEmail,
                        etFirstName.text.toString(),
                        etSecondName.text.toString()
                    )
                    viewModel.putUserDetails(
                        User(
                            etFirstName.text.toString(),
                            etSecondName.text.toString()
                        )
                    )
                    if (isContactClick) {
                        RequestListActivity.builder()
                            .show(requireActivity())
                    } else {
                        HelpCenterActivity.builder()
                            .show(requireActivity())
                    }

                    dialog.dismiss()
                }
            }
    }

    private fun setZendeskIdentity(email: String, firstName: String, lastName: String) {
        shouldShowDetailsRequiredPrompt = false
        val identity = AnonymousIdentity.Builder()
            .withEmailIdentifier(email)
            .withNameIdentifier("$firstName $lastName")
            .build()
        Zendesk.INSTANCE.setIdentity(identity)
    }
}