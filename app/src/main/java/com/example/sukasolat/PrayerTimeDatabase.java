package com.example.sukasolat;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {PrayerTime.class}, version = 4, exportSchema = false)
public abstract class PrayerTimeDatabase extends RoomDatabase {
    public abstract PrayerTimeDao prayerTimeDao();

    private static PrayerTimeDatabase instance;

    public static synchronized PrayerTimeDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            PrayerTimeDatabase.class, "prayer_time_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}