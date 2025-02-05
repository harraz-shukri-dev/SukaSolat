package com.example.sukasolat;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.appcompat.widget.SwitchCompat;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsActivity extends AppCompatActivity {

    private EditText editTextCity, editTextCountry;
    private Spinner spinnerCalculationMethod;
    private SwitchCompat switchAutoLocate;
    private Button buttonSave;

    private String[] calculationMethods = {
            "Jafari / Shia Ithna-Ashari",
            "University of Islamic Sciences, Karachi",
            "Islamic Society of North America",
            "Muslim World League",
            "Umm Al-Qura University, Makkah",
            "Egyptian General Authority of Survey",
            "Institute of Geophysics, University of Tehran",
            "Gulf Region",
            "Kuwait",
            "Qatar",
            "Majlis Ugama Islam Singapura, Singapore",
            "Union Organization islamic de France",
            "Diyanet İşleri Başkanlığı, Turkey",
            "Spiritual Administration of Muslims of Russia",
            "Moonsighting Committee Worldwide (also requires shafaq parameter)",
            "Dubai (experimental)",
            "Jabatan Kemajuan Islam Malaysia (JAKIM)",
            "Tunisia",
            "Algeria",
            "KEMENAG - Kementerian Agama Republik Indonesia",
            "Morocco",
            "Comunidade Islamica de Lisboa",
            "Ministry of Awqaf, Islamic Affairs and Holy Places, Jordan"
    };

    private int[] calculationMethodIds = {
            0, 1, 2, 3, 4, 5, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize views
        editTextCity = findViewById(R.id.edit_text_city);
        editTextCountry = findViewById(R.id.edit_text_country);
        spinnerCalculationMethod = findViewById(R.id.spinner_calculation_method);
        switchAutoLocate = findViewById(R.id.switch_auto_locate);
        buttonSave = findViewById(R.id.button_save);

        // Set up spinner adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, calculationMethods);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCalculationMethod.setAdapter(adapter);

        // Load saved preferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        String city = sharedPreferences.getString("city", "");
        String country = sharedPreferences.getString("country", "");
        int methodId = sharedPreferences.getInt("calculation_method_id", 3); // Default to Muslim World League
        boolean autoLocate = sharedPreferences.getBoolean("auto_locate", false);

        editTextCity.setText(city);
        editTextCountry.setText(country);

        // Set spinner selection based on saved preference
        for (int i = 0; i < calculationMethodIds.length; i++) {
            if (calculationMethodIds[i] == methodId) {
                spinnerCalculationMethod.setSelection(i);
                break;
            }
        }

        // Set switch state based on saved preference
        switchAutoLocate.setChecked(autoLocate);
        toggleCityCountryFields(autoLocate);

        // Toggle city/country fields when auto-locate is toggled
        switchAutoLocate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            toggleCityCountryFields(isChecked);
        });

        // Save Button Click Listener
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationDialog();
            }
        });

        // Bottom Navigation Setup
        BottomNavigationView bottomNavView = findViewById(R.id.bottom_nav_view);
        bottomNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                    return true;
                } else if (id == R.id.nav_settings) {
                    // Already in SettingsActivity
                    return true;
                } else if (id == R.id.nav_about) {
                    startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
                    return true;
                }
                return false;
            }
        });

        // Set the selected item in the bottom navigation bar
        bottomNavView.setSelectedItemId(R.id.nav_settings);
    }

    private void toggleCityCountryFields(boolean autoLocateEnabled) {
        editTextCity.setEnabled(!autoLocateEnabled);
        editTextCountry.setEnabled(!autoLocateEnabled);

        // Show/hide info message
        TextView textAutoLocateInfo = findViewById(R.id.text_auto_locate_info);
        textAutoLocateInfo.setVisibility(autoLocateEnabled ? View.VISIBLE : View.GONE);
    }


    private void showConfirmationDialog() {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("Save Changes")
                    .setMessage("Are you sure you want to save these changes?")
                    .setPositiveButton("Yes", (dialog, which) -> saveChanges())
                    .setNegativeButton("No", null)
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error displaying dialog!", Toast.LENGTH_SHORT).show();
        }
    }


    private void saveChanges() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        boolean autoLocate = switchAutoLocate.isChecked();
        int selectedMethodIndex = spinnerCalculationMethod.getSelectedItemPosition();
        int methodId = calculationMethodIds[selectedMethodIndex];

        if (autoLocate) {
            // Clear city and country when auto-locate is enabled
            editor.remove("city");
            editor.remove("country");
        } else {
            String city = editTextCity.getText().toString().trim();
            String country = editTextCountry.getText().toString().trim();

            // Validate city and country
            if (city.isEmpty()) {
                editTextCity.setError("City cannot be empty!");
                editTextCity.requestFocus();
                return;
            }
            if (!city.matches("^[a-zA-Z ]+$")) {
                editTextCity.setError("City name should only contain letters!");
                editTextCity.requestFocus();
                return;
            }
            if (country.isEmpty()) {
                editTextCountry.setError("Country cannot be empty!");
                editTextCountry.requestFocus();
                return;
            }
            if (!country.matches("^[a-zA-Z ]+$")) {
                editTextCountry.setError("Country name should only contain letters!");
                editTextCountry.requestFocus();
                return;
            }

            editor.putString("city", city);
            editor.putString("country", country);
        }

        editor.putInt("calculation_method_id", methodId);
        editor.putBoolean("auto_locate", autoLocate);
        editor.apply();

        Toast.makeText(this, "Changes saved successfully!", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK, new Intent());
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Ensure the correct item is selected when returning to SettingsActivity
        BottomNavigationView bottomNavView = findViewById(R.id.bottom_nav_view);
        bottomNavView.setSelectedItemId(R.id.nav_settings);
    }
}