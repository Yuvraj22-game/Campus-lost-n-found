package com.example.campuslost_and_found.util;

import com.example.campuslost_and_found.data.ItemEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class to evaluate and score potential smart matches between Lost and Found items.
 */
public class MatchScorer {

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "an", "the", "in", "on", "at", "to", "for", "of", "with", "by", "is", "it",
            "and", "or", "my", "i", "lost", "found", "near", "near", "near", "bag", "item"
    ));

    /**
     * Class holding match details for a candidate item.
     */
    public static class MatchCandidate implements Comparable<MatchCandidate> {
        private final ItemEntity item;
        private final int score;
        private final String confidence;
        private final List<String> reasons;

        public MatchCandidate(ItemEntity item, int score, String confidence, List<String> reasons) {
            this.item = item;
            this.score = score;
            this.confidence = confidence;
            this.reasons = reasons;
        }

        public ItemEntity getItem() {
            return item;
        }

        public int getScore() {
            return score;
        }

        public String getConfidence() {
            return confidence;
        }

        public List<String> getReasons() {
            return reasons;
        }

        @Override
        public int compareTo(MatchCandidate other) {
            return Integer.compare(other.score, this.score); // Descending order
        }
    }

    /**
     * Finds smart match candidates for a target item against a pool of candidate items.
     *
     * @param targetItem The item for which match suggestions are required.
     * @param candidates Pool of active items from database.
     * @return List of MatchCandidates sorted by highest score (minimum threshold 25 points).
     */
    public static List<MatchCandidate> findMatches(ItemEntity targetItem, List<ItemEntity> candidates) {
        if (targetItem == null || candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }

        List<MatchCandidate> matches = new ArrayList<>();
        String targetType = targetItem.getType();

        for (ItemEntity candidate : candidates) {
            // Only compare items of opposite status/type (LOST vs FOUND) and exclude same ID
            if (candidate.getId() == targetItem.getId()) {
                continue;
            }
            if (targetType != null && targetType.equalsIgnoreCase(candidate.getType())) {
                continue;
            }

            MatchCandidate match = calculateMatchScore(targetItem, candidate);
            if (match.getScore() >= 25) { // Minimum confidence threshold
                matches.add(match);
            }
        }

        Collections.sort(matches);
        return matches;
    }

    /**
     * Calculates the match score (0 to 100) between two items.
     */
    public static MatchCandidate calculateMatchScore(ItemEntity item1, ItemEntity item2) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        // 1. Category Match (+40 Points)
        if (item1.getCategory() != null && item1.getCategory().equalsIgnoreCase(item2.getCategory())) {
            score += 40;
            reasons.add("Same Category: " + item1.getCategory());
        }

        // 2. Zone / Location Match (+10 Points)
        if (item1.getZone() != null && item1.getZone().equalsIgnoreCase(item2.getZone())) {
            score += 10;
            reasons.add("Same Campus Zone: " + item1.getZone());
        }

        // 3. Date Proximity (+15 Max)
        long dayDiffMs = Math.abs(item1.getDateMillis() - item2.getDateMillis());
        long daysDiff = dayDiffMs / (1000L * 60L * 60L * 24L);

        if (daysDiff <= 1) {
            score += 15;
            reasons.add("Reported within 24 hours of each other");
        } else if (daysDiff <= 3) {
            score += 10;
            reasons.add("Reported within 3 days");
        } else if (daysDiff <= 7) {
            score += 5;
            reasons.add("Reported within 7 days");
        }

        // 4. Keyword Overlap in Title & Description (+35 Max)
        Set<String> words1 = extractKeywords(item1.getTitle() + " " + item1.getDescription());
        Set<String> words2 = extractKeywords(item2.getTitle() + " " + item2.getDescription());

        int commonWords = 0;
        List<String> matchedWords = new ArrayList<>();
        for (String word : words1) {
            if (words2.contains(word)) {
                commonWords++;
                matchedWords.add(word);
            }
        }

        if (commonWords > 0) {
            int keywordPoints = Math.min(35, commonWords * 12);
            score += keywordPoints;
            reasons.add("Matching keywords: " + String.join(", ", matchedWords));
        }

        // Bound total score between 0 and 100
        score = Math.min(100, Math.max(0, score));

        // Assign Confidence Level
        String confidence;
        if (score >= 70) {
            confidence = "HIGH";
        } else if (score >= 45) {
            confidence = "MEDIUM";
        } else {
            confidence = "LOW";
        }

        return new MatchCandidate(item2, score, confidence, reasons);
    }

    /**
     * Helper to extract clean, non-stopword tokens from text.
     */
    private static Set<String> extractKeywords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptySet();
        }

        String[] tokens = text.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "").split("\\s+");
        Set<String> keywords = new HashSet<>();

        for (String token : tokens) {
            if (token.length() > 2 && !STOP_WORDS.contains(token)) {
                keywords.add(token);
            }
        }

        return keywords;
    }
}
