package com.project.uoft.thermostat_interface;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
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

public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String THERMOSTAT_KEY = "thermostat_key";
    private static final String STRUCTURE_KEY = "structure_key";
    private static final String KEY_AWAY = "away";
    private static final String KEY_AUTO_AWAY = "auto-away";
    private static final String KEY_HOME = "home";
    private static final String KEY_HEAT = "heat";
    private static final String KEY_COOL = "cool";
    private static final String KEY_HEAT_COOL = "heat-cool";
    private static final String KEY_OFF = "off";
    private static final String DEG_F = "%d°";
    private static final String DEG_C = "%.1f°";
    private static final String CLIENT_ID = Constants.CLIENT_ID;
    private static final String CLIENT_SECRET = Constants.CLIENT_SECRET;
    private static final String REDIRECT_URL = Constants.REDIRECT_URL;
    private static final int AUTH_TOKEN_REQUEST_CODE = 123;

    private static Context context;
    private TextView mAmbientTempText;
    private View mSingleControlContainer;
    private TextView mCurrentTempText;
    private TextView mStructureNameText;
    private View mThermostatView;
    private View mRangeControlContainer;
    private TextView mCurrentCoolTempText;
    private TextView mCurrentHeatTempText;
    private Button mStructureAway;
    private NestAPI mNest;
    private NestToken mToken;
    private Thermostat mThermostat;
    private Structure mStructure;
    private Activity mActivity;

    private View mCoinView;
    private TextView mSavingText;
    private TextView mSavingUp;
    private TextView mSavingDown;
    private TextView mTempUp;
    private TextView mTempDown;
    private double display_temp;
//    private Drawable default_btn_bg;
    private TextView mElecStatusText;
    private ImageView mCoinStackImg;
    private ImageView mMercuryImg;
    private ImageView mThermometerBottom1;
    private ImageView mThermometerBottom2;
    private VerticalSeekBar mTempSeekbar;

    // Auth
//    private String UID;

    //Database
    private Database mDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        setContentView(R.layout.ui_coin_stack);


        // Initialization
        mActivity = this;
        mThermostatView = findViewById(R.id.thermostat_view);
        mSingleControlContainer = findViewById(R.id.single_control);
        mCurrentTempText = (TextView) findViewById(R.id.current_temp);
//        mStructureNameText = (TextView) findViewById(R.id.structure_name);
        mAmbientTempText = (TextView) findViewById(R.id.ambient_temp);
        mStructureAway = (Button) findViewById(R.id.structure_away_btn);
        mRangeControlContainer = findViewById(R.id.range_control);
        mCurrentCoolTempText = (TextView) findViewById(R.id.current_cool_temp);
        mCurrentHeatTempText = (TextView) findViewById(R.id.current_heat_temp);

        mCoinView = findViewById(R.id.coin_view);
        mSavingText = (TextView) findViewById(R.id.saving_text);
        mSavingUp = (TextView)findViewById(R.id.saving_up);
        mSavingDown = (TextView)findViewById(R.id.saving_down);
        mElecStatusText = (TextView)findViewById(R.id.elec_status_text);
        mTempUp = (TextView) findViewById(R.id.temp_up);
        mTempDown = (TextView) findViewById(R.id.temp_down);

        mCoinStackImg = (ImageView) findViewById(R.id.coin_stack_img);
        mMercuryImg = (ImageView)findViewById(R.id.mercury_img);
        mThermometerBottom1 = (ImageView)findViewById(R.id.bottom1);
        mThermometerBottom2 = (ImageView)findViewById(R.id.bottom2);
        mTempSeekbar = (VerticalSeekBar)findViewById(R.id.tempSeekbar);
        setSeekbarListener();
        mTempSeekbar.getThumb().mutate().setAlpha(0);

        mStructureAway.setOnClickListener(this);
        findViewById(R.id.logout_button).setOnClickListener(this);
        findViewById(R.id.heat).setOnClickListener(this);
        findViewById(R.id.cool).setOnClickListener(this);

        findViewById(R.id.heat_cool).setOnClickListener(this);
        findViewById(R.id.heat_cool).setEnabled(false);
        findViewById(R.id.off).setOnClickListener(this);
        findViewById(R.id.confirm_btn).setOnClickListener(this);

        findViewById(R.id.temp_up).setOnClickListener(this);
        findViewById(R.id.temp_down).setOnClickListener(this);
        findViewById(R.id.temp_cool_up).setOnClickListener(this);
        findViewById(R.id.temp_cool_down).setOnClickListener(this);
        findViewById(R.id.temp_heat_up).setOnClickListener(this);
        findViewById(R.id.temp_heat_down).setOnClickListener(this);
        findViewById(R.id.thermostat_view).setOnClickListener(this);
        findViewById(R.id.coin_img).setOnClickListener(this);
        findViewById(R.id.saving_up).setOnClickListener(this);
        findViewById(R.id.saving_down).setOnClickListener(this);

        MainActivity.context=getApplicationContext();

        NestAPI.setAndroidContext(this);
        mNest = NestAPI.getInstance();

        // Authentication
        mToken = Auth.loadAuthToken(this);
        Auth.initialize();

        if (mToken != null) {
            authenticate(mToken);
        } else {
            mNest.setConfig(CLIENT_ID, CLIENT_SECRET, REDIRECT_URL);
            mNest.launchAuthFlow(this, AUTH_TOKEN_REQUEST_CODE);
        }

        //
        if (savedInstanceState != null) {
            Log.v(TAG, "savedInstanceState != null");
            mThermostat = savedInstanceState.getParcelable(THERMOSTAT_KEY);
            mStructure = savedInstanceState.getParcelable(STRUCTURE_KEY);
            updateViews();
        }

        // Database
        mDB = new Database();
    }

    public static Context getAppContext(){
        return MainActivity.context;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(THERMOSTAT_KEY, mThermostat);
        outState.putParcelable(STRUCTURE_KEY, mStructure);
    }

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

    @Override
    public void onStart(){
        super.onStart();
        Auth.addAuthListener();
        fetchData();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
        mNest.removeAllListeners();
        Auth.removeAuthListener();
        Auth.signOut();
    }

    @Override
    public void onClick(View v) {
        if (mThermostat == null || mStructure == null) {
            return;
        }

        String thermostatID = mThermostat.getDeviceId();
        String mode = mThermostat.getHvacMode();
        String awayState = mStructure.getAway();
        double init_temp = mThermostat.getTargetTemperatureC();
        int coinRadius, savingTextSize, thermRadius, tempTextSize;

        double targetC = display_temp;
        double ambientC = mThermostat.getAmbientTemperatureC();
        double tempDiffC = targetC - ambientC;
        boolean isHeating = KEY_HEAT.equals(mode) && tempDiffC > 0;
        boolean isCooling = KEY_COOL.equals(mode) && tempDiffC < 0;
        boolean isAboveAmbient = tempDiffC > 0;
        boolean isBelowAmbient = tempDiffC < 0;

        switch (v.getId()) {
            case R.id.coin_img:
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
                mCurrentTempText.setTextSize(tempTextSize);

                break;
            case R.id.thermostat_view:
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
                mCurrentTempText.setTextSize(tempTextSize);
                break;
            case R.id.confirm_btn:
                Log.d(TAG, "Clicked Confirm");
                if (display_temp != init_temp){ //if target temp is different from initial temperature
                    mNest.thermostats.setTargetTemperatureC(mThermostat.getDeviceId(), display_temp);
                    Date timeStamp = new Date();
                    Action newAction = new Action(mThermostat.getTargetTemperatureC(), display_temp, mThermostat.getAmbientTemperatureC());
                    String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Database.writeNewAction(UID, timeStamp, newAction);
                }
                break;
            case R.id.saving_up:
                if(display_temp < 32 && KEY_COOL.equals(mode) && isCooling)
                    display_temp+=0.5;
                else if(display_temp > 9 && KEY_HEAT.equals(mode) && isHeating)
                    display_temp-=0.5;
                updateSingleControlView();
                break;
            case R.id.temp_up:
                if(display_temp < 32)
                    display_temp+=0.5;
                updateSingleControlView();
                break;
            case R.id.saving_down:
                if(display_temp > 9 && KEY_COOL.equals(mode))
                    display_temp-=0.5;
                else if(display_temp < 32 && KEY_HEAT.equals(mode))
                    display_temp+=0.5;
                updateSingleControlView();
                break;
            case R.id.temp_down:
                if(display_temp > 9)
                    display_temp-=0.5;
                updateSingleControlView();
                break;
            case R.id.temp_heat_up:
            case R.id.temp_heat_down:
            case R.id.temp_cool_up:
            case R.id.temp_cool_down:
                if (KEY_HEAT_COOL.equals(mode)) {
                    updateTempRange(v);
                } else if (KEY_OFF.equals(mode)) {
                    Log.d(TAG, "Cannot set temperature when HVAC mode is off.");
                }
                break;
            case R.id.heat:
                mNest.thermostats.setHVACMode(thermostatID, KEY_HEAT);
                break;
            case R.id.cool:
                mNest.thermostats.setHVACMode(thermostatID, KEY_COOL);
                break;
            case R.id.heat_cool:
                mNest.thermostats.setHVACMode(thermostatID, KEY_HEAT_COOL);
                break;
            case R.id.off:
                mNest.thermostats.setHVACMode(thermostatID, KEY_OFF);
                break;
            case R.id.structure_away_btn:
                if (KEY_AUTO_AWAY.equals(awayState) || KEY_AWAY.equals(awayState)) {
                    awayState = KEY_HOME;
                    mStructureAway.setText(R.string.away_state_home);
                } else if (KEY_HOME.equals(awayState)) {
                    awayState = KEY_AWAY;
                    mStructureAway.setText(R.string.away_state_away);
                } else {
                    return;
                }

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
            case R.id.logout_button:
                Auth.saveAuthToken(this, null);
                mNest.launchAuthFlow(this, AUTH_TOKEN_REQUEST_CODE);
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

    private void updateTempRange(View v) {
        String thermostatID = mThermostat.getDeviceId();
        long tempHigh = mThermostat.getTargetTemperatureHighF();
        long tempLow = mThermostat.getTargetTemperatureLowF();

        switch (v.getId()) {
            case R.id.temp_cool_down:
                tempLow -= 1;
                mNest.thermostats.setTargetTemperatureLowF(thermostatID, tempLow);
                mCurrentCoolTempText.setText(String.format(DEG_F, tempLow));
                break;
            case R.id.temp_cool_up:
                tempLow += 1;
                mNest.thermostats.setTargetTemperatureLowF(thermostatID, tempLow);
                mCurrentCoolTempText.setText(String.format(DEG_F, tempLow));
                break;
            case R.id.temp_heat_down:
                tempHigh -= 1;
                mNest.thermostats.setTargetTemperatureHighF(thermostatID, tempHigh);
                mCurrentHeatTempText.setText(String.format(DEG_F, tempHigh));
                break;
            case R.id.temp_heat_up:
                tempHigh += 1;
                mNest.thermostats.setTargetTemperatureHighF(thermostatID, tempHigh);
                mCurrentHeatTempText.setText(String.format(DEG_F, tempHigh));
                break;
        }
    }

    private void updateViews() {
        if (mStructure == null || mThermostat == null) {
            return;
        }

        display_temp = mThermostat.getTargetTemperatureC();
        Log.v(TAG,"display_temp="+display_temp);
        mTempSeekbar.setProgress((int)((display_temp-9)/(32-9)*100));
        updateAmbientTempTextView();
        updateStructureViews();
        updateThermostatViews();
    }

    private void updateAmbientTempTextView() {
        mAmbientTempText.setText(String.format(DEG_C, mThermostat.getAmbientTemperatureC()));
    }

    private void updateStructureViews() {
//        mStructureNameText.setText(mStructure.getName());
        mStructureAway.setText(mStructure.getAway());
    }

    private void updateThermostatViews() {
        int singleControlVisibility;
        int rangeControlVisibility;
        String mode = mThermostat.getHvacMode();
        String state = mStructure.getAway();
        boolean isAway = state.equals(KEY_AWAY) || state.equals(KEY_AUTO_AWAY);
        Drawable default_btn_bg = findViewById(R.id.structure_away_btn).getBackground();

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

        if (isAway) {
            singleControlVisibility = View.VISIBLE;
            rangeControlVisibility = View.GONE;
            updateSingleControlView();
        } else if (KEY_HEAT_COOL.equals(mode)) {
            singleControlVisibility = View.GONE;
            rangeControlVisibility = View.VISIBLE;
            updateRangeControlView();
        } else if (KEY_OFF.equals(mode)) {
            singleControlVisibility = View.VISIBLE;
            rangeControlVisibility = View.GONE;
            findViewById(R.id.temp_up).setVisibility(View.GONE);
            findViewById(R.id.temp_down).setVisibility(View.GONE);
            mCurrentTempText.setText(R.string.thermostat_off);
            mThermostatView.setBackgroundResource(R.drawable.off_thermostat_drawable);
        } else {
            singleControlVisibility = View.VISIBLE;
            rangeControlVisibility = View.GONE;
            updateSingleControlView();
        }

        mSingleControlContainer.setVisibility(singleControlVisibility);
        mRangeControlContainer.setVisibility(rangeControlVisibility);
        mElecStatusText.setText("Electricity Status: "+ Energy.currElecStatus());
    }

    private void updateRangeControlView() {
        int thermostatDrawable;
        long lowF = mThermostat.getTargetTemperatureLowF();
        long highF = mThermostat.getTargetTemperatureHighF();
        long ambientF = mThermostat.getAmbientTemperatureF();
        boolean isCooling = (highF - ambientF) < 0;
        boolean isHeating = (lowF - ambientF) > 0;

        if (isCooling) {
            thermostatDrawable = R.drawable.cool_thermostat_drawable;
        } else if (isHeating) {
            thermostatDrawable = R.drawable.heat_thermostat_drawable;
        } else {
            thermostatDrawable = R.drawable.off_thermostat_drawable;
        }

        // Update the view.
        mCurrentHeatTempText.setText(String.format(DEG_F, highF));
        mCurrentCoolTempText.setText(String.format(DEG_F, lowF));
        mThermostatView.setBackgroundResource(thermostatDrawable);
    }

    private void updateSingleControlView() {
        int thermDrawable = R.drawable.off_thermostat_drawable;
        int upColor = Color.WHITE;
        int downColor = Color.WHITE;
        int thermometerColor = Color.BLACK;
//        double targetC= mThermostat.getTargetTemperatureC();
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

        if (isAway) {
            mCurrentTempText.setText(R.string.thermostat_away);
            mThermostatView.setBackgroundResource(thermDrawable);
            return;
        } else if (isHeating) {
            thermDrawable = R.drawable.heat_thermostat_drawable;
            upColor = Color.RED;
            downColor = Color.GREEN;
            thermometerColor = getResources().getColor(R.color.heat);
        } else if (isCooling) {
            thermDrawable = R.drawable.cool_thermostat_drawable;
            upColor = Color.GREEN;
            downColor = Color.RED;
            thermometerColor = getResources().getColor(R.color.cool);
        }else{
        }

        // Update the view.
        mCurrentTempText.setText(String.format(DEG_C, targetC));
        mThermostatView.setBackgroundResource(thermDrawable);
        mTempUp.setTextColor(upColor);
        mTempDown.setTextColor(downColor);

        shapeBottom1.setColor(thermometerColor);
        shapeBottom2.setColor(getResources().getColor(R.color.heat));

        updateSaving(!isCooling && !isHeating);
    }

    /*
    Todo: update comment
     */
    private void updateSaving(boolean isOff) {
        String mode = mThermostat.getHvacMode();
        double ambient_temp = mThermostat.getAmbientTemperatureC();
        double init_target_temp = mThermostat.getTargetTemperatureC();
        double temp_diff = init_target_temp - display_temp;    // cooling -> negative: saving, heating -> positive: saving
        double ambientC = mThermostat.getAmbientTemperatureC();
        if(temp_diff == 0)
            mSavingText.setText("¢ "+0);

        if(KEY_COOL.equals(mode) && init_target_temp > ambient_temp)
            init_target_temp = ambient_temp;
        else if(KEY_HEAT.equals(mode) && init_target_temp < ambient_temp)
            init_target_temp = ambient_temp;

        if(!isOff || display_temp == ambient_temp) {  // while the HVAC is ON, the saving will be updated
            Log.v(TAG, "Update Saving");
            double saving = Energy.tempToCents(ambientC, init_target_temp, KEY_COOL.equals(mode))
                    - Energy.tempToCents(ambientC, display_temp, KEY_COOL.equals(mode));
            double maxSaving = Energy.tempToCents(ambientC, init_target_temp, KEY_COOL.equals(mode));

            ClipDrawable mCoinStackClip = (ClipDrawable) mCoinStackImg.getDrawable();
            mCoinStackClip.setLevel((int)(saving/maxSaving*10000));

            mSavingText.setText("¢ "+String.format("%.0f", saving));
        }

    }

    private void setSeekbarListener(){
        mTempSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                if(progress<=100 && progress>=0){
                    display_temp = Tools.roundToHalf((32-9)*progress/100.0+9);
                    Log.v(TAG, "temp:"+display_temp);
                    ClipDrawable mMercuryClip = (ClipDrawable) mMercuryImg.getDrawable();
                    mMercuryClip.setLevel(progress*100);
                    updateSingleControlView();
                }
            }
        });
    }
}