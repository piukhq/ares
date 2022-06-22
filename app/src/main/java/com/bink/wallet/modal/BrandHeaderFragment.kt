package com.bink.wallet.modal

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.bink.wallet.modal.generic.GenericModalFragment

class BrandHeaderFragment : GenericModalFragment() {
    var url: String? = null

     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { bundle ->
            BrandHeaderFragmentArgs.fromBundle(bundle).apply {
                setupUi(genericModalParameters)
                url = planUrl
            }
        }
    }

    override fun onFirstButtonClicked() {
        url?.let {
            if(it.isNotEmpty()){
                findNavController().navigate(BrandHeaderFragmentDirections.globalToWeb(it))
                return
            }
        }

        findNavController().popBackStack()

    }
}