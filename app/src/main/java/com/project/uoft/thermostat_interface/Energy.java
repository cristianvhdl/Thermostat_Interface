package com.project.uoft.thermostat_interface;

/**
 ToDo: add comments
 */

import java.util.Calendar;

public class Energy {

    private static final String TAG = Energy.class.getSimpleName();

    public static double HEATING_RATE = 0.1f; // degree per minute (C)
    public static double COOLING_RATE = 0.1f; // degree per minute (C)
    public static double HEATING_POWER = 3.5f;    // kW for heating
    public static double COOLING_POWER = 3.5f;    // kW for cooling
    public static double MAINTAIN_PERCENTAGE = 0.2; // percentage of time the HVAC is ON to maintain the target temperature

    // Time-of-Use Rates cents per kWh, weekends and holidays are off-peak all day
    public static final double ELEC_ON_PEAK = 18f;    // 11 am - 4 pm
    public static final double ELEC_MID_PEAK = 13.2f; // 7-10 am & 5-6 pm
    public static final double ELEC_OFF_PEAK = 8.7f;  // 7pm - 6am

    // how many minutes it takes to reach target temperature
    public static double timeToTemp(double currTempC, double tarTempC, boolean cooling){
        if(cooling){
            return Math.abs(tarTempC - currTempC)/COOLING_RATE;
        }else{
            return Math.abs(tarTempC - currTempC)/HEATING_RATE;
        }
    }

    // how much cents it takes to reach target temperature
    public static double tempToCents(double currTempC, double tarTempC, boolean cooling){
        double elec_price = currElecPrice();
        double power;
        power = cooling?COOLING_POWER:HEATING_POWER;    // power=cooling_power if mode=Cooling or power=heating_power
//        if(cooling){
//            power = COOLING_POWER;
//        }else{
//            power = HEATING_POWER;
//        }

//        Log.v(TAG, "Need "+ timeToTemp(currTempC, tarTempC, cooling)/60 + "hours to reach "+tarTempC +" C");
        return timeToTemp(currTempC, tarTempC, cooling)/60 * power * elec_price;
    }

    // calculate the cost to maintain target temp
    public static double centsToMaintain(boolean cooling){
        double elec_price = currElecPrice();
        double power = cooling?COOLING_POWER:HEATING_POWER; // power=cooling_power if mode=Cooling or power=heating_power

        return power * MAINTAIN_PERCENTAGE * elec_price;
    }


    // how much can a given saving amount change the target temperature
//    public static double centsToTemp(double cents, double ambientTempC, boolean cooling){
//        double elec_price = currElecPrice();
//        double power, rate, duration, tempDiff;
//
//        if(cooling){
//            power = COOLING_POWER;
//            rate = COOLING_RATE;
//        }else{
//            power = HEATING_POWER;
//            rate = HEATING_RATE * -1;
//        }
//
//        duration = cents/(elec_price*power)*60;
//        tempDiff = duration*rate;
//
//        return tempDiff;
//    }

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
