package com.project.uoft.thermostat_interface;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

/**
 * The Energy class encapsulates energy usage related methods.
 */
public class Energy {

    private static final String TAG = Energy.class.getSimpleName();
    // Time-of-Use Rates cents per kWh, weekends and holidays are off-peak all day
    public static final double ELEC_ON_PEAK = 18f;    // 11 am - 4 pm
    public static final double ELEC_MID_PEAK = 13.2f; // 7-10 am & 5-6 pm
    public static final double ELEC_OFF_PEAK = 8.7f;  // 7pm - 6am
    public static final double DEFAULT_HEATING_POWER = 3.5f;
    public static final double DEFAULT_COOLING_POWER = 3.5f;

    public static double HEATING_RATE = 0.1f; // degree per minute (C)
    public static double COOLING_RATE = 0.1f; // degree per minute (C)
    public static double HEATING_POWER = DEFAULT_HEATING_POWER;    // kW for heating
    public static double COOLING_POWER = DEFAULT_COOLING_POWER;    // kW for cooling
    public static double MAINTAIN_PERCENTAGE = 0.2; // percentage of time the HVAC is ON to maintain the target temperature


    /**
     * Constructor for the Energy class
     * It retrieves the cooling power and heating power from the Firebase database
     */
    Energy(){
        ValueEventListener postListenerAC = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                COOLING_POWER = Double.parseDouble(dataSnapshot.getValue().toString());
                Log.v(TAG, "COOLING_POWER = "+ COOLING_POWER);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onCancelled", databaseError.toException());
            }
        };
        ValueEventListener postListenerHeat = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HEATING_POWER = Double.parseDouble(dataSnapshot.getValue().toString());
                Log.v(TAG, "HEATING_POWER = "+ HEATING_POWER);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onCancelled", databaseError.toException());
            }
        };
        String UID;
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Database.mDatabase.child("users").child(UID).child(Database.KEY_AC_POWER).addValueEventListener(postListenerAC);
            Database.mDatabase.child("users").child(UID).child(Database.KEY_HEAT_POWER).addValueEventListener(postListenerHeat);
            Log.v(TAG,"Retrieve Power Value Successful");
        }
    }

    /**
     * It calculates how many minutes it takes to reach target temperature from current room temperature.
     *
     * @param currTempC Current room temperature.
     * @param tarTempC  Target termperature.
     * @param cooling   If the HVAC system is cooling.
     * @return  How many minutes it takes to reach target temperature from current room temperature.
     */
    public static double timeToTemp(double currTempC, double tarTempC, boolean cooling){
        if(cooling){
            return Math.abs(tarTempC - currTempC)/COOLING_RATE;
        }else{
            return Math.abs(tarTempC - currTempC)/HEATING_RATE;
        }
    }

    /**
     * It calculates how much cents it costs to reach target temperature.
     *
     * @param currTempC Current room temperature.
     * @param tarTempC  Target termperature.
     * @param cooling   Whether the HVAC system is cooling.
     * @return How much cents it costs to reach target temperature.
     */
    public static double tempToCents(double currTempC, double tarTempC, boolean cooling){
        double elec_price = currElecPrice();
        double power;
        power = cooling?COOLING_POWER:HEATING_POWER;    // power=cooling_power if mode=Cooling or power=heating_power

//        Log.v(TAG, "Need "+ timeToTemp(currTempC, tarTempC, cooling)/60 + "hours to reach "+tarTempC +" C");
        return timeToTemp(currTempC, tarTempC, cooling)/60 * power * elec_price;
    }

    /**
     * It calculate the cost per hour to maintain target temperature.
     * It needs to be reworked because the calculation is very inaccurate.
     *
     * @param cooling   Whether the HVAC system is cooling.
     * @return  Cost per hour to maintain target temperature.
     */
    public static double centsToMaintain(boolean cooling){
        double elec_price = currElecPrice();
        double power = cooling?COOLING_POWER:HEATING_POWER; // power=cooling_power if mode=Cooling or power=heating_power

        return power * MAINTAIN_PERCENTAGE * elec_price;
    }

    /**
     * It tells you the electricity status based on time of the day.
     * Modification is required for this method, because it can not identify holidays.
     *
     * @return Electricity status: OFF PEAK, MID PEAK or ON PEAK.
     */
    public static String currElecStatus(){
        String status = "OFF PEAK";

        // Time
        Calendar c = Calendar.getInstance();
        int hr24=c.get(Calendar.HOUR_OF_DAY);
        int day = c.get(Calendar.DAY_OF_WEEK);

        if(day == Calendar.SATURDAY || day == Calendar.SUNDAY){
            status = "OFF PEAK";
        }else if(hr24 >= 11 && hr24 <= 16){
            status = "ON PEAK";
        }else if( (hr24 >= 7 && hr24 <= 10) || (hr24 >= 5 && hr24 <= 6)){
            status = "MID PEAK";
        }
//        Log.v(TAG, "Current Electricity: "+status);

        return status;
    }

    /**
     * It tells you the electricity price based on time of the day.
     *
     * @return  Current electricity price.
     */
    public static double currElecPrice(){
        double price = ELEC_OFF_PEAK;   //default price

        String status = currElecStatus();
        if(status.equals("ON PEAK"))
            price = ELEC_ON_PEAK;
        else if(status.equals("MID PEAK"))
            price = ELEC_MID_PEAK;

//        Log.v(TAG, "Current Price is "+price);
        return price;
    }
}
