package com.example.campuslost_and_found.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object for Room database operations on Lost and Found items.
 */
@Dao
public interface ItemDao {

    @Query("SELECT * FROM items ORDER BY dateMillis DESC")
    LiveData<List<ItemEntity>> getAllItems();

    @Query("SELECT * FROM items WHERE type = :type ORDER BY dateMillis DESC")
    LiveData<List<ItemEntity>> getItemsByType(String type);

    @Query("SELECT * FROM items WHERE status = :status ORDER BY dateMillis DESC")
    LiveData<List<ItemEntity>> getItemsByStatus(String status);

    @Query("SELECT * FROM items WHERE postedByRoll = :postedByRoll ORDER BY dateMillis DESC")
    LiveData<List<ItemEntity>> getItemsByRollNumber(String postedByRoll);

    @Query("SELECT * FROM items WHERE id = :id")
    LiveData<ItemEntity> getItemById(int id);

    @Query("SELECT * FROM items WHERE id = :id")
    ItemEntity getItemByIdSync(int id);

    @Query("SELECT * FROM items WHERE status = 'ACTIVE' ORDER BY dateMillis DESC")
    List<ItemEntity> getAllActiveItemsSync();

    @Query("SELECT * FROM items WHERE " +
            "(:query IS NULL OR :query = '' OR title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR zone LIKE '%' || :query || '%') " +
            "AND (:type IS NULL OR :type = 'ALL' OR type = :type) " +
            "AND (:category IS NULL OR :category = 'ALL' OR category = :category) " +
            "AND (:status IS NULL OR :status = 'ALL' OR status = :status) " +
            "ORDER BY dateMillis DESC")
    LiveData<List<ItemEntity>> searchAndFilterItems(String query, String type, String category, String status);

    @Query("SELECT COUNT(*) FROM items WHERE type = 'LOST' AND status = 'ACTIVE'")
    LiveData<Integer> getActiveLostCount();

    @Query("SELECT COUNT(*) FROM items WHERE type = 'FOUND' AND status = 'ACTIVE'")
    LiveData<Integer> getActiveFoundCount();

    @Query("SELECT COUNT(*) FROM items WHERE status = 'RESOLVED'")
    LiveData<Integer> getResolvedCount();

    @Query("SELECT zone, COUNT(*) as count FROM items WHERE status = 'ACTIVE' GROUP BY zone ORDER BY count DESC")
    LiveData<List<ZoneCount>> getZoneCounts();

    @Query("SELECT COUNT(*) FROM items")
    int getItemCountSync();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ItemEntity item);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ItemEntity> items);

    @Update
    void update(ItemEntity item);

    @Delete
    void delete(ItemEntity item);

    @Query("DELETE FROM items WHERE id = :id")
    void deleteById(int id);

    @Query("UPDATE items SET status = :status WHERE id = :id")
    void updateStatus(int id, String status);
}
