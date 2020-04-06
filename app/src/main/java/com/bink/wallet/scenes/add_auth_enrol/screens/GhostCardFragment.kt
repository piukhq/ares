package com.bink.wallet.scenes.add_auth_enrol.screens

import android.os.Bundle
import android.view.View
import com.bink.wallet.R
import com.bink.wallet.scenes.add_auth_enrol.view_models.GhostCardViewModel
import com.bink.wallet.utils.FirebaseEvents.REGISTRATION_FORM_VIEW
import org.koin.androidx.viewmodel.ext.android.viewModel

class GhostCardFragment : BaseAddAuthFragment() {

    override val viewModel: GhostCardViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewsContent()
        viewModel.addItems()
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