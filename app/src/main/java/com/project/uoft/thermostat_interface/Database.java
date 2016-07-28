package com.project.uoft.thermostat_interface;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**

 */
public class Database {
    static FirebaseDatabase databaseInstance;

    public static void initDatabase(){
        databaseInstance = FirebaseDatabase.getInstance();
        DatabaseReference myRef = databaseInstance.getReference("message");

        myRef.setValue("Hello, World!");
    }
}
