package com.bink.wallet.scenes.binkweb

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bink.wallet.BaseFragment
import com.bink.wallet.BinkWebViewBinding
import com.bink.wallet.R
import com.bink.wallet.utils.displayModalPopup
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class BinkWebFragment : BaseFragment<BinkWebViewModel, BinkWebViewBinding>() {
    private val args by navArgs<BinkWebFragmentArgs>()
    override val layoutRes: Int
        get() = R.layout.fragment_web_view
    override val viewModel by viewModel<BinkWebViewModel>()

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder().build()
    }

    private var hasOpenedEmail = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.webPageLoadingIndicator.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.webPageLoadingIndicator.visibility = View.GONE
                binding.buttonBack.isEnabled = binding.webView.canGoBack()
                binding.buttonNext.isEnabled = binding.webView.canGoForward()
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                if(isAdded){
                    binding.webView.visibility = View.INVISIBLE
                    if (request.url.toString().startsWith("mailto:")) {
                        hasOpenedEmail = true
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.type = "message/rfc882"
                        intent.putExtra(
                            Intent.EXTRA_EMAIL,
                            arrayOf(request.url.toString().split(":")[1])
                        )
                        try {
                            startActivity(
                                Intent.createChooser(
                                    intent,
                                    getString(R.string.contact_us_select_email_client)
                                )
                            )
                        } catch (e: Exception) {
                            showWebViewError()
                        }
                    } else {
                        showWebViewError()
                    }
                }
            }
        }

        binding.buttonRefresh.setOnClickListener {
            binding.webView.reload()
        }
        binding.buttonNext.setOnClickListener {
            binding.webView.goForward()
        }
        binding.buttonBack.setOnClickListener {
            binding.webView.goBack()
        }
        binding.buttonClose.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.webView.loadUrl(args.url)
    }

    override fun onResume() {
        super.onResume()
        if (hasOpenedEmail) {
            findNavController().navigateUp()
        }
    }

    private fun showWebViewError() {
        requireContext().displayModalPopup(
            getString(R.string.webview_error_title),
            getString(R.string.webview_error_message),
            {
                findNavController().navigateUp()
            },
            isCancelable = false
        )
    }

}