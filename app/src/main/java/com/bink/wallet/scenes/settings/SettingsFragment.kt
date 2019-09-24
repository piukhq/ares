package com.bink.wallet.scenes.settings

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.BuildConfig
import com.bink.wallet.R
import com.bink.wallet.databinding.SettingsFragmentBinding
import com.bink.wallet.model.SettingsItem
import com.bink.wallet.model.SettingsItemType
import com.bink.wallet.network.ApiConstants
import com.bink.wallet.utils.displayModalPopup
import com.bink.wallet.utils.toolbar.FragmentToolbar
import kotlinx.android.synthetic.main.settings_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : BaseFragment<SettingsViewModel, SettingsFragmentBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar).shouldDisplayBack(activity!!)
            .build()
    }

    override val layoutRes: Int
        get() = R.layout.settings_fragment
    override val viewModel: SettingsViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.toolbar.title = getString(R.string.debug_menu)

        val email = "email here"//viewModel.loginData.value?.email!!
        val itemsList: ArrayList<SettingsItem> = ArrayList()

        itemsList.add(SettingsItem(
            getString(R.string.current_version),
            versionName(),
            SettingsItemType.VERSION_NUMBER))
        itemsList.add(SettingsItem(
            getString(R.string.environment_base_url),
            ApiConstants.BASE_URL,
            SettingsItemType.BASE_URL))
        itemsList.add(SettingsItem(
            getString(R.string.current_email_address),
            email,
            SettingsItemType.EMAIL_ADDRESS))
        val settingsAdapter = SettingsAdapter(itemsList, itemClickListener = { openEmailDialog(it) })

        settings_container.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = settingsAdapter
        }

        viewModel.retrieveStoredLoginData()
        viewModel.loginData.observe(this, Observer {
            settingsAdapter.setEmail(it.email!!)
        })
    }

    fun versionName(): String =
        getString(R.string.version_name_format, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)

    fun openEmailDialog(item: SettingsItem) {
        if (item.type == SettingsItemType.EMAIL_ADDRESS) {
            requireContext().displayModalPopup(
                getString(R.string.edit_email_address),
                getString(R.string.edit_email_description)
            )
        }
    }
}