package com.example.sukasolat;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface PrayerTimeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PrayerTime prayerTime);

    @Query("SELECT * FROM prayer_times ORDER BY id DESC LIMIT 1")
    LiveData<PrayerTime> getLatestPrayerTime();
}