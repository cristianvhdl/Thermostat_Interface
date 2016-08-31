package com.project.uoft.thermostat_interface;

/**
 * The User class stores user related information and will be sent to Firebase database
 */
public class User {
    public String email;
    public String postal;

    /**
     * Constructor with one input.
     *
     * @param e The email of the user
     */
    public User( String e){
        email = e;
    }

    /**
     * Constructor with two inputs.
     *
     * @param e The email of the user.
     * @param p The postal code of the user.
     */
    public User( String e, String p){
        email = e;
        postal = p;
    }
}