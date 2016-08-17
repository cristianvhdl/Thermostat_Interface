package com.project.uoft.thermostat_interface;

import android.util.Log;
import android.widget.Toast;

//import com.nestapi.lib.API.Thermostat;

import java.util.Calendar;

public class Energy {
    public static double HEATING_RATE = 0.1f; // degree per minute (C)
    public static double COOLING_RATE = 0.1f; // degree per minute (C)
    public static double HEATING_POWER = 3.5f;    // kW for heating
    public static double COOLING_POWER = 3.5f;    // kW for cooling

    // Time-of-Use Rates cents per kWh, weekends and holidays are off-peak all day
    public static final double ELEC_ON_PEAK = 18f;    // 11 am - 4 pm
    public static final double ELEC_MID_PEAK = 13.2f; // 7-10 am & 5-6 pm
    public static final double ELEC_OFF_PEAK = 8.7f;  // 7pm - 6am

    // how many minutes it takes to reach target temperature
    public static double timeToTemp(double currTempC, double tarTempC, boolean cooling){
        if(cooling){
//            Log.v("Energy", "Need "+ Math.abs(tarTempC - currTempC)/COOLING_RATE + " minutes to reach "+tarTempC +" C");
            return Math.abs(tarTempC - currTempC)/COOLING_RATE;
        }else{
            return Math.abs(tarTempC - currTempC)/HEATING_RATE;
        }
    }

    // how much cents it takes to reach target temperature
    public static double tempToCents(double currTempC, double tarTempC, boolean cooling){
        double elec_price = currElecPrice();
        double power;
        if(cooling){
            power = COOLING_POWER;
        }else{
            power = HEATING_POWER;
        }

        Log.v("Energy", "Need "+ timeToTemp(currTempC, tarTempC, cooling)/60 + "hours to reach "+tarTempC +" C");
        return timeToTemp(currTempC, tarTempC, cooling)/60 * power * elec_price;
    }


    // how much can a given saving amount change the target temperature
    public static double centsToTemp(double cents, double ambientTempC, boolean cooling){
        double elec_price = currElecPrice();
        double power, rate, duration, tempDiff;

        if(cooling){
            power = COOLING_POWER;
            rate = COOLING_RATE;
        }else{
            power = HEATING_POWER;
            rate = HEATING_RATE * -1;
        }

        duration = cents/(elec_price*power)*60;
        tempDiff = duration*rate;

        return tempDiff;
    }

    public static String currElecStatus(){
        String status = "OFF PEAK";
        // Time
        Calendar c = Calendar.getInstance();
        int Hr24=c.get(Calendar.HOUR_OF_DAY);

        if(Hr24 >= 11 && Hr24 <= 16){
            status = "ON PEAK";
        }else if( (Hr24 >= 7 && Hr24 <= 10) || (Hr24 >= 5 && Hr24 <= 6)){
            status = "MID PEAK";
        }
        Log.v("Energy", "Current Electricity: "+status);

        return status;
    }

    public static double currElecPrice(){
        double price = ELEC_OFF_PEAK;   //default price

        String status = currElecStatus();
        if(status.equals("ON PEAK"))
            price = ELEC_ON_PEAK;
        else if(status.equals("MID PEAK"))
            price = ELEC_MID_PEAK;

        Log.v("Energy", "Current Price is "+price);
        return price;
    }
}
