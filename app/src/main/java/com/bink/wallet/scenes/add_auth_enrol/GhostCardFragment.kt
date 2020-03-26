package com.bink.wallet.scenes.add_auth_enrol

import android.os.Bundle
import android.view.View
import com.bink.wallet.R

class GhostCardFragment : BaseAddAuthFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewsContent()
    }

    private fun setViewsContent() {
        viewModel.titleText.set(getString(R.string.register_ghost_card_title))
        viewModel.ctaText.set(getString(R.string.register_ghost_card_button))
    }
}