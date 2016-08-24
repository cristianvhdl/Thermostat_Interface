package com.project.uoft.thermostat_interface;

/**
 * Created by Lenovo on 7/29/2016.
 */
public class Action {
    Double oldTemp;
    Double newTemp;
    Double ambientTemp;
    int saving;

    Action(Double t1, Double t2, Double aT, int s){
        oldTemp = t1;
        newTemp = t2;
        ambientTemp = aT;
        saving = s;
    }
}
