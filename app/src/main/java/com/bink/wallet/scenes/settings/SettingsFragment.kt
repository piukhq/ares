package com.bink.wallet.scenes.settings

import android.app.AlertDialog
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.EditText
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
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.settings_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.bink.wallet.MainActivity
import com.bink.wallet.R
import kotlinx.coroutines.*
import kotlin.collections.ArrayList

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
            "",
            SettingsItemType.EMAIL_ADDRESS))
        val settingsAdapter = SettingsAdapter(
            itemsList,
            itemClickListener = { openEmailDialog(it) })

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
            val builder = AlertDialog.Builder(context)
            val inflater = layoutInflater
            builder.setTitle(getString(R.string.edit_email_address))
            val dialogLayout = inflater.inflate(R.layout.settings_change_email_dialog, null)
            val editText  = dialogLayout.findViewById<EditText>(R.id.email)
            editText.setText(viewModel.loginData.value!!.email)
            builder.setView(dialogLayout)
            builder.setPositiveButton(getString(R.string.ok))
                { dialogInterface, _ ->
                    if (setEmail(editText.text.toString())) {
                        dialogInterface.dismiss()
                    } else {
                        val textInputLayout =
                            dialogLayout.findViewById<TextInputLayout>(R.id.text_input_layout)
                        textInputLayout.error = getString(R.string.please_enter_valid_email)
                    }
                }
            builder.setNegativeButton(getString(R.string.cancel_text))
                { dialogInterface, _ -> dialogInterface.dismiss() }
            builder.show()
        }
    }

    private fun setEmail(email: String): Boolean {
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (email != viewModel.loginData.value!!.email) {
                progress_spinner.visibility = View.VISIBLE

                val data = MutableLiveData<LoginData>()
                data.value = LoginData("0", email)
                viewModel.storeLoginData(email)
                viewModel.loginData.observe(this, Observer {
                    if (viewModel.loginData.value!!.email.equals(email)) {
                        restartApp()
                    }
                })
            }
            return true
        }
        return false
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