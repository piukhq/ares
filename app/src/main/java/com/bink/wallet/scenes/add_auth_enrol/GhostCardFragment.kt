package com.bink.wallet.scenes.add_auth_enrol

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddAuthFragmentBinding
import com.bink.wallet.model.request.membership_card.Account
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.request.membership_card.PlanFieldsRequest
import com.bink.wallet.model.response.membership_plan.PlanFields
import com.bink.wallet.utils.*
import com.bink.wallet.utils.enums.FieldType
import com.bink.wallet.utils.enums.LoginStatus
import com.bink.wallet.utils.enums.TypeOfField
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel


class GhostCardFragment : BaseFragment<SignUpViewModel, AddAuthFragmentBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar).shouldDisplayBack(requireActivity())
            .build()
    }

    companion object {
        const val BARCODE_TEXT = "Barcode"
    }

    override val layoutRes: Int
        get() = R.layout.add_auth_fragment

    private val args: SignUpFragmentArgs by navArgs()

    override val viewModel: SignUpViewModel by viewModel()

    override fun onResume() {
        super.onResume()
        windowFullscreenHandler.toFullscreen()
    }

    private val planFieldsList: MutableList<Pair<PlanFields, PlanFieldsRequest>>? =
        mutableListOf()

    private val planBooleanFieldsList: MutableList<Pair<PlanFields, PlanFieldsRequest>>? =
        mutableListOf()

    private fun addFieldToList(planField: PlanFields) {
        if (planField.type == FieldType.BOOLEAN.type) {
            planBooleanFieldsList?.add(
                Pair(
                    planField, PlanFieldsRequest(
                        planField.column, ""
                    )
                )
            )
        } else {
            if (!planField.column.equals(BARCODE_TEXT))
                planFieldsList?.add(
                    Pair(
                        planField, PlanFieldsRequest(
                            planField.column, ""
                        )
                    )
                )
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val currentMembershipPlan = args.currentMembershipPlan
        val currentMembershipCard = args.membershipCard

        binding.item = currentMembershipPlan
        binding.descriptionAddAuth.text =
            getString(R.string.add_auth_description, currentMembershipPlan.account?.company_name)

        binding.toolbar.setNavigationOnClickListener {
            windowFullscreenHandler.toNormalScreen()
            activity?.onBackPressed()
        }
        binding.close.setOnClickListener {
            view?.hideKeyboard()
            windowFullscreenHandler.toNormalScreen()
            findNavController().navigateIfAdded(this, R.id.global_to_home)
        }

        if (currentMembershipCard != null) {
            if (currentMembershipPlan.feature_set?.has_points != null &&
                currentMembershipPlan.feature_set.has_points == true &&
                currentMembershipPlan.feature_set.transactions_available != null
            ) {
                if (currentMembershipPlan.feature_set.transactions_available == true) {
                    binding.descriptionAddAuth.text = getString(
                        R.string.log_in_transaction_available,
                        currentMembershipPlan.account?.plan_name_card
                    )
                } else {
                    binding.descriptionAddAuth.text =
                        getString(
                            R.string.log_in_transaction_unavailable,
                            currentMembershipPlan.account?.plan_name_card
                        )
                }
            }

            if (MembershipPlanUtils.getAccountStatus(
                    currentMembershipPlan,
                    currentMembershipCard
                ) == LoginStatus.STATUS_LOGIN_FAILED
            ) {
                binding.descriptionAddAuth.text = getString(
                    R.string.log_in_transaction_available,
                    currentMembershipPlan.account?.plan_name_card
                )
            }
        } else {
            currentMembershipPlan.account?.add_fields?.map {
                it.typeOfField = TypeOfField.ADD
                addFieldToList(it)
            }
        }

        currentMembershipPlan.account?.registration_fields?.map {
            it.typeOfField = TypeOfField.REGISTRATION
            addFieldToList(it)
        }

        if (currentMembershipCard == null)
            if (currentMembershipPlan.feature_set?.has_points != null &&
                currentMembershipPlan.feature_set.has_points == true &&
                currentMembershipPlan.feature_set.transactions_available != null
            ) {
                currentMembershipPlan.account?.add_fields?.map {
                    addFieldToList(it)
                }
            }

        currentMembershipPlan.account?.registration_fields?.map {
            addFieldToList(it)
        }

        planBooleanFieldsList?.map { planFieldsList?.add(it) }

        val addRegisterFieldsRequest = Account()

        planFieldsList?.map {

            when (it.first.typeOfField) {
                TypeOfField.ADD -> addRegisterFieldsRequest.add_fields?.add(it.second)
                TypeOfField.AUTH -> addRegisterFieldsRequest.authorise_fields?.add(it.second)
                TypeOfField.ENROL -> addRegisterFieldsRequest.enrol_fields?.add(it.second)
                else -> addRegisterFieldsRequest.registration_fields?.add(it.second)
            }

        }

        binding.authAddFields.apply {
            layoutManager = GridLayoutManager(activity, 1)
            adapter = SignUpAdapter(
                planFieldsList?.toList()!!
            )
        }

        binding.addCardButton.setOnClickListener {
            if (viewModel.createCardError.value == null) {
                if (verifyAvailableNetwork(requireActivity())) {

                    val currentRequest = MembershipCardRequest(
                        addRegisterFieldsRequest,
                        currentMembershipPlan.id
                    )

                    if (currentMembershipCard != null &&
                        MembershipPlanUtils.getAccountStatus(
                            currentMembershipPlan,
                            currentMembershipCard
                        ) == LoginStatus.STATUS_LOGIN_FAILED
                    ) {
                        viewModel.createMembershipCard(
                            currentRequest
                        )
                    } else {
                        viewModel.createMembershipCard(
                            currentRequest
                        )
                    }
                } else {
                    showNoInternetConnectionDialog()
                }
                binding.addCardButton.isEnabled = false
                binding.progressSpinner.visibility = View.VISIBLE
            }
        }

        if (viewModel.membershipCardData.hasActiveObservers())
            viewModel.membershipCardData.removeObservers(this)
        else
            viewModel.membershipCardData.observe(this, Observer {

                val currentRequest = MembershipCardRequest(
                    Account(
                        null,
                        null,
                        null,
                        addRegisterFieldsRequest.registration_fields
                    ),
                    currentMembershipPlan.id
                )

                viewModel.ghostMembershipCard(
                    it,
                    currentRequest
                )

                when (currentMembershipPlan.feature_set?.card_type) {
                    //TODO The condition is temporary removed for testing regarding to AB20-35(comment section)
//                    0, 1 -> {
//                        val directions =
//                            AddAuthFragmentDirections.addAuthToDetails(
//                                currentMembershipPlan, it
//                            )
//                        findNavController().navigateIfAdded(this, directions)
//                    }
                    0, 1, 2 -> {
                        if (it.membership_transactions != null && it.membership_transactions?.isEmpty()!!) {
                            val directions =
                                SignUpFragmentDirections.addAuthToPllEmpty(
                                    currentMembershipPlan, it
                                )
                            findNavController().navigateIfAdded(this, directions)
                        }
                    }
                }
                hideLoadingViews()
            })


        viewModel.createCardError.observeNonNull(this) {
            requireContext().displayModalPopup(
                getString(R.string.add_card_error_title),
                getString(R.string.add_card_error_message)
            )
            hideLoadingViews()
        }
    }

    private fun hideLoadingViews() {
        binding.progressSpinner.visibility = View.GONE
        viewModel.createCardError.value = null
        binding.addCardButton.isEnabled = true
    }
}
