package com.example.campuslost_and_found.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.campuslost_and_found.R;
import com.example.campuslost_and_found.databinding.FragmentSettingsBinding;

/**
 * Settings screen for updating user profile and app preferences (language, theme, notifications).
 */
public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsManager settingsManager;
    private MainActivityListener activityListener;

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
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        settingsManager = new SettingsManager(requireContext());

        setupToolbar();
        loadCurrentSettings();
        setupListeners();
    }

    private void setupToolbar() {
        // DATA PASSING: Callback (Fragment -> Activity)
        if (activityListener != null) {
            activityListener.updateToolbarTitle(getString(R.string.title_settings), "");
        }
    }

    private void loadCurrentSettings() {
        binding.etStudentName.setText(settingsManager.getStudentName());
        binding.etRollNumber.setText(settingsManager.getRollNumber());
        binding.etSettingsPhone.setText(settingsManager.getContactPhone());
        binding.etSettingsEmail.setText(settingsManager.getContactEmail());

        // Language Spinner
        String[] languages = new String[]{"English", "हिंदी (Hindi)"};
        ArrayAdapter<String> langAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, languages);
        binding.spinnerLanguage.setAdapter(langAdapter);

        if ("hi".equalsIgnoreCase(settingsManager.getLanguage())) {
            binding.spinnerLanguage.setSelection(1);
        } else {
            binding.spinnerLanguage.setSelection(0);
        }

        // Theme Radio
        String theme = settingsManager.getTheme();
        if ("light".equalsIgnoreCase(theme)) {
            binding.rbThemeLight.setChecked(true);
        } else if ("dark".equalsIgnoreCase(theme)) {
            binding.rbThemeDark.setChecked(true);
        } else {
            binding.rbThemeSystem.setChecked(true);
        }

        // Notifications Switch
        binding.switchNotifications.setChecked(settingsManager.isNotificationsEnabled());
    }

    private void setupListeners() {
        binding.btnSaveSettings.setOnClickListener(v -> saveSettings());
    }

    private void saveSettings() {
        String name = binding.etStudentName.getText() != null ? binding.etStudentName.getText().toString().trim() : "";
        String roll = binding.etRollNumber.getText() != null ? binding.etRollNumber.getText().toString().trim() : "";
        String phone = binding.etSettingsPhone.getText() != null ? binding.etSettingsPhone.getText().toString().trim() : "";
        String email = binding.etSettingsEmail.getText() != null ? binding.etSettingsEmail.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            binding.tilStudentName.setError("Name is required");
            return;
        }

        if (TextUtils.isEmpty(roll)) {
            binding.tilRollNumber.setError("Roll number is required");
            return;
        }

        settingsManager.setStudentName(name);
        settingsManager.setRollNumber(roll);
        settingsManager.setContactPhone(phone);
        settingsManager.setContactEmail(email);

        // Language selection
        int selectedLangPos = binding.spinnerLanguage.getSelectedItemPosition();
        String selectedLangCode = (selectedLangPos == 1) ? "hi" : "en";
        boolean langChanged = !selectedLangCode.equalsIgnoreCase(settingsManager.getLanguage());
        settingsManager.setLanguage(selectedLangCode);

        // Theme selection
        String selectedTheme = "system";
        int checkedThemeId = binding.rgTheme.getCheckedRadioButtonId();
        if (checkedThemeId == R.id.rb_theme_light) {
            selectedTheme = "light";
        } else if (checkedThemeId == R.id.rb_theme_dark) {
            selectedTheme = "dark";
        }
        settingsManager.setTheme(selectedTheme);

        // Notifications
        settingsManager.setNotificationsEnabled(binding.switchNotifications.isChecked());

        Toast.makeText(requireContext(), R.string.toast_settings_saved, Toast.LENGTH_SHORT).show();

        if (langChanged) {
            requireActivity().recreate();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
