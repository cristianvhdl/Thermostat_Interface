package com.project.uoft.thermostat_interface;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.nestlabs.sdk.NestToken;
import com.nestlabs.sdk.Structure;
import com.nestlabs.sdk.Thermostat;

import java.util.concurrent.Executor;

/**
 * The Auth class encapsulates authentication related methods for both Nest and Firebase.
 */
public class Auth implements Executor {
    private static final String TOKEN_KEY = "token";
    private static final String EXPIRATION_KEY = "expiration";
    private static FirebaseAuth firebaseAuth;
    public static final String TAG = Auth.class.getSimpleName();
    public static FirebaseAuth.AuthStateListener mAuthListener;

    public static boolean isSignedIn = false;

    /**
     * It initialize Firebase authentication process.
     */
    public static void initializeFirebaseAuth(){
        firebaseAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    isSignedIn = true;
                    new Energy();  // The constructor for energy class requires user to be logged in
//                    new Database();
                    MainActivity.UID = user.getUid();
                    Log.d(TAG, "onAuthStateChanged: signed in with ID: " + user.getUid());
                }else{
                    isSignedIn = false;
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                }
            }
        };
    }

    /**
     * It adds the Firebase authentication listener.
     */
    public static void addAuthListener(){
        firebaseAuth.addAuthStateListener(mAuthListener);
    }

    /**
     * It removes the Firebase authentication listener.
     */
    public static void removeAuthListener(){
        if(mAuthListener != null){
            firebaseAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
     * It signs out the the user.
     */
    public static void signOut(){
        Log.d(TAG, "Signing Out");
        firebaseAuth.signOut();
    }

    /**
     * Sign the user in with his/her structure ID as email and thermostat device ID as password.
     * If the user is not exist on the Firebase server, sign the user up with the email/password
     * using the signUp method.
     *
     * @param mThermostat   Thermostat device ID is used as password.
     * @param mStructure    Structure ID is used as email address.
     */
    public static void signIn(Thermostat mThermostat, Structure mStructure) {
        if(mThermostat == null || mStructure == null) {
            Log.e(TAG, "SignIn: mThermostat or mStructure == null");
            return;
        }
        final String email = mStructure.getStructureId()+"@user.com";
        final String password = mThermostat.getDeviceId();
        Log.d(TAG,"Signing In");
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                    new Energy();
                    MainActivity.UID = firebaseAuth.getCurrentUser().getUid();

                    if (!task.isSuccessful()) {
                        Log.w(TAG, "signInWithEmail:failed", task.getException());
                        if(task.getException() instanceof FirebaseAuthInvalidUserException){    //if user doesn't exist, sign the user up
                            signUp(email, password);
                        }
                    }
                }
            });
    }

    /**
     * It signs the new user up with a email address and password for the Firebase server.
     *
     * @param email     The email
     * @param password  The password
     */
    private static void signUp(final String email, String password){
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                        if(task.isSuccessful()){
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            User newUser = new User(email);
                            String UID = user.getUid();
                            Database.writeNewUser(UID, newUser);
                            Database.writeACPower(UID, Energy.DEFAULT_COOLING_POWER);
                            Database.writeHeatPower(UID, Energy.DEFAULT_HEATING_POWER);
                        }
                    }
                });
    }

    /**
     * It saves the authentication token on the device.
     *
     * @param context   Activity context.
     * @param token     The Nest authentication token.
     */
    public static void saveAuthToken(Context context, NestToken token) {
        if (token == null) {
            getPrefs(context).edit().remove(TOKEN_KEY).remove(EXPIRATION_KEY).commit();
            return;
        }
        getPrefs(context).edit()
                .putString(TOKEN_KEY, token.getToken())
                .putLong(EXPIRATION_KEY, token.getExpiresIn())
                .apply();   // or .commit()
    }

    /**
     * It loads the authentication token from tha activity and checks if it is expired.
     * A new authentication token is created after the process.
     *
     * @param context   Activity context.
     * @return  Nest authentication token.
     */
    public static NestToken loadAuthToken(Context context) {
        final SharedPreferences prefs = getPrefs(context);
        final String token = prefs.getString(TOKEN_KEY, null);
        final long expirationDate = prefs.getLong(EXPIRATION_KEY, -1);

        if (token == null || expirationDate == -1) {
            return null;
        }

        return new NestToken(token, expirationDate);
    }

    /**
     * It extract authentication related information from the activity.
     *
     * @param context   Activity context.
     * @return  A SharedPreferences object.
     */
    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(NestToken.class.getSimpleName(), 0);
    }

    /**
     * In order to use addOnCompleteListener in signIn and signUp method,
     * the Auth class need to implement Executor interface which requires execute method.
     * This method has no implementation and no usage for the program.
     *
     * @param command   The command.
     */
    @Override
    public void execute(@NonNull Runnable command) {    }
}
