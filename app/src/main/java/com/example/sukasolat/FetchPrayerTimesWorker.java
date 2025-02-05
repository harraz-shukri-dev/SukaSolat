package com.example.sukasolat;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import retrofit2.Call;
import retrofit2.Response;

public class FetchPrayerTimesWorker extends Worker {

    public FetchPrayerTimesWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        double latitude = getInputData().getDouble("latitude", 0.0);
        double longitude = getInputData().getDouble("longitude", 0.0);
        int methodId = getInputData().getInt("method_id", 3); // Default to Muslim World League
        String city = getInputData().getString("city");
        String country = getInputData().getString("country");

        AladhanService service = ApiClient.getClient().create(AladhanService.class);
        Call<PrayerTimesResponse> call;

        if (latitude != 0.0 && longitude != 0.0) {
            call = service.getPrayerTimesByCoordinates(latitude, longitude, methodId);
        } else {
            call = service.getPrayerTimes(city, country, methodId);
        }

        try {
            Response<PrayerTimesResponse> response = call.execute();
            if (response.isSuccessful() && response.body() != null) {
                PrayerTimesResponse.Data.Timings timings = response.body().getData().getTimings();
                PrayerTime prayerTime = new PrayerTime(
                        timings.getImsak(),
                        timings.getFajr(),
                        timings.getSunrise(),
                        timings.getDhuhr(),
                        timings.getAsr(),
                        timings.getMaghrib(),
                        timings.getIsha()
                );

                PrayerTimeRepository repository = new PrayerTimeRepository((Application) getApplicationContext());
                repository.insert(prayerTime);

                Data outputData = new Data.Builder()
                        .putBoolean("success", true)
                        .build();

                return Result.success(outputData);
            } else {
                return Result.failure();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
}