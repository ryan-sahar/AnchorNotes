package com.example.anchornotes.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(
        entities = {
                Note.class,
                Reminder.class,
                Tag.class,
                NoteTag.class
        },
        version = 3,           // ⬅ IMPORTANT: bump version (2 → 3)
        exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class LocalDatabase extends RoomDatabase {

    private static volatile LocalDatabase INSTANCE;

    public abstract NoteDao noteDao();
    public abstract TagDao tagDao();   // ⬅ NEW

    public static LocalDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (LocalDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    LocalDatabase.class,
                                    "anchornotes.db"
                            )
                            .allowMainThreadQueries() // OK for this class project
                            .fallbackToDestructiveMigration() // wipe + recreate on schema change
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
