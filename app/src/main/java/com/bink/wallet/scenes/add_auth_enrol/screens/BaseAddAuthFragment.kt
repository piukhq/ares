package com.bink.wallet.scenes.add_auth_enrol.screens

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.BaseAddAuthFragmentBinding
import com.bink.wallet.model.request.membership_card.Account
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.add_auth_enrol.AuthAnimationHelper
import com.bink.wallet.scenes.add_auth_enrol.AuthNavigationHandler
import com.bink.wallet.scenes.add_auth_enrol.FormsUtil
import com.bink.wallet.scenes.add_auth_enrol.adapter.AddAuthAdapter
import com.bink.wallet.scenes.add_auth_enrol.view_models.AddAuthViewModel
import com.bink.wallet.utils.ADD_AUTH_BARCODE
import com.bink.wallet.utils.ApiErrorUtils
import com.bink.wallet.utils.ExceptionHandlingUtils
import com.bink.wallet.utils.RecyclerViewHelper
import com.bink.wallet.utils.displayModalPopup
import com.bink.wallet.utils.enums.CardType
import com.bink.wallet.utils.enums.HandledException
import com.bink.wallet.utils.hideKeyboard
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.requestCameraPermissionAndNavigate
import com.bink.wallet.utils.requestPermissionsResult
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.HttpException
import java.net.SocketTimeoutException

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
    protected var barcode: String? = null
    private var addAuthAdapter: AddAuthAdapter? = null
    private lateinit var account: com.bink.wallet.model.response.membership_plan.Account

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(args) {
            currentMembershipPlan = membershipPlan
            this@BaseAddAuthFragment.isRetryJourney = isRetryJourney
            this@BaseAddAuthFragment.membershipCardId = membershipCardId
            this@BaseAddAuthFragment.barcode = barcode
        }
        SharedPreferenceManager.isLoyaltySelected = true

        navigationHandler = AuthNavigationHandler(this, args.membershipPlan)
        animationHelper = AuthAnimationHelper(this, binding, RecyclerViewHelper())

        setKeyboardTypeToAdjustResize()

        binding.viewModel = viewModel
        binding.membershipPlan = args.membershipPlan
        binding.footerSimple.viewModel = viewModel
        binding.footerComposed.viewModel = viewModel

        addAuthAdapter = AddAuthAdapter(
            mutableListOf(),
            null,
            null,
            null,
            checkValidation = {
                if (FormsUtil.areAllFieldsValid()) {
                    viewModel.haveValidationsPassed.set(true)
                } else {
                    viewModel.haveValidationsPassed.set(false)

                }
            },
            navigateToHeader = {
                navigationHandler?.navigateToBrandHeader()
            },
            onLinkClickListener = { url ->
                findNavController().navigate(BaseAddAuthFragmentDirections.globalToWeb(url))
            },
            onNavigateToBarcodeScanListener = { account ->
                onScannerActivated(account)
            }
        )
        binding.toolbar.setNavigationOnClickListener {
            handleToolbarAction()
            findNavController().navigateUp()
        }
        binding.buttonCancel.setOnClickListener {
            handleToolbarAction()
            findNavController().navigate(BaseAddAuthFragmentDirections.globalToHome())
        }

        viewModel.addRegisterFieldsRequest.observeNonNull(this) {
            populateRecycler(it)

            barcode?.let {

                viewModel.setBarcode(it)
            }
        }

        viewModel.createCardError.observeNonNull(this) { exception ->
            binding.loadingIndicator.visibility = View.GONE
            when (ExceptionHandlingUtils.onHttpException(exception)) {
                HandledException.BAD_REQUEST -> {
                    requireContext().displayModalPopup(
                        getString(R.string.error),
                        getString(R.string.error_server_down_message)
                    )
                }
                else -> {
                    if (((exception is HttpException)
                                && exception.code() >= ApiErrorUtils.SERVER_ERROR)
                        || exception is SocketTimeoutException
                    ) {
                        requireContext().displayModalPopup(
                            requireContext().getString(R.string.error_server_down_title),
                            requireContext().getString(R.string.error_server_down_message)
                        )
                    } else {
                        requireContext().displayModalPopup(
                            getString(R.string.add_card_error_title),
                            getString(R.string.add_card_error_message)
                        )
                    }
                }
            }
        }
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>(
            ADD_AUTH_BARCODE
        )
            ?.observe(viewLifecycleOwner,
                Observer { result ->
                    onResult(result)
                })
    }

    override fun onResume() {
        super.onResume()
        animationHelper?.enableGlobalListeners(::endTransition, ::beginTransition)
    }

    private fun populateRecycler() {
        binding.authFields.apply {
            layoutManager = object : GridLayoutManager(requireContext(), 1) {
                override fun requestChildRectangleOnScreen(
                    parent: RecyclerView,
                    child: View,
                    rect: Rect,
                    immediate: Boolean
                ): Boolean {
                    return false
                }
            }
            viewModel.haveValidationsPassed.set(false)

            addAuthAdapter?.setValues(
                viewModel.addAuthItemsList,
                currentMembershipPlan,
                viewModel.titleText.get(),
                viewModel.descriptionText.get()
            )
            adapter = addAuthAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        recyclerView.clearFocus()
                    }
                }
            })
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
                if (SharedPreferenceManager.isPaymentEmpty || SharedPreferenceManager.hasNoActivePaymentCards) {
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
        binding.loadingIndicator.visibility = View.GONE
        viewModel.addAuthItemsList.clear()
        super.onPause()
    }

    override fun onDestroy() {
        originalMode?.let { activity?.window?.setSoftInputMode(it) }
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        requestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            { navigateToScanLoyaltyCard() },
            null,
            null
        )
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun beginTransition() {
        viewModel.isKeyboardHidden.set(false)
    }

    private fun endTransition() {
        viewModel.isKeyboardHidden.set(true)
    }

    private fun setKeyboardTypeToAdjustResize() {
        originalMode = activity?.window?.attributes?.softInputMode
        activity?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
    }

    private fun onScannerActivated(account: com.bink.wallet.model.response.membership_plan.Account) {
        this.account = account
        requestCameraPermissionAndNavigate(
            true
        ) { navigateToScanLoyaltyCard() }
    }

    private fun navigateToScanLoyaltyCard() {
        findNavController().navigate(
            BaseAddAuthFragmentDirections.baseAddToAddLoyaltyFragment(
                null,
                null,
                account,
                true
            )
        )
    }

    private fun onResult(result: String) {
        addAuthAdapter?.setBarcode(result)
    }

    override fun onDestroyView() {
        FormsUtil.clearForms()
        super.onDestroyView()
    }
}