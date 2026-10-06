package com.example.campuslost_and_found;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.campuslost_and_found.databinding.ActivityMainBinding;
import com.example.campuslost_and_found.ui.AddItemFragment;
import com.example.campuslost_and_found.ui.HomeFragment;
import com.example.campuslost_and_found.ui.ItemDetailFragment;
import com.example.campuslost_and_found.ui.MainActivityListener;
import com.example.campuslost_and_found.ui.MyItemsFragment;
import com.example.campuslost_and_found.ui.SettingsFragment;
import com.example.campuslost_and_found.ui.SettingsManager;
import com.example.campuslost_and_found.util.NotificationHelper;
import com.google.android.material.badge.BadgeDrawable;

/**
 * Single Activity container managing Fragment transactions, Notification Channels, and Permissions.
 */
public class MainActivity extends AppCompatActivity implements MainActivityListener {

    public static final String TAG_HOME = "HOME_FRAGMENT";
    public static final String TAG_ADD = "ADD_ITEM_FRAGMENT";
    public static final String TAG_MY_ITEMS = "MY_ITEMS_FRAGMENT";
    public static final String TAG_SETTINGS = "SETTINGS_FRAGMENT";
    public static final String TAG_DETAIL = "ITEM_DETAIL_FRAGMENT";

    private ActivityMainBinding binding;
    private SettingsManager settingsManager;

    private final ActivityResultLauncher<String> requestNotificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, R.string.toast_permission_denied, Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settingsManager = new SettingsManager(this);
        settingsManager.applyAppPreferences();

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom / 2);
            return insets;
        });

        NotificationHelper.createNotificationChannel(this);
        checkNotificationPermission();

        setupToolbar();
        setupBottomNavigation();

        if (savedInstanceState == null) {
            navigateToFragment(TAG_HOME, null);
        }

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof HomeFragment) {
                binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
            } else if (currentFragment instanceof AddItemFragment) {
                binding.bottomNavigation.setSelectedItemId(R.id.nav_add_item);
            } else if (currentFragment instanceof MyItemsFragment) {
                binding.bottomNavigation.setSelectedItemId(R.id.nav_my_items);
            } else if (currentFragment instanceof SettingsFragment) {
                binding.bottomNavigation.setSelectedItemId(R.id.nav_settings);
            }
        });
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void setupToolbar() {
        binding.toolbar.inflateMenu(R.menu.toolbar_menu);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_settings) {
                navigateToFragment(TAG_SETTINGS, null);
                return true;
            } else if (itemId == R.id.action_refresh) {
                Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (current instanceof HomeFragment) {
                    ((HomeFragment) current).refreshData();
                } else {
                    Toast.makeText(this, R.string.menu_refresh, Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (itemId == R.id.action_about) {
                // IMPLICIT INTENT: ACTION_VIEW for Campus Guidelines URL
                try {
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.campus_guidelines_url)));
                    startActivity(webIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, R.string.toast_no_app_found, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                navigateToFragment(TAG_HOME, null);
                return true;
            } else if (id == R.id.nav_add_item) {
                navigateToFragment(TAG_ADD, null);
                return true;
            } else if (id == R.id.nav_my_items) {
                navigateToFragment(TAG_MY_ITEMS, null);
                return true;
            } else if (id == R.id.nav_settings) {
                navigateToFragment(TAG_SETTINGS, null);
                return true;
            }
            return false;
        });
    }

    // DATA PASSING: Callback (Fragment -> Activity implementation)
    @Override
    public void updateToolbarTitle(String title, String subtitle) {
        if (binding.toolbar != null) {
            binding.toolbar.setTitle(title != null ? title : getString(R.string.app_name));
            binding.toolbar.setSubtitle(subtitle != null ? subtitle : "");
        }
    }

    // DATA PASSING: Callback (Fragment -> Activity implementation)
    @Override
    public void updateActiveCountBadge(int activeCount) {
        if (binding.bottomNavigation != null) {
            BadgeDrawable badge = binding.bottomNavigation.getOrCreateBadge(R.id.nav_home);
            if (activeCount > 0) {
                badge.setVisible(true);
                badge.setNumber(activeCount);
            } else {
                badge.setVisible(false);
            }
        }
    }

    // DATA PASSING: Callback & Bundle (Fragment -> Activity transaction)
    @Override
    public void navigateToFragment(String fragmentTag, Bundle args) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);

        if (currentFragment != null && fragmentTag.equals(currentFragment.getTag()) && args == null) {
            return;
        }

        Fragment targetFragment;
        switch (fragmentTag) {
            case TAG_ADD:
                targetFragment = new AddItemFragment();
                break;
            case TAG_MY_ITEMS:
                targetFragment = new MyItemsFragment();
                break;
            case TAG_SETTINGS:
                targetFragment = new SettingsFragment();
                break;
            case TAG_DETAIL:
                targetFragment = new ItemDetailFragment();
                break;
            case TAG_HOME:
            default:
                targetFragment = new HomeFragment();
                break;
        }

        // DATA PASSING: Bundle arguments
        if (args != null) {
            targetFragment.setArguments(args);
        }

        var transaction = fragmentManager.beginTransaction()
                .setCustomAnimations(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out)
                .replace(R.id.fragment_container, targetFragment, fragmentTag);

        if (!TAG_HOME.equals(fragmentTag)) {
            transaction.addToBackStack(fragmentTag);
        } else {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        transaction.commit();
    }
}
