package com.example.campuslost_and_found.ui;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.campuslost_and_found.R;
import com.example.campuslost_and_found.data.ItemEntity;
import com.example.campuslost_and_found.databinding.FragmentAddItemBinding;
import com.example.campuslost_and_found.util.DateUtils;
import com.example.campuslost_and_found.util.NotificationHelper;
import com.example.campuslost_and_found.viewmodel.SharedViewModel;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment form for reporting a new Lost or Found item with Camera/Gallery photo attachment & Match alerts.
 */
public class AddItemFragment extends Fragment {

    private FragmentAddItemBinding binding;
    private SharedViewModel viewModel;
    private SettingsManager settingsManager;
    private MainActivityListener activityListener;

    private long selectedDateMillis = System.currentTimeMillis();
    private Uri currentPhotoUri = null;

    private static final String KEY_TITLE = "SAVED_TITLE";
    private static final String KEY_DESC = "SAVED_DESC";
    private static final String KEY_DATE = "SAVED_DATE";
    private static final String KEY_TYPE = "SAVED_TYPE";

    // ActivityResult Launcher for Gallery Image Selection
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    currentPhotoUri = uri;
                    binding.imgPreview.setImageURI(uri);
                }
            });

    // ActivityResult Launcher for Camera Capture
    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && currentPhotoUri != null) {
                    binding.imgPreview.setImageURI(currentPhotoUri);
                }
            });

    // ActivityResult Launcher for Camera Permission
    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    launchCameraIntent();
                } else {
                    Toast.makeText(requireContext(), R.string.toast_permission_denied, Toast.LENGTH_SHORT).show();
                }
            });

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
        binding = FragmentAddItemBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        settingsManager = new SettingsManager(requireContext());
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        setupToolbar();
        setupSpinners();
        setupDefaultFields();
        setupListeners();

        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        }
    }

    private void setupToolbar() {
        // DATA PASSING: Callback (Fragment -> Activity)
        if (activityListener != null) {
            activityListener.updateToolbarTitle(getString(R.string.title_add_item), "");
        }
    }

    private void setupSpinners() {
        String[] categories = new String[]{
                getString(R.string.cat_electronics),
                getString(R.string.cat_books),
                getString(R.string.cat_cards),
                getString(R.string.cat_keys),
                getString(R.string.cat_clothing),
                getString(R.string.cat_personal),
                getString(R.string.cat_others)
        };
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        binding.spinnerCategory.setAdapter(catAdapter);

        String[] zones = new String[]{
                getString(R.string.zone_library),
                getString(R.string.zone_canteen),
                getString(R.string.zone_academic),
                getString(R.string.zone_sports),
                getString(R.string.zone_hostel),
                getString(R.string.zone_auditorium),
                getString(R.string.zone_parking),
                getString(R.string.zone_others)
        };
        ArrayAdapter<String> zoneAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, zones);
        binding.spinnerZone.setAdapter(zoneAdapter);
    }

    private void setupDefaultFields() {
        binding.etPhone.setText(settingsManager.getContactPhone());
        binding.etEmail.setText(settingsManager.getContactEmail());
        binding.tvSelectedDate.setText(DateUtils.formatFullDate(selectedDateMillis));
    }

    private void setupListeners() {
        binding.rgItemType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_found) {
                binding.layoutVerification.setVisibility(View.VISIBLE);
            } else {
                binding.layoutVerification.setVisibility(View.GONE);
            }
        });

        binding.btnPickDate.setOnClickListener(v -> showDatePicker());

        binding.btnPickGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        binding.btnTakePhoto.setOnClickListener(v -> checkCameraPermissionAndLaunch());

        binding.btnSubmit.setOnClickListener(v -> saveItemReport());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(selectedDateMillis);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCal = Calendar.getInstance();
                    selectedCal.set(year, month, dayOfMonth);
                    selectedDateMillis = selectedCal.getTimeInMillis();
                    binding.tvSelectedDate.setText(DateUtils.formatFullDate(selectedDateMillis));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchCameraIntent();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCameraIntent() {
        try {
            File photoFile = createImageFile();
            currentPhotoUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    photoFile
            );
            cameraLauncher.launch(currentPhotoUri);
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Error creating photo file", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void saveItemReport() {
        String title = binding.etTitle.getText() != null ? binding.etTitle.getText().toString().trim() : "";
        String desc = binding.etDescription.getText() != null ? binding.etDescription.getText().toString().trim() : "";
        String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : "";
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";

        if (TextUtils.isEmpty(title)) {
            binding.tilTitle.setError("Item title is required");
            return;
        } else {
            binding.tilTitle.setError(null);
        }

        if (TextUtils.isEmpty(desc)) {
            binding.tilDescription.setError("Description is required");
            return;
        } else {
            binding.tilDescription.setError(null);
        }

        boolean isFound = binding.rbFound.isChecked();
        String type = isFound ? "FOUND" : "LOST";
        String category = binding.spinnerCategory.getSelectedItem() != null ? binding.spinnerCategory.getSelectedItem().toString() : "Others";
        String zone = binding.spinnerZone.getSelectedItem() != null ? binding.spinnerZone.getSelectedItem().toString() : "Other Campus Area";
        boolean showPhone = binding.chkPublicPhone.isChecked();

        String question = "";
        String answer = "";
        if (isFound) {
            question = binding.etVerificationQ.getText() != null ? binding.etVerificationQ.getText().toString().trim() : "";
            answer = binding.etVerificationA.getText() != null ? binding.etVerificationA.getText().toString().trim() : "";
        }

        String imageUriStr = currentPhotoUri != null ? currentPhotoUri.toString() : "";
        String userRoll = settingsManager.getRollNumber();
        long now = System.currentTimeMillis();

        ItemEntity newItem = new ItemEntity(
                type,
                title,
                desc,
                category,
                zone,
                selectedDateMillis,
                imageUriStr,
                phone,
                email,
                showPhone,
                question,
                answer,
                "ACTIVE",
                userRoll,
                now
        );

        // DATA PASSING: ViewModel (inserting item into Room DB)
        viewModel.insertItem(newItem, () -> {
            // Check for Smart Match notification trigger
            viewModel.calculateMatchSuggestions(newItem, matches -> {
                if (!matches.isEmpty()) {
                    var topMatch = matches.get(0);
                    if (topMatch.getScore() >= 45) {
                        NotificationHelper.showMatchNotification(requireContext(), newItem.getTitle(), topMatch.getScore());
                    }
                }
            });

            requireActivity().runOnUiThread(() -> {
                // DIALOG: Success Confirmation Alert
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.dialog_success_title)
                        .setMessage(R.string.dialog_success_msg)
                        .setPositiveButton(R.string.dialog_confirm, (dialog, which) -> {
                            // DATA PASSING: Fragment Result API
                            Bundle result = new Bundle();
                            result.putBoolean("SUCCESS", true);
                            getParentFragmentManager().setFragmentResult("ADD_ITEM_RESULT", result);

                            getParentFragmentManager().popBackStack();
                        })
                        .setCancelable(false)
                        .show();
            });
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (binding != null) {
            outState.putString(KEY_TITLE, binding.etTitle.getText().toString());
            outState.putString(KEY_DESC, binding.etDescription.getText().toString());
            outState.putLong(KEY_DATE, selectedDateMillis);
            outState.putBoolean(KEY_TYPE, binding.rbFound.isChecked());
        }
    }

    private void restoreState(@NonNull Bundle savedInstanceState) {
        String title = savedInstanceState.getString(KEY_TITLE, "");
        String desc = savedInstanceState.getString(KEY_DESC, "");
        selectedDateMillis = savedInstanceState.getLong(KEY_DATE, System.currentTimeMillis());
        boolean isFound = savedInstanceState.getBoolean(KEY_TYPE, false);

        binding.etTitle.setText(title);
        binding.etDescription.setText(desc);
        binding.tvSelectedDate.setText(DateUtils.formatFullDate(selectedDateMillis));
        binding.rbFound.setChecked(isFound);
        binding.rbLost.setChecked(!isFound);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
