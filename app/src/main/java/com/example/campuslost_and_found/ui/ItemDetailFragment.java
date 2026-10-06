package com.example.campuslost_and_found.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.campuslost_and_found.MainActivity;
import com.example.campuslost_and_found.R;
import com.example.campuslost_and_found.adapter.MatchAdapter;
import com.example.campuslost_and_found.data.ItemEntity;
import com.example.campuslost_and_found.databinding.FragmentItemDetailBinding;
import com.example.campuslost_and_found.util.DateUtils;
import com.example.campuslost_and_found.viewmodel.SharedViewModel;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Item Detail Screen with Claim Verification Gate, Implicit Intents, and Smart Matches.
 */
public class ItemDetailFragment extends Fragment implements MatchAdapter.OnMatchClickListener {

    private FragmentItemDetailBinding binding;
    private SharedViewModel viewModel;
    private MainActivityListener activityListener;
    private MatchAdapter matchAdapter;
    private ItemEntity currentItem;

    private boolean isClaimVerified = false;

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
        binding = FragmentItemDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        setupRecyclerView();
        observeSelectedItem();
        setupListeners();
    }

    private void setupRecyclerView() {
        matchAdapter = new MatchAdapter(requireContext(), this);
        binding.rvSmartMatches.setAdapter(matchAdapter);
    }

    private void observeSelectedItem() {
        // DATA PASSING: ViewModel (observing selected item state)
        viewModel.getSelectedItem().observe(getViewLifecycleOwner(), item -> {
            if (item != null) {
                this.currentItem = item;
                bindItemDetails(item);
                loadSmartMatches(item);
            }
        });
    }

    private void bindItemDetails(ItemEntity item) {
        // DATA PASSING: Callback (updating toolbar title)
        if (activityListener != null) {
            activityListener.updateToolbarTitle(item.getTitle(), item.getType() + " • " + item.getZone());
        }

        binding.tvDetailTitle.setText(item.getTitle());
        binding.tvDetailCategory.setText("Category: " + item.getCategory());
        binding.tvDetailZone.setText("Zone: " + item.getZone());
        binding.tvDetailDescription.setText(item.getDescription());

        String dateStr = DateUtils.formatFullDate(item.getDateMillis());
        binding.tvDetailDatePosted.setText("Date: " + dateStr + " • Posted By: " + item.getPostedByRoll());

        // Type Badge Setup
        boolean isLost = "LOST".equalsIgnoreCase(item.getType());
        binding.tvDetailTypeBadge.setText(isLost ? getString(R.string.type_lost) : getString(R.string.type_found));
        int typeBgColor = isLost ? ContextCompat.getColor(requireContext(), R.color.status_lost)
                : ContextCompat.getColor(requireContext(), R.color.status_found);
        binding.tvDetailTypeBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(typeBgColor));

        // Status Badge
        boolean isResolved = "RESOLVED".equalsIgnoreCase(item.getStatus());
        binding.tvDetailStatusBadge.setText(isResolved ? getString(R.string.status_resolved) : getString(R.string.status_active));

        // Aging Badge
        binding.tvDetailAgingBadge.setText(DateUtils.getRelativeTimeString(item.getDateMillis(), requireContext()));

        // Image Loading
        if (item.getImageUri() != null && !item.getImageUri().trim().isEmpty()) {
            Glide.with(requireContext())
                    .load(item.getImageUri())
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_placeholder_image)
                    .into(binding.imgDetailImage);
        } else {
            binding.imgDetailImage.setImageResource(R.drawable.ic_placeholder_image);
        }

        // Contact Info
        String phoneText = item.isShowPhonePublicly() ? item.getContactPhone() : "Private (Request via App)";
        binding.tvDetailPhone.setText("Phone: " + phoneText);
        binding.tvDetailEmail.setText("Email: " + item.getContactEmail());

        // Claim Verification Section (for FOUND items with question)
        if (!isLost && !TextUtils.isEmpty(item.getVerificationQuestion())) {
            binding.cardClaimVerification.setVisibility(View.VISIBLE);
            binding.tvVerificationQText.setText(item.getVerificationQuestion());
        } else {
            binding.cardClaimVerification.setVisibility(View.GONE);
            isClaimVerified = true; // Auto-verify if no question exists or for LOST items
        }
    }

    private void loadSmartMatches(ItemEntity targetItem) {
        // DATA PASSING: ViewModel (asynchronous match calculation)
        viewModel.calculateMatchSuggestions(targetItem, matches -> {
            if (isAdded() && getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    matchAdapter.setMatches(matches);
                });
            }
        });
    }

    private void setupListeners() {
        // Claim Verification Answer Check Button
        binding.btnVerifyClaim.setOnClickListener(v -> checkClaimAnswer());

        // IMPLICIT INTENT: ACTION_DIAL for phone call
        binding.btnCall.setOnClickListener(v -> {
            performVerifiedAction(() -> {
                if (currentItem != null && !TextUtils.isEmpty(currentItem.getContactPhone())) {
                    try {
                        Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + currentItem.getContactPhone()));
                        startActivity(dialIntent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(requireContext(), R.string.toast_no_app_found, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        // IMPLICIT INTENT: ACTION_SENDTO for Email
        binding.btnEmail.setOnClickListener(v -> {
            performVerifiedAction(() -> {
                if (currentItem != null && !TextUtils.isEmpty(currentItem.getContactEmail())) {
                    try {
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                        emailIntent.setData(Uri.parse("mailto:" + currentItem.getContactEmail()));
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Boomerang App Query: " + currentItem.getTitle());
                        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello, regarding your post for '" + currentItem.getTitle() + "' in zone " + currentItem.getZone() + "...");
                        startActivity(Intent.createChooser(emailIntent, "Send Email"));
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(requireContext(), R.string.toast_no_app_found, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        // IMPLICIT INTENT: ACTION_SEND for Sharing Text
        binding.btnShareItem.setOnClickListener(v -> {
            if (currentItem != null) {
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    String shareText = "[" + currentItem.getType() + "] " + currentItem.getTitle() +
                            "\nZone: " + currentItem.getZone() +
                            "\nDescription: " + currentItem.getDescription() +
                            "\nShared via Boomerang Campus App";
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.btn_share)));
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(requireContext(), R.string.toast_no_app_found, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // DIALOG: Mark as Resolved Confirmation
        binding.btnResolveItem.setOnClickListener(v -> {
            if (currentItem != null) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.dialog_resolve_title)
                        .setMessage(R.string.dialog_resolve_msg)
                        .setPositiveButton(R.string.dialog_confirm, (dialog, which) -> {
                            viewModel.resolveItem(currentItem.getId());
                            Toast.makeText(requireContext(), R.string.toast_item_resolved, Toast.LENGTH_SHORT).show();
                            if (activityListener != null) {
                                activityListener.navigateToFragment(MainActivity.TAG_HOME, null);
                            }
                        })
                        .setNegativeButton(R.string.dialog_cancel, null)
                        .show();
            }
        });
    }

    private void checkClaimAnswer() {
        if (currentItem == null) return;

        String userAns = binding.etClaimAnswer.getText() != null ? binding.etClaimAnswer.getText().toString().trim() : "";
        String expectedAns = currentItem.getVerificationAnswer();

        if (TextUtils.isEmpty(userAns)) {
            binding.tilClaimAnswer.setError("Please enter an answer");
            return;
        }

        if (expectedAns != null && expectedAns.equalsIgnoreCase(userAns)) {
            binding.tilClaimAnswer.setError(null);
            isClaimVerified = true;
            Toast.makeText(requireContext(), R.string.toast_claim_correct, Toast.LENGTH_LONG).show();
        } else {
            binding.tilClaimAnswer.setError("Incorrect answer");
            Toast.makeText(requireContext(), R.string.toast_claim_incorrect, Toast.LENGTH_SHORT).show();
        }
    }

    private void performVerifiedAction(Runnable action) {
        if (isClaimVerified) {
            action.run();
        } else {
            // DIALOG: Verification Prompt
            EditText input = new EditText(requireContext());
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_verification_required_title)
                    .setMessage(currentItem.getVerificationQuestion())
                    .setView(input)
                    .setPositiveButton(R.string.btn_verify_claim, (dialog, which) -> {
                        String ans = input.getText().toString().trim();
                        if (currentItem.getVerificationAnswer() != null && currentItem.getVerificationAnswer().equalsIgnoreCase(ans)) {
                            isClaimVerified = true;
                            Toast.makeText(requireContext(), R.string.toast_claim_correct, Toast.LENGTH_SHORT).show();
                            action.run();
                        } else {
                            Toast.makeText(requireContext(), R.string.toast_claim_incorrect, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .show();
        }
    }

    // MatchAdapter.OnMatchClickListener implementation
    @Override
    public void onMatchClick(com.example.campuslost_and_found.util.MatchScorer.MatchCandidate candidate) {
        if (candidate != null && candidate.getItem() != null) {
            // DATA PASSING: ViewModel & Bundle
            viewModel.setSelectedItem(candidate.getItem());
            Bundle args = new Bundle();
            args.putInt("ITEM_ID", candidate.getItem().getId());
            args.putString("ITEM_TITLE", candidate.getItem().getTitle());

            if (activityListener != null) {
                activityListener.navigateToFragment(MainActivity.TAG_DETAIL, args);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
