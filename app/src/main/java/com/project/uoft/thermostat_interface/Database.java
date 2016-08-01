package com.project.uoft.thermostat_interface;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

/**
ToDO: add comments
 */
public class Database {
    private static final String TAG = Database.class.getSimpleName();
    private static final String USERS = "users";
    public static FirebaseDatabase instance;
    public static DatabaseReference mDatabase;

    Database(){
        instance = FirebaseDatabase.getInstance();
        mDatabase = instance.getReference();
    }

    public void helloWorld(){
        DatabaseReference myRef = instance.getReference("message2");
        myRef.setValue("Hello, World!");
    }

    public static void writeNewAction(String UID, Date timeStamp, Action newAction){
        Log.d(TAG, "writeNewAction: "+"UID=" + UID);
        mDatabase.child(USERS).child(UID).child("History").child(String.format("%tb %<td, %<tT",timeStamp)).setValue(newAction);
    }

    public static void writeNewUser(String UID, User newUser){
        mDatabase.child(USERS).child(UID).setValue(newUser);
    }


}



