package com.bink.wallet.scenes.settings

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
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
import com.bink.wallet.model.PointScrapeResponse
import com.bink.wallet.utils.*
import com.bink.wallet.utils.enums.ApiVersion
import com.bink.wallet.utils.enums.BackendVersion
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
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
            DebugItemType.FORCE_CRASH -> {
                throw RuntimeException()
            }
            DebugItemType.TESCO_LPS -> {
                launchTescoLPSDialog()
            }
        }
    }

    private fun launchTescoLPSDialog() {
        val dialog: androidx.appcompat.app.AlertDialog
        context?.let { context ->
            val builder = androidx.appcompat.app.AlertDialog.Builder(context)
            builder.setTitle("Enter Tesco Credentials")
            val container = layoutInflater.inflate(R.layout.layout_lps_login, null)
            val etFirstName = container.findViewById<EditText>(R.id.et_email)
            val etSecondName = container.findViewById<EditText>(R.id.et_password)

            builder.setView(container).setPositiveButton("Okay", null)
            dialog = builder.create()

            dialog.show()
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (etFirstName.text.isNotEmpty() && etSecondName.text.isNotEmpty()) {
                    launchTescoLPS(etFirstName.text.toString(), etSecondName.text.toString())
                    dialog.dismiss()
                }
            }
        }
    }

    private fun launchDialog(message: String) {
        AlertDialog.Builder(context)
            .setTitle("Tesco LPS")
            .setMessage(message)
            .setPositiveButton("Okay", null)
            .show()
    }

    private fun launchTescoLPS(email: String, password: String) {
        Log.d("TescoLPS", "Email: $email, Password: $password")

        val webview = WebView(context)
        webview.visibility = View.GONE
        binding.parent.addView(webview)

        webview.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
        }

        webview.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                val jsString = when (url) {
                    "https://secure.tesco.com/account/en-GB/login?from=https://secure.tesco.com/Clubcard/MyAccount/home/Home" -> {
                        val javascriptClass = readFileText("lps_tesco_login.txt")
                        val replacedEmail = javascriptClass.replaceFirst("%@", email)
                        val replacedPassword = replacedEmail.replaceFirst("%@", password)
                        replacedPassword
                    }
                    "https://secure.tesco.com/Clubcard/MyAccount/home/Home" -> readFileText("lps_tesco_scrape.txt")
                    else -> null
                }

                jsString?.let { js ->
                    Handler().postDelayed({
                        webview.evaluateJavascript(js) { response ->
                            Log.d("TescoLPS", "Response $response")
                            var pointScrapeResponse: PointScrapeResponse
                            try {
                                pointScrapeResponse = Gson().fromJson(response, object : TypeToken<PointScrapeResponse?>() {}.type)
                                if (pointScrapeResponse.success) {
                                    pointScrapeResponse.points?.let { points ->
                                        Log.d("TescoLPS", "points $points")
                                        launchDialog("$email has $points points")
                                    }
                                } else {
                                    pointScrapeResponse.error_message?.let { error ->
                                        Log.d("TescoLPS", "error_message $error")
                                        launchDialog(error)
                                    }
                                }
                            } catch (e: JsonParseException) {
                                Log.d("TescoLPS", "JsonParseException ${e.localizedMessage}")
                                launchDialog("JsonParseException ${e.localizedMessage}")
                            }
                        }
                    }, 250)
                }

                Log.d("TescoLPS", "onPageFinished $url")
            }
        }

        webview.loadUrl("https://secure.tesco.com/account/en-GB/login?from=https://secure.tesco.com/Clubcard/MyAccount/home/Home")
    }

    fun readFileText(fileName: String): String {
        return try {
            context?.assets?.open(fileName)?.bufferedReader().use {
                it?.readText() ?: "JS Error"
            }
        } catch (e: Exception) {
            e.localizedMessage ?: ""
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
        adb.setSingleChoiceItems(items, selection) { d, n ->
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
}