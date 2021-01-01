package com.example.przypominajka.activities;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.util.Log;
import android.view.MenuItem;


import com.example.przypominajka.R;
import com.example.przypominajka.databases.entities.SettingsModel;
import com.example.przypominajka.fragments.calendar.CalendarFragment;
import com.example.przypominajka.fragments.events.EventsFragment;
import com.example.przypominajka.fragments.settings.SettingFragment;
import com.example.przypominajka.services.SetAlarmService;
import com.example.przypominajka.utils.CheckNotDoneEvent;
import com.example.przypominajka.utils.MyPrzypominajkaApp;
import com.example.przypominajka.databases.viewModels.SettingsViewModel;
import com.google.android.material.navigation.NavigationView;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;

    NavigationView navigationView;
    CheckNotDoneEvent checkNotDoneEvent = new CheckNotDoneEvent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CalendarFragment()).commit();
        }
        navigationView.setCheckedItem(R.id.nav_calendar);

        SettingsViewModel settingsViewModel = new SettingsViewModel(MyPrzypominajkaApp.get());
        // making job schediler to run job service in custom interval time
        final ComponentName componentName = new ComponentName(this, SetAlarmService.class);
        long checkIntervalTime = settingsViewModel.getCheckEventInterval();
        if (checkIntervalTime == 0) {
            long result = settingsViewModel.insertSettings(new SettingsModel(28800000, 900000, "", ""));
            if (result != -1) {
                Log.d("MainActivity onCreate", "Dodawanie domyślnych ustawień udane");
            } else {
                Log.d("MainActivity onCreate", "Dodawanie domyślnych ustawień nieudane");
            }
        } else {

            int _id = 123;
            JobInfo jobInfo = new JobInfo.Builder(_id, componentName)
                    .setPeriodic(checkIntervalTime)
                    .build();
            JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(jobInfo);

        }

        checkNotDoneEvent.checkAllNotDoneEventInPast(this);

    }

    // Necessary to close menu if is open when back button is pressed
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack("calendar", 0);
            navigationView.setCheckedItem(R.id.nav_calendar);
        } else {
            super.onBackPressed();
        }
    }


    // Navigate throw fragment by drawer menu.
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_calendar) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CalendarFragment()).addToBackStack("calendar").commit();
        } else if (item.getItemId() == R.id.nav_events) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new EventsFragment()).addToBackStack("events_list").commit();
        } else if (item.getItemId() == R.id.nav_settings) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingFragment()).addToBackStack("settings").commit();
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}