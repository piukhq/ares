package com.bink.wallet.scenes.add_auth_enrol

import android.os.Bundle
import android.view.View
import com.bink.wallet.R
import com.bink.wallet.utils.FirebaseEvents.ENROL_FORM_VIEW
import com.bink.wallet.utils.enums.SignUpFormType
import com.bink.wallet.utils.enums.TypeOfField

class GetNewCardFragment : BaseAddAuthFragment() {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViewsContent()
        addItems()
    }

    private fun addItems() {
        binding.membershipPlan?.account?.enrol_fields?.map {
            it.typeOfField = TypeOfField.ENROL
            addPlanField(it)
        }
        binding.membershipPlan?.account?.plan_documents?.map {
            it.display?.let { display ->
                if (display.contains(SignUpFormType.ENROL.type)) {
                    addPlanDocument(it)
                }
            }
        }
        mapItems()
    }

    override fun onResume() {
        super.onResume()
        logScreenView(ENROL_FORM_VIEW)
    }

    private fun setViewsContent() {
        viewModel.titleText.set(getString(R.string.sign_up_enrol))
        viewModel.ctaText.set(getString(R.string.sign_up_text))
        viewModel.descriptionText.set(
            getString(
                R.string.enrol_description,
                binding.membershipPlan?.account?.plan_name_card
            )
        )
        viewModel.isNoAccountFooter.set(false)
    }
}