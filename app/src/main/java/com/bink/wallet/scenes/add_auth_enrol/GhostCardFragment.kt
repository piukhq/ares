package com.bink.wallet.scenes.add_auth_enrol

import android.os.Bundle
import android.view.View
import com.bink.wallet.R
import com.bink.wallet.utils.FirebaseEvents.REGISTRATION_FORM_VIEW
import com.bink.wallet.utils.enums.SignUpFormType
import com.bink.wallet.utils.enums.TypeOfField

class GhostCardFragment : BaseAddAuthFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewsContent()
        addItems()
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

    private fun addItems() {
        with(binding.membershipPlan!!) {
            account?.add_fields?.map {
                it.typeOfField = TypeOfField.ADD
                addFieldToList(it)
            }
            account?.registration_fields?.map {
                it.typeOfField = TypeOfField.REGISTRATION
                addFieldToList(it)
            }
            account?.plan_documents?.map {
                it.display?.let { display ->
                    if (display.contains(SignUpFormType.GHOST.type)) {
                        addFieldToList(it)
                    }
                }
            }
        }
        mapItems()
    }
}