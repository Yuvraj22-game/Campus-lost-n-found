package com.example.campuslost_and_found.data;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository class managing data operations for Lost & Found items.
 * Handles background threading using ExecutorService.
 */
public class ItemRepository {

    private final ItemDao itemDao;
    private final ExecutorService executorService;

    public ItemRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.itemDao = db.itemDao();
        this.executorService = Executors.newFixedThreadPool(4);
    }

    public LiveData<List<ItemEntity>> getAllItems() {
        return itemDao.getAllItems();
    }

    public LiveData<List<ItemEntity>> getItemsByType(String type) {
        return itemDao.getItemsByType(type);
    }

    public LiveData<List<ItemEntity>> getItemsByStatus(String status) {
        return itemDao.getItemsByStatus(status);
    }

    public LiveData<List<ItemEntity>> getItemsByRollNumber(String roll) {
        return itemDao.getItemsByRollNumber(roll);
    }

    public LiveData<ItemEntity> getItemById(int id) {
        return itemDao.getItemById(id);
    }

    public LiveData<List<ItemEntity>> searchAndFilterItems(String query, String type, String category, String status) {
        return itemDao.searchAndFilterItems(query, type, category, status);
    }

    public LiveData<Integer> getActiveLostCount() {
        return itemDao.getActiveLostCount();
    }

    public LiveData<Integer> getActiveFoundCount() {
        return itemDao.getActiveFoundCount();
    }

    public LiveData<Integer> getResolvedCount() {
        return itemDao.getResolvedCount();
    }

    public LiveData<List<ZoneCount>> getZoneCounts() {
        return itemDao.getZoneCounts();
    }

    // Async Operations
    public void insert(ItemEntity item, Runnable onComplete) {
        executorService.execute(() -> {
            itemDao.insert(item);
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    public void update(ItemEntity item) {
        executorService.execute(() -> itemDao.update(item));
    }

    public void delete(ItemEntity item) {
        executorService.execute(() -> itemDao.delete(item));
    }

    public void updateStatus(int id, String status) {
        executorService.execute(() -> itemDao.updateStatus(id, status));
    }

    public void getAllActiveItemsSync(OnActiveItemsLoadedListener listener) {
        executorService.execute(() -> {
            List<ItemEntity> items = itemDao.getAllActiveItemsSync();
            if (listener != null) {
                listener.onLoaded(items);
            }
        });
    }

    public interface OnActiveItemsLoadedListener {
        void onLoaded(List<ItemEntity> items);
    }
}
