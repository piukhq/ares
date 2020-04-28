package com.bink.wallet.scenes.binkweb

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bink.wallet.BaseFragment
import com.bink.wallet.BinkWebViewBinding
import com.bink.wallet.R
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        }
        binding.webView.loadUrl(args.url)

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
    }

}