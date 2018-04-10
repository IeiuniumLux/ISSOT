package io.androidthings.issot.motorhat;


import android.util.Log;


/**
 * Ported from Antonio Zugaldia's adaptation of the python motor-hat library
 */
public class MotorHat {
    private static final String TAG = MotorHat.class.getSimpleName();


    public static final int MOTOR_FREQUENCY = 2400;//1600;
    public static final int FORWARD = 1;
    public static final int BACKWARD = 2;
    public static final int BRAKE = 3;
    public static final int RELEASE = 4;
    public static final int SINGLE = 1;
    public static final int DOUBLE = 2;
    public static final int INTERLEAVE = 3;
    public static final int MICROSTEP = 4;

    // For maximum speed set to false; which turns off all delays between steps
    public static final boolean SLEEP_BETWEEN_STEPS = false;

    private I2CDevice device;
    private DCMotor[] motors;
    private StepperMotor[] steppers;

    public MotorHat() {

        // TODO: Pass in address so we can stack them.  Not scalable now.
        device = new I2CDevice();

        device.setPWMFreq(MOTOR_FREQUENCY);

        motors = new DCMotor[]{
                new DCMotor(this, 0),
                new DCMotor(this, 1),
                new DCMotor(this, 2),
                new DCMotor(this, 3)
        };

        steppers = new StepperMotor[]{
                // Might want to parameterize steps per motor (200 here)
                new StepperMotor(this, 1, 200, SLEEP_BETWEEN_STEPS),
                new StepperMotor(this, 2, 200, SLEEP_BETWEEN_STEPS)
        };
    }

    public I2CDevice getDevice() {
        return device;
    }

    public void setPin(int pin, int value) {
        if ((pin < 0) || (pin > 15)) {
            throw new RuntimeException("PWM pin must be between 0 and 15 inclusive");
        }
        if ((value != 0) && (value != 1)) {
            throw new RuntimeException("Pin value must be 0 or 1!");
        }
        if ((value == 0)) {
            device.setChannel(pin, 0, 4096);
        }
        if ((value == 1)) {
            device.setChannel(pin, 4096, 0);
        }
    }

    public DCMotor getMotor(int num) {
        if ((num < 1) || (num > 4)) {
            throw new RuntimeException("MotorHAT Motor must be between 1 and 4 inclusive");
        }
        return motors[num - 1];
    }

    public StepperMotor getStepper(int num) {
        if ((num < 1) || (num > 2)) {
            throw new RuntimeException("MotorHAT Stepper must be between 1 and 2 inclusive");
        }
        return steppers[num - 1];
    }

    public void close() {
        try {
            device.close();
        } catch (Exception e) {
            Log.w(TAG, "Unable to close I2C device:", e);
        }
    }
}
