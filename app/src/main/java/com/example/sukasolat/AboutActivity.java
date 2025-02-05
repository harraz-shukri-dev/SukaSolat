package com.example.sukasolat;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Bottom Navigation Setup
        BottomNavigationView bottomNavView = findViewById(R.id.bottom_nav_view);
        bottomNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    startActivity(new Intent(AboutActivity.this, MainActivity.class));
                    return true;
                } else if (id == R.id.nav_settings) {
                    startActivity(new Intent(AboutActivity.this, SettingsActivity.class));
                    return true;
                } else if (id == R.id.nav_about) {
                    // Already in AboutActivity
                    return true;
                }
                return false;
            }
        });

        // Set the selected item in the bottom navigation bar
        bottomNavView.setSelectedItemId(R.id.nav_about);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure the correct item is selected when returning to AboutActivity
        BottomNavigationView bottomNavView = findViewById(R.id.bottom_nav_view);
        bottomNavView.setSelectedItemId(R.id.nav_about);
    }
}