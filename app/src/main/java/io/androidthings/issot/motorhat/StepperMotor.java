package io.androidthings.issot.motorhat;

import android.util.Log;


/**
 * Ported from Antonio Zugaldia's adaptation of the python motor-hat library
 */
public class StepperMotor {

    private static final String TAG = StepperMotor.class.getSimpleName();

    private static int[] MICROSTEP_CURVE = {0, 50, 98, 142, 180, 212, 236, 250, 255};
    private int MICROSTEPS = 8;         // 8 or 16
    private MotorHat MC;
    private int revsteps;
    private double sec_per_step;
    private int currentstep;
    private boolean sleepBetweenSteps = true;
    private boolean alreadySetPWM = false;

    private int PWMA = 8;
    private int AIN2 = 9;
    private int AIN1 = 10;
    private int PWMB = 13;
    private int BIN2 = 12;
    private int BIN1 = 11;

    public StepperMotor(MotorHat MC, int motorNumber, int steps, boolean sleepBetweenSteps) {

        this.MC = MC;
        this.revsteps = steps;
        this.sec_per_step = 0.01;
        this.currentstep = 0;
        this.sleepBetweenSteps = sleepBetweenSteps;

        // Really retarded handling of zero based motor numbers but whatever for now
        motorNumber -= 1;

        if (motorNumber == 0) {

            this.PWMA = 8;
            this.AIN2 = 9;
            this.AIN1 = 10;
            this.PWMB = 13;
            this.BIN2 = 12;
            this.BIN1 = 11;

        } else if (motorNumber == 1) {

            this.PWMA = 2;
            this.AIN2 = 3;
            this.AIN1 = 4;
            this.PWMB = 7;
            this.BIN2 = 6;
            this.BIN1 = 5;

        } else {
            Log.e(".", "MotorHAT Stepper must be between 1 and 2 inclusive");
        }
    }

    // If you just set speed, it's assumed you want to sleep between steps (which is what setting speed affects)
    // so the overloaded method is called with sleepBetweenSteps set to true
    public void setSpeed(int rpm) {
        setSpeed(rpm, true);
    }

    public void setSpeed(int rpm, boolean sleepBetweenSteps) {
        this.sleepBetweenSteps = sleepBetweenSteps;
        sec_per_step = 60.0 / (revsteps * rpm);
    }

    // It makes a single step in the style you request, with no delay. This will let you step
    // exactly when you like, for the most control
    private int oneStep(int dir, int style) {

        int pwm_a = 255;
        int pwm_b = 255;

        // Set up coil energizing!
        int coils[] = {0, 0, 0, 0};

        int step2coils[][] = {
                {1, 0, 0, 0},
                {1, 1, 0, 0},
                {0, 1, 0, 0},
                {0, 1, 1, 0},
                {0, 0, 1, 0},
                {0, 0, 1, 1},
                {0, 0, 0, 1},
                {1, 0, 0, 1}
        };

        // First determine what sort of stepping procedure we're up to
        if (style == MotorHat.SINGLE) {

            if ((currentstep / (MICROSTEPS / 2)) % 2 > 0) {

                // We're at an odd step, weird
                if (dir == MotorHat.FORWARD) {

                    // Probably don't need parens, but being extra careful.  Don't want to fry boards.
                    currentstep += (MICROSTEPS / 2);
                } else {
                    currentstep -= (MICROSTEPS / 2);
                }

            } else {

                // Go to next even step
                if (dir == MotorHat.FORWARD) {
                    currentstep += MICROSTEPS;
                } else {
                    currentstep -= MICROSTEPS;
                }
            }

        } else if (style == MotorHat.DOUBLE) {

            //Log.d(TAG, "Double step");

            if ((currentstep / (MICROSTEPS / 2)) % 2 == 0) {

                // We're at an even step, weird
                if (dir == MotorHat.FORWARD) {
                    currentstep += (MICROSTEPS / 2);
                } else {
                    currentstep -= (MICROSTEPS / 2);
                }

            } else {

                // Go to next even step
                if (dir == MotorHat.FORWARD) {
                    currentstep += MICROSTEPS;
                } else {
                    currentstep -= MICROSTEPS;
                }
            }

        } else if (style == MotorHat.INTERLEAVE) {

            if (dir == MotorHat.FORWARD) {
                currentstep += (MICROSTEPS / 2);
            } else {
                currentstep -= (MICROSTEPS / 2);
            }

        } else if (style == MotorHat.MICROSTEP) {

            if (dir == MotorHat.FORWARD) {
                currentstep++;
            } else {

                currentstep--;
                // Go to next 'step' and wrap around
                currentstep += (MICROSTEPS * 4);
                currentstep %= (MICROSTEPS * 4);
            }

            pwm_a = 0;
            pwm_b = 0;

            if (currentstep >= 0 && currentstep < MICROSTEPS) {
                pwm_a = MICROSTEP_CURVE[MICROSTEPS - currentstep];
                pwm_b = MICROSTEP_CURVE[currentstep];
            } else if (currentstep >= MICROSTEPS && currentstep < MICROSTEPS * 2) {
                pwm_a = MICROSTEP_CURVE[currentstep - MICROSTEPS];
                pwm_b = MICROSTEP_CURVE[MICROSTEPS * 2 - currentstep];
            } else if (currentstep >= MICROSTEPS * 2 && currentstep < MICROSTEPS * 3) {
                pwm_a = MICROSTEP_CURVE[MICROSTEPS * 3 - currentstep];
                pwm_b = MICROSTEP_CURVE[currentstep - MICROSTEPS * 2];
            } else if (currentstep >= MICROSTEPS * 3 && currentstep < MICROSTEPS * 4) {
                pwm_a = MICROSTEP_CURVE[currentstep - MICROSTEPS * 3];
                pwm_b = MICROSTEP_CURVE[MICROSTEPS * 4 - currentstep];
            }
        }

        // Go to next 'step' and wrap around
        currentstep += (MICROSTEPS * 4);
        currentstep %= (MICROSTEPS * 4);

        // Only really used for microstepping, otherwise always on!
        if (!alreadySetPWM || style == MotorHat.MICROSTEP) {

            // If we're not doing microstepping, then the value will always be the same,
            // so only do it once.  Otherwise it wastes 5 milliseconds according to logs
            alreadySetPWM = true;
            MC.getDevice().setChannel(PWMA, 0, pwm_a * 16);
            MC.getDevice().setChannel(PWMB, 0, pwm_b * 16);
        }

        if (style == MotorHat.MICROSTEP) {
            if (currentstep >= 0 && currentstep < MICROSTEPS) {
                coils = new int[]{1, 1, 0, 0};
            } else if (currentstep >= MICROSTEPS && currentstep < MICROSTEPS * 2) {
                coils = new int[]{0, 1, 1, 0};
            } else if (currentstep >= MICROSTEPS * 2 && currentstep < MICROSTEPS * 3) {
                coils = new int[]{0, 0, 1, 1};
            } else if (currentstep >= MICROSTEPS * 3 && currentstep < MICROSTEPS * 4) {
                coils = new int[]{1, 0, 0, 1};
            }
        } else {
            coils = step2coils[currentstep / (MICROSTEPS / 2)];
        }

        //Log.d(TAG, "coils state = " + coils);

        MC.setPin(AIN2, coils[0]);
        MC.setPin(BIN1, coils[1]);
        MC.setPin(AIN1, coils[2]);
        MC.setPin(BIN2, coils[3]);

        return currentstep;
    }

    public void step(int steps, int direction, int stepstyle) {

        double s_per_s = sec_per_step;
        int lateststep = 0;

        if (stepstyle == MotorHat.INTERLEAVE) {
            s_per_s /= 2.0;
        } else if (stepstyle == MotorHat.MICROSTEP) {
            s_per_s /= MICROSTEPS;
            steps *= MICROSTEPS;
            Log.i(TAG, String.valueOf(steps));
        }

//        Log.d(TAG, "secs per step: " + s_per_s);

        for (int s = 1; s <= steps; s++) {

            lateststep = oneStep(direction, stepstyle);

            if (sleepBetweenSteps) {
                try {
                    Thread.sleep((long) s_per_s * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if (stepstyle == MotorHat.MICROSTEP) {

            // This is an edge case, if we are in between full steps, lets just keep going
            // So we end on a full step
            while (lateststep != 0 && lateststep != MICROSTEPS) {

                lateststep = oneStep(direction, stepstyle);

                if (sleepBetweenSteps) {
                    try {
                        Thread.sleep((long) s_per_s * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
