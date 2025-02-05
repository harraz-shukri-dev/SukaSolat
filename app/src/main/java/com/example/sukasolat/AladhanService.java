package com.example.sukasolat;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AladhanService {
    @GET("v1/timingsByCity")
    Call<PrayerTimesResponse> getPrayerTimes(@Query("city") String city, @Query("country") String country, @Query("method") int methodId);

    @GET("v1/timings")
    Call<PrayerTimesResponse> getPrayerTimesByCoordinates(@Query("latitude") double latitude, @Query("longitude") double longitude, @Query("method") int methodId);
}