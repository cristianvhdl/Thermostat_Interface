package com.project.uoft.thermostat_interface;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

/**
 * The Database class encapsulates Firebase database related methods.
 */
public class Database {
    private static final String TAG = Database.class.getSimpleName();
    private static final String USERS = "users";
    public static final String KEY_AC_POWER = "AC_Power";
    public static final String KEY_HEAT_POWER = "Heat_Power";
    public static String userSelectedUI = "default";

    public static FirebaseDatabase instance;
    public static DatabaseReference mDatabase;

    /**
     * The constructor of the Database class.
     */
    Database(){
        instance = FirebaseDatabase.getInstance();
        mDatabase = instance.getReference();
        if(!MainActivity.UID.equals("0"))
            getUserSelectedUI(MainActivity.UID);
    }


    /**
     * It writes a user action to the Firebase database.
     *
     * @param UID   User ID.
     * @param timeStamp Time of the action.
     * @param newAction An Action object that contains information about the user action.
     */
    public static void writeNewAction(String UID, Date timeStamp, Action newAction){
        Log.d(TAG, "writeNewAction: "+"UID=" + UID);
        mDatabase.child(USERS).child(UID).child("History").child(String.format("%tb %<td, %<tT",timeStamp)).setValue(newAction);
    }

    /**
     * It writes a new user to the Firebase database.
     *
     * @param UID   User ID.
     * @param newUser   A User object that contains information about the user.
     */
    public static void writeNewUser(String UID, User newUser){
        mDatabase.child(USERS).child(UID).setValue(newUser);
    }

    /**
     * It writes the AC power to the Firebase database
     *
     * @param UID   User ID
     * @param acPower   AC power in kW
     */
    public static void writeACPower(String UID, Double acPower){
        mDatabase.child(USERS).child(UID).child(KEY_AC_POWER).setValue(acPower);
    }

    /**
     * It writes the Heating power to the Firebase database
     *
     * @param UID   User ID.
     * @param heatPower Heating power in kW.
     */
    public static void writeHeatPower(String UID, Double heatPower){
        mDatabase.child(USERS).child(UID).child(KEY_HEAT_POWER).setValue(heatPower);
    }

     /**
     * It writes the feedback message left by the user to the Firebase database.
     *
     * @param UID   User ID.
     * @param timeStamp The time when message is sent.
     * @param message   Feedback message.
     */
    public static void writeFeedback(String UID, Date timeStamp, String message){
        mDatabase.child(USERS).child(UID).child("Feedback").child(String.format("%tb %<td, %<tT",timeStamp)).setValue(message);
    }

    /**
     * It writes the user selected UI mode from the ui_picker_spinner to the Firebase database.
     *
     * @param UID   User ID.
     * @param userSelectedUI    user selected UI mode from the ui_picker_spinner (default, 0,1,2).
     *                          default means the user allows the UI being remotely modified by the firebase server.
     */
    public static void writeUserSelectedUI(String UID, String userSelectedUI){
        mDatabase.child(USERS).child(UID).child("user_selected_ui_mode").setValue(userSelectedUI);
    }

    /**
     * It retrieves the user_selected_ui_mode parameter from the Firebase database.
     *
     * @param UID User ID.
     */
    public static void getUserSelectedUI(String UID){
        Log.v(TAG,"getUserSelectedUI");
        ValueEventListener userSelectedUIListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                COOLING_POWER = Double.parseDouble(dataSnapshot.getValue().toString());
                userSelectedUI = dataSnapshot.getValue().toString();
                Log.v(TAG, "user_selected_ui_mode = "+ userSelectedUI);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onCancelled", databaseError.toException());
            }
        };
        Database.mDatabase.child("users").child(UID).child("user_selected_ui_mode").addValueEventListener(userSelectedUIListener);
    }


}



