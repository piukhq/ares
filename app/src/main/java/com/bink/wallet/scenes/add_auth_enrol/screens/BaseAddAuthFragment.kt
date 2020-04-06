package com.bink.wallet.scenes.add_auth_enrol.screens

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.BaseAddAuthFragmentBinding
import com.bink.wallet.modal.generic.GenericModalParameters
import com.bink.wallet.model.request.membership_card.Account
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.membership_plan.PlanDocument
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.scenes.add_auth_enrol.adapter.AddAuthAdapter
import com.bink.wallet.scenes.add_auth_enrol.view_models.AddAuthViewModel
import com.bink.wallet.utils.*
import com.bink.wallet.utils.enums.FieldType
import com.bink.wallet.utils.toolbar.FragmentToolbar
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
    private lateinit var layoutListener: ViewTreeObserver.OnGlobalLayoutListener
    private lateinit var footerLayoutListener: ViewTreeObserver.OnGlobalLayoutListener
    var membershipCardId: String? = null
    var isRetryJourney = false
    var currentMembershipPlan: MembershipPlan? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentMembershipPlan = args.membershipPlan
        membershipCardId = args.membershipCardId

        SharedPreferenceManager.isLoyaltySelected = true

        setKeyboardTypeToAdjustResize()

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
            navigateToBrandHeader()
        }

        viewModel.addRegisterFieldsRequest.observeNonNull(this) {
            populateRecycler(it)
        }

        binding.footerComposed.addAuthCta.setOnClickListener {
            if (viewModel.createCardError.value == null) {
                if (UtilFunctions.isNetworkAvailable(requireActivity(), true)) {
                    viewModel.addRegisterFieldsRequest.value?.plan_documents?.map { plan ->
                        var required = true
                        viewModel.planDocumentsList.map { field ->
                            if (field.second.column == plan.column) {
                                (field.first as PlanDocument).checkbox?.let { hasCheckbox ->
                                    if (!hasCheckbox) {
                                        required = false
                                    }
                                }
                            }
                        }
                        if (required && plan.value != true.toString()) {
                            requireContext().displayModalPopup(
                                EMPTY_STRING,
                                getString(R.string.required_fields)
                            )
                            return@setOnClickListener
                        }
                    }

                    viewModel.planFieldsList.map {
                        if (it.first is PlanField) {
                            if (!UtilFunctions.isValidField(
                                    (it.first as PlanField).validation,
                                    it.second.value
                                )
                            ) {
                                requireContext().displayModalPopup(
                                    null,
                                    getString(R.string.all_fields_must_be_valid)
                                )
                                return@setOnClickListener
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        enableGlobalListeners()
    }


    private fun populateRecycler(addRegisterFieldsRequest: Account) {
        binding.authFields.apply {
            layoutManager = GridLayoutManager(requireContext(), 1)
            adapter = AddAuthAdapter(
                viewModel.planFieldsList.toList(),
                buttonRefresh = {
                    addRegisterFieldsRequest.plan_documents?.map { plan ->
                        var required = true
                        viewModel.planDocumentsList.map { field ->
                            if (field.second.column == plan.column) {
                                (field.first as PlanDocument).checkbox?.let { bool ->
                                    required = !bool
                                }
                            }
                        }
                        if (required &&
                            plan.value != true.toString()
                        ) {
//                            binding.addCardButton.isEnabled = false
                            return@AddAuthAdapter
                        }
                    }

                    viewModel.planFieldsList.map {
                        val item = it.first
                        if (item is PlanField &&
                            item.type != FieldType.BOOLEAN_OPTIONAL.type
                        ) {
                            if (it.second.value.isNullOrEmpty()) {
                                //binding.addCardButton.isEnabled = false
                                return@AddAuthAdapter
                            } else {
                                if (!UtilFunctions.isValidField(
                                        (it.first as PlanField).validation,
                                        it.second.value
                                    )
                                ) {
                                    //binding.addCardButton.isEnabled = false
                                    return@AddAuthAdapter
                                }
                            }
                        }
                    }
                }
            )
        }
    }

    private fun navigateToBrandHeader() {
        currentMembershipPlan?.let { plan ->
            if (plan.account?.plan_description != null) {
                findNavController().navigateIfAdded(
                    this,
                    BaseAddAuthFragmentDirections.baseAddAuthToBrandHeader(
                        GenericModalParameters(
                            R.drawable.ic_close,
                            true,
                            plan.account.plan_name
                                ?: getString(R.string.plan_description),
                            plan.account.plan_description
                        )
                    )
                )
            } else if (plan.account?.plan_name_card != null) {
                plan.account.plan_name?.let { planName ->
                    findNavController().navigateIfAdded(
                        this,
                        BaseAddAuthFragmentDirections.baseAddAuthToBrandHeader(
                            GenericModalParameters(
                                R.drawable.ic_close,
                                true,
                                planName
                            )
                        )
                    )
                }
            }
        }
    }

    private fun handleToolbarAction() {
        view?.hideKeyboard()
        windowFullscreenHandler.toNormalScreen()
    }

    override fun onPause() {
        super.onPause()
        disableGlobalListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        originalMode?.let { activity?.window?.setSoftInputMode(it) }
    }

    private fun enableGlobalListeners() {
        layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            handleKeyboardHiddenListener(binding.layout, ::endTransition)
            handleKeyboardVisibleListener(binding.layout, ::beginTransition)
        }
        footerLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            handleFooterFadeEffect(
                mutableListOf(binding.footerSimple.addAuthCta),
                binding.authFields,
                binding.footerSimple.footerBottomGradient
            )
            handleFooterFadeEffect(
                mutableListOf(binding.footerComposed.noAccount, binding.footerComposed.addAuthCta),
                binding.authFields,
                binding.footerComposed.footerBottomGradient
            )
        }
        binding.layout.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
        binding.layout.viewTreeObserver.addOnGlobalLayoutListener(footerLayoutListener)
    }

    private fun disableGlobalListeners() {
        binding.layout.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
        binding.layout.viewTreeObserver.removeOnGlobalLayoutListener(footerLayoutListener)
    }

    private fun beginTransition() {
        binding.layout.viewTreeObserver.removeOnGlobalLayoutListener(footerLayoutListener)
        Handler().postDelayed({
            binding.layout.transitionToState(R.id.collapsed)
            viewModel.isKeyboardHidden.set(false)
        }, 100)
    }

    private fun endTransition() {
        binding.layout.viewTreeObserver.addOnGlobalLayoutListener(footerLayoutListener)
        Handler().postDelayed({
            viewModel.isKeyboardHidden.set(true)
        }, 100)
    }

    private fun setKeyboardTypeToAdjustResize() {
        originalMode = activity?.window?.attributes?.softInputMode
        activity?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
    }

    companion object {
        const val BARCODE_TEXT = "Barcode"
    }
}