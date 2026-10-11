package com.example.campuscompanion;

// Experiment 8: Fragments + Intents - single Activity hosting 5 fragments via bottom navigation

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNav);

        // Show Home fragment by default
        loadFragment(new HomeFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selected = new HomeFragment();
            } else if (id == R.id.nav_records) {
                selected = new RecordsFragment();
            } else if (id == R.id.nav_weather) {
                selected = new WeatherFragment();
            } else if (id == R.id.nav_map) {
                selected = new MapFragment();
            } else {
                selected = new ProfileFragment();
            }
            return loadFragment(selected);
        });
    }

    public void selectTab(int id) { bottomNav.setSelectedItemId(id); }

    private boolean loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
        return true;
    }
}
