package com.bink.wallet.scenes.settings

import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.BuildConfig
import com.bink.wallet.databinding.SettingsFragmentBinding
import com.bink.wallet.model.LoginData
import com.bink.wallet.model.SettingsItem
import com.bink.wallet.model.SettingsItemType
import com.bink.wallet.network.ApiConstants
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.bink.wallet.MainActivity
import com.bink.wallet.R
import com.bink.wallet.model.ListHolder
import com.bink.wallet.scenes.login.LoginRepository.Companion.DEFAULT_LOGIN_ID
import com.bink.wallet.utils.ENVIRONMENTS_TO_DEBUG
import com.bink.wallet.utils.observeNonNull
import kotlinx.coroutines.*

class SettingsFragment :
    BaseFragment<SettingsViewModel, SettingsFragmentBinding>(),
    Observer<ListHolder<SettingsItem>> {

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar).shouldDisplayBack(requireActivity())
            .build()
    }

    private val buildTypes: List<String> = ENVIRONMENTS_TO_DEBUG

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

        if (buildTypes.contains(BuildConfig.BUILD_TYPE)) {
            viewModel.itemsList.value = ListHolder()
            viewModel.itemsList.addItem(
                SettingsItem(
                    getString(R.string.current_version),
                    versionName(),
                    SettingsItemType.VERSION_NUMBER
                )
            )
            viewModel.itemsList.addItem(
                SettingsItem(
                    getString(R.string.environment_base_url),
                    ApiConstants.BASE_URL,
                    SettingsItemType.BASE_URL
                )
            )
            viewModel.itemsList.addItem(
                SettingsItem(
                    getString(R.string.current_email_address),
                    "",
                    SettingsItemType.EMAIL_ADDRESS
                )
            )

            binding.toolbar.title = getString(R.string.debug_menu)

            val settingsAdapter = SettingsAdapter(
                viewModel.itemsList,
                itemClickListener = { openEmailDialog(it) })

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
                                it.email!!,
                                item.type)
                        viewModel.itemsList.setItem(i, newItem)
                    }
                }
            }
            viewModel.itemsList.observe(this, this)
        }
    }

    private fun versionName(): String =
        getString(R.string.version_name_format, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)

    private fun openEmailDialog(item: SettingsItem) {
        if (item.type == SettingsItemType.EMAIL_ADDRESS) {
            val dialog = SettingsEmailDialog(requireContext(), viewModel.loginData.value!!.email!!)
            dialog.newEmail.observeNonNull(this) {
                dialog.dismiss()
                val email = dialog.newEmail.value!!

                binding.progressSpinner.visibility = View.VISIBLE

                val data = MutableLiveData<LoginData>()
                data.value = LoginData(DEFAULT_LOGIN_ID, email)
                viewModel.storeLoginData(email)
                viewModel.loginData.observe(this, Observer {
                    if (viewModel.loginData.value!!.email.equals(email)) {
                        restartApp()
                    }
                })
            }
            dialog.show()
        }
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