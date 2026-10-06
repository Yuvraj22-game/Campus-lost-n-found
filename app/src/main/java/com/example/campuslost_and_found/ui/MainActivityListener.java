package com.example.campuslost_and_found.ui;

import android.os.Bundle;

/**
 * Interface callback for Fragment -> Activity communication (toolbar updates, badges, navigation).
 */
public interface MainActivityListener {

    // DATA PASSING: Callback (Fragment to Activity)
    void updateToolbarTitle(String title, String subtitle);

    // DATA PASSING: Callback (Fragment to Activity)
    void updateActiveCountBadge(int activeCount);

    // DATA PASSING: Callback (Fragment to Activity)
    void navigateToFragment(String fragmentTag, Bundle args);
}
