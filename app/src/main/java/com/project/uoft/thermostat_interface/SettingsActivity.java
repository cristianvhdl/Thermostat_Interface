package com.project.uoft.thermostat_interface;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * The activity allows the user to change the AC hand heating power.
 */
public class SettingsActivity extends Activity implements View.OnClickListener{

    private EditText mACEditText, mHeatEditText;
    private String UID;

    /**
     * onCreate for the SettingsActivity.
     *
     * @param savedInstanceState    For restoring activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Intialization
        mACEditText = (EditText) findViewById(R.id.ac_power_edit_text);
        mHeatEditText = (EditText) findViewById(R.id.heat_power_edit_text);

        // display the current power value
        mACEditText.setHint(String.valueOf(Energy.COOLING_POWER));
        mHeatEditText.setHint(String.valueOf(Energy.HEATING_POWER));

        // Add listeners for button
        findViewById(R.id.ac_power_btn).setOnClickListener(this);
        findViewById(R.id.heat_power_btn).setOnClickListener(this);

        // Retrieve user id from MainActivity
        Intent intent = getIntent();
        UID = intent.getStringExtra("UID");

    }

    /**
     * Defines what to do when buttons are clicked.
     * The ac_power_btn sends the new power value to the Firebase server for the current user.
     * The heat_power_btn sends the new power value to the Firebase server for the current user.
     *
     * @param v The view of the activity
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ac_power_btn:
                double ac_power = Energy.COOLING_POWER; // default value
                boolean acSuccess = false;
                String acPowerString = mACEditText.getText().toString();// get value from editText

                // check if input is valid
                if(acPowerString.length()>0){
                    try{
                        ac_power = Double.parseDouble(acPowerString);
                        acSuccess = true;
                    }catch (Exception e1){
                        e1.printStackTrace();
                    }
                }

                if(acSuccess) {    // check if the value is valid
                    Database.writeACPower(UID, ac_power);
                    mACEditText.setHint(""+ac_power);
                    mACEditText.setText("");
                    Toast.makeText(getApplicationContext(), "AC power updated", Toast.LENGTH_SHORT).show();
                }else{
                    mACEditText.setText("");
                    Toast.makeText(getApplicationContext(), "Invalid power value", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.heat_power_btn:
                double heat_power = Energy.HEATING_POWER;   // default value
                boolean heatSuccess = false;
                String heatPowerString = mHeatEditText.getText().toString();// get value from editText

                // check if input is valid
                if(heatPowerString.length()>0){
                    try{
                        heat_power = Double.parseDouble(heatPowerString);
                        heatSuccess = true;
                    }catch (Exception e1){
                        e1.printStackTrace();
                    }
                }

                if(heatSuccess) {  // check if the value is valid
                    Database.writeHeatPower(UID, heat_power);
                    mHeatEditText.setHint(""+heat_power);
                    mHeatEditText.setText("");
                    Toast.makeText(getApplicationContext(), "Heating power updated", Toast.LENGTH_SHORT).show();
                }else{
                    mHeatEditText.setText("");
                    Toast.makeText(getApplicationContext(), "Invalid power value", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
