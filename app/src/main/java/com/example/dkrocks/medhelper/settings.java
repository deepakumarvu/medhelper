package com.example.dkrocks.medhelper;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

public class settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Switch switch1=(Switch)findViewById(R.id.switch1);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(settings.this);
        switch1.setChecked(pref.getBoolean("ttos",false));
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(settings.this);
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("ttos",b);
                editor.commit();
            }
        });
    }
}
