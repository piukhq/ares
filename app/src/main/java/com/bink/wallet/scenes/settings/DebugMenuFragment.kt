package com.bink.wallet.scenes.settings

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.MainActivity
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.FragmentDebugMenuBinding
import com.bink.wallet.model.DebugItem
import com.bink.wallet.model.DebugItemType
import com.bink.wallet.model.ListHolder
import com.bink.wallet.utils.enums.ApiVersion
import com.bink.wallet.utils.observeNetworkDrivenErrorNonNull
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel


class DebugMenuFragment : BaseFragment<DebugMenuViewModel, FragmentDebugMenuBinding>() {
    override val layoutRes: Int
        get() = R.layout.fragment_debug_menu
    override val viewModel: DebugMenuViewModel by viewModel()

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.debugItems.value = ListHolder()
        DebugItemsPopulation.populateItems(requireContext().resources)
            .forEach { item -> viewModel.debugItems.addItem(item) }

        binding.debugItems.let { recycler ->
            recycler.adapter =
                DebugItemAdapter(viewModel.debugItems, itemClickListener = { onDebugItemClick(it) })
            recycler.layoutManager = LinearLayoutManager(requireContext())
        }

        viewModel.logOutResponse.observeNonNull(this) {
            viewModel.clearData()

        }

        viewModel.logOutErrorResponse.observeNetworkDrivenErrorNonNull(
            requireContext(),
            this,
            "",
            "",
            true
        ) {
            viewModel.clearData()
        }

        viewModel.clearResponse.observeNonNull(this) {
            restartApplication()
        }

        viewModel.clearErrorResponse.observeNonNull(this) {
            restartApplication()
        }
    }

    private fun onDebugItemClick(item: DebugItem) {
        when (item.type) {
            DebugItemType.EMAIL,
            DebugItemType.CURRENT_VERSION -> {
                // these don't do nothing at the mom
            }
            DebugItemType.ENVIRONMENT -> {
                displayPicker()
            }
        }
    }

    private fun displayPicker() {
        val adb = AlertDialog.Builder(requireContext())
        val items =
            arrayOf<CharSequence>(
                ApiVersion.DEV.name,
                ApiVersion.STAGING.name
            )
        var selection = -1
        adb.setSingleChoiceItems(items, selection) { d, n ->
            selection = n
        }

        adb.setPositiveButton(
            getString(R.string.ok)
        ) { _, _ ->
            when (selection) {
                0 -> SharedPreferenceManager.storedApiUrl = ApiVersion.DEV.url
                1 -> SharedPreferenceManager.storedApiUrl = ApiVersion.STAGING.url
            }

            if (SharedPreferenceManager.isUserLoggedIn) {
                viewModel.logOut()
            } else {
                restartApplication()
            }
        }
        adb.setNegativeButton(getString(R.string.cancel_text), null)
        adb.show()
    }

    private fun restartApplication() {
        (requireActivity() as MainActivity).forceRunApp()
    }
}