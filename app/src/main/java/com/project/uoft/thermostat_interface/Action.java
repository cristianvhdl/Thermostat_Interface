package com.project.uoft.thermostat_interface;

/**
 * Information about user action.
 * The newly created Action object will be sent to the Firebase realtime database.
 */
public class Action {
    Double initTemp;
    Double newTemp;
    Double ambientTemp;
    int saving;
    String HVACMode;
    String UIMode;

    /**
     * Constructor for the Action class.
     *
     * @param initTemp  Initial target temperature.
     * @param newTemp   New target temperature.
     * @param ambientTemp   Room temperature.
     * @param saving    Potential saving amount.
     * @param HVACMode  Current HVAC mode (heating or cooling).
     * @param UIMode    The UI design is used by the user.
     */
    Action(Double initTemp, Double newTemp, Double ambientTemp, int saving, String HVACMode, String UIMode){
        this.initTemp = initTemp;
        this.newTemp = newTemp;
        this.ambientTemp = ambientTemp;
        this.saving = saving;
        this.HVACMode = HVACMode;
        this.UIMode = UIMode;
    }
}
