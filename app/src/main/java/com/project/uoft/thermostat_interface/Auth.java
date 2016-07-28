package com.project.uoft.thermostat_interface;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.nestlabs.sdk.Structure;
import com.nestlabs.sdk.Thermostat;

import java.util.concurrent.Executor;

/**
 * Created by Lenovo on 7/28/2016.
 */
public class Auth implements Executor {
    private static final String TAG = Auth.class.getSimpleName();
    private static FirebaseAuth firebaseAuth;
    private static FirebaseAuth.AuthStateListener mAuthListener;
    public boolean isSignedIn = false;

    public void initialize(){
        firebaseAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    isSignedIn = true;
                    Log.d(TAG, "onAuthStateChanged:signed in with ID: " + user.getUid());
                }else{
                    isSignedIn = false;
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                }

            }
        };
    }

    public void addAuthListener(){
        firebaseAuth.addAuthStateListener(mAuthListener);
    }

    public void removeAuthListener(){
        if(mAuthListener != null){
            firebaseAuth.removeAuthStateListener(mAuthListener);
        }
        firebaseAuth.signOut();
    }

    public void signIn(Thermostat mThermostat, Structure mStructure) {
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

                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            if(task.getException() instanceof FirebaseAuthInvalidUserException){    //if user doesn't exist, sign the user up
                                signUp(email, password);
                            }
                        }
                    }
                });
    }

    private void signUp(String email, String password){
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                    }
                });
    }

    @Override
    public void execute(@NonNull Runnable command) {
        //In order to use addOnCompleteListener in signIn and signUp method, the Auth need to implement Executor interface which requires execute method.
    }
}
