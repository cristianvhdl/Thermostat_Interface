package com.project.uoft.thermostat_interface;

/**
 * Created by Lenovo on 7/29/2016.
 */
public class Action {
//    String timeStamp;
    Double oldTemp;
    Double newTemp;
    Double ambientTemp;

    Action(/*String time,*/ Double t1, Double t2, Double aT){
//        timeStamp = time;
        oldTemp = t1;
        newTemp = t2;
        ambientTemp = aT;
    }
}
