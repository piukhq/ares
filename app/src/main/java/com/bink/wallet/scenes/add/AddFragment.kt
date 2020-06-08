package com.bink.wallet.scenes.add

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddFragmentBinding
import com.bink.wallet.ui.factory.DialogFactory
import com.bink.wallet.utils.FirebaseEvents.ADD_OPTIONS_VIEW
import com.bink.wallet.utils.INT_ONE_HUNDRED
import com.bink.wallet.utils.LocalStoreUtils
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.getbouncer.cardscan.ScanActivity
import com.getbouncer.cardscan.base.ScanBaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddFragment : BaseFragment<AddViewModel, AddFragmentBinding>() {
    private val args by navArgs<AddFragmentArgs>()
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
    }

    override val layoutRes: Int
        get() = R.layout.add_fragment

    override val viewModel: AddViewModel by viewModel()

    private val marginPercent = 75
    private var hasViewedPermissionDialog = false
    private var didAttemptToAddPaymentCard = false

    override fun onResume() {
        super.onResume()
        logScreenView(ADD_OPTIONS_VIEW)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.getLocalMembershipCards()
        binding.cancelButton.setOnClickListener {
            findNavController().navigateIfAdded(
                this,
                R.id.global_to_home
            )
        }
        binding.browseBrandsContainer.setOnClickListener {
            navigateToBrowseBrands()
        }
        binding.paymentCardContainer.setOnClickListener {
            requestCameraPermissionAndNavigate(false)
        }
        binding.loyaltyCardContainer.setOnClickListener {
            requestCameraPermissionAndNavigate(true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.paymentCardContainer.waitForLayout { setCardMarginRelativeToButton() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (ScanActivity.isScanResult(requestCode)) {
            if (resultCode == ScanActivity.RESULT_OK && data != null) {
                val scanResult = ScanActivity.creditCardFromResult(data)
                scanResult?.number?.let { safeCardNumber ->
                    navigateToAddPaymentCard(safeCardNumber)
                }
            } else if (resultCode == ScanActivity.RESULT_CANCELED) {
                data?.let { safeIntent ->
                    if (safeIntent.getBooleanExtra(
                            ScanBaseActivity.RESULT_ENTER_CARD_MANUALLY_REASON,
                            false
                        )
                    ) {
                        navigateToAddPaymentCard()
                    } else if (safeIntent.getBooleanExtra(ScanActivity.RESULT_FATAL_ERROR, false)) {
                        DialogFactory.showTryAgainGenericError(requireActivity())
                    } else {
                        // We don't need to do anything here as this condition is when the user
                        // has closed the scan screen. In this case, we'll end up back at the
                        // 'AddFragment' (where we currently are) which is expected behaviour.
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (permissions[0] == Manifest.permission.CAMERA
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                if (didAttemptToAddPaymentCard) {
                    openScanPaymentCard()
                } else {
                    navigateToScanLoyaltyCard()
                }
            } else {
                val shouldShowPermissionExplanation =
                    !ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.CAMERA
                    )

                if (shouldShowPermissionExplanation && !hasViewedPermissionDialog) {
                    DialogFactory.showPermissionsSettingsDialog(requireActivity(), {
                        if (didAttemptToAddPaymentCard) {
                            navigateToAddPaymentCard()
                        } else {
                            navigateToBrowseBrands()
                        }
                    })
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun setCardMarginRelativeToButton() {
        val lastCardHeight = binding.paymentCardContainer.height
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.root)
        constraintSet.connect(
            R.id.payment_card_container,
            ConstraintSet.BOTTOM,
            R.id.cancel_button,
            ConstraintSet.TOP,
            marginPercent * lastCardHeight / INT_ONE_HUNDRED
        )
        constraintSet.applyTo(binding.root)
    }

    private inline fun View.waitForLayout(crossinline f: () -> Unit) = with(viewTreeObserver) {
        addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                removeOnGlobalLayoutListener(this)
                f()
            }
        })
    }

    private fun requestCameraPermissionAndNavigate(navigateToScanLoyaltyCard: Boolean) {
        didAttemptToAddPaymentCard = !navigateToScanLoyaltyCard
        val permission = activity?.let {
            ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.CAMERA
            )
        }

        if (permission != PackageManager.PERMISSION_GRANTED) {
            val shouldShowPermissionExplanation =
                ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.CAMERA
                )

            // This allows us to verify if the user has just seen a permission dialog before
            // we show them our permission explanation dialog (in onRequestPermissionsResult).
            // "ActivityCompat.shouldShowRequestPermissionRationale" will be true
            // until the user ticks "Don't show me this again".
            hasViewedPermissionDialog = shouldShowPermissionExplanation
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE
            )
        } else {
            if (navigateToScanLoyaltyCard) {
                navigateToScanLoyaltyCard()
            } else {
                openScanPaymentCard()
            }
        }
    }

    private fun navigateToScanLoyaltyCard() {
        viewModel.membershipCards.value?.let {
            val directions = AddFragmentDirections.addToAddLoyalty(
                args.membershipPlans,
                it.toTypedArray()
            )
            findNavController().navigateIfAdded(this, directions)
        }
    }

    private fun navigateToBrowseBrands() {
        viewModel.membershipCards.value?.let {
            Log.e("ConnorDebug", "membershipCards: " + it.size + " plans: " + args.membershipPlans.size)
            val directions = AddFragmentDirections.addToBrowse(
                args.membershipPlans,
                it.toTypedArray()
            )
            findNavController().navigateIfAdded(this, directions)
        }
    }

    private fun navigateToAddPaymentCard(cardNumber: String = "") {
        val directions = AddFragmentDirections.addToPcd(
            cardNumber
        )
        findNavController().navigateIfAdded(this, directions)
    }

    private fun openScanPaymentCard() {
        val bouncerKey = LocalStoreUtils.getAppSharedPref(
            LocalStoreUtils.KEY_BOUNCER_KEY
        ) as String

        ScanActivity.start(
            this,
            bouncerKey,
            "",
            requireContext().getString(R.string.payment_card_scanning_position),
            false,
            true
        )
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 101
    }
}
