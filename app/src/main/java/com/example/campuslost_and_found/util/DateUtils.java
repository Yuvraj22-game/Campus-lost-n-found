package com.example.campuslost_and_found.util;

import android.content.Context;

import com.example.campuslost_and_found.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for formatting dates and calculating aging badges.
 */
public class DateUtils {

    /**
     * Returns relative aging string ("Today", "Yesterday", "X days ago", or "Old (30+ days)").
     */
    public static String getRelativeTimeString(long timeMillis, Context context) {
        long now = System.currentTimeMillis();
        long diffMs = Math.max(0, now - timeMillis);
        long daysDiff = diffMs / (1000L * 60L * 60L * 24L);

        if (daysDiff == 0) {
            return context.getString(R.string.aging_today);
        } else if (daysDiff == 1) {
            return context.getString(R.string.aging_yesterday);
        } else if (daysDiff < 30) {
            return context.getString(R.string.aging_days_ago, (int) daysDiff);
        } else {
            return context.getString(R.string.aging_old);
        }
    }

    /**
     * Returns true if an item is considered aging/old (> 30 days old).
     */
    public static boolean isAgingItem(long timeMillis) {
        long now = System.currentTimeMillis();
        long diffMs = Math.max(0, now - timeMillis);
        long daysDiff = diffMs / (1000L * 60L * 60L * 24L);
        return daysDiff >= 30;
    }

    /**
     * Formats timestamp to a clean date string (e.g., "12 Oct 2026").
     */
    public static String formatFullDate(long timeMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return sdf.format(new Date(timeMillis));
    }
}
