package com.example.campuslost_and_found.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Singleton Room Database class for Campus Lost & Found.
 */
@Database(entities = {ItemEntity.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "campus_lost_and_found_db";
    private static volatile AppDatabase INSTANCE;

    public abstract ItemDao itemDao();

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME)
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    // Seed initial campus data in background thread on DB creation
                                    Executors.newSingleThreadExecutor().execute(() -> {
                                        ItemDao dao = getInstance(context).itemDao();
                                        dao.insertAll(getInitialSeedData());
                                    });
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Helper method producing 8 realistic initial sample items.
     */
    public static List<ItemEntity> getInitialSeedData() {
        List<ItemEntity> items = new ArrayList<>();
        long now = System.currentTimeMillis();
        long DAY_IN_MS = 24L * 60L * 60L * 1000L;

        // 1. Lost HP Laptop
        items.add(new ItemEntity(
                "LOST",
                "HP Pavilion Laptop Black",
                "Dark silver 15-inch HP laptop inside a grey Targus bag. Left on 2nd floor reading table near window.",
                "Electronics",
                "Central Library",
                now - (1 * DAY_IN_MS),
                "",
                "9876543210",
                "student1@campus.edu",
                true,
                "",
                "",
                "ACTIVE",
                "2024CS102",
                now - (1 * DAY_IN_MS)
        ));

        // 2. Found Boat Airdopes
        items.add(new ItemEntity(
                "FOUND",
                "Boat Airdopes 141 Blue Case",
                "Found blue wireless earbuds charging case with sticker on bottom near counter #3.",
                "Electronics",
                "Student Canteen",
                now,
                "",
                "9812345678",
                "finder2@campus.edu",
                false,
                "What sticker or initials are on the case bottom?",
                "Batman superhero sticker",
                "ACTIVE",
                "2023EC045",
                now
        ));

        // 3. Lost Student ID Card
        items.add(new ItemEntity(
                "LOST",
                "Campus ID Card & Lanyard",
                "Lost blue lanyard with ID card belonging to Roll No 2025ME088 along with hostel gate pass.",
                "ID Cards & Wallets",
                "Academic Block",
                now - (3 * DAY_IN_MS),
                "",
                "9765432109",
                "me2025@campus.edu",
                true,
                "",
                "",
                "ACTIVE",
                "2025ME088",
                now - (3 * DAY_IN_MS)
        ));

        // 4. Found Milton Water Bottle
        items.add(new ItemEntity(
                "FOUND",
                "Milton Stainless Steel Flask",
                "Blue 1 litre insulated water bottle with stickers on side found near basketball court bench.",
                "Personal Belongings",
                "Sports Complex",
                now - (5 * DAY_IN_MS),
                "",
                "9654321098",
                "sports_sec@campus.edu",
                true,
                "Name one of the anime stickers on the bottle.",
                "Naruto",
                "ACTIVE",
                "2023PE012",
                now - (5 * DAY_IN_MS)
        ));

        // 5. Lost Black Leather Wallet
        items.add(new ItemEntity(
                "LOST",
                "Black WildHorn Leather Wallet",
                "Black fold wallet containing Driving License, SBI ATM card, and mess token.",
                "ID Cards & Wallets",
                "Hostels Block",
                now - (2 * DAY_IN_MS),
                "",
                "9543210987",
                "hostel3_student@campus.edu",
                true,
                "",
                "",
                "ACTIVE",
                "2024EE067",
                now - (2 * DAY_IN_MS)
        ));

        // 6. Found Scientific Calculator
        items.add(new ItemEntity(
                "FOUND",
                "Casio FX-991EX Calculator",
                "Found black scientific calculator after Mid-Term Exam in Hall B.",
                "Electronics",
                "Main Auditorium",
                now - (12 * DAY_IN_MS),
                "",
                "9432109876",
                "invigilator@campus.edu",
                false,
                "What name or roll number is etched on the sliding cover back?",
                "Rahul 2024",
                "ACTIVE",
                "2022CS001",
                now - (12 * DAY_IN_MS)
        ));

        // 7. Lost Honda Bike Keychain (Old item >30 days)
        items.add(new ItemEntity(
                "LOST",
                "Honda Key with Red Keychain",
                "Honda Activa key attached to a red rubber Marvel keychain lost near gate 2 parking.",
                "Keys & Chains",
                "Campus Parking",
                now - (35 * DAY_IN_MS),
                "",
                "9321098765",
                "parking_user@campus.edu",
                true,
                "",
                "",
                "ACTIVE",
                "2023CE090",
                now - (35 * DAY_IN_MS)
        ));

        // 8. Found Engineering Physics Book (Resolved)
        items.add(new ItemEntity(
                "FOUND",
                "Engineering Physics Textbook",
                "Hardbound textbook found on row 4 seat during morning lecture. Successfully returned!",
                "Books & Documents",
                "Academic Block",
                now - (10 * DAY_IN_MS),
                "",
                "9210987654",
                "cr_physics@campus.edu",
                true,
                "What is written on the first inner cover page?",
                "Library Stamp",
                "RESOLVED",
                "2025PH002",
                now - (10 * DAY_IN_MS)
        ));

        return items;
    }
}
