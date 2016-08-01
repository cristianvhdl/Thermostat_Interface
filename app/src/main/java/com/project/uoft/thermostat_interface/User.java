package com.project.uoft.thermostat_interface;

/**
ToDo: add comments
 */
public class User {
    public String email;
    public String postal;

    public User( String e){
        email = e;
    }

    public User( String e, String p){
        email = e;
        postal = p;
    }
}