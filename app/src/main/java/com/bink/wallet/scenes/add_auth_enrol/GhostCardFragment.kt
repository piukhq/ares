package com.bink.wallet.scenes.add_auth_enrol

import android.os.Bundle
import android.view.View
import com.bink.wallet.R
import com.bink.wallet.utils.FirebaseEvents.REGISTRATION_FORM_VIEW

class GhostCardFragment : BaseAddAuthFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewsContent()
    }

    override fun onResume() {
        super.onResume()
        logScreenView(REGISTRATION_FORM_VIEW)
    }

    private fun setViewsContent() {
        viewModel.titleText.set(getString(R.string.register_ghost_card_title))
        viewModel.ctaText.set(getString(R.string.register_ghost_card_button))
        viewModel.isNoAccountFooter.set(false)
    }
}