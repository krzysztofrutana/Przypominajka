package com.example.przypominajka.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.example.przypominajka.R;

import com.example.przypominajka.utils.MyPrzypominajkaApp;
import com.example.przypominajka.databases.viewModels.SettingsViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;


import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;


public class RemoteBackupSettings extends AppCompatActivity {

    private static final String TAG = "RemoteBackupSettingsActivity";
    private static final int RC_SIGN_IN = 3;

    // Strings
    private static final String LOGGED_IN = MyPrzypominajkaApp.get().getResources().getString(R.string.RBS_logged_in);
    private static final String LOGGED_OUT = MyPrzypominajkaApp.get().getResources().getString(R.string.RBS_logged_out);
    private static final String LOG_IN = MyPrzypominajkaApp.get().getResources().getString(R.string.RBS_log_in);
    private static final String LOG_OUT = MyPrzypominajkaApp.get().getResources().getString(R.string.RBS_log_out);


    private Button signInButton;
    private TextView statusField;
    private TextView accountNameField;

    private EditText fileNameField;
    private CheckBox addDateTimeToNameChechbox;

    private final SettingsViewModel settingsViewModel = new SettingsViewModel(MyPrzypominajkaApp.get());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_backup_settings);

        Toolbar toolbar = findViewById(R.id.toolbarRemoteBackupSettings);
        setSupportActionBar(toolbar);
        // and back button
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        signInButton = findViewById(R.id.button_log_in);
        signInButton.setOnClickListener(v -> {
            if (statusField.getText().toString().equals("Nie zalogowano")) {
                signIn();
            } else if (statusField.getText().toString().equals("Zalogowano")) {
                signOut();
            }

        });
        statusField = findViewById(R.id.textView_sign_in_status);
        accountNameField = findViewById(R.id.textView_account_name);

        fileNameField = findViewById(R.id.editText_file_name_remote_backup);
        addDateTimeToNameChechbox = findViewById(R.id.chechbox_add_date_time_to_name_remote_backup);
        Button saveSettings = findViewById(R.id.button_save_settings_remote_backup);

        saveSettings.setOnClickListener(v -> {
            if (!fileNameField.getText().toString().equals("")) {
                String filename;
                if (addDateTimeToNameChechbox.isChecked()) {
                    filename = fileNameField.getText().toString() + "_" + DateTime.now().toString(DateTimeFormat.forPattern("dd.MM.YYYY-HH:mm")) + ".db";
                } else {
                    filename = fileNameField.getText().toString() + ".db";
                }
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(MyPrzypominajkaApp.get());
                boolean temp = GoogleSignIn.hasPermissions(account, new Scope(Scopes.DRIVE_FILE), new Scope(Scopes.EMAIL));
                if (account != null && temp) {
                    filename = filename + "|" + account.getDisplayName();
                    int result = settingsViewModel.updateRemoteBackupFileName(filename);
                    if (result == 1) {
                        this.finish();
                    }
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if the user is already signed in and all required scopes are granted
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(MyPrzypominajkaApp.get());
        boolean temp = GoogleSignIn.hasPermissions(account, new Scope(Scopes.DRIVE_FILE), new Scope(Scopes.EMAIL));
        if (account != null && temp) {
            updateUI(account);
        } else {
            updateUI(null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(new Scope(Scopes.DRIVE_FILE))
                        .requestEmail()
                        .build();
        return GoogleSignIn.getClient(MyPrzypominajkaApp.get(), signInOptions);
    }

    private void signIn() {

        GoogleSignInClient mGoogleSignInClient = buildGoogleSignInClient();
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void signOut() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(new Scope(Scopes.DRIVE_FILE))
                        .requestEmail()
                        .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(MyPrzypominajkaApp.get(), signInOptions);
        googleSignInClient.signOut().addOnCompleteListener(this, task -> updateUI(null));
    }


    private void updateUI(@Nullable GoogleSignInAccount account) {
        if (account != null) {
            statusField.setText(LOGGED_IN);
            accountNameField.setText(account.getDisplayName());
            accountNameField.setVisibility(View.VISIBLE);
            signInButton.setText(LOG_IN);
        } else {
            statusField.setText(LOGGED_OUT);
            accountNameField.setText("");
            accountNameField.setVisibility(View.GONE);
            signInButton.setText(LOG_OUT);
        }
    }

}