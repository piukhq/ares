package com.bink.wallet.scenes.loyalty_details

import android.app.AlertDialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.FragmentLoyaltyCardDetailsBinding
import com.bink.wallet.modal.generic.GenericModalParameters
import com.bink.wallet.model.response.membership_card.CardBalance
import com.bink.wallet.model.response.membership_card.Earn
import com.bink.wallet.model.response.membership_card.Voucher
import com.bink.wallet.utils.*
import com.bink.wallet.utils.FirebaseEvents.FIREBASE_REQUEST_REVIEW_TRANSACTIONS
import com.bink.wallet.utils.FirebaseEvents.LOYALTY_DETAIL_VIEW
import com.bink.wallet.utils.UtilFunctions.isNetworkAvailable
import com.bink.wallet.utils.enums.LinkStatus
import com.bink.wallet.utils.enums.LoginStatus
import com.bink.wallet.utils.enums.MembershipCardStatus
import com.bink.wallet.utils.enums.VoucherStates
import com.bink.wallet.utils.toolbar.FragmentToolbar
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
        // The reason the max is 248, is because the alpha of our toolbar background requires
        // a value up to 255. The opacity we've received from designers is 97%, so 3% of 255
        // leaves us with 248.
        const val MAX_ALPHA = 248f
        const val MIN_ALPHA = 0f
        const val MIN_DIST = 0
        const val MAX_DIST = 650
        const val currentDestination = R.id.loyalty_card_detail_fragment
    }

    private var scrollY = 0
    private var isFromPll = false
    private var isAnimating = false

    override val viewModel: LoyaltyCardDetailsViewModel by viewModel()
    override val layoutRes: Int
        get() = R.layout.fragment_loyalty_card_details

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.lifecycleOwner = this
        binding.toolbar.setNavigationIcon(R.drawable.ic_close)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateIfAdded(this, LoyaltyCardDetailsFragmentDirections.detailToHome(), currentDestination)

        }

        fetchData()

        binding.viewModel = viewModel

        arguments?.let {
            val tiles = arrayListOf<String>()
            viewModel.apply {
                membershipPlan.value = LoyaltyCardDetailsFragmentArgs.fromBundle(it).membershipPlan
                membershipCard.value = LoyaltyCardDetailsFragmentArgs.fromBundle(it).membershipCard
                isFromPll = LoyaltyCardDetailsFragmentArgs.fromBundle(it).isFromPll
                membershipPlan.value?.images
                    ?.filter { image -> image.type == 2 }
                    ?.forEach { image -> tiles.add(image.url.toString()) }
                this.tiles.value = tiles
            }
        }

        handleBrandHeader()
        setPointsModuleClickListener()
        setLinkModuleClickListener()
        handleFootersListeners()

        val colorDrawable =
            ColorDrawable(ContextCompat.getColor(requireContext(), android.R.color.white))
        colorDrawable.alpha = MIN_ALPHA.toInt()
        binding.toolbar.background = colorDrawable

        viewModel.paymentCardsMerger.observeNonNull(this) {
            if (isNetworkAvailable(requireContext(), false) &&
                isFromPll
            ) {
                viewModel.updateMembershipCard()
            } else {
                viewModel.setAccountStatus()
                viewModel.setLinkStatus()
            }
        }

        setUpScrollView(colorDrawable)

        binding.swipeLayoutLoyaltyDetails.setOnRefreshListener {
            if (isNetworkAvailable(requireActivity(), true)) {
                viewModel.updateMembershipCard(true)
            } else {
                binding.swipeLayoutLoyaltyDetails.isRefreshing = false
                viewModel.setAccountStatus()
                viewModel.setLinkStatus()
                setLoadingState(false)
            }
        }

        binding.offerTiles.layoutManager = LinearLayoutManager(context)
        binding.offerTiles.adapter = viewModel.tiles.value?.let { LoyaltyDetailsTilesAdapter(it) }

        viewModel.updatedMembershipCard.observeNonNull(this) {
            viewModel.membershipCard.value = it
            binding.swipeLayoutLoyaltyDetails.isRefreshing = false
            viewModel.setAccountStatus()
            viewModel.setLinkStatus()
        }

        viewModel.membershipCard.observeNonNull(this) { card ->
            binding.swipeLayoutLoyaltyDetails.isRefreshing = false
            viewModel.membershipPlan.value?.let { plan ->
                binding.cardHeader.linkCard(card, plan)
            }
            if (!viewModel.membershipCard.value?.vouchers.isNullOrEmpty()) {
                setupVouchers()
            }
        }

        val titleMessage =
            viewModel.membershipPlan.value?.account?.plan_name_card
                ?: getString(R.string.delete_card_ending)

        binding.footerDelete.binding.title.text =
            getString(
                R.string.delete_card_plan,
                titleMessage
            )
        binding.footerDelete.binding.description.text =
            getString(R.string.remove_card)

        val aboutTitle =
            if (viewModel.membershipPlan.value?.account?.plan_name.isNullOrEmpty()) {
                getString(R.string.about_membership)
            } else {
                getString(
                    R.string.about_membership_plan_name,
                    viewModel.membershipPlan.value?.account!!.plan_name!!
                )
            }

        binding.footerAbout.binding.title.text = aboutTitle

        if (viewModel.membershipCard.value?.vouchers.isNullOrEmpty()) {
            binding.footerPlrRewards.visibility = View.GONE
            binding.footerPlrSeparator.visibility = View.GONE
        } else {
            binding.footerPlrRewards.setOnClickListener {
                val directions =
                    viewModel.membershipCard.value?.let { card ->
                        viewModel.membershipPlan.value?.let { plan ->
                            LoyaltyCardDetailsFragmentDirections.detailToRewardsHistory(
                                plan, card
                            )
                        }
                    }
                directions?.let {
                    findNavController().navigateIfAdded(
                        this@LoyaltyCardDetailsFragment, it, currentDestination
                    )
                }
            }
        }
        viewModel.linkStatus.observeNonNull(this) { status ->
            if (viewModel.accountStatus.value != null
            ) {
                setLoadingState(false)
            } else {
                if (isNetworkAvailable(requireActivity(), false)) {
                    viewModel.fetchPaymentCards()
                }
            }
            configureLinkStatus(status)
        }

        viewModel.accountStatus.observeNonNull(this) { status ->
            configureLoginStatus(status)
        }

        viewModel.deleteError.observeErrorNonNull(
            requireContext(),
            this@LoyaltyCardDetailsFragment,
            getString(R.string.title_2_4),
            getString(
                R.string.description_2_4,
                viewModel.deleteError.value?.cause?.message,
                viewModel.deleteError.value?.localizedMessage
            ),
            true
        ) {}

        viewModel.deletedCard.observeNonNull(this@LoyaltyCardDetailsFragment) {
            findNavController().navigateIfAdded(this, R.id.global_to_home, currentDestination)
            val planId = viewModel.membershipCard.value?.membership_plan
            val uuid = viewModel.membershipCard.value?.uuid
            if (planId == null || uuid == null) {
                failedEvent(FirebaseEvents.DELETE_LOYALTY_CARD_RESPONSE_SUCCESS)
            } else {
                logEvent(
                    FirebaseEvents.DELETE_LOYALTY_CARD_RESPONSE_SUCCESS,
                    getDeleteLoyaltyCardGenericMap(planId, uuid)
                )

            }

        }

        viewModel.refreshError.observeErrorNonNull(
            requireContext(),
            true,
            this@LoyaltyCardDetailsFragment
        )

        viewModel.deleteError.observeNonNull(this) {
            val planId = viewModel.membershipCard.value?.membership_plan
            val uuid = viewModel.membershipCard.value?.uuid
            if (planId == null || uuid == null) {
                failedEvent(FirebaseEvents.DELETE_LOYALTY_CARD_RESPONSE_FAILURE)
            } else {
                logEvent(
                    FirebaseEvents.DELETE_LOYALTY_CARD_RESPONSE_FAILURE,
                    getDeleteLoyaltyCardGenericMap(planId, uuid)
                )

            }
        }
    }

    private fun setUpScrollView(colorDrawable: ColorDrawable) {
        binding.scrollView.setOnScrollChangeListener { v: NestedScrollView?, _: Int, _: Int, _: Int, _: Int ->
            val scrollValue = v?.scrollY?.let {
                getAlphaForActionBar(it)
            }!!

            if (scrollValue < MAX_ALPHA.toInt()) {
                isAnimating = false
                binding.containerToolbarTitle.visibility = View.GONE
            }

            colorDrawable.alpha = scrollValue
            if (scrollValue == MAX_ALPHA.toInt()) {
                viewModel.membershipPlan.value?.account?.company_name?.let { name ->
                    binding.toolbarTitle.text = name
                    if (!isAnimating) {
                        isAnimating = true
                        binding.containerToolbarTitle.visibility = View.VISIBLE
                        binding.containerToolbarTitle.startAnimation(
                            AnimationUtils.loadAnimation(
                                requireContext(),
                                android.R.anim.fade_in
                            )
                        )
                    }
                }
                var voucherTitle = false
                viewModel.membershipCard.value?.let { it ->
                    if (!it.vouchers.isNullOrEmpty() &&
                        it.status?.state == MembershipCardStatus.AUTHORISED.status
                    ) {
                        if (!it.vouchers.isNullOrEmpty()) {
                            it.vouchers?.first()?.let { voucher ->
                                voucherTitle = true
                                binding.toolbarSubtitle.text =
                                    getVoucherToolbarSubtitle(voucher.earn)
                            }
                        }
                    }
                }
                if (!voucherTitle) {
                    viewModel.membershipCard.value?.balances?.firstOrNull().let { balance ->
                        binding.toolbarSubtitle.text =
                            ValueDisplayUtils.displayValue(
                                balance?.value?.toFloat(),
                                balance?.prefix,
                                balance?.suffix,
                                null
                            )
                    }
                }
            }
        }
    }

    private fun fetchData() {
        if (isNetworkAvailable(requireActivity(), false)) {
            viewModel.fetchPaymentCards()
        } else {
            viewModel.fetchLocalPaymentCards()
        }
    }

    private fun viewAboutInformation() {
        var aboutText = getString(R.string.about_membership)
        var description = getString(R.string.no_plan_description_available)
        var summary = ""

        viewModel.membershipPlan.value?.account?.plan_name?.let { plan_name ->
            aboutText = getString(R.string.about_membership_title_template, plan_name)
        }
        viewModel.membershipPlan.value?.account?.plan_description?.let { plan_description ->
            description = plan_description
        }
        viewModel.membershipPlan.value?.account?.plan_summary?.let {planSummary ->
            summary = planSummary

        }

        findNavController().navigateIfAdded(
            this,
            LoyaltyCardDetailsFragmentDirections.detailToBrandHeader(
                GenericModalParameters(
                    R.drawable.ic_close,
                    true,
                    aboutText,
                    summary,
                    description2 = description,
                    firstButtonText = getString(R.string.go_to_site)
                ), viewModel.membershipPlan.value?.account?.plan_url ?: ""
            ),
            currentDestination
        )

    }

    private fun viewVoucherDetails(voucher: Voucher) {
        viewModel.membershipPlan.value?.let { membershipPlan ->
            findNavController().navigateIfAdded(
                this,
                LoyaltyCardDetailsFragmentDirections.detailToVoucher(
                    membershipPlan, voucher
                ), currentDestination
            )

        }
    }

    private fun setBalanceText(balance: CardBalance?) {
        balance?.prefix?.let { prefix ->
            binding.pointsText.text = if (balance.suffix.isNullOrEmpty()) {
                balance.formatBalance()
            } else {
                getString(
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
        with(binding) {
            if (isLoading) {
                loadingIndicator.visibility = View.VISIBLE
                linkedWrapper.visibility = View.INVISIBLE
                pointsWrapper.visibility = View.INVISIBLE
            } else {
                loadingIndicator.visibility = View.GONE
                linkedWrapper.visibility = View.VISIBLE
                pointsWrapper.visibility = View.VISIBLE
            }
        }
    }

    override fun onPause() {
        super.onPause()
        scrollY = binding.scrollView.scrollY
    }

    override fun onResume() {
        super.onResume()
        logScreenView(LOYALTY_DETAIL_VIEW)
        binding.scrollView.postDelayed({
            binding.scrollView.scrollTo(0, scrollY)
            if (isAnimating) {
                binding.containerToolbarTitle.visibility = View.VISIBLE
            } else {
                binding.containerToolbarTitle.visibility = View.GONE
            }
        }, SCROLL_DELAY)

        RequestReviewUtil.triggerViaCardDetails(this){
            logEvent(FirebaseEvents.FIREBASE_REQUEST_REVIEW, getRequestReviewMap(FIREBASE_REQUEST_REVIEW_TRANSACTIONS))
        }
    }

    private fun handleFootersListeners() {
        binding.footerAbout.setOnClickListener {
            viewAboutInformation()
        }

        binding.footerSecurity.setOnClickListener {
            val action =
                LoyaltyCardDetailsFragmentDirections.detailToSecurity(
                    GenericModalParameters(
                        R.drawable.ic_close,
                        true,
                        getString(R.string.security_and_privacy_title),
                        getString(R.string.security_and_privacy_copy),
                        description2 = getString(R.string.security_and_privacy_copy_2)
                    )
                )
            findNavController().navigateIfAdded(this, action, currentDestination)

        }

        binding.footerDelete.setOnClickListener {
            with(AlertDialog.Builder(requireContext())) {
                setMessage(getString(R.string.delete_card_modal_body))
                setNeutralButton(getString(R.string.no_text)) { _, _ -> }
                setPositiveButton(getString(R.string.yes_text)) { dialog, _ ->
                    if (isNetworkAvailable(requireActivity(), true)) {
                        runBlocking {
                            viewModel.deleteCard(viewModel.membershipCard.value?.id)
                            val planId = viewModel.membershipCard.value?.membership_plan
                            val uuid = viewModel.membershipCard.value?.uuid
                            if (planId == null || uuid == null) {
                                failedEvent(FirebaseEvents.DELETE_LOYALTY_CARD_REQUEST)
                            } else {
                                logEvent(
                                    FirebaseEvents.DELETE_LOYALTY_CARD_REQUEST,
                                    getDeleteLoyaltyCardGenericMap(planId, uuid)
                                )

                            }

                        }
                    }
                    dialog.dismiss()
                }
                create().show()
            }
        }
    }

    private fun configureLoginStatus(loginStatus: LoginStatus) {
        with(binding) {
            pointsImage.setImageDrawable(
                getDrawable(
                    requireContext(),
                    loginStatus.pointsImage
                )
            )
            loginStatus.pointsText?.let {
                pointsText.text = getString(R.string.points_login)
            }
            loginStatus.pointsDescription?.let {
                pointsDescription.text = getString(it)
            }
            pointsText.text = loginStatus.pointsText?.let { getString(it) }
        }

        when (loginStatus) {
            LoginStatus.STATUS_LOGGED_IN_HISTORY_UNAVAILABLE -> {
                viewModel.membershipCard.value?.let { card ->
                    if (!card.vouchers.isNullOrEmpty() &&
                        card.status?.state == MembershipCardStatus.AUTHORISED.status
                    ) {
                        setPlrPointsModuleText()
                    } else if (!card.balances.isNullOrEmpty()) {
                        val updateTime: Long?
                        card.balances?.first().let { balance ->
                            setBalanceText(balance)
                            updateTime = balance?.updated_at
                        }
                        val currentTime = Calendar.getInstance().timeInMillis / 1000
                        updateTime?.let {
                            val timeSinceUpdate = currentTime - it
                            binding.pointsDescription.text =
                                timeSinceUpdate.getElapsedTime(requireContext())
                        }
                    }
                }
            }
            LoginStatus.STATUS_LOGGED_IN_HISTORY_AVAILABLE -> {
                viewModel.membershipCard.value?.let {
                    if (!it.vouchers.isNullOrEmpty() &&
                        it.status?.state == MembershipCardStatus.AUTHORISED.status
                    ) {
                        setPlrPointsModuleText()
                    } else if (!it.balances.isNullOrEmpty()) {
                        it.balances?.first().let { balance ->
                            if (balance != null) {
                                setBalanceText(balance)
                            } else {
                                binding.pointsText.text = getString(R.string.points_signing_up)
                            }
                        }
                    }
                } ?: run {
                    binding.pointsText.text = getString(R.string.points_signing_up)
                }
            }
            else -> {
            }
        }
    }

    private fun setPlrPointsModuleText() {
        with(binding) {
            pointsText.text = getString(R.string.collecting)
            pointsDescription.text = getString(R.string.towards_rewards)
        }
    }

    private fun configureLinkStatus(linkStatus: LinkStatus) {
        when (linkStatus) {
            LinkStatus.STATUS_LINKED_TO_SOME_OR_ALL -> {
                viewModel.paymentCardsMerger.value?.let { paymentCards ->
                    viewModel.membershipCard.value?.let { membershipCard ->
                        val activeLinkedParams =
                            listOf(
                                MembershipPlanUtils.countLinkedPaymentCards(
                                    membershipCard,
                                    paymentCards.toMutableList()
                                ),
                                paymentCards.size
                            )
                        linkStatus.descriptionParams = activeLinkedParams
                        linkStatus.pluralSize = paymentCards.size
                    }
                }
            }
            else -> linkStatus.descriptionParams = null
        }
        with(binding) {
            activeLinked.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    linkStatus.drawable
                )
            )
            linkStatusText.text =
                getString(linkStatus.statusText)

            if (linkStatus.descriptionParams.isNullOrEmpty()) {
                linkDescription.text =
                    getString(linkStatus.descriptionText)
            } else {
                linkStatus.descriptionParams?.let { descParams ->
                    linkDescription.text = resources.getQuantityString(
                        linkStatus.descriptionText,
                        linkStatus.pluralSize,
                        descParams[0],
                        descParams[1]
                    )
                }
            }
        }
    }

    private fun setLinkModuleClickListener() {
        binding.linkedWrapper.setOnClickListener {
            when (viewModel.linkStatus.value) {
                LinkStatus.STATUS_LINKED_TO_SOME_OR_ALL -> {
                    viewModel.membershipCard.value?.let { membershipCard ->
                        val directions =
                            viewModel.membershipPlan.value?.let { membershipPlan ->
                                LoyaltyCardDetailsFragmentDirections.detailToPll(
                                    membershipCard, membershipPlan, false
                                )
                            }
                        directions?.let { _ ->
                            findNavController().navigateIfAdded(
                                this,
                                directions,
                                currentDestination
                            )

                        }
                    }
                }
                LinkStatus.STATUS_LINKABLE_NO_PAYMENT_CARDS -> {
                    val directions = viewModel.membershipPlan.value?.let { membershipPlan ->
                        viewModel.membershipCard.value?.let { membershipCard ->
                            LoyaltyCardDetailsFragmentDirections.detailToPllEmpty(
                                membershipPlan, membershipCard, true
                            )
                        }
                    }
                    directions?.let {
                        findNavController().navigateIfAdded(this, it, currentDestination)

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
                        findNavController().navigateIfAdded(this, directions, currentDestination)

                    }
                }
                LinkStatus.STATUS_LINKABLE_GENERIC_ERROR -> {
                    val directions =
                        LoyaltyCardDetailsFragmentDirections.detailToErrorModal(
                            GenericModalParameters(
                                R.drawable.ic_close,
                                true,
                                getString(R.string.title_2_4),
                                getString(R.string.description_2_4)
                            )
                        )
                    findNavController().navigateIfAdded(this, directions, currentDestination)

                }

                LinkStatus.STATUS_LINKABLE_REQUIRES_AUTH_PENDING -> {
                    pendingCardStatusModal()
                }
                LinkStatus.STATUS_UNLINKABLE -> {
                    val directions =
                        LoyaltyCardDetailsFragmentDirections.detailToErrorModal(
                            GenericModalParameters(
                                R.drawable.ic_close,
                                true,
                                getString(R.string.title_2_8),
                                getString(R.string.description_2_8_1),
                                EMPTY_STRING,
                                EMPTY_STRING,
                                EMPTY_STRING,
                                getString(R.string.description_2_8_2)
                            )
                        )
                    findNavController().navigateIfAdded(this, directions, currentDestination)

                }

                LinkStatus.STATUS_LINKABLE_REQUIRES_AUTH_PENDING_FAILED -> {
                    viewModel.membershipCard.value?.let { card ->
                        viewModel.membershipPlan.value?.let { plan ->
                            val directions =
                                LoyaltyCardDetailsFragmentDirections.detailToAddCard(
                                    plan,
                                    membershipCardId = card.id,
                                    isRetryJourney = true
                                )
                            findNavController().navigateIfAdded(
                                this,
                                directions,
                                currentDestination
                            )

                        }
                    }
                }

                LinkStatus.STATUS_LINKABLE_SIGN_UP_FAILED -> {
                    viewModel.membershipCard.value?.let { card ->
                        viewModel.membershipPlan.value?.let { plan ->
                            val directions =
                                LoyaltyCardDetailsFragmentDirections.addJoinToGetNewCardFragment(
                                    plan,
                                    isRetryJourney = true,
                                    membershipCardId = card.id
                                )
                            findNavController().navigateIfAdded(this, directions)
                        }
                    }
                }
                LinkStatus.STATUS_LINKABLE_REQUIRES_AUTH_GHOST_CARD -> {
                    viewModel.membershipCard.value?.let { card ->
                        viewModel.membershipPlan.value?.let { plan ->
                            findNavController().navigateIfAdded(
                                this,
                                LoyaltyCardDetailsFragmentDirections.detailToGhostCard(
                                    plan,
                                    membershipCardId = card.id,
                                    isRetryJourney = true
                                ),
                                currentDestination
                            )
                        }
                    }
                }
                LinkStatus.STATUS_NO_REASON_CODES -> {
                    viewModel.membershipPlan.value?.let {
                        val directions =
                            LoyaltyCardDetailsFragmentDirections.detailToAddJoin(
                                it,
                                viewModel.membershipCard.value?.id,
                                false,
                                isRetryJourney = true,
                                isFromNoReasonCodes = true
                            )
                        findNavController().navigateIfAdded(this, directions, currentDestination)

                    }
                }
                else -> {
                }
            }
        }
    }

    private fun handleBrandHeader() {
        viewModel.membershipCard.value?.let { membershipCard ->
            if (!membershipCard.card?.barcode.isNullOrEmpty() ||
                !membershipCard.card?.membership_id.isNullOrEmpty()
            ) {
                binding.cardHeader.setOnClickListener {
                    viewModel.membershipPlan.value?.let { plan ->
                        findNavController().navigateIfAdded(
                            this,
                            LoyaltyCardDetailsFragmentDirections.detailToBarcode(
                                plan, membershipCard
                            ),
                            currentDestination
                        )

                    }
                }
            }
        }
    }

    private fun pendingCardStatusModal() {
        val directions =
            LoyaltyCardDetailsFragmentDirections.detailToErrorModal(
                GenericModalParameters(
                    R.drawable.ic_close,
                    true,
                    getString(R.string.title_lcd_pending),
                    getString(R.string.description_lcd_pending)
                )
            )
        findNavController().navigateIfAdded(this, directions, currentDestination)

    }

    private fun setPointsModuleClickListener() {
        binding.pointsWrapper.setOnClickListener {
            val genericModalParameters: GenericModalParameters?
            when (viewModel.accountStatus.value) {
                LoginStatus.STATUS_LOGGED_IN_HISTORY_UNAVAILABLE,
                LoginStatus.STATUS_LOGGED_IN_HISTORY_AVAILABLE,
                LoginStatus.STATUS_LOGGED_IN_HISTORY_AND_VOUCHERS_AVAILABLE -> {
                    viewModel.membershipPlan.value?.let { membershipPlan ->
                        val hasCorrectCardType = membershipPlan.feature_set?.card_type == 2
                        val hasTransactions =
                            membershipPlan.feature_set?.transactions_available ?: false
                        val hasVouchers = membershipPlan.feature_set?.has_vouchers ?: false

                        if ((hasCorrectCardType && hasTransactions)
                            || (hasCorrectCardType && hasTransactions && hasVouchers)
                        ) {
                            val action =
                                LoyaltyCardDetailsFragmentDirections.detailToTransactions(
                                    viewModel.membershipCard.value!!,
                                    viewModel.membershipPlan.value!!
                                )

                            findNavController().navigateIfAdded(this, action, currentDestination)
                        } else {
                            viewAboutInformation()
                        }
                    }
                }
                LoginStatus.STATUS_NOT_LOGGED_IN_HISTORY_UNAVAILABLE -> {
                    genericModalParameters = GenericModalParameters(
                        R.drawable.ic_close,
                        true,
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
                    action.let {
                        findNavController().navigateIfAdded(this, action, currentDestination)
                    }
                }

                LoginStatus.STATUS_PENDING -> {
                    pendingCardStatusModal()
                }
                LoginStatus.STATUS_LOGIN_UNAVAILABLE -> {
                    viewModel.membershipPlan.value?.let {
                        genericModalParameters = GenericModalParameters(
                            R.drawable.ic_close,
                            true,
                            getString(R.string.title_1_5),
                            getString(R.string.description_1_5_part_1, it.account?.plan_name),
                            "",
                            "",
                            "",
                            getString(R.string.description_1_5_part_2)
                        )
                        val action =
                            genericModalParameters.let { params ->
                                LoyaltyCardDetailsFragmentDirections.detailToErrorModal(
                                    params
                                )
                            }
                        action.let {
                            findNavController().navigateIfAdded(this, action, currentDestination)

                        }
                    }
                }
                LoginStatus.STATUS_NOT_LOGGED_IN_HISTORY_AVAILABLE,
                LoginStatus.STATUS_CARD_ALREADY_EXISTS,
                LoginStatus.STATUS_LOGIN_FAILED -> {
                    viewModel.membershipCard.value?.let { card ->
                        viewModel.membershipPlan.value?.let { plan ->
                            val directions =
                                LoyaltyCardDetailsFragmentDirections.detailToAddCard(
                                    plan,
                                    membershipCardId = card.id,
                                    isRetryJourney = true
                                )
                            findNavController().navigateIfAdded(
                                this,
                                directions,
                                currentDestination
                            )

                        }
                    }
                }
                LoginStatus.STATUS_NO_REASON_CODES -> {
                    viewModel.membershipPlan.value?.let {
                        val directions =
                            LoyaltyCardDetailsFragmentDirections.detailToAddJoin(
                                it,
                                viewModel.membershipCard.value?.id,
                                false,
                                isRetryJourney = true,
                                isFromNoReasonCodes = true
                            )
                        findNavController().navigateIfAdded(this, directions, currentDestination)

                    }
                }
                LoginStatus.STATUS_REGISTRATION_REQUIRED_GHOST_CARD -> {
                    viewModel.membershipCard.value?.let { card ->
                        viewModel.membershipPlan.value?.let { plan ->
                            val directions =
                                LoyaltyCardDetailsFragmentDirections.detailToGhostCard(
                                    plan,
                                    membershipCardId = card.id,
                                    isRetryJourney = true
                                )
                            findNavController().navigateIfAdded(
                                this,
                                directions,
                                currentDestination
                            )

                        }
                    }
                }
                LoginStatus.STATUS_SIGN_UP_FAILED -> {
                    viewModel.membershipCard.value?.let { card ->
                        viewModel.membershipPlan.value?.let { plan ->
                            val directions =
                                LoyaltyCardDetailsFragmentDirections.addJoinToGetNewCardFragment(
                                    plan,
                                    isRetryJourney = true,
                                    membershipCardId = card.id
                                )
                            findNavController().navigateIfAdded(this, directions)
                        }
                    }
                }
                else -> {
                }
            }
        }
    }

    private fun getAlphaForActionBar(scrollY: Int): Int {
        return when {
            scrollY > MAX_DIST -> MAX_ALPHA.toInt()
            scrollY < MIN_DIST -> MIN_ALPHA.toInt()
            else -> (MAX_ALPHA / MAX_DIST * scrollY).toInt()
        }
    }

    private fun setupVouchers() {
        binding.voucherTiles.apply {
            visibility = View.VISIBLE
            layoutManager = LinearLayoutManager(requireContext())
            viewModel.membershipCard.value?.vouchers?.filter {
                it.state == VoucherStates.IN_PROGRESS.state ||
                        it.state == VoucherStates.ISSUED.state
            }?.let { vouchers ->
                adapter = VouchersAdapter(
                    vouchers
                ).apply {
                    setOnVoucherClickListener { voucher ->
                        viewVoucherDetails(voucher)
                    }
                }
            }
        }
    }

    private fun getVoucherToolbarSubtitle(earn: Earn?): String =
        if (earn?.suffix != null) {
            getString(
                R.string.voucher_stamp_collected,
                earn.value?.toInt(),
                earn.target_value?.toInt(),
                earn.suffix
            )
        } else {
            if (earn?.value?.rem(100) ?: 0 != 0f) {
                getString(
                    R.string.loyalty_wallet_plr_value,
                    ValueDisplayUtils.displayValue(
                        earn?.value,
                        earn?.prefix,
                        earn?.suffix,
                        earn?.currency,
                        forceTwoDecimals = true
                    ),
                    earn?.prefix + earn?.target_value?.toInt()
                )
            } else {
                getString(
                    R.string.loyalty_wallet_plr_value,
                    earn?.prefix + earn?.value?.toInt(),
                    earn?.prefix + earn?.target_value?.toInt()
                )
            }
        }

}