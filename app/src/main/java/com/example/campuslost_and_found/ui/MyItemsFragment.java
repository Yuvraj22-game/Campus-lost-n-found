package com.example.campuslost_and_found.ui;

import android.content.Context;
import android.os.Bundle;
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
import com.example.campuslost_and_found.data.ItemEntity;
import com.example.campuslost_and_found.databinding.FragmentMyItemsBinding;
import com.example.campuslost_and_found.viewmodel.SharedViewModel;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Fragment displaying items reported by the currently logged in student, with delete & resolve dialogs.
 */
public class MyItemsFragment extends Fragment implements ItemAdapter.OnItemClickListener {

    private FragmentMyItemsBinding binding;
    private SharedViewModel viewModel;
    private SettingsManager settingsManager;
    private MainActivityListener activityListener;
    private ItemAdapter adapter;

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
        binding = FragmentMyItemsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        settingsManager = new SettingsManager(requireContext());
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupObservers();
        setupListeners();
    }

    private void setupToolbar() {
        // DATA PASSING: Callback (Fragment -> Activity)
        if (activityListener != null) {
            activityListener.updateToolbarTitle(getString(R.string.nav_my_items), "Roll No: " + settingsManager.getRollNumber());
        }
        binding.tvUserRollInfo.setText("Listings for Roll No: " + settingsManager.getRollNumber());
    }

    private void setupRecyclerView() {
        adapter = new ItemAdapter(requireContext(), this);
        binding.rvMyItems.setAdapter(adapter);
    }

    private void setupObservers() {
        String rollNo = settingsManager.getRollNumber();
        // DATA PASSING: ViewModel (observing items posted by roll number)
        viewModel.getFilteredItems().observe(getViewLifecycleOwner(), items -> {
            if (items == null) return;

            java.util.List<ItemEntity> myItems = new java.util.ArrayList<>();
            for (ItemEntity item : items) {
                if (rollNo.equalsIgnoreCase(item.getPostedByRoll())) {
                    myItems.add(item);
                }
            }

            if (myItems.isEmpty()) {
                binding.rvMyItems.setVisibility(View.GONE);
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
            } else {
                binding.layoutEmptyState.setVisibility(View.GONE);
                binding.rvMyItems.setVisibility(View.VISIBLE);
                adapter.setItems(myItems);
            }
        });
    }

    private void setupListeners() {
        binding.btnReportFirstItem.setOnClickListener(v -> {
            if (activityListener != null) {
                activityListener.navigateToFragment(MainActivity.TAG_ADD, null);
            }
        });
    }

    // ItemAdapter.OnItemClickListener implementation
    @Override
    public void onItemClick(ItemEntity item) {
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
            // DIALOG: Mark as Resolved Confirmation
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_resolve_title)
                    .setMessage(R.string.dialog_resolve_msg)
                    .setPositiveButton(R.string.dialog_confirm, (dialog, which) -> {
                        viewModel.resolveItem(item.getId());
                        Toast.makeText(requireContext(), R.string.toast_item_resolved, Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .show();
        } else if (menuItemId == R.id.action_delete_item) {
            // DIALOG: Delete Confirmation
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_delete_title)
                    .setMessage(R.string.dialog_delete_msg)
                    .setPositiveButton(R.string.dialog_confirm, (dialog, which) -> {
                        viewModel.deleteItem(item);
                        Toast.makeText(requireContext(), "Listing deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .show();
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
