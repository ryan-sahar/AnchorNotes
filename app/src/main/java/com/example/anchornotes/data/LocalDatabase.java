package com.example.anchornotes.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(
        entities = {Note.class, Reminder.class},
        version = 1,
        exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class LocalDatabase extends RoomDatabase {

    private static volatile LocalDatabase INSTANCE;

    public abstract NoteDao noteDao();

    public static LocalDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (LocalDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    LocalDatabase.class,
                                    "anchornotes.db"
                            )
                            .allowMainThreadQueries() // OK for this class project, we'll keep things simple
                            .fallbackToDestructiveMigration() // fine for class projects
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
