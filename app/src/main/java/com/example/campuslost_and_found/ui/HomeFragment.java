package com.example.campuslost_and_found.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.campuslost_and_found.MainActivity;
import com.example.campuslost_and_found.R;
import com.example.campuslost_and_found.adapter.ItemAdapter;
import com.example.campuslost_and_found.adapter.ZoneCountAdapter;
import com.example.campuslost_and_found.data.ItemEntity;
import com.example.campuslost_and_found.databinding.FragmentHomeBinding;
import com.example.campuslost_and_found.viewmodel.SharedViewModel;

import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayout;

/**
 * Main Home screen showing active lost & found listings, statistics, search, and category filters.
 */
public class HomeFragment extends Fragment implements ItemAdapter.OnItemClickListener {

    private FragmentHomeBinding binding;
    private SharedViewModel viewModel;
    private SettingsManager settingsManager;
    private MainActivityListener activityListener;

    private ItemAdapter itemAdapter;
    private ZoneCountAdapter zoneAdapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivityListener) {
            this.activityListener = (MainActivityListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        settingsManager = new SettingsManager(requireContext());
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        setupHeader();
        setupRecyclerViews();
        setupSearchAndFilters();
        setupObservers();
        setupListeners();
        setupFragmentResultListener();
    }

    private void setupHeader() {
        String studentName = settingsManager.getStudentName();
        binding.tvWelcomeName.setText("Hello, " + studentName + " 👋");

        // DATA PASSING: Callback (Fragment -> Activity)
        if (activityListener != null) {
            activityListener.updateToolbarTitle(getString(R.string.app_name), getString(R.string.app_tagline));
        }
    }

    private void setupRecyclerViews() {
        itemAdapter = new ItemAdapter(requireContext(), this);
        binding.rvItems.setAdapter(itemAdapter);

        zoneAdapter = new ZoneCountAdapter(zoneName -> {
            // Filter by zone when zone chip is clicked
            binding.etSearch.setText(zoneName);
        });
        binding.rvZoneCounts.setAdapter(zoneAdapter);
    }

    private void setupSearchAndFilters() {
        // Search text watcher
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // DATA PASSING: ViewModel (passing search query state)
                viewModel.setSearchQuery(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Type filter tabs (All, Lost, Found)
        binding.tabLayoutType.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                String typeFilter = "ALL";
                if (pos == 1) typeFilter = "LOST";
                else if (pos == 2) typeFilter = "FOUND";

                // DATA PASSING: ViewModel (passing type filter state)
                viewModel.setTypeFilter(typeFilter);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Category Chip Group listener
        binding.chipGroupCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                viewModel.setCategoryFilter("ALL");
                return;
            }
            int checkedId = checkedIds.get(0);
            Chip chip = group.findViewById(checkedId);
            if (chip != null) {
                String catText = chip.getText().toString();
                if (getString(R.string.filter_category_all).equals(catText)) {
                    viewModel.setCategoryFilter("ALL");
                } else {
                    viewModel.setCategoryFilter(catText);
                }
            }
        });
    }

    private void setupObservers() {
        // Observe Filtered Items list
        viewModel.getFilteredItems().observe(getViewLifecycleOwner(), items -> {
            if (items == null || items.isEmpty()) {
                binding.rvItems.setVisibility(View.GONE);
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
            } else {
                binding.layoutEmptyState.setVisibility(View.GONE);
                binding.rvItems.setVisibility(View.VISIBLE);
                itemAdapter.setItems(items);
            }
        });

        // Observe Statistics Counters
        viewModel.getActiveLostCount().observe(getViewLifecycleOwner(), lost -> {
            int count = lost != null ? lost : 0;
            binding.tvStatLostCount.setText(String.valueOf(count));
            updateTotalActiveBadge();
        });

        viewModel.getActiveFoundCount().observe(getViewLifecycleOwner(), found -> {
            int count = found != null ? found : 0;
            binding.tvStatFoundCount.setText(String.valueOf(count));
            updateTotalActiveBadge();
        });

        viewModel.getResolvedCount().observe(getViewLifecycleOwner(), resolved -> {
            binding.tvStatResolvedCount.setText(String.valueOf(resolved != null ? resolved : 0));
        });

        // Observe Zone Counts
        viewModel.getZoneCounts().observe(getViewLifecycleOwner(), zones -> {
            zoneAdapter.setZoneList(zones);
        });

        // Observe Loading State
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE);
        });
    }

    private void updateTotalActiveBadge() {
        Integer lost = viewModel.getActiveLostCount().getValue();
        Integer found = viewModel.getActiveFoundCount().getValue();
        int total = (lost != null ? lost : 0) + (found != null ? found : 0);

        // DATA PASSING: Callback (passing count to Activity badge)
        if (activityListener != null) {
            activityListener.updateActiveCountBadge(total);
        }
    }

    private void setupListeners() {
        binding.fabAddItem.setOnClickListener(v -> {
            // DATA PASSING: Callback (Activity navigation)
            if (activityListener != null) {
                activityListener.navigateToFragment(MainActivity.TAG_ADD, null);
            }
        });
    }

    // DATA PASSING: Fragment Result API listener (receiving result from AddItemFragment)
    private void setupFragmentResultListener() {
        getParentFragmentManager().setFragmentResultListener("ADD_ITEM_RESULT", getViewLifecycleOwner(), (requestKey, result) -> {
            boolean success = result.getBoolean("SUCCESS", false);
            if (success) {
                Toast.makeText(requireContext(), R.string.toast_item_saved, Toast.LENGTH_SHORT).show();
                refreshData();
            }
        });
    }

    public void refreshData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (binding != null) {
                binding.progressBar.setVisibility(View.GONE);
            }
        }, 600);
    }

    // ItemAdapter.OnItemClickListener implementations
    @Override
    public void onItemClick(ItemEntity item) {
        // DATA PASSING: ViewModel & Bundle
        viewModel.setSelectedItem(item);

        Bundle args = new Bundle();
        args.putInt("ITEM_ID", item.getId());
        args.putString("ITEM_TITLE", item.getTitle());

        if (activityListener != null) {
            activityListener.navigateToFragment(MainActivity.TAG_DETAIL, args);
        }
    }

    @Override
    public void onMenuItemClick(ItemEntity item, int menuItemId) {
        if (menuItemId == R.id.action_view_details) {
            onItemClick(item);
        } else if (menuItemId == R.id.action_mark_resolved) {
            viewModel.resolveItem(item.getId());
            Toast.makeText(requireContext(), R.string.toast_item_resolved, Toast.LENGTH_SHORT).show();
        } else if (menuItemId == R.id.action_share_item) {
            Toast.makeText(requireContext(), "Share: " + item.getTitle(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
