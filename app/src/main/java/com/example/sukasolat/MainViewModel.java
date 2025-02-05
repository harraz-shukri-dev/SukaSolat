package com.example.sukasolat;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class MainViewModel extends AndroidViewModel {
    private PrayerTimeRepository repository;
    private LiveData<PrayerTime> latestPrayerTime;

    public MainViewModel(Application application) {
        super(application);
        repository = new PrayerTimeRepository(application);
        latestPrayerTime = repository.getLatestPrayerTime();
    }

    public LiveData<PrayerTime> getPrayerTime() {
        return latestPrayerTime;
    }

    public void insert(PrayerTime prayerTime) {
        repository.insert(prayerTime);
    }
}