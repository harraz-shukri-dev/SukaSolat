package com.example.sukasolat;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PrayerTimeRepository {
    private PrayerTimeDao prayerTimeDao;
    private LiveData<PrayerTime> latestPrayerTime;

    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    public PrayerTimeRepository(Application application) {
        PrayerTimeDatabase database = PrayerTimeDatabase.getInstance(application);
        prayerTimeDao = database.prayerTimeDao();
        latestPrayerTime = prayerTimeDao.getLatestPrayerTime();
    }

    public LiveData<PrayerTime> getLatestPrayerTime() {
        return latestPrayerTime;
    }

    public void insert(PrayerTime prayerTime) {
        executorService.execute(() -> prayerTimeDao.insert(prayerTime));
    }
}