package com.bink.wallet.scenes.browse_brands

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.BaseFragment
import com.bink.wallet.BrowseBrandsBinding
import com.bink.wallet.R
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.FirebaseEvents.BROWSE_BRANDS_VIEW
import com.bink.wallet.utils.getCategories
import com.bink.wallet.utils.getOwnedMembershipCardsIds
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.setVisible
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

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
                findNavController().navigate(
                    BrowseBrandsFragmentDirections.browseToAddJoin(
                        membershipPlan,
                        null,
                        isFromJoinCard = false,
                        isRetryJourney = false
                    )
                )
            }
            registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    binding.brandsRecyclerView.scrollToPosition(0)
                }
            })
        }

        binding.filtersList.layoutManager = GridLayoutManager(context, 2)
        binding.filtersList.adapter = filtersAdapter.apply {
            setOnFilterClickListener {
                viewModel.updateFilters(it)
                Toast.makeText(context, it.category, Toast.LENGTH_SHORT).show()
            }
            setFilters(args.membershipPlans.toList().getCategories().map { BrandsFilter(it) })
        }

        binding.buttonClearSearch.setOnClickListener {
            viewModel.searchText.value = EMPTY_STRING
        }

        binding.buttonFilters.setOnClickListener {
            binding.filtersList.setVisible(binding.filtersList.visibility != View.VISIBLE)
        }
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
        }
    }

    private fun filterBrands(
        membershipPlans: List<MembershipPlan>,
        searchQuery: String
    ): List<MembershipPlan> {
        return if (searchQuery != EMPTY_STRING) {
            val searchList = mutableListOf<MembershipPlan>()
            membershipPlans.forEach {
                if (it.account?.company_name?.toLowerCase(Locale.ENGLISH)
                        ?.contains(searchQuery.toLowerCase(Locale.ENGLISH)) == true
                ) {
                    searchList.add(it)
                }
            }
            searchList
        } else {
            membershipPlans
        }
    }
}
