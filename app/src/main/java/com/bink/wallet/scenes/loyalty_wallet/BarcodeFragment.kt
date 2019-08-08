package com.bink.wallet.scenes.loyalty_wallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bink.wallet.R
import com.bink.wallet.databinding.BarcodeFragmentBinding

class BarcodeFragment: Fragment() {

    private lateinit var binding: BarcodeFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.barcode_fragment, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.actionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        activity?.actionBar?.setDisplayHomeAsUpEnabled(true)
    }
}