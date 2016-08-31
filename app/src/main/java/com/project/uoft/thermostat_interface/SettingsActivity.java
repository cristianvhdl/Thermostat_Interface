package com.project.uoft.thermostat_interface;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * The activity allows the user to change the AC hand heating power
 */
public class SettingsActivity extends Activity implements View.OnClickListener{

    private EditText mACEditText, mHeatEditText;
    private String UID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mACEditText = (EditText) findViewById(R.id.ac_power_edit_text);
        mHeatEditText = (EditText) findViewById(R.id.heat_power_edit_text);
        findViewById(R.id.ac_power_btn).setOnClickListener(this);
        findViewById(R.id.heat_power_btn).setOnClickListener(this);

        Intent intent = getIntent();
        UID = intent.getStringExtra("UID");

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ac_power_btn:
                Database.writeACPower(UID, Double.parseDouble(mACEditText.getText().toString()));
                mACEditText.setText("");
                Toast.makeText(getApplicationContext(), "AC power updated", Toast.LENGTH_SHORT).show();
                break;
            case R.id.heat_power_btn:
                Database.writeHeatPower(UID, Double.parseDouble(mHeatEditText.getText().toString()));
                mHeatEditText.setText("");
                Toast.makeText(getApplicationContext(), "Heating power updated", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
