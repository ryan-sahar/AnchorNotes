package com.example.anchornotes.data;

import androidx.room.TypeConverter;

import java.util.Date;
import java.util.UUID;

public class Converters {

    // ---------- UUID <-> String ----------

    @TypeConverter
    public static UUID fromStringToUUID(String value) {
        if (value == null) return null;
        return UUID.fromString(value);
    }

    @TypeConverter
    public static String fromUUIDToString(UUID uuid) {
        if (uuid == null) return null;
        return uuid.toString();
    }

    // ---------- Date <-> Long ----------

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        if (value == null) return null;
        return new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        if (date == null) return null;
        return date.getTime();
    }
}
