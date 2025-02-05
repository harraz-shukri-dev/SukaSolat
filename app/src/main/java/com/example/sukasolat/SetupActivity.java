package com.example.sukasolat;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class SetupActivity extends AppCompatActivity {

    private EditText editTextCity, editTextCountry;
    private SwitchCompat switchAutoLocate;
    private Spinner spinnerCalculationMethod;
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
        setContentView(R.layout.activity_setup);

        // Initialize views
        editTextCity = findViewById(R.id.edit_text_city);
        editTextCountry = findViewById(R.id.edit_text_country);
        spinnerCalculationMethod = findViewById(R.id.spinner_calculation_method);
        buttonSave = findViewById(R.id.button_save);
        switchAutoLocate = findViewById(R.id.switch_auto_locate);

        // Set up spinner adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, calculationMethods);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCalculationMethod.setAdapter(adapter);

        // Toggle City & Country fields based on switch state
        switchAutoLocate.setOnCheckedChangeListener((buttonView, isChecked) -> toggleCityCountryFields(isChecked));

        // Save Button Click Listener
        buttonSave.setOnClickListener(v -> showSaveConfirmationDialog());
    }

    private void showSaveConfirmationDialog() {
        String city = editTextCity.getText().toString().trim();
        String country = editTextCountry.getText().toString().trim();
        int selectedMethodIndex = spinnerCalculationMethod.getSelectedItemPosition();
        boolean autoLocate = switchAutoLocate.isChecked();

        // Validate inputs only if auto-locate is OFF
        if (!autoLocate) {
            if (TextUtils.isEmpty(city)) {
                editTextCity.setError("City cannot be empty");
                editTextCity.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(country)) {
                editTextCountry.setError("Country cannot be empty");
                editTextCountry.requestFocus();
                return;
            }
        }

        if (selectedMethodIndex < 0 || selectedMethodIndex >= calculationMethodIds.length) {
            Toast.makeText(this, "Invalid calculation method selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Save");
        builder.setMessage("Are you sure you want to save these settings?");

        // "Yes" button saves preferences
        builder.setPositiveButton("Yes", (dialog, which) -> savePreferences(city, country, selectedMethodIndex, autoLocate));

        // "No" button cancels saving
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

        // Show dialog
        builder.create().show();
    }

    private void savePreferences(String city, String country, int selectedMethodIndex, boolean autoLocate) {
        int methodId = calculationMethodIds[selectedMethodIndex];

        try {
            // Save to SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("calculation_method_id", methodId);
            editor.putBoolean("auto_locate", autoLocate);
            editor.putBoolean("setup_complete", true); // Mark setup as complete

            if (autoLocate) {
                // Clear city and country when auto-locate is enabled
                editor.remove("city");
                editor.remove("country");
            } else {
                editor.putString("city", city);
                editor.putString("country", country);
            }

            editor.apply();

            // Show success message
            Toast.makeText(this, "Preferences saved successfully", Toast.LENGTH_SHORT).show();

            // Navigate to MainActivity
            Intent intent = new Intent(SetupActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving preferences", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleCityCountryFields(boolean autoLocateEnabled) {
        editTextCity.setEnabled(!autoLocateEnabled);
        editTextCountry.setEnabled(!autoLocateEnabled);

        // Show/hide info message
        TextView textAutoLocateInfo = findViewById(R.id.text_auto_locate_info);
        textAutoLocateInfo.setVisibility(autoLocateEnabled ? View.VISIBLE : View.GONE);
    }

}
