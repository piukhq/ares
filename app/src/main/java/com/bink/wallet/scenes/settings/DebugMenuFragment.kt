package com.bink.wallet.scenes.settings

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.MainActivity
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.DebugMenuEditTextBinding
import com.bink.wallet.databinding.FragmentDebugMenuBinding
import com.bink.wallet.model.DebugItem
import com.bink.wallet.model.DebugItemType
import com.bink.wallet.model.ListHolder
import com.bink.wallet.scenes.points_scrapping.TescoPointsScrapping
import com.bink.wallet.utils.*
import com.bink.wallet.utils.enums.ApiVersion
import com.bink.wallet.utils.enums.BackendVersion
import com.bink.wallet.utils.toolbar.FragmentToolbar
import kotlinx.android.synthetic.main.debug_menu_edit_text.view.*
import kotlinx.android.synthetic.main.fragment_debug_menu.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class DebugMenuFragment : BaseFragment<DebugMenuViewModel, FragmentDebugMenuBinding>() {
    override val layoutRes: Int
        get() = R.layout.fragment_debug_menu
    override val viewModel: DebugMenuViewModel by viewModel()

    private var shouldApplyChanges = false
    private lateinit var client: WebViewClient

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

        viewModel.logOutResponse.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            viewModel.clearData()
        })

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

        binding.tvClubcardPoints.setOnClickListener {
            displayDialog()
        }
    }

    private fun onDebugItemClick(item: DebugItem) {
        when (item.type) {
            DebugItemType.EMAIL,
            DebugItemType.CURRENT_VERSION -> {
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
        }
    }

    private fun displayVersionPicker() {
        val adb = AlertDialog.Builder(requireContext())
        val items =
            arrayOf<CharSequence>(
                BackendVersion.VERSION_1.name,
                BackendVersion.VERSION_2.name
            )
        var selection = -1
        adb.setSingleChoiceItems(items, selection) { d, n ->
            selection = n
        }

        adb.setPositiveButton(
            getString(R.string.ok)
        ) { _, _ ->
            if (selection == 0) {
                SharedPreferenceManager.storedBackendVersion = BackendVersion.VERSION_1.version
            } else {
                SharedPreferenceManager.storedBackendVersion = BackendVersion.VERSION_2.version
            }
            shouldApplyChanges = true
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
        adb.setSingleChoiceItems(items, selection) { d, n ->
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

    private fun applyChanges() {
        if (SharedPreferenceManager.isUserLoggedIn) {
            viewModel.logOut()
        } else {
            restartApplication()
        }
    }

    private fun restartApplication() {
        (requireActivity() as MainActivity).forceRunApp()
    }

    private fun displayDialog() {
        val dialog: androidx.appcompat.app.AlertDialog
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireActivity())
        builder.setTitle(getString(R.string.points_scrapping_message))
        val container = layoutInflater.inflate(R.layout.points_scrapping_credentails, null)
        val etEmail = container.findViewById<EditText>(R.id.et_email)
        val etPassword = container.findViewById<EditText>(R.id.et_password)

        client = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                view?.loadUrl(TescoPointsScrapping.tescoLogin(etEmail, etPassword))

                Log.d("DebugMenu", "Page loaded")

                view?.evaluateJavascript(TescoPointsScrapping.getClubCardPoints(),
                    object : ValueCallback<String> {
                        override fun onReceiveValue(value: String?) {
                            Log.d("DebugMenu", "Points received is $value")
                            //We get everything back as a string,even null comes back as "null"
                            if (!value.equals("null")) {
                                val point = value?.replace("\"", " ")?.trim()
                                SharedPreferenceManager.tescoPointsBalance = point
                                point?.let { showPointsValue(it) }
                            }
                        }

                    })
            }
        }

        web_view.apply {
            webViewClient = client
            settings.javaScriptEnabled = true
        }

        builder.setView(container)
            .setPositiveButton(
                getString(R.string.points_scrapping_ok), object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        if (etEmail.text.toString().trim()
                                .isNotEmpty() && etPassword.text.toString().trim()
                                .isNotEmpty()
                        ) {
                            web_view.loadUrl(TESCO_CLUBCARD_URL)

                        }
                    }
                }
            )
            .setNegativeButton(getString(android.R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }

        dialog = builder.create()
        dialog.show()
    }

    private fun showPointsValue(points: String) {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setMessage("You currently have $points clubcard points")
        builder.setPositiveButton(getString(android.R.string.ok)) { dialogInterface, _ ->
            dialogInterface.cancel()
        }

        builder.create().show()
    }

    companion object {
        const val TESCO_CLUBCARD_URL =
            "https://secure.tesco.com/account/en-GB/login?from=https://secure.tesco.com/Clubcard/MyAccount/home/Home"
    }
}