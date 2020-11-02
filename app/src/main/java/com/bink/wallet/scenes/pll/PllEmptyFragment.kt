package com.bink.wallet.scenes.pll

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.FragmentPllEmptyBinding
import com.bink.wallet.modal.generic.GenericModalParameters
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.utils.logPaymentCardSuccess
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.requestCameraPermissionAndNavigate
import com.bink.wallet.utils.requestPermissionsResult
import com.bink.wallet.utils.scanResult
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class PllEmptyFragment : BaseFragment<PllEmptyViewModel, FragmentPllEmptyBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
    }

    override val layoutRes: Int
        get() = R.layout.fragment_pll_empty

    private var currentMembershipCard: MembershipCard? = null
    private var currentMembershipPlan: MembershipPlan? = null

    override val viewModel: PllEmptyViewModel by viewModel()

    private lateinit var pendingAdapter: PllPendingAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.viewModel = viewModel

        arguments.let { bundle ->
            if (bundle != null) {
                PllEmptyFragmentArgs.fromBundle(bundle).apply {
                    currentMembershipCard = membershipCard
                    currentMembershipPlan = membershipPlan
                    viewModel.isLCDJourney.set(isLCDJourney)
                }
            }
        }

        binding.header.setOnClickListener {
            currentMembershipPlan?.account?.plan_description?.let { planDescription ->
                findNavController().navigateIfAdded(
                    this,
                    PllEmptyFragmentDirections.pllEmptyToBrandHeader(
                        GenericModalParameters(
                            R.drawable.ic_close,
                            true,
                            currentMembershipPlan?.account?.plan_name
                                ?: getString(R.string.plan_description),
                            planDescription
                        )
                    )
                )
            }
        }

        pendingAdapter = PllPendingAdapter(mutableListOf())

        binding.rvPendingCards.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pendingAdapter
        }
        viewModel.getPaymentCards()

        viewModel.paymentCards.observeNonNull(this){
            if (hasPendingCards(it).isNotEmpty()){
                    //show recyclerView
                binding.rvPendingCards.visibility = View.GONE
                pendingAdapter.updateData(it)

                } else {
                //hide the recyclerView
                binding.rvPendingCards.visibility = View.GONE

            }
        }

        currentMembershipPlan?.let {
            binding.membershipPlan = it
        }

        binding.back.setOnClickListener {
            navigateToLCDScreen()
        }

        binding.buttonDone.setOnClickListener {
            navigateToLCDScreen()
        }

        binding.buttonAddPaymentCardNonModal.setOnClickListener {
            requestCameraPermissionAndNavigate(false, null)
        }

        binding.addPaymentCardModal.setOnClickListener {
            navigateToAddPaymentCards()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        scanResult(
            requestCode,
            resultCode,
            data,
            { navigateToAddPaymentCards(it) },
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
            null,
            { navigateToAddPaymentCards() },
            null
        )
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun navigateToAddPaymentCards(cardNumber: String = "") {
        val directions = PllEmptyFragmentDirections.pllEmptyToNewPaymentCard(
            cardNumber
        )
        findNavController().navigateIfAdded(this, directions)
    }

    private fun navigateToLCDScreen() {
        val directions =
            currentMembershipPlan?.let { membershipPlan ->
                currentMembershipCard?.let { membershipCard ->
                    PllEmptyFragmentDirections.pllEmptyToDetail(
                        membershipPlan, membershipCard
                    )
                }
            }
        directions?.let { _ -> findNavController().navigateIfAdded(this, directions) }
    }

    private fun hasPendingCards(paymentCards: List<PaymentCard>):List<PaymentCard> {
        val pendingCards = mutableListOf<PaymentCard>()

        paymentCards.forEach { card ->
            if (card.status == "pending"){
                pendingCards.add(card)
            }
        }
        return pendingCards
    }
}
