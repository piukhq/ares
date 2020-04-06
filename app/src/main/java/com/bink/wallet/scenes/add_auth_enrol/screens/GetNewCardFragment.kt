package com.bink.wallet.scenes.add_auth_enrol.screens

import android.os.Bundle
import android.view.View
import com.bink.wallet.R
import com.bink.wallet.scenes.add_auth_enrol.view_models.GetNewCardViewModel
import com.bink.wallet.utils.FirebaseEvents.ENROL_FORM_VIEW
import org.koin.androidx.viewmodel.ext.android.viewModel

class GetNewCardFragment : BaseAddAuthFragment() {

    override val viewModel: GetNewCardViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewsContent()
        viewModel.addItems()
    }

    override fun onResume() {
        super.onResume()
        logScreenView(ENROL_FORM_VIEW)
    }

    private fun setViewsContent() {
        viewModel.titleText.set(getString(R.string.sign_up_enrol))
        viewModel.ctaText.set(getString(R.string.sign_up_text))
        viewModel.currentMembershipPlan.value?.let {
            viewModel.descriptionText.set(
                getString(
                    R.string.enrol_description,
                    it.account?.plan_name_card
                )
            )
        }
        viewModel.isNoAccountFooter.set(false)
    }
}