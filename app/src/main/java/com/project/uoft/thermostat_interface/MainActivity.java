package com.project.uoft.thermostat_interface;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.nestlabs.sdk.Callback;
import com.nestlabs.sdk.GlobalUpdate;
import com.nestlabs.sdk.NestAPI;
import com.nestlabs.sdk.NestException;
import com.nestlabs.sdk.NestListener;
import com.nestlabs.sdk.NestToken;
import com.nestlabs.sdk.Structure;
import com.nestlabs.sdk.Thermostat;
import com.project.uoft.thermostat_interface.widget.VerticalSeekBar;

import java.util.Date;
import java.util.Locale;

/**
 * The main activity of the application.
 */
public class MainActivity extends Activity implements View.OnClickListener {
    // Constants
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String THERMOSTAT_KEY = "thermostat_key";
    private static final String STRUCTURE_KEY = "structure_key";
    private static final String KEY_AWAY = "away";
    private static final String KEY_AUTO_AWAY = "auto-away";
    private static final String KEY_HOME = "home";
    private static final String KEY_HEAT = "heat";
    private static final String KEY_COOL = "cool";
    private static final String KEY_OFF = "off";
    private static final String DEG_C = "%.1f°";
    private static final String CLIENT_ID = Constants.CLIENT_ID;
    private static final String CLIENT_SECRET = Constants.CLIENT_SECRET;
    private static final String REDIRECT_URL = Constants.REDIRECT_URL;
    private static final int AUTH_TOKEN_REQUEST_CODE = 123;

    // Variables
    private int saving;
    private double display_temp;
    private long ui_mode;
    private static Context context;

    // Nest related
    private NestAPI mNest;
    private NestToken mToken;
    private Thermostat mThermostat;
    private Structure mStructure;
    private Activity mActivity;

    // UI components
    // General UI components
    private Menu menu;
    private TextView mAmbientTempText;
    private TextView mTargetTempText;
    private TextView mElecStatusText;
    private TextView mWaitText;
    // UI 0
    private View mThermostatView;
    private View mCoinView;
    private TextView mSavingText;
    private TextView mSavingUp;
    private TextView mSavingDown;
    private TextView mTempUp;
    private TextView mTempDown;
    // UI 1
    private ImageView mCoinStackImg;
    private ImageView mMercuryImg;
    private ImageView mThermometerBottom1;
    private ImageView mThermometerBottom2;
    private VerticalSeekBar mTempSeekbar;

    //Database
    private Database mDB;

    //Remote Config
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private long cacheExpiration;

    /**
     * It initialize the main activity.
     *
     * @param savedInstanceState    It contains data used to restore the activity to a previous state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivity.context=getApplicationContext();

        // Initializations
        // General UI components
        mActivity = this;
        mAmbientTempText = (TextView) findViewById(R.id.ambient_temp);
        mWaitText = (TextView) findViewById(R.id.wait_text);
        mTargetTempText = (TextView) findViewById(R.id.target_temp);
        mElecStatusText = (TextView)findViewById(R.id.elec_status_text);
        // UI 0
        mThermostatView = findViewById(R.id.thermostat_view);
        mCoinView = findViewById(R.id.coin_view);
        mSavingText = (TextView) findViewById(R.id.saving_text);
        mSavingUp = (TextView)findViewById(R.id.saving_up);
        mSavingDown = (TextView)findViewById(R.id.saving_down);
        mTempUp = (TextView) findViewById(R.id.temp_up);
        mTempDown = (TextView) findViewById(R.id.temp_down);
        // UI 1
        mCoinStackImg = (ImageView) findViewById(R.id.coin_stack_img);
        mMercuryImg = (ImageView)findViewById(R.id.mercury_img);
        mThermometerBottom1 = (ImageView)findViewById(R.id.bottom1);
        mThermometerBottom2 = (ImageView)findViewById(R.id.bottom2);
        mTempSeekbar = (VerticalSeekBar)findViewById(R.id.tempSeekbar);
        setSeekbarListener();
        mTempSeekbar.getThumb().mutate().setAlpha(0);

        // Initialize Click Listeners
        findViewById(R.id.heat).setOnClickListener(this);
        findViewById(R.id.cool).setOnClickListener(this);
        findViewById(R.id.off).setOnClickListener(this);
        findViewById(R.id.confirm_btn).setOnClickListener(this);
        findViewById(R.id.temp_up).setOnClickListener(this);
        findViewById(R.id.temp_down).setOnClickListener(this);
        findViewById(R.id.thermostat_view).setOnClickListener(this);
        findViewById(R.id.coin_img).setOnClickListener(this);
        findViewById(R.id.saving_up).setOnClickListener(this);
        findViewById(R.id.saving_down).setOnClickListener(this);

        // Initialize Nest API
        NestAPI.setAndroidContext(this);
        mNest = NestAPI.getInstance();

        // Nest and Firebase Authentication
        mToken = Auth.loadAuthToken(this);
        Auth.initializeFirebaseAuth();
        if (mToken != null) {
            authenticate(mToken);
        } else {
            mNest.setConfig(CLIENT_ID, CLIENT_SECRET, REDIRECT_URL);
            mNest.launchAuthFlow(this, AUTH_TOKEN_REQUEST_CODE);
        }

        // If the activity need to be restored
        if (savedInstanceState != null) {
            Log.v(TAG, "savedInstanceState != null");
            mThermostat = savedInstanceState.getParcelable(THERMOSTAT_KEY);
            mStructure = savedInstanceState.getParcelable(STRUCTURE_KEY);
            updateViews();
        }

        // Initialize Database class
        mDB = new Database();

        // Initialize Remote Config
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        cacheExpiration = 3600; // 1 hour in seconds.
        // If in developer mode cacheExpiration is set to 0 so each fetch will retrieve values from the server.
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        updateUIMode();
    }

    /**
     * It initializes the menu button located on the toolbar.
     *
     * @param menu  Menu object waiting to be inflated by the main_menu.xml.
     * @return  super.onCreateOptionsMenu(menu) will execute any code that has to be
     *           executed for the options menu to work properly.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        this.menu = menu;

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * It determines what to do when a menu item is clicked.
     * The Refresh UI item updates the UI if the remote config parameter is changed through Firebase console.
     * The Settings item launches a new activity that allow the user to change some settings.
     * The I'm Home/Away item toggles the home/away status of the user.
     * The Logout button logs out the user.
     *
     * @param item  It contains the menu items in the Menu
     * @return  super.onOptionsItemSelected(item) will execute any code that has to be
     *           executed for the menu item to work properly.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.menu_refresh_ui:
                Log.v(TAG,"clicked Refresh UI");
                updateUIMode();
                updateViews();
                break;
            case R.id.menu_settings:
                Log.v(TAG,"clicked Settings");
                Intent intent = new Intent(this, SettingsActivity.class);
                String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                intent.putExtra("UID", UID);
                startActivity(intent);
                break;
            case R.id.menu_away:
                Log.v(TAG,"clicked home/away");
                String awayState = mStructure.getAway();
                // Sets awayState
                if (KEY_AUTO_AWAY.equals(awayState) || KEY_AWAY.equals(awayState)) {
                    awayState = KEY_HOME;
                } else if (KEY_HOME.equals(awayState)) {
                    awayState = KEY_AWAY;
                } else {
                    break;
                }

                // Change away status
                mNest.structures.setAway(mStructure.getStructureId(), awayState, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Successfully set away state.");
                    }

                    @Override
                    public void onFailure(NestException exception) {
                        Log.d(TAG, "Failed to set away state: " + exception.getMessage());
                    }
                });
                break;
            case R.id.menu_logout:
                Auth.signOut();
                Auth.saveAuthToken(this, null);
                mNest.launchAuthFlow(this, AUTH_TOKEN_REQUEST_CODE);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * It updates the text of one of the menu items: the home/away toggle button.
     */
    private void updateMenuItems(){
        String awayState = mStructure.getAway();
        MenuItem menuAway = menu.findItem(R.id.menu_away);
        if (KEY_AUTO_AWAY.equals(awayState) || KEY_AWAY.equals(awayState)) {
            menuAway.setTitle(R.string.away_state_home);
        } else if (KEY_HOME.equals(awayState)) {
            menuAway.setTitle(R.string.away_state_away);
        }
    }

//    public static Context getAppContext(){
//        return MainActivity.context;
//    }

    /**
     * It stores information to a bundle which is used to restore the activity to a previous state.
     * It saves the Thermostat and Structure object.
     *
     * @param outState The storage bundle.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(THERMOSTAT_KEY, mThermostat);
        outState.putParcelable(STRUCTURE_KEY, mStructure);
    }

    /**
     * Called when an activity you launched exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode   The integer request code originally supplied to startActivityForResult(),
     *                       allowing you to identify who this result came from.
     * @param resultCode    The integer result code returned by the child activity through its setResult().
     * @param intent    An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != RESULT_OK || requestCode != AUTH_TOKEN_REQUEST_CODE) {
            Log.e(TAG, "Finished with no result.");
            return;
        }

        mToken = NestAPI.getAccessTokenFromIntent(intent);
        if (mToken != null) {
            Auth.saveAuthToken(this, mToken);
            authenticate(mToken);
        } else {
            Log.e(TAG, "Unable to resolve access token from payload.");
        }
    }

    /**
     * Called after onCreate(Bundle) or after onRestart() when the activity had been stopped,
     * but is now again being displayed to the user. It will be followed by onResume().
     * It re-adds listeners for Nest and Firebase authentication.
     */
    @Override
    public void onStart(){
        super.onStart();
        Auth.addAuthListener();
        fetchData();
    }

    /**
     * Called when you are no longer visible to the user. You will next receive either onRestart(),
     * onDestroy(), or nothing, depending on later user activity.
     * It removes all the listeners.
     */
    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
        mNest.removeAllListeners();
        Auth.removeAuthListener();
    }

    /**
     * Click listener for buttons.
     *
     * @param v The view
     */
    @Override
    public void onClick(View v) {
        if (mThermostat == null || mStructure == null) {
            return;
        }

        String thermostatID = mThermostat.getDeviceId();
        String mode = mThermostat.getHvacMode();
        double init_temp = mThermostat.getTargetTemperatureC();
        int coinRadius, savingTextSize, thermRadius, tempTextSize;

        double targetC = display_temp;
        double ambientC = mThermostat.getAmbientTemperatureC();
        double tempDiffC = targetC - ambientC;
        boolean isHeating = KEY_HEAT.equals(mode) && tempDiffC > 0;
        boolean isCooling = KEY_COOL.equals(mode) && tempDiffC < 0;

        switch (v.getId()) {
            case R.id.coin_img: // switch size with the circular controller in UI 0
                Log.d(TAG, "Clicked Coin");
                coinRadius = (int) (getResources().getDimension(R.dimen.thermostat_radius));
                savingTextSize = (int) (getResources().getDimension(R.dimen.coin_text_size_big)/ getResources().getDisplayMetrics().density);
                thermRadius = (int) (getResources().getDimension(R.dimen.coin_radius));
                tempTextSize = (int) (getResources().getDimension(R.dimen.coin_text_size_normal)/ getResources().getDisplayMetrics().density);

                mCoinView.requestLayout();
                mCoinView.getLayoutParams().height = coinRadius;
                mCoinView.getLayoutParams().width = coinRadius;
                mSavingUp.setVisibility(View.VISIBLE);
                mSavingDown.setVisibility(View.VISIBLE);
                mSavingText.setTextSize(savingTextSize);

                mThermostatView.requestLayout();
                mThermostatView.getLayoutParams().height = thermRadius;
                mThermostatView.getLayoutParams().width = thermRadius;
                mTempUp.setVisibility(View.GONE);
                mTempDown.setVisibility(View.GONE);
                mTargetTempText.setTextSize(tempTextSize);

                break;
            case R.id.thermostat_view:  // switch size with the big coin in UI 0
                Log.d(TAG, "Clicked Thermostat");
                coinRadius = (int) (getResources().getDimension(R.dimen.coin_radius));
                savingTextSize = (int) (getResources().getDimension(R.dimen.coin_text_size_normal)/ getResources().getDisplayMetrics().density);
                thermRadius = (int) (getResources().getDimension(R.dimen.thermostat_radius));
                tempTextSize = (int) (getResources().getDimension(R.dimen.control_temp_size)/ getResources().getDisplayMetrics().density);

                mCoinView.requestLayout();
                mCoinView.getLayoutParams().height = coinRadius;
                mCoinView.getLayoutParams().width = coinRadius;
                mSavingUp.setVisibility(View.GONE);
                mSavingDown.setVisibility(View.GONE);
                mSavingText.setTextSize(savingTextSize);

                mThermostatView.requestLayout();
                mThermostatView.getLayoutParams().height = thermRadius;
                mThermostatView.getLayoutParams().width = thermRadius;
                mTempUp.setVisibility(View.VISIBLE);
                mTempDown.setVisibility(View.VISIBLE);
                mTargetTempText.setTextSize(tempTextSize);
                break;
            case R.id.confirm_btn:  // confirms new temperature settings, sends user action to Firebase database
                Log.d(TAG, "Clicked Confirm");
                if (display_temp != init_temp){ //if target temp is different from initial temperature
                    mNest.thermostats.setTargetTemperatureC(mThermostat.getDeviceId(), display_temp);
                    Date timeStamp = new Date();
                    Action newAction = new Action(mThermostat.getTargetTemperatureC(),
                            display_temp,
                            mThermostat.getAmbientTemperatureC(),
                            saving,
                            mode,
                            ""+ui_mode);
                    String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Database.writeNewAction(UID, timeStamp, newAction);
                }
                break;
            case R.id.saving_up:    // when the + button on the big coin in UI 0 is clicked
                if(display_temp < 32 && KEY_COOL.equals(mode) && isCooling)
                    display_temp+=0.5;
                else if(display_temp > 9 && KEY_HEAT.equals(mode) && isHeating)
                    display_temp-=0.5;
                updateControlView();
                break;
            case R.id.temp_up:  // when the + button on the circular controller in UI 0 is clicked
                if(display_temp < 32)
                    display_temp+=0.5;
                updateControlView();
                break;
            case R.id.saving_down:  // when the - button on the big coin in UI 0 is clicked
                if(display_temp > 9 && KEY_COOL.equals(mode))
                    display_temp-=0.5;
                else if(display_temp < 32 && KEY_HEAT.equals(mode))
                    display_temp+=0.5;
                updateControlView();
                break;
            case R.id.temp_down:    // when the - button on the circular controller in UI 0 is clicked
                if(display_temp > 9)
                    display_temp-=0.5;
                updateControlView();
                break;
            case R.id.heat: // when the heat button is clicked, switch HVAC mode to heat
                mNest.thermostats.setHVACMode(thermostatID, KEY_HEAT);
                break;
            case R.id.cool: // when the cool button is clicked, switch HVAC mode to cool
                mNest.thermostats.setHVACMode(thermostatID, KEY_COOL);
                break;
            case R.id.off:  // when the off button  is clicked, switch HVAC mode to off
                mNest.thermostats.setHVACMode(thermostatID, KEY_OFF);
                break;
        }

    }

    /**
     * Authenticate with the Nest API and start listening for updates.
     *
     * @param token the token used to authenticate.
     */
    private void authenticate(NestToken token) {
        mNest.authWithToken(token, new NestListener.AuthListener() {

            @Override
            public void onAuthSuccess() {
                Log.v(TAG, "Authentication succeeded.");
                fetchData();
            }

            @Override
            public void onAuthFailure(NestException exception) {
                Log.e(TAG, "Authentication failed with error: " + exception.getMessage());
                Auth.saveAuthToken(mActivity, null);
                mNest.launchAuthFlow(mActivity, AUTH_TOKEN_REQUEST_CODE);
            }

            @Override
            public void onAuthRevoked() {
                Log.e(TAG, "Auth token was revoked!");
                Auth.saveAuthToken(mActivity, null);
                mNest.launchAuthFlow(mActivity, AUTH_TOKEN_REQUEST_CODE);
            }
        });
    }

    /**
     * Setup global listener, start listening, and update view when update received.
     *
     */
    private void fetchData() {
        mNest.addGlobalListener(new NestListener.GlobalListener() {
            @Override
            public void onUpdate(@NonNull GlobalUpdate update) {
                mThermostat = update.getThermostats().get(0);
                mStructure = update.getStructures().get(0);
                if(!Auth.isSignedIn){
                    Log.d(TAG, "GlobalListener: onUpdate: User is not signed in, signing in");
                    Auth.signIn(mThermostat,mStructure);
                }
                updateViews();
            }
        });
    }

    /**
     * It updates all views, also update the progress of the hidden seekbar in UI 1.
     *
     */
    private void updateViews() {
        if (mStructure == null || mThermostat == null) {
            return;
        }

        display_temp = mThermostat.getTargetTemperatureC();
        Log.v(TAG,"updateViews: display_temp="+display_temp);
        // updates all views
        updateAmbientTempTextView();
        updateMenuItems();
        updateThermostatViews();
        updateControlView();

        //update the seekbar progress
        double temp = display_temp;
        mTempSeekbar.setProgress(0);
        display_temp = temp;
        mTempSeekbar.setProgress((int)((display_temp-9)/(32-9)*100));
    }

    /**
     * It update the room temperature.
     */
    private void updateAmbientTempTextView() {
        mAmbientTempText.setText(String.format(Locale.CANADA, DEG_C, mThermostat.getAmbientTemperatureC()));
    }

    /**
     * It updates the look of HVAC mode buttons and electricity status.
     */
    private void updateThermostatViews() {
        String mode = mThermostat.getHvacMode();
//        String state = mStructure.getAway();
//        boolean isAway = state.equals(KEY_AWAY) || state.equals(KEY_AUTO_AWAY);
        Drawable default_btn_bg = findViewById(R.id.confirm_btn).getBackground();

        if(KEY_HEAT.equals(mode)){
            findViewById(R.id.heat).setBackground(ContextCompat.getDrawable(this,R.drawable.highlight_button));
            findViewById(R.id.cool).setBackground(default_btn_bg);
            findViewById(R.id.off).setBackground(default_btn_bg);
        }else if(KEY_COOL.equals(mode)){
            findViewById(R.id.cool).setBackground(ContextCompat.getDrawable(this,R.drawable.highlight_button));
            findViewById(R.id.heat).setBackground(default_btn_bg);
            findViewById(R.id.off).setBackground(default_btn_bg);
        }else if(KEY_OFF.equals(mode)){
            findViewById(R.id.off).setBackground(ContextCompat.getDrawable(this,R.drawable.highlight_button));
            findViewById(R.id.cool).setBackground(default_btn_bg);
            findViewById(R.id.heat).setBackground(default_btn_bg);
        }

        if (KEY_OFF.equals(mode)) {
            findViewById(R.id.temp_up).setVisibility(View.GONE);
            findViewById(R.id.temp_down).setVisibility(View.GONE);
            mTargetTempText.setText(R.string.thermostat_off);
            mThermostatView.setBackgroundResource(R.drawable.off_thermostat_drawable);
        }

        mElecStatusText.setText("Electricity Status: "+ Energy.currElecStatus());
    }

    /**
     * It updates the temperature controller.
     */
    private void updateControlView() {
        // initialize varables with default value
        int thermDrawable = R.drawable.off_thermostat_drawable;
        int mercuryDrawable = R.drawable.clip_mercury_off;
        int upColor = Color.WHITE;
        int downColor = Color.WHITE;
        int thermometerColor = Color.BLACK;
        double targetC = display_temp;
        double ambientC = mThermostat.getAmbientTemperatureC();
        double tempDiffC = targetC - ambientC;
        String state = mStructure.getAway();
        String mode = mThermostat.getHvacMode();
        boolean isAway = state.equals(KEY_AWAY) || state.equals(KEY_AUTO_AWAY);
        boolean isHeating = KEY_HEAT.equals(mode) && tempDiffC > 0;
        boolean isCooling = KEY_COOL.equals(mode) && tempDiffC < 0;
        GradientDrawable shapeBottom1 = (GradientDrawable) mThermometerBottom1.getDrawable();
        GradientDrawable shapeBottom2 = (GradientDrawable) mThermometerBottom2.getDrawable();

        Log.v(TAG, "updateControlView");

        // change varables based on HVAC status
        if (isHeating) {    // if the HVAC is heating
            thermDrawable = R.drawable.heat_thermostat_drawable;
            mercuryDrawable = R.drawable.clip_mercury_heat;
            upColor = Color.RED;
            downColor = Color.GREEN;
            thermometerColor = ContextCompat.getColor(context, R.color.heat);
        } else if (isCooling) { // if the HVAC is cooling
            thermDrawable = R.drawable.cool_thermostat_drawable;
            mercuryDrawable = R.drawable.clip_mercury_cool;
            upColor = Color.GREEN;
            downColor = Color.RED;
            thermometerColor =ContextCompat.getColor(context,R.color.cool);
        }

        // Update the view.
        if (isAway) {
            mTargetTempText.setText(R.string.thermostat_away);
        }else {
            mTargetTempText.setText(String.format(Locale.CANADA, DEG_C, targetC));
        }
        mThermostatView.setBackgroundResource(thermDrawable);
        mTempUp.setTextColor(upColor);
        mTempDown.setTextColor(downColor);

        shapeBottom1.setColor(thermometerColor);
        shapeBottom2.setColor(thermometerColor);
        mMercuryImg.setImageResource(mercuryDrawable);

        updateSaving(!isCooling && !isHeating);
    }

    /**
     * It updates the saving amount and corresponding UI components.
     *
     * @param isOff Whether the HVAC is off.
     */
    private void updateSaving(boolean isOff) {
        String mode = mThermostat.getHvacMode();
        double timeToTemp;
        int maintainCost;
        double ambient_temp = mThermostat.getAmbientTemperatureC();
        double init_target_temp = mThermostat.getTargetTemperatureC();
        double temp_diff = init_target_temp - display_temp;    // cooling -> negative: saving, heating -> positive: saving
        double ambientC = mThermostat.getAmbientTemperatureC();
        if(temp_diff == 0) {
            mSavingText.setText("¢ " + 0);
            // changing the height of the coin stack based on saving amount (UI1)
            ClipDrawable mCoinStackClip = (ClipDrawable) mCoinStackImg.getDrawable();
            mCoinStackClip.setLevel(0);
        }

        if(KEY_COOL.equals(mode) && init_target_temp > ambient_temp)    // if mode is cooling and initial temp is hotter than room temp
            init_target_temp = ambient_temp;
        else if(KEY_HEAT.equals(mode) && init_target_temp < ambient_temp)   // if mode is heating and initial temp is cooler than room temp
            init_target_temp = ambient_temp;

        if(!isOff || display_temp == ambient_temp) {  // while the HVAC is ON, the saving will be updated
            Log.v(TAG, "Update Saving");
            saving = (int)Energy.tempToCents(ambientC, init_target_temp, KEY_COOL.equals(mode))
                    - (int)Energy.tempToCents(ambientC, display_temp, KEY_COOL.equals(mode));    // calculate potential saving amount
            double maxSaving = Energy.tempToCents(ambientC, init_target_temp, KEY_COOL.equals(mode));  // calculate the maximum saving possible for current inital target temperature
            timeToTemp = Energy.timeToTemp(ambientC, display_temp, KEY_COOL.equals(mode))/60; // calculate time-to-temp
            maintainCost = (int)Energy.centsToMaintain(KEY_COOL.equals(mode)); // calculate the cost to maintain target temp

            // changing the height of the coin stack based on saving amount
            ClipDrawable mCoinStackClip = (ClipDrawable) mCoinStackImg.getDrawable();
            mCoinStackClip.setLevel((int)(saving/maxSaving*10000));

            // update and display the potential saving amount
            mSavingText.setText("¢ "+saving);
            if(saving < 0 ){
                mSavingText.setTextColor(Color.RED);
            }else{
                mSavingText.setTextColor(Color.BLACK);
            }

            // update and display the wait time and cost to maintain target temperature
            if(display_temp == ambient_temp){
                mWaitText.setText("");
            }else{
                mWaitText.setText("Wait: "+String.format("%.1f",timeToTemp)+" hrs + ¢"+maintainCost+"/hr");
            }
        }else{
            // update and display the wait time and cost to maintain target temperature
            mWaitText.setText("");
        }
    }

    /**
     * The listener for the hidden vertical seekbar used in the UI with thermometer style temp controller (UI1).
     */
    private void setSeekbarListener(){
        mTempSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                if(progress<=100 && progress>=0){
                    // change display temp based on seekbar progress and update the view
                    display_temp = Tools.roundToHalf((32-9)*progress/100.0+9);
                    Log.v(TAG, "onProgressChanged: temp:"+display_temp+"seekbar progress="+progress);
                    updateControlView();
                    // update the mercury of the thermometer
                    ClipDrawable mMercuryClip = (ClipDrawable) mMercuryImg.getDrawable();
                    mMercuryClip.setLevel(progress*100);
                }
            }
        });
    }

    /**
     * It changes the UI based on selected mode.
     *
     * @param mode The new UI mode that will replace the old one.
     */
    private void changeUIMode(int mode){
        findViewById(R.id.ui_circle_thermostat).setVisibility(View.GONE);
        findViewById(R.id.ui_coinstack_thermometer).setVisibility(View.GONE);
        findViewById(R.id.saving_text1).setVisibility(View.GONE);

        if(mode == 0){
            mSavingText = (TextView) findViewById(R.id.saving_text);
            mTargetTempText = (TextView) findViewById(R.id.target_temp);

            findViewById(R.id.ui_circle_thermostat).setVisibility(View.VISIBLE);
            findViewById(R.id.coin_view).setVisibility(View.VISIBLE);
        }else if(mode == 1){
            mSavingText = (TextView) findViewById(R.id.saving_text1);
            mTargetTempText = (TextView) findViewById(R.id.target_thermometer_temp);

            findViewById(R.id.ui_coinstack_thermometer).setVisibility(View.VISIBLE);
            findViewById(R.id.thermometer).setVisibility(View.VISIBLE);
            mSavingText.setVisibility(View.VISIBLE);
        }else if(mode == 2){
            mTargetTempText = (TextView) findViewById(R.id.target_temp);
            mSavingText = (TextView) findViewById(R.id.saving_text1);

            findViewById(R.id.ui_circle_thermostat).setVisibility(View.VISIBLE);
            findViewById(R.id.ui_coinstack_thermometer).setVisibility(View.VISIBLE);
            mCoinStackImg.setVisibility(View.VISIBLE);
            mSavingText.setVisibility(View.VISIBLE);

            findViewById(R.id.thermometer).setVisibility(View.GONE);
            findViewById(R.id.coin_view).setVisibility(View.GONE);
        }
        updateViews();
    }

    /**
     * It checks the ui_mode parameter on the Firebase remote config console then change the application UI accordingly
     */
    private void updateUIMode(){
        // Fetches ui_mode (remote config parameter) from Firebase console
        mFirebaseRemoteConfig.fetch(cacheExpiration).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.v(TAG, "Fetch Succeeded");
                    // Once the config is successfully fetched it must be activated before newly fetched
                    // values are returned.
                    mFirebaseRemoteConfig.activateFetched();

                } else {
                    Log.e(TAG,"remote_config fetch failed");
                }
                // change the UI mode
                ui_mode = mFirebaseRemoteConfig.getLong("ui_mode");
                changeUIMode((int)ui_mode);
            }
        });
    }
}