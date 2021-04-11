package com.highcom.todolog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        setTitle(getString(R.string.setting_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView licenseTextView = findViewById(R.id.license_text);
        licenseTextView.setOnClickListener(view -> {
            OssLicensesMenuActivity.setActivityTitle(getString(R.string.setting_license));
            startActivity(new Intent(getApplicationContext(), OssLicensesMenuActivity.class));
        });

        TextView privacyPolicyTextView = findViewById(R.id.privacy_policy_text);
        privacyPolicyTextView.setOnClickListener(view -> {
            Uri uri = Uri.parse(getString(R.string.privacy_policy_url));
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            startActivity(intent);
        });

        TextView starTextView = findViewById(R.id.star_text);
        starTextView.setOnClickListener(view -> {
            Uri uri = Uri.parse(getString(R.string.star_url));
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            startActivity(intent);
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}