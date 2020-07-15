package com.bink.wallet.scenes.add

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.constraintlayout.widget.ConstraintSet
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddFragmentBinding
import com.bink.wallet.utils.FirebaseEvents.ADD_OPTIONS_VIEW
import com.bink.wallet.utils.INT_ONE_HUNDRED
import com.bink.wallet.utils.logPaymentCardSuccess
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.scanResult
import com.bink.wallet.utils.requestCameraPermissionAndNavigate
import com.bink.wallet.utils.requestPermissionsResult
import com.bink.wallet.utils.toolbar.FragmentToolbar
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
            requestCameraPermissionAndNavigate(
                false,
                null
            )

        }
        binding.loyaltyCardContainer.setOnClickListener {
            requestCameraPermissionAndNavigate(
                true
            ) { navigateToScanLoyaltyCard() }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.paymentCardContainer.waitForLayout { setCardMarginRelativeToButton() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        scanResult(
            requestCode,
            resultCode,
            data,
            { navigateToAddPaymentCard(it) },
            { logPaymentCardSuccess(it) })
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
            { navigateToAddPaymentCard() },
            { navigateToBrowseBrands() })

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

}
