package com.bink.wallet.scenes.loyalty_details

import android.app.AlertDialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.FragmentLoyaltyCardDetailsBinding
import com.bink.wallet.modal.generic.GenericModalParameters
import com.bink.wallet.model.response.membership_card.CardBalance
import com.bink.wallet.utils.enums.LinkStatus
import com.bink.wallet.utils.enums.LoginStatus
import com.bink.wallet.utils.enums.SignUpFormType
import com.bink.wallet.utils.getElapsedTime
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.bink.wallet.utils.verifyAvailableNetwork
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class LoyaltyCardDetailsFragment :
    BaseFragment<LoyaltyCardDetailsViewModel, FragmentLoyaltyCardDetailsBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
    }

    companion object {
        const val MAX_ALPHA = 127f
        const val MIN_ALPHA = 0f
    }

    override val viewModel: LoyaltyCardDetailsViewModel by viewModel()
    override val layoutRes: Int
        get() = R.layout.fragment_loyalty_card_details

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.toolbar.setNavigationIcon(R.drawable.ic_close)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateIfAdded(this, R.id.detail_to_home)
        }

        setLoadingState(true)

        runBlocking {
            viewModel.fetchPaymentCards()
        }

        val cd = ColorDrawable(ContextCompat.getColor(requireContext(), R.color.cool_grey))
        cd.alpha = MIN_ALPHA.toInt()
        binding.toolbar.background = cd

        viewModel.paymentCards.observeNonNull(this) {
            viewModel.setLinkStatus()
        }

        arguments?.let {
            viewModel.membershipPlan.value =
                LoyaltyCardDetailsFragmentArgs.fromBundle(it).membershipPlan
            val tiles = arrayListOf<String>()
            viewModel.membershipPlan.value?.images?.filter { image -> image.type == 2 }
                ?.forEach { image -> tiles.add(image.url.toString()) }
            viewModel.tiles.value = tiles
            viewModel.membershipCard.value =
                LoyaltyCardDetailsFragmentArgs.fromBundle(it).membershipCard
            runBlocking { viewModel.updateMembershipCard() }
            binding.viewModel = viewModel
            viewModel.setAccountStatus()
        }

        viewModel.updatedMembershipCard.observeNonNull(this) {
            viewModel.membershipCard.value = it
            binding.swipeLayoutLoyaltyDetails.isRefreshing = false
            viewModel.setAccountStatus()
            viewModel.setLinkStatus()
        }

        viewModel.membershipCard.observeNonNull(this) {
            binding.swipeLayoutLoyaltyDetails.isRefreshing = false
        }


        binding.offerTiles.layoutManager = LinearLayoutManager(context)
        binding.offerTiles.adapter = viewModel.tiles.value?.let { LoyaltyDetailsTilesAdapter(it) }

        if (viewModel.membershipPlan.value?.account?.plan_name != null) {
            binding.footerDelete.binding.title.text =
                getString(
                    R.string.delete_card_plan,
                    viewModel.membershipPlan.value?.account?.plan_name
                )
        }

        val aboutTitle =
            if (viewModel.membershipPlan.value?.account!!.plan_name.isNullOrEmpty()) {
                getString(R.string.about_membership)
            } else
                getString(
                    R.string.about_membership_plan_name,
                    viewModel.membershipPlan.value?.account!!.plan_name!!
                )

        binding.footerAbout.binding.title.text = aboutTitle

        binding.footerAbout.setOnClickListener {
            val aboutText =
                if (viewModel.membershipPlan.value?.account!!.plan_name.isNullOrEmpty()) {
                    getString(R.string.about_membership)
                } else
                    viewModel.membershipPlan.value?.account!!.plan_name!!
            val description =
                if (viewModel.membershipPlan.value?.account?.plan_description.isNullOrEmpty()) {
                    getString(R.string.no_plan_description_available)
                } else
                    viewModel.membershipPlan.value?.account?.plan_description
            val directions =
                description?.let { _ ->
                    GenericModalParameters(
                        R.drawable.ic_close,
                        aboutText,
                        description,
                        getString(R.string.ok)
                    ).let { arguments ->
                        LoyaltyCardDetailsFragmentDirections.detailToAbout(
                            arguments
                        )
                    }
                }
            directions?.let { _ -> findNavController().navigateIfAdded(this, directions) }
        }

        if (viewModel.membershipCard.value?.card != null &&
            !viewModel.membershipCard.value?.card?.barcode.isNullOrEmpty()) {
            binding.cardHeader.setOnClickListener {
                val directions = viewModel.membershipCard.value?.card?.barcode_type.let { type ->
                    viewModel.membershipPlan.value?.let { plan ->
                        type?.let {
                            LoyaltyCardDetailsFragmentDirections.detailToBarcode(
                                plan, viewModel.membershipCard.value!!
                            )
                        }
                    }
                }

                directions?.let { findNavController().navigateIfAdded(this, directions) }
            }
        }

        binding.footerSecurity.setOnClickListener {
            val directions = LoyaltyCardDetailsFragmentDirections.detailToSecurity(
                GenericModalParameters(
                    R.drawable.ic_close,
                    getString(R.string.security_modal_title),
                    getString(
                        R.string.security_modal_body,
                        getString(R.string.security_modal_body_1),
                        getString(R.string.security_modal_body_2)
                                + getString(R.string.security_modal_body_3)
                    ),
                    getString(R.string.ok)
                )
            )
            findNavController().navigateIfAdded(this, directions)
        }

        viewModel.linkStatus.observeNonNull(this) { status ->
            if (viewModel.accountStatus.value != null &&
                viewModel.paymentCards.value != null) {
                setLoadingState(false)
            } else {
                runBlocking {
                    viewModel.fetchPaymentCards()
                }
            }
            configureLinkStatus(status)
        }

        binding.scrollView.setOnScrollChangeListener {
                v: NestedScrollView?, _: Int, _: Int, _: Int, _: Int ->
            cd.alpha = v?.scrollY?.let {
                getAlphaForActionBar(it)
            }!!

        }

        binding.swipeLayoutLoyaltyDetails.setOnRefreshListener {
            runBlocking {
                viewModel.updateMembershipCard()
            }
        }

        viewModel.accountStatus.observeNonNull(this) { status ->
            configureLoginStatus(status)
        }

        setPointsModuleClickListener()
        setLinkModuleClickListener()

        binding.footerDelete.setOnClickListener { footerView ->
            val builder = AlertDialog.Builder(context)
            var dialog: AlertDialog? = null
            builder.setMessage(getString(R.string.delete_card_modal_body))
            builder.setNeutralButton(getString(R.string.no_text)) { _, _ -> }
            builder.setPositiveButton(getString(R.string.yes_text)) { _, _ ->
                if (verifyAvailableNetwork(requireActivity())) {
                    runBlocking {
                        viewModel.deleteCard(viewModel.membershipCard.value?.id)
                    }
                    viewModel.deleteError.observeNonNull(this@LoyaltyCardDetailsFragment) { error ->
                        Snackbar.make(footerView, error, Snackbar.LENGTH_SHORT).show()
                        dialog?.dismiss()
                    }
                    viewModel.deletedCard.observeNonNull(this@LoyaltyCardDetailsFragment) {
                        dialog?.dismiss()
                        findNavController().navigateIfAdded(this, R.id.detail_to_home)
                    }
                } else {
                    showNoInternetConnectionDialog()
                }
            }
            dialog = builder.create()
            dialog.show()
        }
    }

    private fun setBalanceText(balance: CardBalance?) {
        balance?.prefix?.let { prefix ->
            if (balance.suffix.isNullOrEmpty()) {
                binding.pointsText.text =
                    getString(R.string.points_prefix_or_suffix, prefix, balance.value)
            } else {
                binding.pointsText.text = getString(
                    R.string.points_prefix_and_suffix,
                    prefix,
                    balance.value,
                    balance.suffix
                )
            }
        }
        balance?.suffix?.let { suffix ->
            if (balance.prefix.isNullOrEmpty()) {
                binding.pointsText.text =
                    getString(R.string.points_prefix_or_suffix, balance.value, suffix)
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingIndicator.visibility = View.VISIBLE
            binding.linkedWrapper.visibility = View.INVISIBLE
            binding.pointsWrapper.visibility = View.INVISIBLE
        } else {
            binding.loadingIndicator.visibility = View.GONE
            binding.linkedWrapper.visibility = View.VISIBLE
            binding.pointsWrapper.visibility = View.VISIBLE
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.paymentCards.value = null
    }


    private fun configureLoginStatus(loginStatus: LoginStatus) {
        binding.pointsImage.setImageDrawable(
            getDrawable(
                requireContext(),
                loginStatus.pointsImage
            )
        )
        if (loginStatus.pointsText != null) {
            binding.pointsText.text = getString(R.string.points_login)
        }
        if (loginStatus.pointsDescription != null) {
            binding.pointsDescription.text = getString(R.string.description_see_history)
        }

        binding.pointsText.text = loginStatus.pointsText?.let { getString(it) }
        binding.pointsDescription.text = loginStatus.pointsDescription?.let { getString(it) }

        when (loginStatus) {
            LoginStatus.STATUS_LOGGED_IN_HISTORY_UNAVAILABLE -> {
                val balance = viewModel.membershipCard.value?.balances?.first()
                setBalanceText(balance)
                val updateTime = balance?.updated_at
                val currentTime = Calendar.getInstance().timeInMillis / 1000
                updateTime?.let {
                    val timeSinceUpdate = currentTime - it
                    binding.pointsDescription.text =
                        timeSinceUpdate.getElapsedTime(requireContext())
                }
            }
            LoginStatus.STATUS_LOGGED_IN_HISTORY_AVAILABLE -> {
                val balance = viewModel.membershipCard.value?.balances?.first()
                if(balance != null) {
                    setBalanceText(balance)
                } else {
                    binding.pointsText.text = getString(R.string.points_signing_up)
                }
            }
            else -> {
            }
        }
    }

    private fun configureLinkStatus(linkStatus: LinkStatus) {
        when (linkStatus) {
            LinkStatus.STATUS_LINKED_TO_SOME_OR_ALL -> {
                val activeLinkedParams =
                    listOf(
                        viewModel.membershipCard.value?.payment_cards?.count { card -> card.active_link == true },
                        viewModel.paymentCards.value?.size
                    )
                linkStatus.descriptionParams = activeLinkedParams
            }
            else -> linkStatus.descriptionParams = null
        }

        binding.activeLinked.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                linkStatus.drawable
            )
        )
        binding.linkStatusText.text =
            getString(linkStatus.statusText)

        if (linkStatus.descriptionParams.isNullOrEmpty()) {
            binding.linkDescription.text =
                getString(linkStatus.descriptionText)
        } else {
            binding.linkDescription.text =
                getString(
                    linkStatus.descriptionText,
                    linkStatus.descriptionParams!![0],
                    linkStatus.descriptionParams!![1]
                )
        }
    }

    private fun setLinkModuleClickListener() {
        binding.linkedWrapper.setOnClickListener {
            when (viewModel.linkStatus.value) {
                LinkStatus.STATUS_LINKED_TO_SOME_OR_ALL -> {
                    val directions =
                        viewModel.membershipCard.value?.let { membershipCard ->
                            viewModel.membershipPlan.value?.let { membershipPlan ->
                                LoyaltyCardDetailsFragmentDirections.detailToPll(
                                    membershipCard, membershipPlan, false
                                )
                            }

                        }
                    directions?.let { _ ->
                        findNavController().navigateIfAdded(
                            this,
                            directions
                        )
                    }
                }
                LinkStatus.STATUS_LINKABLE_NO_PAYMENT_CARDS -> {
                    val directions = viewModel.membershipPlan.value?.let { membershipPlan ->
                        viewModel.membershipCard.value?.let { membershipCard ->
                            LoyaltyCardDetailsFragmentDirections.detailToPllEmpty(
                                membershipPlan, membershipCard
                            )
                        }
                    }
                    if (directions != null) {
                        findNavController().navigateIfAdded(this, directions)
                    }
                }
                LinkStatus.STATUS_LINKABLE_NO_PAYMENT_CARDS_LINKED -> {
                    val directions =
                        viewModel.membershipCard.value?.let { membershipCard ->
                            viewModel.membershipPlan.value?.let { membershipPlan ->
                                LoyaltyCardDetailsFragmentDirections.detailToPll(
                                    membershipCard, membershipPlan, false
                                )
                            }

                        }
                    directions?.let { _ ->
                        findNavController().navigateIfAdded(
                            this,
                            directions
                        )
                    }

                }
                LinkStatus.STATUS_LINKABLE_GENERIC_ERROR -> {
                    val directions =
                        LoyaltyCardDetailsFragmentDirections.detailToErrorModal(
                            GenericModalParameters(
                                R.drawable.ic_close,
                                getString(R.string.title_2_4),
                                getString(R.string.description_2_4)
                            )
                        )
                    findNavController().navigateIfAdded(this, directions)
                }

                LinkStatus.STATUS_LINKABLE_REQUIRES_AUTH_PENDING -> {
                    val directions =
                        LoyaltyCardDetailsFragmentDirections.detailToErrorModal(
                            GenericModalParameters(
                                R.drawable.ic_close,
                                getString(R.string.title_1_7),
                                getString(R.string.description_1_7)
                            )
                        )
                    findNavController().navigateIfAdded(this, directions)
                }
                LinkStatus.STATUS_UNLINKABLE -> {
                    val directions =
                        LoyaltyCardDetailsFragmentDirections.detailToErrorModal(
                            GenericModalParameters(
                                R.drawable.ic_close,
                                getString(R.string.title_2_8),
                                getString(R.string.description_2_8)
                            )
                        )
                    findNavController().navigateIfAdded(this, directions)
                }
                else -> {
                }
            }
        }
    }

    private fun setPointsModuleClickListener() {
        binding.pointsWrapper.setOnClickListener {
            val genericModalParameters: GenericModalParameters?
            when (viewModel.accountStatus.value) {
                LoginStatus.STATUS_LOGGED_IN_HISTORY_AVAILABLE -> {
                    val action =
                        LoyaltyCardDetailsFragmentDirections.detailToTransactions(
                            viewModel.membershipCard.value!!,
                            viewModel.membershipPlan.value!!
                        )
                    findNavController().navigateIfAdded(this, action)
                }
                LoginStatus.STATUS_NOT_LOGGED_IN_HISTORY_UNAVAILABLE -> {
                    genericModalParameters = GenericModalParameters(
                        R.drawable.ic_close,
                        getString(R.string.title_1_2),
                        getString(
                            R.string.description_1_2,
                            viewModel.membershipPlan.value?.account?.plan_name
                        )
                    )
                    val action =
                        genericModalParameters.let { params ->
                            LoyaltyCardDetailsFragmentDirections.detailToErrorModal(
                                params
                            )
                        }
                    action.let { findNavController().navigateIfAdded(this, action) }
                }
                LoginStatus.STATUS_LOGIN_PENDING -> {
                    genericModalParameters = GenericModalParameters(
                        R.drawable.ic_close,
                        getString(R.string.title_1_7),
                        getString(R.string.description_1_7)
                    )
                    val action =
                        genericModalParameters.let { params ->
                            LoyaltyCardDetailsFragmentDirections.detailToErrorModal(
                                params
                            )
                        }
                    action.let { findNavController().navigateIfAdded(this, action) }
                }
                LoginStatus.STATUS_SIGN_UP_PENDING -> {
                    genericModalParameters = GenericModalParameters(
                        R.drawable.ic_close,
                        getString(R.string.title_1_9),
                        getString(R.string.description_1_9)
                    )
                    val action =
                        genericModalParameters.let { params ->
                            LoyaltyCardDetailsFragmentDirections.detailToErrorModal(
                                params
                            )
                        }
                    action.let { findNavController().navigateIfAdded(this, action) }
                }
                LoginStatus.STATUS_REGISTER_GHOST_CARD_PENDING -> {
                    genericModalParameters = GenericModalParameters(
                        R.drawable.ic_close,
                        getString(R.string.title_1_11),
                        getString(R.string.description_1_11)
                    )
                    val action =
                        genericModalParameters.let { params ->
                            LoyaltyCardDetailsFragmentDirections.detailToErrorModal(
                                params
                            )
                        }
                    action.let { findNavController().navigateIfAdded(this, action) }
                }
                LoginStatus.STATUS_NOT_LOGGED_IN_HISTORY_AVAILABLE,
                LoginStatus.STATUS_LOGIN_FAILED -> {
                    val action =
                        LoyaltyCardDetailsFragmentDirections.detailToAuth(
                            SignUpFormType.ADD_AUTH,
                            viewModel.membershipPlan.value!!,
                            viewModel.membershipCard.value!!
                        )
                    findNavController().navigateIfAdded(this, action)
                }
                else -> {
                }
            }
        }
    }

    private fun getAlphaForActionBar(scrollY: Int): Int {
        val minDist = 0
        val maxDist = 650
        return when {
            scrollY > maxDist -> MAX_ALPHA.toInt()
            scrollY < minDist -> MIN_ALPHA.toInt()
            else -> (MAX_ALPHA / maxDist * scrollY).toInt()
        }
    }
}