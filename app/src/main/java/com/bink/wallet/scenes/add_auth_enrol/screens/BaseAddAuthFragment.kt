package com.bink.wallet.scenes.add_auth_enrol.screens

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.BaseAddAuthFragmentBinding
import com.bink.wallet.model.request.membership_card.Account
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.add_auth_enrol.AuthAnimationHelper
import com.bink.wallet.scenes.add_auth_enrol.AuthNavigationHandler
import com.bink.wallet.scenes.add_auth_enrol.adapter.AddAuthAdapter
import com.bink.wallet.scenes.add_auth_enrol.view_models.AddAuthViewModel
import com.bink.wallet.utils.*
import com.bink.wallet.utils.enums.CardType
import com.bink.wallet.utils.toolbar.FragmentToolbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel

open class BaseAddAuthFragment : BaseFragment<AddAuthViewModel, BaseAddAuthFragmentBinding>() {

    override val layoutRes: Int
        get() = R.layout.base_add_auth_fragment
    override val viewModel: AddAuthViewModel by viewModel()

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .build()
    }

    private val args: BaseAddAuthFragmentArgs by navArgs()

    private var originalMode: Int? = null
    var membershipCardId: String? = null
    var isRetryJourney = false
    var currentMembershipPlan: MembershipPlan? = null
    var navigationHandler: AuthNavigationHandler? = null
    var animationHelper: AuthAnimationHelper? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(args) {
            currentMembershipPlan = membershipPlan
            this@BaseAddAuthFragment.membershipCardId = membershipCardId
        }
        SharedPreferenceManager.isLoyaltySelected = true

        navigationHandler = AuthNavigationHandler(this, args.membershipPlan)
        animationHelper = AuthAnimationHelper(this, binding)

        setKeyboardTypeToAdjustResize()
        setRecyclerViewBottomPadding()

        binding.viewModel = viewModel
        binding.membershipPlan = args.membershipPlan
        binding.footerSimple.viewModel = viewModel
        binding.footerComposed.viewModel = viewModel

        binding.toolbar.setNavigationOnClickListener {
            handleToolbarAction()
            findNavController().navigateUp()
        }
        binding.buttonCancel.setOnClickListener {
            handleToolbarAction()
            findNavController().navigate(AddAuthFragmentDirections.globalToHome())
        }
        binding.addJoinReward.setOnClickListener {
            navigationHandler?.navigateToBrandHeader()
        }
        viewModel.addRegisterFieldsRequest.observeNonNull(this) {
            populateRecycler(it)
        }

        binding.footerComposed.addAuthCta.setOnClickListener {
            if (viewModel.createCardError.value == null) {
                if (UtilFunctions.isNetworkAvailable(requireActivity(), true)) {
//                    viewModel.addRegisterFieldsRequest.value?.plan_documents?.map { plan ->
//                        var required = true
//                        viewModel.planDocumentsList.map { field ->
//                            if (field.second.column == plan.column) {
//                                (field.first as PlanDocument).checkbox?.let { hasCheckbox ->
//                                    if (!hasCheckbox) {
//                                        required = false
//                                    }
//                                }
//                            }
//                        }
//                        if (required && plan.value != true.toString()) {
//                            requireContext().displayModalPopup(
//                                EMPTY_STRING,
//                                getString(R.string.required_fields)
//                            )
//                            return@setOnClickListener
//                        }
//                    }
//
//                    viewModel.planFieldsList.map {
//                        if (it.first is PlanField) {
//                            if (!UtilFunctions.isValidField(
//                                    (it.first as PlanField).validation,
//                                    it.second.value
//                                )
//                            ) {
//                                requireContext().displayModalPopup(
//                                    null,
//                                    getString(R.string.all_fields_must_be_valid)
//                                )
//                                return@setOnClickListener
//                            }
//                        }
//                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        animationHelper?.enableGlobalListeners(::endTransition, ::beginTransition)
    }

    private fun populateRecycler(addRegisterFieldsRequest: Account) {
        binding.authFields.apply {
            layoutManager = GridLayoutManager(requireContext(), 1)
            adapter = AddAuthAdapter(
                viewModel.addAuthItemsList,
                checkValidation = {
                    if (!viewModel.didPlanDocumentsPassValidations(addRegisterFieldsRequest)) {
                        viewModel.haveValidationsPassed.set(false)
                        return@AddAuthAdapter
                    }
                    if (!viewModel.didPlanFieldsPassValidations()) {
                        viewModel.haveValidationsPassed.set(false)
                        return@AddAuthAdapter
                    }
                    viewModel.haveValidationsPassed.set(true)
                }
            )
        }
    }

    fun handleNavigationAfterCardCreation(membershipCard: MembershipCard, isGhost: Boolean) {
        if (viewModel.newMembershipCard.hasActiveObservers()) {
            viewModel.newMembershipCard.removeObservers(this)
        }
        when (currentMembershipPlan?.getCardType()) {
            CardType.VIEW,
            CardType.STORE -> {
                navigationHandler?.navigateToLCD(membershipCard)
            }
            CardType.PLL -> {
                if ((isGhost && membershipCard.membership_transactions.isNullOrEmpty())
                    || SharedPreferenceManager.isPaymentEmpty
                ) {
                    navigationHandler?.navigateToPllEmpty(membershipCard)
                } else {
                    navigationHandler?.navigateToPll(membershipCard)
                }
            }
        }
    }


    private fun handleToolbarAction() {
        view?.hideKeyboard()
        windowFullscreenHandler.toNormalScreen()
    }

    override fun onPause() {
        animationHelper?.disableGlobalListeners()
        super.onPause()
    }

    override fun onDestroy() {
        originalMode?.let { activity?.window?.setSoftInputMode(it) }
        super.onDestroy()
    }

    private fun beginTransition() {
        viewModel.isKeyboardHidden.set(false)
        binding.layout.loadLayoutDescription(R.xml.keyboard_toolbar)
        binding.layout.viewTreeObserver.removeOnGlobalLayoutListener(animationHelper?.footerLayoutListener)

    }

    private fun endTransition() {
        viewModel.isKeyboardHidden.set(true)
        binding.layout.loadLayoutDescription(R.xml.collapsing_toolbar)
        binding.layout.viewTreeObserver.addOnGlobalLayoutListener(animationHelper?.footerLayoutListener)
    }

    private fun setKeyboardTypeToAdjustResize() {
        originalMode = activity?.window?.attributes?.softInputMode
        activity?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
    }

    private fun setRecyclerViewBottomPadding() {
        binding.authFields.setPadding(
            0, 0, 0,
            requireContext().toDipFromPixel(requireContext().toPixelFromDip(binding.footerSimple.root.height.toFloat())).toInt()
        )
    }
}