package com.example.sukasolat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.time.chrono.HijrahChronology;
import java.time.chrono.HijrahDate;

public class MainActivity extends AppCompatActivity {
    private TextView textViewNextPrayerName, textViewNextPrayerTimeRemaining, textViewUserLocation, textViewDay, textViewGregorianDate, textViewHijriDate;
    private RecyclerView recyclerViewPrayerTimes;
    private PrayerTimeAdapter prayerTimeAdapter;
    private MainViewModel viewModel;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        textViewNextPrayerName = findViewById(R.id.text_view_next_prayer_name);
        textViewNextPrayerTimeRemaining = findViewById(R.id.text_view_next_prayer_time_remaining);
        textViewUserLocation = findViewById(R.id.text_view_user_location);
        textViewGregorianDate = findViewById(R.id.text_view_gregorian_date);
        recyclerViewPrayerTimes = findViewById(R.id.recycler_view_prayer_times);
        textViewHijriDate = findViewById(R.id.text_view_hijri_date);
        textViewDay = findViewById(R.id.text_view_day);


        // Setup RecyclerView
        prayerTimeAdapter = new PrayerTimeAdapter();
        recyclerViewPrayerTimes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPrayerTimes.setAdapter(prayerTimeAdapter);

        // Setup ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getPrayerTime().observe(this, new Observer<PrayerTime>() {
            @Override
            public void onChanged(PrayerTime prayerTime) {
                if (prayerTime != null) {
                    updateUIWithPrayerTimes(prayerTime);
                }
            }
        });

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Fetch Prayer Times
        fetchPrayerTimes();

        // Bottom Navigation Setup
        BottomNavigationView bottomNavView = findViewById(R.id.bottom_nav_view);
        bottomNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    // Already in MainActivity
                    return true;
                } else if (id == R.id.nav_settings) {
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    return true;
                } else if (id == R.id.nav_about) {
                    startActivity(new Intent(MainActivity.this, AboutActivity.class));
                    return true;
                }
                return false;
            }
        });

        // Set the selected item in the bottom navigation bar
        bottomNavView.setSelectedItemId(R.id.nav_home);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure the correct item is selected when returning to MainActivity
        BottomNavigationView bottomNavView = findViewById(R.id.bottom_nav_view);
        bottomNavView.setSelectedItemId(R.id.nav_home);
    }

    private void fetchPrayerTimes() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        boolean autoLocate = sharedPreferences.getBoolean("auto_locate", false);

        if (autoLocate) {
            // Check location permissions
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
                return;
            }

            // Fetch last known location
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            fetchCityAndCountryFromLocation(latitude, longitude);
                            fetchPrayerTimesByCoordinates(latitude, longitude);
                        } else {
                            fetchPrayerTimesByCityAndCountry();
                        }
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        textViewUserLocation.setText("Location not available");
                        fetchPrayerTimesByCityAndCountry(); // Fallback
                    });

        } else {
            fetchPrayerTimesByCityAndCountry();
        }
    }

    private void fetchCityAndCountryFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String city = address.getLocality(); // Get city name
                String country = address.getCountryName(); // Get country name

                // Save city and country to SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("city", city);
                editor.putString("country", country);
                editor.apply();

                // Update UI
                textViewUserLocation.setText(city + ", " + country);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fetchPrayerTimesByCoordinates(double latitude, double longitude) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        int methodId = sharedPreferences.getInt("calculation_method_id", 3); // Default to Muslim World League
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(FetchPrayerTimesWorker.class)
                .setInputData(new Data.Builder()
                        .putDouble("latitude", latitude)
                        .putDouble("longitude", longitude)
                        .putInt("method_id", methodId)
                        .build())
                .build();
        WorkManager.getInstance(this).enqueue(workRequest);
    }

    private void fetchPrayerTimesByCityAndCountry() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        String city = sharedPreferences.getString("city", "Unknown");
        String country = sharedPreferences.getString("country", "Unknown");
        int methodId = sharedPreferences.getInt("calculation_method_id", 3); // Default to Muslim World League
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(FetchPrayerTimesWorker.class)
                .setInputData(new Data.Builder()
                        .putString("city", city)
                        .putString("country", country)
                        .putInt("method_id", methodId)
                        .build())
                .build();

        WorkManager.getInstance(this).enqueue(workRequest);
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(workRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        Data outputData = workInfo.getOutputData();
                        if (outputData.getBoolean("success", false)) {
                            // Prayer times updated successfully
                        } else {
                            textViewNextPrayerTimeRemaining.setText("Error fetching prayer times");
                        }
                    }
                });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchPrayerTimes();
            }
        }
    }

    private String addMinutesToTime(String time, int minutesToAdd) {
        try {
            if (time == null || !time.matches("\\d{1,2}:\\d{2}")) {
                return time; // Return the original time if invalid
            }

            // Parse the time string into LocalDateTime
            LocalDate currentDate = LocalDate.now();
            String[] parts = time.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);

            LocalDateTime dateTime = LocalDateTime.of(currentDate, LocalTime.of(hours, minutes));

            // Add the specified minutes
            LocalDateTime updatedDateTime = dateTime.plusMinutes(minutesToAdd);

            // Format back to 24-hour time string
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());
            return updatedDateTime.format(formatter);
        } catch (Exception e) {
            e.printStackTrace();
            return time; // Return the original time if an error occurs
        }
    }

    private void updateUIWithPrayerTimes(PrayerTime prayerTime) {

        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        int methodId = sharedPreferences.getInt("calculation_method_id", 3); // Default to Muslim World League

        // Adjust prayer times if the method ID is 17
        if (methodId == 17) {
            prayerTime.setImsak(addMinutesToTime(prayerTime.getImsak(), 10)); // +10 min for Imsak
            prayerTime.setFajr(addMinutesToTime(prayerTime.getFajr(), 10));   // +10 min for Fajr
            prayerTime.setSunrise(addMinutesToTime(prayerTime.getSunrise(), 1)); // +1 min for Sunrise
            prayerTime.setDhuhr(addMinutesToTime(prayerTime.getDhuhr(), 3));  // +3 min for Dhuhr
            prayerTime.setAsr(addMinutesToTime(prayerTime.getAsr(), 3));      // +3 min for Asr
            prayerTime.setMaghrib(addMinutesToTime(prayerTime.getMaghrib(), 1)); // +1 min for Maghrib
            prayerTime.setIsha(addMinutesToTime(prayerTime.getIsha(), 2));    // +2 min for Isha
        }

        // Update Next Prayer Time
        String[] prayerNames = {"Imsak", "Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha"};
        String[] prayerTimes = {prayerTime.getImsak(), prayerTime.getFajr(), prayerTime.getSunrise(), prayerTime.getDhuhr(), prayerTime.getAsr(), prayerTime.getMaghrib(), prayerTime.getIsha()};
        // Current date and time
        LocalDateTime currentDateTime = LocalDateTime.now();
        // Find the current prayer time
        LocalDateTime currentPrayerDateTime = null;
        String currentPrayerName = "";
        for (int i = 0; i < prayerTimes.length; i++) {
            LocalDateTime prayerDateTime = parseTimeToLocalDateTime(prayerTimes[i]);
            if (currentDateTime.isAfter(prayerDateTime) && (i == prayerTimes.length - 1 || currentDateTime.isBefore(parseTimeToLocalDateTime(prayerTimes[i + 1])))) {
                currentPrayerDateTime = prayerDateTime;
                currentPrayerName = prayerNames[i];
                break;
            }
        }
        // Find the next prayer time
        LocalDateTime nextPrayerDateTime = null;
        String nextPrayerName = "";
        for (int i = 0; i < prayerTimes.length; i++) {
            LocalDateTime prayerDateTime = parseTimeToLocalDateTime(prayerTimes[i]);
            if (prayerDateTime.isAfter(currentDateTime)) {
                nextPrayerDateTime = prayerDateTime;
                nextPrayerName = prayerNames[i];
                break;
            }
        }
        // If no future prayer time found, wrap around to the next day
        if (nextPrayerDateTime == null) {
            nextPrayerDateTime = parseTimeToLocalDateTime(prayerTimes[0]).plusDays(1);
            nextPrayerName = prayerNames[0];
        }
        // Calculate time remaining
        Duration duration = Duration.between(currentDateTime, nextPrayerDateTime);
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        textViewNextPrayerName.setText(nextPrayerName + ", " + nextPrayerDateTime.toLocalTime());
        textViewNextPrayerTimeRemaining.setText(nextPrayerName + " in " + hours + " hours, " + minutes + " minutes");
        // Update User Location and Dates
        // SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        String city = sharedPreferences.getString("city", "");
        String country = sharedPreferences.getString("country", "");
        textViewUserLocation.setText(city + ", " + country);
        LocalDate currentDate = LocalDate.now();
        // Get Day Name (Monday, Tuesday, etc.)
        String dayName = currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
        // Set Day Name to TextView
        textViewDay.setText(dayName);
        // Get real Gregorian date
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault());
        String gregorianDate = currentDate.format(dateFormatter);
        textViewGregorianDate.setText(gregorianDate);
        // Convert to Hijri
        HijrahDate hijriDate = HijrahChronology.INSTANCE.date(currentDate);
        DateTimeFormatter hijriFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault());
        String hijriDateString = hijriDate.format(hijriFormatter);
        textViewHijriDate.setText(hijriDateString);
        // Update Prayer Times List
        List<PrayerTimeItem> prayerTimeItems = new ArrayList<>();
        prayerTimeItems.add(new PrayerTimeItem(R.drawable.ic_imsak, "Imsak", prayerTime.getImsak()));
        prayerTimeItems.add(new PrayerTimeItem(R.drawable.ic_fajr, "Fajr", prayerTime.getFajr()));
        prayerTimeItems.add(new PrayerTimeItem(R.drawable.ic_sunrise, "Sunrise", prayerTime.getSunrise()));
        prayerTimeItems.add(new PrayerTimeItem(R.drawable.ic_dhuhr, "Dhuhr", prayerTime.getDhuhr()));
        prayerTimeItems.add(new PrayerTimeItem(R.drawable.ic_asr, "Asr", prayerTime.getAsr()));
        prayerTimeItems.add(new PrayerTimeItem(R.drawable.ic_maghrib, "Maghrib", prayerTime.getMaghrib()));
        prayerTimeItems.add(new PrayerTimeItem(R.drawable.ic_isha, "Isha", prayerTime.getIsha()));

        // Highlight the current prayer time, excluding Imsak and Sunrise
        for (PrayerTimeItem item : prayerTimeItems) {
            if (item.getName().equals(currentPrayerName) && !item.getName().equals("Imsak") && !item.getName().equals("Sunrise")) {
                item.setCurrentHighlighted(true);
            } else {
                item.setCurrentHighlighted(false);
            }
        }

        // Highlight the next prayer time, excluding Imsak and Sunrise
        for (PrayerTimeItem item : prayerTimeItems) {
            if (item.getName().equals(nextPrayerName) && !item.getName().equals("Imsak") && !item.getName().equals("Sunrise")) {
                item.setHighlighted(true);
            } else {
                item.setHighlighted(false);
            }
        }

        prayerTimeAdapter.setPrayerTimeItems(prayerTimeItems);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // Refresh data when returning from SettingsActivity
            fetchPrayerTimes();
        }
    }

    private LocalDateTime parseTimeToLocalDateTime(String time) {
        try {
            if (time == null || !time.matches("\\d{1,2}:\\d{2} ?(AM|PM)?")) {
                return LocalDateTime.now().plusMinutes(1); // Default to 1 min later if invalid
            }

            LocalDate currentDate = LocalDate.now();
            String[] parts = time.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
            boolean isPM = time.contains("PM");

            if (isPM && hours != 12) {
                hours += 12;
            } else if (!isPM && hours == 12) {
                hours = 0;
            }

            return LocalDateTime.of(currentDate, LocalTime.of(hours, minutes));
        } catch (Exception e) {
            e.printStackTrace();
            return LocalDateTime.now().plusMinutes(1); // Prevent crashes by defaulting to a future time
        }
    }

    @Override
    public void onBackPressed() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    finishAffinity(); // Close the app
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss(); // Dismiss dialog
                })
                .show();
    }

}