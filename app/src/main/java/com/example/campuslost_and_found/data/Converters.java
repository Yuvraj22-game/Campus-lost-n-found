package com.example.campuslost_and_found.data;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * Room Type Converters for converting Date objects to timestamps and vice versa.
 */
public class Converters {

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
