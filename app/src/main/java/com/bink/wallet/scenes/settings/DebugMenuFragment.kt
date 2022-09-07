package com.bink.wallet.scenes.settings

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.Intent.getIntent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.DebugMenuEditTextBinding
import com.bink.wallet.databinding.FragmentDebugMenuBinding
import com.bink.wallet.model.DebugItem
import com.bink.wallet.model.DebugItemType
import com.bink.wallet.model.ListHolder
import com.bink.wallet.utils.*
import com.bink.wallet.utils.enums.ApiVersion
import com.bink.wallet.utils.enums.BackendVersion
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.bumptech.glide.Glide
import org.koin.androidx.viewmodel.ext.android.viewModel


class DebugMenuFragment : BaseFragment<DebugMenuViewModel, FragmentDebugMenuBinding>() {
    override val layoutRes: Int
        get() = R.layout.fragment_debug_menu
    override val viewModel: DebugMenuViewModel by viewModel()

    private var shouldApplyChanges = false

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

        viewModel.logOutResponse.observe(viewLifecycleOwner) {
            viewModel.clearData()
        }

        viewModel.logOutErrorResponse.observeNetworkDrivenErrorNonNull(
            requireContext(),
            this,
            EMPTY_STRING,
            EMPTY_STRING,
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

        binding.applyChanges.setOnClickListener {
            if (shouldApplyChanges) {
                applyChanges()
            } else {
                requireContext().displayModalPopup(
                    getString(R.string.no_changes_to_apply),
                    getString(R.string.select_something_message)
                )
            }
        }
    }

    private fun onDebugItemClick(item: DebugItem) {
        when (item.type) {
            DebugItemType.EMAIL,
            DebugItemType.CURRENT_VERSION,
            -> {
                // these don't do nothing at the mom
            }
            DebugItemType.ENVIRONMENT -> {
                displayEnvironmentPicker()
            }
            DebugItemType.BACKEND_VERSION -> {
                displayVersionPicker()
            }
            DebugItemType.COLOR_SWATCHES -> {
                findNavController().navigateIfAdded(this, R.id.debug_to_color_swatches)
            }
            DebugItemType.FORCE_CRASH -> {
                throw RuntimeException()
            }
            DebugItemType.CARD_ON_BOARDING -> {
                displayStatePicker()
            }
            DebugItemType.RESET_CACHE -> {
                clearCache()
            }
            DebugItemType.EXPORT_NETWORK -> {
                exportNetworkRequests(SharedPreferenceManager.networkExports)
            }
            DebugItemType.CURRENT_TOKEN -> {
                //Copy to clip board and pop toast
                val clipboard = requireContext().getSystemService(ClipboardManager::class.java)
                val clip = ClipData.newPlainText(getString(R.string.current_token_title), LocalStoreUtils.getAppSharedPref(LocalStoreUtils.KEY_TOKEN))
                clipboard.setPrimaryClip(clip)
                Toast.makeText(requireContext(), getString(R.string.current_token_copied), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun displayVersionPicker() {
        val adb = AlertDialog.Builder(requireContext())
        val items =
            arrayOf<CharSequence>(
                BackendVersion.VERSION_1.name,
                BackendVersion.VERSION_2.name,
                BackendVersion.VERSION_3.name
            )
        var selection = -1
        adb.setSingleChoiceItems(items, selection) { _, n ->
            selection = n
        }

        adb.setPositiveButton(
            getString(R.string.ok)
        ) { _, _ ->
            when (selection) {
                0 -> SharedPreferenceManager.storedBackendVersion = BackendVersion.VERSION_1.version
                1 -> SharedPreferenceManager.storedBackendVersion = BackendVersion.VERSION_2.version
                else -> SharedPreferenceManager.storedBackendVersion = BackendVersion.VERSION_3.version
            }
            shouldApplyChanges = true
        }
        adb.setNegativeButton(getString(R.string.cancel_text), null)
        adb.show()
    }

    private fun displayStatePicker() {
        val adb = AlertDialog.Builder(requireContext())

        val items =
            arrayOf<CharSequence>(
                "1",
                "2",
                "3",
                "4"
            )
        var selection = -1
        adb.setSingleChoiceItems(items, selection) { _, n ->
            selection = n
        }

        adb.setPositiveButton(
            getString(R.string.ok)
        ) { _, _ ->
            when (selection) {
                0 -> SharedPreferenceManager.cardOnBoardingState = 1
                1 -> SharedPreferenceManager.cardOnBoardingState = 2
                2 -> SharedPreferenceManager.cardOnBoardingState = 3
                3 -> SharedPreferenceManager.cardOnBoardingState = 4
            }
        }
        adb.setNegativeButton(getString(R.string.cancel_text), null)
        adb.show()
    }

    private fun displayEnvironmentPicker() {
        val adb = AlertDialog.Builder(requireContext())
        val items =
            arrayOf<CharSequence>(
                ApiVersion.DEV.name,
                ApiVersion.STAGING.name,
                ApiVersion.DAEDALUS.name
            )

        val editTextView = layoutInflater.inflate(R.layout.debug_menu_edit_text, null)
        val adbBinding = DebugMenuEditTextBinding.bind(editTextView)
        adb.setView(editTextView)

        var selection = -1
        adb.setSingleChoiceItems(items, selection) { _, n ->
            selection = n
        }

        adb.setPositiveButton(
            getString(R.string.ok)
        ) { _, _ ->
            when (selection) {
                0 -> SharedPreferenceManager.storedApiUrl = ApiVersion.DEV.url
                1 -> SharedPreferenceManager.storedApiUrl = ApiVersion.STAGING.url
                2 -> SharedPreferenceManager.storedApiUrl = ApiVersion.DAEDALUS.url
                else -> if (adbBinding.etCustomBaseUrl.text.toString().trim().isNotEmpty()) {
                    SharedPreferenceManager.storedApiUrl =
                        adbBinding.etCustomBaseUrl.text.toString()
                }
            }
            shouldApplyChanges = true
        }
        adb.setNegativeButton(getString(R.string.cancel_text), null)
        adb.show()
    }

    private fun clearCache() {
        Glide.get(requireContext()).clearMemory()
        Toast.makeText(requireContext(), "Glide cache cleared", Toast.LENGTH_SHORT).show()
    }

    private fun exportNetworkRequests(content: String?) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "message/rfc822"
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.export_network))
        intent.putExtra(Intent.EXTRA_TEXT, content)
        startActivity(
            Intent.createChooser(
                intent,
                getString(R.string.export_network)
            )
        )
    }

    private fun applyChanges() {
        if (SharedPreferenceManager.isUserLoggedIn) {
            viewModel.logOut()
        } else {
            restartApplication()
        }
    }

    private fun restartApplication() {
        getMainActivity().forceRunApp()
    }
}