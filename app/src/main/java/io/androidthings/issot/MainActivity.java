package io.androidthings.issot;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.things.contrib.driver.apa102.Apa102;
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;
import com.google.android.things.contrib.driver.ht16k33.Ht16k33;
import com.google.android.things.contrib.driver.pwmservo.Servo;
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat;

import org.json.JSONObject;

import java.io.IOException;

import io.androidthings.issot.motorhat.MotorHat;
import io.androidthings.issot.motorhat.StepperMotor;

/**
 * %                       KKKKKKKKKKKKK
 * %                    KKKKKKKKKKKKKKKKKKK
 * %                   KKKKKKKKKKKKKKKKKKKKK
 * %                 KKKKKKKKKKKKKKKKKKKKKKKKK
 * %                KKKKKKKKKKKKKKKKKKKKKKKKKKKK
 * %               KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK
 * %              KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK
 * %             KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK
 * %            KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK
 * %            KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK
 * %           KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK
 * %           KKKKKKKK     KKKKKK        KKKKK
 * %          KKKKKKKK      KKKK          KKKf          K
 * %          KKKKKKKK      KKK           KK;          KK
 * %          KKKKKKKK     .KK           KKK           KKK
 * %          KKKKKKK      KKK      :KKKKKK       KKKKKKKKG
 * %          KKKKKKK      KKK      KKKKKKK      KKKKKKKKKK
 * %          KKKKKKK      KKK      KKKKKKK       KKKKKKKKK
 * %          KKKKKK      KKKK       KKKKKKK      iKKKKKKKKK
 * %          KKKKKK      KKKKK       KKKKKK       KKKKKKKKK
 * %          KKKKKK      KKKKKK       KKKKKK       KKKKKKKK
 * %          KKKKKt     KKKKKKKE      kKKKKKK       KKKKKKK
 * %          KKKKK      KKKKKKKK       KKKKKKK      KKKKKKK
 * %           KKKK      KKKKKKKKK      KKKKKKK      KKKKKKK
 * %           KKKK     KKKKKKKKK       KKKKKK       KKKKKKK
 * %           KKK      KKKK           KK:           KKKKKKK
 * %            KK      KKKK          KKK           KKKKKKKK
 * %             K     :KKK          KKKK          KKKKKKKKK
 * %                   KKKK         KKKKK        KKKKKKKKKKK
 * %              KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK
 * %               KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK
 * %                KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK
 * %                 KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK
 * %                  KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK
 * %                   KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK
 * %                    KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK
 * %                      KKKKKKKKKKKKKKKKKKKKKKKKKKKK
 * %                        KKKKKKKKKKKKKKKKKKKKKKKKK
 * %                          KKKKKKKKKKKKKKKKKKKKK
 * %                            kKKKKKKKKKKKKKKK
 * %                                 kKKKKKk
 * %
 */
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String ISS_URL = "https://api.wheretheiss.at/v1/satellites/25544";

    // Azimuth degrees per step based on the 16:40 tooth sprockets ratio
    private static final double DEGREES_PER_STEP = 1.3815;

    private static final int EARTH_RADIUS = 6371; //km equal 3959 ml

    // TODO: Add these to the Android app settings, but until then you need to hardcode your local coordinates
    private static final double TRACKER_LAT = 37.3041612;
    private static final double TRACKER_LON = -121.9769608;

    private static boolean mTrackerIntialized; // check if the stepper direction has been initialized
    private Handler mHandler;
    private MotorHat mMotorHat;
    private double mPrevAzimuth;
    private int mStepsNext = 0;
    private int mDirNext = MotorHat.FORWARD;
    private double mError = 0.0;
    private StepperMotor mStepperMotor;
    private RequestQueue mRequestQueue;  //Volley for JSONObject requests

    private Servo mServo;
    private AlphanumericDisplay mDisplay;
    private Apa102 mLedStrip;
    private int[] mLedColors;

    private static final int HSV_GREEN = 90;
    private static final int HSV_RED = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        while (!isOnline());

        mTrackerIntialized = false;

        try {
            mServo = RainbowHat.openServo();
            mServo.setPulseDurationRange(0.64, 2.44);
            mServo.setAngleRange(0.0, 180.0);
            mServo.setEnabled(true);

            mLedColors = new int[RainbowHat.LEDSTRIP_LENGTH];
            mLedStrip = RainbowHat.openLedStrip();
            mLedStrip.setBrightness(Apa102.MAX_BRIGHTNESS);

            // Initialize 7-segment display
            mDisplay = RainbowHat.openDisplay();
            mDisplay.setBrightness(Ht16k33.HT16K33_BRIGHTNESS_MAX);
            mDisplay.setEnabled(true);
            mDisplay.display("0000");
            Log.d(TAG, "Initialized I2C Display");
        } catch (IOException e) {
            throw new RuntimeException("Error initializing display", e);
        }

        mMotorHat = new MotorHat();
        mStepperMotor = mMotorHat.getStepper(2);
//        mStepperMotor.setSpeed(60, false);
        mRequestQueue = Volley.newRequestQueue(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mHandler = new Handler();
        mHandler.postDelayed(mTrackISSRunnable, 1000);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mDisplay != null) {
            try {
                mDisplay.clear();
                mDisplay.setEnabled(false);
                mDisplay.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing display", e);
            } finally {
                mDisplay = null;
            }
        }

        if (mServo != null) {
            try {
                mServo.setEnabled(false);
                mServo.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing servo", e);
            } finally {
                mServo = null;
            }
        }

        if (mLedStrip != null) {
            try {
                mLedStrip.setBrightness(0);
                mLedStrip.write(new int[7]);
                mLedStrip.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing LED strip", e);
            } finally {
                mLedStrip = null;
            }
        }

        if (mMotorHat != null) {
            mMotorHat.close();
            mMotorHat = null;
        }

        if (mHandler != null) {
            mHandler.removeCallbacks(mTrackISSRunnable);
        }

        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(TAG);
        }
    }


    /**
     * Get the Lat and Lon of ISS and move the pointer to that position.
     */
    private Runnable mTrackISSRunnable = new Runnable() {

        @Override
        public void run() {

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, ISS_URL,
                    null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        final double issLat = Double.parseDouble(response.getString("latitude"));
                        final double issLon = Double.parseDouble(response.getString("longitude"));
                        final double issAlt = Double.parseDouble(response.getString("altitude"));

                        final double φ1 = Math.toRadians(TRACKER_LAT); // Phi1
                        final double λ1 = Math.toRadians(TRACKER_LON); // Lambda1
                        final double φ2 = Math.toRadians(issLat); // Phi2
                        final double λ2 = Math.toRadians(issLon); // Lambda2

                        // Azimuth measured clockwise from true north
                        final double ψ = Math.toDegrees(Math.atan2(Math.sin(λ2 - λ1) * Math.cos(φ2),
                                Math.cos(φ1) * Math.sin(φ2) - Math.sin(φ1) * Math.cos(φ2) * Math.cos(λ2 - λ1)
                        ));

                        final double azimuth = (ψ < 0) ? 360 + ψ : ψ;

                        if (mDisplay != null && mPrevAzimuth != azimuth) {
                            try {
                                mDisplay.display(azimuth);
                            } catch (IOException e) {
                                Log.e(TAG, "Error updating display", e);
                            }
                        }

                        final double rS = issAlt + EARTH_RADIUS; // Radius from the center of the earth to the station (km)
                        final double γ = Math.acos(Math.sin(φ1) * Math.sin(φ2) + Math.cos(φ1) * Math.cos(φ2) * Math.cos(λ1 - λ2));  // earth central angle

                        final double d = Math.sqrt((1 + Math.pow((EARTH_RADIUS / rS), 2)) - (2 * (EARTH_RADIUS / rS) * Math.cos(γ))); // distance to the iss
                        final double El = Math.toDegrees(Math.acos(Math.sin(γ) / d) * ((d > 0.34) ? -1 : 1));

                        Log.d(TAG, Double.toString(azimuth));
                        Log.d("el:", Double.toString(El));

                        // Initialize the tracker direction using True North as the starting position
                        if (!mTrackerIntialized) {
                            mStepsNext = (int) Math.round(azimuth * DEGREES_PER_STEP);
                            mDirNext = MotorHat.FORWARD;
                            mStepperMotor.step(mStepsNext, mDirNext, MotorHat.MICROSTEP);
                            mPrevAzimuth = azimuth;
                            mTrackerIntialized = true;
                            Log.i(TAG, "Tracker Initialized...");
                        } else {

                            mDirNext = (azimuth < mPrevAzimuth) ? MotorHat.BACKWARD : MotorHat.FORWARD;

                            // Check if azimuth has crossed from 360 to 0 degrees or vice versa
                            if ((azimuth < 160 && mPrevAzimuth > 200) || (azimuth > 200 && mPrevAzimuth < 160)) {
                                // Recalculate direction of rotation
                                mDirNext = (azimuth > mPrevAzimuth) ? MotorHat.BACKWARD : MotorHat.FORWARD;
                                mPrevAzimuth = azimuth;
                                Log.i(TAG, "It crossed over...");
                            }

                            mStepsNext = (int) ((Math.abs(azimuth - mPrevAzimuth)) - (mError / DEGREES_PER_STEP));
                            if (mStepsNext > 0) {
                                mPrevAzimuth = azimuth;
                                mError = mStepsNext * 0.3;
                            }
                            mStepperMotor.step(mStepsNext, mDirNext, MotorHat.MICROSTEP);
                        }
                        mServo.setAngle(Math.abs(90 + El));
                        for (int i = 0; i < mLedColors.length; i++) {
                            mLedColors[i] = Color.BLACK;
                        }
                        if (El > 0) {
                            final int i = (int)(El/12.8572);
                            Log.i("Index", Integer.toString(i));
                            mLedColors[i] = Color.HSVToColor(255,
                                    new float[]{(HSV_GREEN - (i * (HSV_GREEN - HSV_RED) / mLedColors.length) + 360) % 360, 1.0f, 1.0f});
                        }
                        mLedStrip.write(mLedColors);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkResponse networkResponse = error.networkResponse;
                    if (networkResponse != null) {
                        if (networkResponse.data != null) {
                            Log.e(TAG, String.valueOf(networkResponse.data));
                        } else {
                            Log.e(TAG, "Error code: " + error.networkResponse.statusCode);
                            Log.e(TAG, "Error message " + error.getMessage());
                        }
                    }
                }
            });
            jsonObjectRequest.setTag(TAG);
//        RetryPolicy policy = new DefaultRetryPolicy(30000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
//        jsonObjectRequest.setRetryPolicy(policy);
            mRequestQueue.add(jsonObjectRequest);

            mHandler.postDelayed(this, 5000);
        }
    };

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}