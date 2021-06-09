package com.bink.wallet.scenes.browse_brands

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.BaseFragment
import com.bink.wallet.BrowseBrandsBinding
import com.bink.wallet.R
import com.bink.wallet.scenes.add.AddFragmentDirections
import com.bink.wallet.utils.*
import com.bink.wallet.utils.FirebaseEvents.BROWSE_BRANDS_VIEW
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class BrowseBrandsFragment : BaseFragment<BrowseBrandsViewModel, BrowseBrandsBinding>() {

    private val args by navArgs<BrowseBrandsFragmentArgs>()
    private val adapter = BrowseBrandsAdapter()
    private val filtersAdapter = BrandsFiltersAdapter()
    override val layoutRes = R.layout.browse_brands_fragment
    override val viewModel: BrowseBrandsViewModel by viewModel()

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar).shouldDisplayBack(requireActivity())
            .build()
    }

    override fun onResume() {
        super.onResume()
        logScreenView(BROWSE_BRANDS_VIEW)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewModel = viewModel
        viewModel.setupBrandItems(
            args.membershipPlans.toList(),
            args.membershipCards.toList().getOwnedMembershipCardsIds()
        )

        binding.brandsRecyclerView.adapter = adapter.apply {
            setOnBrandItemClickListener { membershipPlan ->
                findNavController().navigateIfAdded(
                    this@BrowseBrandsFragment,
                    BrowseBrandsFragmentDirections.browseToAddJoin(
                        membershipPlan,
                        null,
                        isFromJoinCard = false,
                        isRetryJourney = false
                    ),
                    R.id.browse_brands
                )
            }

            setOnScanItemClickListener(View.OnClickListener {
                goToScan()
            })

            registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    if (viewModel.activeFilters.value?.size ==
                        args.membershipPlans.toList().getCategories().size
                    ) {
                        binding.brandsRecyclerView.scrollToPosition(0)
                    }
                }
            })
        }
        binding.brandsRecyclerView.itemAnimator?.changeDuration = 0

        binding.filtersList.layoutManager = GridLayoutManager(context, FILTERS_COLUMNS_COUNT)
        binding.filtersList.adapter = filtersAdapter.apply {
            setOnFilterClickListener {
                viewModel.updateFilters(it)
            }
            setFilters(args.membershipPlans.toList().getCategories().map { BrandsFilter(it) })
        }

        binding.buttonClearSearch.setOnClickListener {
            viewModel.searchText.value = EMPTY_STRING
        }

        binding.buttonFilters.setOnClickListener {
            viewModel.isFilterSelected.set(!viewModel.isFilterSelected.get())
            binding.filtersList.setVisible(binding.filtersList.visibility != View.VISIBLE)
        }

        initBrowseBrandsList()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this

        viewModel.searchText.observe(viewLifecycleOwner, Observer {
            viewModel.filterBrandItems()
        })

        viewModel.activeFilters.observe(viewLifecycleOwner, Observer {
            viewModel.filterBrandItems()
        })

        viewModel.filteredBrandItems.observeNonNull(this) {
            adapter.submitList(it)
            binding.labelNoMatch.setVisible(it.isEmpty())
        }
    }

    private fun initBrowseBrandsList() {
        binding.brandsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                hideKeyboard()
            }
        })
    }

    private fun hideKeyboard() {
        val inputMethodManager =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    private fun goToScan() {
        requestCameraPermissionAndNavigate(true) {
            val directions = BrowseBrandsFragmentDirections.browseToAdd(args.membershipPlans, args.membershipCards, null, isFromAddAuth = true)
            findNavController().navigateIfAdded(this, directions)
        }
    }

    companion object {
        private const val FILTERS_COLUMNS_COUNT = 2
    }
}
