package com.bink.wallet.scenes.loyalty_wallet

import android.os.Bundle
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.BarcodeFragmentBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class BarcodeFragment : BaseFragment<BarcodeViewModel, BarcodeFragmentBinding>() {
    override val viewModel: BarcodeViewModel by viewModel()
    override val layoutRes: Int
        get() = R.layout.barcode_fragment

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
    }

}