package com.project.uoft.thermostat_interface;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

/**
 * The Database class encapsulates Firebase database related methods.
 */
public class Database {
    private static final String TAG = Database.class.getSimpleName();
    private static final String USERS = "users";
    public static FirebaseDatabase instance;
    public static DatabaseReference mDatabase;

    /**
     * The constructor of the Database class.
     */
    Database(){
        instance = FirebaseDatabase.getInstance();
        mDatabase = instance.getReference();
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


}



