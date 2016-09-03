package com.project.uoft.thermostat_interface;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Date;

/**
 * The activity allows the user to change the AC hand heating power.
 */
public class SettingsActivity extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = SettingsActivity.class.getSimpleName();
    private EditText mACEditText, mHeatEditText, mFeedbackTeditText;
    private Spinner mUISpinner;
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
        mFeedbackTeditText = (EditText) findViewById(R.id.feedback_message_edit_text);
        mUISpinner = (Spinner)findViewById(R.id.ui_picker_spinner);

        // display the current power value
        mACEditText.setHint(String.valueOf(Energy.COOLING_POWER));
        mHeatEditText.setHint(String.valueOf(Energy.HEATING_POWER));

        // Add listeners for button
        findViewById(R.id.ac_power_btn).setOnClickListener(this);
        findViewById(R.id.heat_power_btn).setOnClickListener(this);
        findViewById(R.id.feedback_send_btn).setOnClickListener(this);
        mUISpinner.setOnItemSelectedListener(this);

        // Retrieve user id from MainActivity
        Intent intent = getIntent();
//        UID = intent.getStringExtra("UID");
        UID = MainActivity.UID;

        // Populate UI picker spinner
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.ui_option_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mUISpinner.setAdapter(adapter);
        mUISpinner.setSelection(userSelectionToSpinnerPos(MainActivity.userSelectedUI));
    }

    @Override
    public void onResume(){
        super.onResume();
//        Auth.signIn(MainActivity.mThermostat, MainActivity.mStructure);
    }

    /**
     * Defines what to do when buttons are clicked.
     * The ac_power_btn sends the new power value to the Firebase server for the current user.
     * The heat_power_btn sends the new power value to the Firebase server for the current user.
     * The feedback_send_btn sends a feedback to the Firebase server for the current user.
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
            case R.id.feedback_send_btn:
                String feedbackMessage = mFeedbackTeditText.getText().toString();
                if(feedbackMessage.length()>0){
                    Date timeStamp = new Date();
                    Database.writeFeedback(UID, timeStamp, feedbackMessage);
                    Toast.makeText(getApplicationContext(), "Feedback sent", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * Click listener for the ui picker dropdown menu
     *
     * @param parent    The AdapterView where the selection happened.
     * @param view  The view within the AdapterView that was clicked.
     * @param position  The position of the view in the adapter.
     * @param id    The row id of the item that is selected.
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.v(TAG,"position="+position+" id="+id);
        String userSelectedUI = "default";
        switch(position){
            case 0: // default: The user allows the UI being remotely modified by the Firebase server
                userSelectedUI = "default";
                break;
            case 1: // UI 0: Coin + Circular Controller
                userSelectedUI = "0";
                break;
            case 2: // UI 1: Coin Stack + Thermometer Controller
                userSelectedUI = "1";
                break;
            case 3: // UI 2: Coin Stack + Circular Controller
                userSelectedUI = "2";
                break;
        }
        Database.writeUserSelectedUI(UID,userSelectedUI);
        Database.getUserSelectedUI(UID);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**
     * Converts user_selected_ui_mode value to spinner position
     *
     * @param userSelection user_selected_ui_mode value
     * @return  spinner position
     */
    private int userSelectionToSpinnerPos(String userSelection){
        int pos = 0;
        if(userSelection.equals("default")){
            pos = 0;
        }else {
            pos = Integer.parseInt(userSelection) + 1;
        }
        return pos;
    }
}
