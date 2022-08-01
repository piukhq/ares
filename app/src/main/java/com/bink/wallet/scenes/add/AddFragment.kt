package com.bink.wallet.scenes.add

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintSet
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddFragmentBinding
import com.bink.wallet.utils.*
import com.bink.wallet.utils.FirebaseEvents.ADD_OPTIONS_VIEW
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddFragment : BaseFragment<AddViewModel, AddFragmentBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
    }

    override val layoutRes: Int
        get() = R.layout.add_fragment

    override val viewModel: AddViewModel by viewModel()

    private val marginPercent = 75

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                requestPermissionsResult(
                    { navigateToScanLoyaltyCard() },
                    { navigateToAddPaymentCard() },
                    { navigateToBrowseBrands() },
                    isGranted
                )

            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }

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
                requestPermissionLauncher,
                false,
                null,
                { navigateToAddPaymentCard() },
                { navigateToBrowseBrands() }
            )
        }
        binding.loyaltyCardContainer.setOnClickListener {
            requestCameraPermissionAndNavigate(
                requestPermissionLauncher,
                true,
                { navigateToScanLoyaltyCard() },
                { navigateToAddPaymentCard() },
                { navigateToBrowseBrands() }
            )
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
        viewModel.membershipCards.value?.let { membershipCards ->
            viewModel.membershipPlans.value?.let { membershipPlans ->
                val directions = AddFragmentDirections.addToAddLoyalty(
                    membershipPlans.toTypedArray(),
                    membershipCards.toTypedArray(),
                    null
                )
                findNavController().navigateIfAdded(this, directions)
            }
        }
    }

    private fun navigateToBrowseBrands() {
        viewModel.membershipCards.value?.let { membershipCards ->
            viewModel.membershipPlans.value?.let { membershipPlans ->

                val directions =
                    AddFragmentDirections.addToBrowse(
                        membershipPlans.toTypedArray(),
                        membershipCards.toTypedArray()
                    )

                directions.let { navDirections ->
                    findNavController().navigateIfAdded(
                        this,
                        navDirections
                    )
                }
            }
        }
    }

    private fun navigateToAddPaymentCard(cardNumber: String = "") {
        val directions = AddFragmentDirections.addToPcd(
            cardNumber
        )
        findNavController().navigateIfAdded(this, directions)
    }

}
