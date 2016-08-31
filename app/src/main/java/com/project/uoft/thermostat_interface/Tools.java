package com.project.uoft.thermostat_interface;

/**
 * The tools class encapsulates utility methods.
 */
public class Tools {

    /**
     * It rounds a number to either .5 or .0.
     * For example 1.3 will be rounded to 1.5, 2.1 will be rounded to 2.0.
     *
     * @param number The number to be rounded.
     * @return  Rounded number.
     */
    public static double roundToHalf(double number) {
        return Math.round(number * 2) / 2.0;
    }

}
