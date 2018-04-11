package io.androidthings.issot;

import android.app.Activity;
import android.content.Context;
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

    private static boolean mStepperIntialized; // check if the stepper direction has been initialized
    private Handler mHandler;
    private MotorHat mMotorHat;
    private double mPrevCAzimuth;
    private int mStepsNext = 0;
    private int mDirNext = MotorHat.FORWARD;
    private double mError = 0.0;
    private StepperMotor mStepperMotor;
    private RequestQueue mRequestQueue;  //Volley for JSONObject requests

    private Servo mServo;
    private AlphanumericDisplay mDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        while (!isOnline());

        mStepperIntialized = false;

        try {
            mServo = RainbowHat.openServo();
            mServo.setPulseDurationRange(0.64, 2.44);
            mServo.setAngleRange(0.0, 180.0);
            mServo.setEnabled(true);

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
     * Get the Lat and Lon of ISS and move the pointer to that position when called.
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
                        final double azimuth = Math.toDegrees(Math.atan2(Math.sin(λ2 - λ1) * Math.cos(φ2),
                                Math.cos(φ1) * Math.sin(φ2) - Math.sin(φ1) * Math.cos(φ2) * Math.cos(λ2 - λ1)
                        ));

                        final double cAzimuth = (azimuth < 0) ? 360 + azimuth : azimuth;

                        if (mDisplay != null && mPrevCAzimuth != cAzimuth) {
                            try {
                                mDisplay.display(cAzimuth);
                            } catch (IOException e) {
                                Log.e(TAG, "Error updating display", e);
                            }
                        }

                        final double rS = Math.toRadians(issAlt) + EARTH_RADIUS; // Radius of ISS (km)
                        final double γ = Math.acos((Math.cos(φ1) * Math.cos(φ2) * Math.cos(λ1 - λ2) + Math.sin(φ1) * Math.sin(φ2)));  // earth central angle
                        final double d = rS * Math.sqrt((1 + Math.pow((EARTH_RADIUS / rS), 2)) - (2 * (EARTH_RADIUS / rS) * Math.cos(γ))); // distance to the iss

                        final double el = Math.toDegrees(Math.acos(rS / d * Math.sin(γ)) * ((rS < (d + EARTH_RADIUS)) ? -1 : 1)); // elevation angle

//                        Log.d(TAG, Double.toString(cAzimuth));
//                        Log.d("el:", Double.toString(el));

                        // Initialize the stepper direction assuming its initial position is true north
                        if (!mStepperIntialized) {
                            mStepsNext = (int) Math.round(cAzimuth * DEGREES_PER_STEP);
                            mDirNext = MotorHat.FORWARD;
                            mStepperMotor.step(mStepsNext, mDirNext, MotorHat.MICROSTEP);
                            mPrevCAzimuth = cAzimuth;
                            mStepperIntialized = true;
                            Log.i(TAG, "Tracker Initialized...");
                        } else {

                            // Determine direction of rotation
                            mDirNext = (cAzimuth < mPrevCAzimuth) ? MotorHat.BACKWARD : MotorHat.FORWARD;

                            // Check if azimuth has crossed from 360 to 0 degrees or vice versa
                            if ((cAzimuth < 160 && mPrevCAzimuth > 200) || (cAzimuth > 200 && mPrevCAzimuth < 160)) {
                                // Recalculate direction of rotation
                                mDirNext = (cAzimuth > mPrevCAzimuth) ? MotorHat.BACKWARD : MotorHat.FORWARD;
                                mPrevCAzimuth = cAzimuth;
                                mError = 0.0;
                                Log.i(TAG, "It crossed over...");
                            }

                            mStepsNext = (int) ((Math.abs(cAzimuth - mPrevCAzimuth)) - (mError / DEGREES_PER_STEP));
                            if (mStepsNext > 0) {
                                mPrevCAzimuth = cAzimuth;
                                mError = mStepsNext * 0.3;
//                                Log.d("mError:", Double.toString(mError));
                            }
                            mStepperMotor.step(mStepsNext, mDirNext, MotorHat.MICROSTEP);
                        }
                        mServo.setAngle(Math.abs(90 + el));
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