package com.example.campuslost_and_found.ui;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

/**
 * Manager class for user settings and preferences stored in SharedPreferences.
 */
public class SettingsManager {

    private static final String PREF_NAME = "campus_lost_found_prefs";

    private static final String KEY_STUDENT_NAME = "student_name";
    private static final String KEY_ROLL_NUMBER = "roll_number";
    private static final String KEY_CONTACT_PHONE = "contact_phone";
    private static final String KEY_CONTACT_EMAIL = "contact_email";
    private static final String KEY_LANGUAGE = "app_language"; // "en" or "hi"
    private static final String KEY_THEME = "app_theme";       // "system", "light", "dark"
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";

    private final SharedPreferences prefs;

    public SettingsManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public String getStudentName() {
        return prefs.getString(KEY_STUDENT_NAME, "Campus Student");
    }

    public void setStudentName(String name) {
        prefs.edit().putString(KEY_STUDENT_NAME, name).apply();
    }

    public String getRollNumber() {
        return prefs.getString(KEY_ROLL_NUMBER, "2024CS101");
    }

    public void setRollNumber(String rollNumber) {
        prefs.edit().putString(KEY_ROLL_NUMBER, rollNumber).apply();
    }

    public String getContactPhone() {
        return prefs.getString(KEY_CONTACT_PHONE, "9876543210");
    }

    public void setContactPhone(String phone) {
        prefs.edit().putString(KEY_CONTACT_PHONE, phone).apply();
    }

    public String getContactEmail() {
        return prefs.getString(KEY_CONTACT_EMAIL, "student@campus.edu");
    }

    public void setContactEmail(String email) {
        prefs.edit().putString(KEY_CONTACT_EMAIL, email).apply();
    }

    public String getLanguage() {
        return prefs.getString(KEY_LANGUAGE, "en");
    }

    public void setLanguage(String languageCode) {
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply();
        applyLanguage(languageCode);
    }

    public String getTheme() {
        return prefs.getString(KEY_THEME, "system");
    }

    public void setTheme(String theme) {
        prefs.edit().putString(KEY_THEME, theme).apply();
        applyTheme(theme);
    }

    public boolean isNotificationsEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATIONS, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply();
    }

    public void applyAppPreferences() {
        applyTheme(getTheme());
        applyLanguage(getLanguage());
    }

    public static void applyTheme(String theme) {
        switch (theme) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    public static void applyLanguage(String languageCode) {
        LocaleListCompat appLocales = LocaleListCompat.forLanguageTags(languageCode);
        AppCompatDelegate.setApplicationLocales(appLocales);
    }
}
