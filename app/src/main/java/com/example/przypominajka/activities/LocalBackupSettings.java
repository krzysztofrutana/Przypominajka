package com.example.przypominajka.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.przypominajka.R;
import com.example.przypominajka.utils.FileUtil;
import com.example.przypominajka.utils.MyPrzypominajkaApp;
import com.example.przypominajka.viewModels.SettingsViewModel;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.File;

public class LocalBackupSettings extends AppCompatActivity {

    private Button chooseFolderButton;
    private TextView dirPathField;
    private EditText fileNameField;
    private CheckBox addDateTimeToNameChechbox;
    private Button saveSettings;

    private String dirPath;

    private static final int PATH_REQUEST_CODE = 1;

    private SettingsViewModel settingsViewModel = new SettingsViewModel(MyPrzypominajkaApp.get());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_backup_settings);
        Toolbar toolbar = findViewById(R.id.toolbarLocalBackupSettings);
        setSupportActionBar(toolbar);
        // and back button
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        chooseFolderButton = findViewById(R.id.button_choose_folder);
        chooseFolderButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            startActivityForResult(Intent.createChooser(intent, "Wybierz folder"), PATH_REQUEST_CODE);
        });

        dirPathField = findViewById(R.id.dir_location);
        fileNameField = findViewById(R.id.editText_file_name);
        addDateTimeToNameChechbox = findViewById(R.id.chechbox_add_date_time_to_name);
        saveSettings = findViewById(R.id.button_save_settings_local_backup);
        saveSettings.setOnClickListener(v -> {
            String filename;
            if (addDateTimeToNameChechbox.isChecked()) {
                filename = fileNameField.getText().toString() + "_" + DateTime.now().toString(DateTimeFormat.forPattern("dd.MM.YYYY-HH:mm")) + ".db";
            } else {
                filename = fileNameField.getText().toString() + ".db";
            }
            String fullPath = dirPath + File.separator + filename;
            settingsViewModel.updateLocalBackupLocation(fullPath + "|" + dirPath + "|" + filename);
            this.finish();
        });

        settingsViewModel.getLocalBackupLocation().observe(this, s -> {
            if (s.equals("") || s == null) {
                dirPathField.setVisibility(LinearLayout.GONE);
            } else {
                String[] paths = s.split("\\|");
                chooseFolderButton.setText("Zmień folder");
                dirPathField.setVisibility(LinearLayout.VISIBLE);
                dirPathField.setText(paths[1]);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PATH_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri treeUri = data.getData();
                    dirPath = FileUtil.getFullPathFromTreeUri(treeUri, this);
                    dirPathField.setText(dirPath);
                    dirPathField.setVisibility(LinearLayout.VISIBLE);
                }
        }
    }

}