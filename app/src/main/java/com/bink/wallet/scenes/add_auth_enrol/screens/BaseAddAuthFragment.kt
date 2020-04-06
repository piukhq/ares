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
import com.bink.wallet.model.request.membership_card.PlanFieldsRequest
import com.bink.wallet.model.response.membership_plan.PlanDocument
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.scenes.add_auth_enrol.*
import com.bink.wallet.scenes.add_auth_enrol.adapter.AddAuthAdapter
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.enums.FieldType
import com.bink.wallet.utils.enums.TypeOfField
import com.bink.wallet.utils.hideKeyboard
import com.bink.wallet.utils.navigateIfAdded
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
    val planFieldsList: MutableList<Pair<Any, PlanFieldsRequest>> =
        mutableListOf()
    val planDocumentsList: MutableList<Pair<Any, PlanFieldsRequest>> =
        mutableListOf()

    private var originalMode: Int? = null
    private lateinit var layoutListener: ViewTreeObserver.OnGlobalLayoutListener
    private lateinit var footerLayoutListener: ViewTreeObserver.OnGlobalLayoutListener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        originalMode = activity?.window?.attributes?.softInputMode

        activity?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
        binding.membershipPlan = args.membershipPlan
        binding.viewModel = viewModel
        binding.footerSimple.viewModel = viewModel
        binding.footerComposed.viewModel = viewModel

        SharedPreferenceManager.isLoyaltySelected = true

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
    }

    override fun onResume() {
        super.onResume()
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

    override fun onDestroy() {
        super.onDestroy()
        originalMode?.let { activity?.window?.setSoftInputMode(it) }
    }

    fun addPlanField(planField: PlanField) {
        val pairPlanField = Pair(
            planField, PlanFieldsRequest(
                planField.column, EMPTY_STRING
            )
        )
        if (planField.type == FieldType.BOOLEAN_OPTIONAL.type) {
            planDocumentsList.add(
                pairPlanField
            )
        } else if (!planField.column.equals(BARCODE_TEXT)) {
            planFieldsList.add(
                pairPlanField
            )
        }
    }

    fun addPlanDocument(planDocument: PlanDocument) {
        planDocumentsList.add(
            Pair(
                planDocument, PlanFieldsRequest(
                    planDocument.name, EMPTY_STRING
                )
            )
        )
    }

    override fun onPause() {
        super.onPause()
        planDocumentsList.clear()
        planFieldsList.clear()
        binding.layout.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
        binding.layout.viewTreeObserver.removeOnGlobalLayoutListener(footerLayoutListener)
    }

    fun mapItems() {
        planDocumentsList.map { planFieldsList.add(it) }
        val addRegisterFieldsRequest = Account()

        planFieldsList.map {
            if (it.first is PlanField) {
                when ((it.first as PlanField).typeOfField) {
                    TypeOfField.ADD -> addRegisterFieldsRequest.add_fields?.add(it.second)
                    TypeOfField.AUTH -> addRegisterFieldsRequest.authorise_fields?.add(it.second)
                    TypeOfField.ENROL -> addRegisterFieldsRequest.enrol_fields?.add(it.second)
                    else -> addRegisterFieldsRequest.registration_fields?.add(it.second)
                }
            } else
                addRegisterFieldsRequest.plan_documents?.add(it.second)
        }
        populateRecycler(addRegisterFieldsRequest)
    }

    private fun populateRecycler(addRegisterFieldsRequest: Account) {
        binding.authFields.apply {
            layoutManager = GridLayoutManager(activity, 1)
            adapter = AddAuthAdapter(
                planFieldsList.toList(),
                buttonRefresh = {
                    addRegisterFieldsRequest.plan_documents?.map { plan ->
                        var required = true
                        planDocumentsList.map { field ->
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

                    planFieldsList.map {
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
        binding.membershipPlan?.let { plan ->
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

    private fun handleToolbarAction() {
        view?.hideKeyboard()
        windowFullscreenHandler.toNormalScreen()
    }

    companion object {
        private const val BARCODE_TEXT = "Barcode"
    }
}