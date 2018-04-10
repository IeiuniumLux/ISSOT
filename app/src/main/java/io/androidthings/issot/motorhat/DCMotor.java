package io.androidthings.issot.motorhat;

/**
 * Ported from Antonio Zugaldia's adaptation of the python motor-hat library
 */

public class DCMotor {
    int PWMpin;
    int IN1pin;
    int IN2pin;
    private MotorHat MC;
    private int motornum;

    public DCMotor(MotorHat MC, int num) {
        this.MC = MC;
        this.motornum = num;
        if (num == 0) {
            PWMpin = 8;
            IN2pin = 9;
            IN1pin = 10;
        } else if (num == 1) {
            PWMpin = 13;
            IN2pin = 12;
            IN1pin = 11;
        } else if (num == 2) {
            PWMpin = 2;
            IN2pin = 3;
            IN1pin = 4;
        } else if (num == 3) {
            PWMpin = 7;
            IN2pin = 6;
            IN1pin = 5;
        } else {
            throw new RuntimeException("Motor number must be between 1 and 4, inclusive.");
        }
    }

    public void run(int command) {
        if (MC == null) {
            return;
        }

        if (command == MotorHat.FORWARD) {
            MC.setPin(IN2pin, 0);
            MC.setPin(IN1pin, 1);
        } else if (command == MotorHat.BACKWARD) {
            MC.setPin(IN1pin, 0);
            MC.setPin(IN2pin, 1);
        } else if (command == MotorHat.RELEASE) {
            MC.setPin(IN1pin, 0);
            MC.setPin(IN2pin, 0);
        }
    }

    public void setSpeed(int speed) {
        if (speed < 0) {
            speed = 0;
        }
        if (speed > 255) {
            speed = 255;
        }

        MC.getDevice().setChannel(PWMpin, 0, speed * 16);
    }
}
