package com.bink.wallet.scenes.settings

import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.MainActivity
import com.bink.wallet.R
import com.bink.wallet.databinding.SettingsFragmentBinding
import com.bink.wallet.modal.generic.GenericModalParameters
import com.bink.wallet.model.ListHolder
import com.bink.wallet.model.LoginData
import com.bink.wallet.model.SettingsItem
import com.bink.wallet.model.SettingsItemType
import com.bink.wallet.scenes.login.LoginRepository.Companion.DEFAULT_LOGIN_ID
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.content.Intent
import android.net.Uri


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

        binding.toolbar.title = getString(R.string.settings)

        val settingsAdapter = SettingsAdapter(
            viewModel.itemsList,
            itemClickListener = { settingsItemClick(it) })

        binding.settingsContainer.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = settingsAdapter
        }

        viewModel.retrieveStoredLoginData()
        viewModel.loginData.observeNonNull(this) {
            val items = viewModel.itemsList.value!!
            for (i in 0 until items.list.size) {
                val item = items.list[i]
                if (item.type == SettingsItemType.EMAIL_ADDRESS) {
                    val newItem =
                        SettingsItem(
                            item.title,
                            it.email,
                            item.type
                        )
                    viewModel.itemsList.setItem(i, newItem)
                }
            }
        }
        viewModel.itemsList.observe(this, this)
    }

    private fun settingsItemClick(item: SettingsItem) {
        when (item.type) {
            SettingsItemType.EMAIL_ADDRESS ->
                emailDialogOpen()
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
            SettingsItemType.FAQS ->
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.faq_url))
                    )
                )
            SettingsItemType.SECURITY_AND_PRIVACY -> {
                val directions =
                    SettingsFragmentDirections.settingsToSecurityAndPrivacy(
                            GenericModalParameters(
                                R.drawable.ic_back,
                                getString(R.string.security_and_privacy_title),
                                getString(R.string.security_and_privacy_copy)
                            )
                        )
                findNavController().navigateIfAdded(this, directions)
            }

            else -> {
                // if not handled, we do nothing, i.e. headers, info rows
            }
        }
    }

    private fun emailDialogOpen() {
        val dialog =
            SettingsEmailDialog(requireContext(), viewModel.loginData.value!!.email!!)
        dialog.newEmail.observeNonNull(this) {
            dialog.dismiss()
            val email = dialog.newEmail.value!!

            binding.progressSpinner.visibility = View.VISIBLE

            val data = MutableLiveData<LoginData>()
            data.value = LoginData(DEFAULT_LOGIN_ID, email)
            viewModel.storeLoginData(email)
            viewModel.loginData.observeNonNull(this) {
                viewModel.loginData.value.let {
                    if (it != null &&
                        it.email.equals(email)) {
                        restartApp()
                    }
                }
            }
        }
        dialog.show()
    }

    private fun restartApp() {
        // wait 3 seconds before kicking the app
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                delay(3000)
                (activity as MainActivity).restartApp()
            }
        }
    }
}