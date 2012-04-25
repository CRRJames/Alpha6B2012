/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.coderedrobotics;

/**
 * Controls the belt which picks balls up off of the floor.
 *
 * @author Derek
 */
public class Belt {

    SafeJaguar upperJaguar;
    SafeJaguar lowerJaguar;
    private final static double CONST_BELT_SPEED = 1;
    int mode = 1;
    //mode constants
    public static final int onMode = 0;
    public static final int offMode = 1;
    public static final int reverseMode = 2;

    /**
     * Controls the belt which picks balls up off of the floor.
     *
     * @param int upperBeltPort is the port number of the Jaguar which controls
     * the upper belt.
     * @param int lowerBeltPort is the port number of the Jaguar which controls
     * the lower belt.
     * @param Gamepad g is the robot's main gamepad
     */
    public Belt(int upperBeltPort, int lowerBeltPort, Gamepad g) {
        upperJaguar = new SafeJaguar(upperBeltPort);
        lowerJaguar = new SafeJaguar(lowerBeltPort);

    }

    /**
     * Sets the belt to an upward constant speed determined by the program
     */
    private synchronized void spinBeltsUp() {
        lowerJaguar.set(-CONST_BELT_SPEED);
        upperJaguar.set(CONST_BELT_SPEED);
    }

    /**
     * Sets the belt speeds to 0
     */
    private synchronized void stopBelts() {
        lowerJaguar.set(0);
        upperJaguar.set(0);
    }

    /**
     * Sets the belt to a downward constant speed determined by the program
     */
    private synchronized void spinBeltsDown() {
        lowerJaguar.set(CONST_BELT_SPEED);
        upperJaguar.set(-CONST_BELT_SPEED);
    }
/**
 * Changes the mode of the belt
 * @param int mode - Mode number 
 */
    private synchronized void setMode(int mode) {
        this.mode = mode;
        if (mode == onMode) {
            spinBeltsUp();
        } else if (mode == offMode) {
            stopBelts();
        } else if (mode == reverseMode) {
            spinBeltsDown();
        } else {
            throw new IllegalStateException("mode is out of range");
        }
    }
    
    public synchronized void spinBelts(double power) {
        lowerJaguar.set(power);
        upperJaguar.set(-power);
    }

    /**
     * Sets the mode to on.
     */
    public void enableBelts() {
        spinBelts(CONST_BELT_SPEED);
    }

    /**
     * Sets the mode to off.
     */
    public void disableBelts() {
        spinBelts(0);
    }

    /**
     * Sets the mode to reverse.
     */
    public void reverseBelts() {
        spinBelts(-CONST_BELT_SPEED);
    }
}
