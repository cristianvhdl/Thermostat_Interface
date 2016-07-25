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
    public static double ELEC_ON_PEAK = 18f;    // 11 am - 4 pm
    public double ELEC_MID_PEAK = 13.2f; // 7-10 am & 5-6 pm
    public double ELEC_OFF_PEAK = 8.7f;  // 7pm - 6am

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
    // TODO: 6/12/2016, update this method so that it reflects real time hydro price
    public static double centsToTemp(double currTempC, double tarTempC, boolean cooling){
        // Time
        Calendar c = Calendar.getInstance();
        int seconds = c.get(Calendar.SECOND);

        if(cooling){
            Log.v("Energy", "Need "+ timeToTemp(currTempC, tarTempC, cooling)/60 + "hours to reach "+tarTempC +" C");
            return timeToTemp(currTempC, tarTempC, cooling)/60 * COOLING_POWER * ELEC_ON_PEAK;
        }else{
            Log.v("Energy", "Need "+ timeToTemp(currTempC, tarTempC, cooling)/60 + "hours to reach "+tarTempC +" C");
            return timeToTemp(currTempC, tarTempC, cooling)/60 * HEATING_POWER * ELEC_ON_PEAK;
        }
    }
}
