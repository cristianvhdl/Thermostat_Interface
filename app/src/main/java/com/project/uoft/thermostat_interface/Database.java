package com.project.uoft.thermostat_interface;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
ToDO: add comments
 */
public class Database {
    private static final String TAG = Database.class.getSimpleName();
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

    public void writeNewAction(String time, Double t1, Double t2, Double ambientT){
        Log.d(TAG, "writeNewAction("+time+", "+t1+", "+t2+", "+ambientT);
        Action newAction = new Action(time, t1, t2, ambientT);
        mDatabase.child("actions").child("test").setValue(newAction);
    }

    public void writeNewUser(){

    }
}
