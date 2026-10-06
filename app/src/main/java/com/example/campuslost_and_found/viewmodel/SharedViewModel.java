package com.example.campuslost_and_found.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.campuslost_and_found.data.ItemEntity;

import com.example.campuslost_and_found.data.ItemRepository;
import com.example.campuslost_and_found.data.ZoneCount;
import com.example.campuslost_and_found.util.MatchScorer;
import com.example.campuslost_and_found.util.MatchScorer.MatchCandidate;

import java.util.List;

/**
 * Shared ViewModel managing UI state, item queries, filters, and repository interactions.
 */
public class SharedViewModel extends AndroidViewModel {

    private final ItemRepository repository;

    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> selectedTypeFilter = new MutableLiveData<>("ALL");
    private final MutableLiveData<String> selectedCategoryFilter = new MutableLiveData<>("ALL");
    private final MutableLiveData<String> selectedStatusFilter = new MutableLiveData<>("ALL");

    private final MutableLiveData<ItemEntity> selectedItem = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private final MediatorLiveData<List<ItemEntity>> filteredItems = new MediatorLiveData<>();
    private LiveData<List<ItemEntity>> currentSource;

    private final LiveData<Integer> activeLostCount;
    private final LiveData<Integer> activeFoundCount;
    private final LiveData<Integer> resolvedCount;
    private final LiveData<List<ZoneCount>> zoneCounts;

    public SharedViewModel(@NonNull Application application) {
        super(application);
        this.repository = new ItemRepository(application);

        this.activeLostCount = repository.getActiveLostCount();
        this.activeFoundCount = repository.getActiveFoundCount();
        this.resolvedCount = repository.getResolvedCount();
        this.zoneCounts = repository.getZoneCounts();

        // Setup MediatorLiveData sources for reactive filter updates
        filteredItems.addSource(searchQuery, q -> updateFilteredSource());
        filteredItems.addSource(selectedTypeFilter, t -> updateFilteredSource());
        filteredItems.addSource(selectedCategoryFilter, c -> updateFilteredSource());
        filteredItems.addSource(selectedStatusFilter, s -> updateFilteredSource());

        updateFilteredSource();
    }

    private void updateFilteredSource() {
        if (currentSource != null) {
            filteredItems.removeSource(currentSource);
        }

        String q = searchQuery.getValue();
        String t = selectedTypeFilter.getValue();
        String c = selectedCategoryFilter.getValue();
        String s = selectedStatusFilter.getValue();

        currentSource = repository.searchAndFilterItems(q, t, c, s);
        filteredItems.addSource(currentSource, items -> filteredItems.setValue(items));
    }

    // Getters for LiveData
    public LiveData<List<ItemEntity>> getFilteredItems() {
        return filteredItems;
    }

    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }

    public LiveData<String> getSelectedTypeFilter() {
        return selectedTypeFilter;
    }

    public LiveData<String> getSelectedCategoryFilter() {
        return selectedCategoryFilter;
    }

    public LiveData<String> getSelectedStatusFilter() {
        return selectedStatusFilter;
    }

    public LiveData<ItemEntity> getSelectedItem() {
        return selectedItem;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Integer> getActiveLostCount() {
        return activeLostCount;
    }

    public LiveData<Integer> getActiveFoundCount() {
        return activeFoundCount;
    }

    public LiveData<Integer> getResolvedCount() {
        return resolvedCount;
    }

    public LiveData<List<ZoneCount>> getZoneCounts() {
        return zoneCounts;
    }

    // Filter Setters
    public void setSearchQuery(String query) {
        searchQuery.setValue(query != null ? query : "");
    }

    public void setTypeFilter(String type) {
        selectedTypeFilter.setValue(type != null ? type : "ALL");
    }

    public void setCategoryFilter(String category) {
        selectedCategoryFilter.setValue(category != null ? category : "ALL");
    }

    public void setStatusFilter(String status) {
        selectedStatusFilter.setValue(status != null ? status : "ALL");
    }

    public void setSelectedItem(ItemEntity item) {
        selectedItem.setValue(item);
    }

    // Operations
    public void insertItem(ItemEntity item, Runnable onComplete) {
        isLoading.setValue(true);
        repository.insert(item, () -> {
            isLoading.postValue(false);
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    public void updateItem(ItemEntity item) {
        repository.update(item);
    }

    public void deleteItem(ItemEntity item) {
        repository.delete(item);
    }

    public void resolveItem(int itemId) {
        repository.updateStatus(itemId, "RESOLVED");
    }

    /**
     * Calculates smart match suggestions for a given item asynchronously.
     */
    public void calculateMatchSuggestions(ItemEntity targetItem, Consumer<List<MatchCandidate>> callback) {
        if (targetItem == null || callback == null) return;

        repository.getAllActiveItemsSync(activeItems -> {
            List<MatchCandidate> matches = MatchScorer.findMatches(targetItem, activeItems);
            callback.accept(matches);
        });
    }
}
