/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.coderedrobotics;

/**
 * Holds constants for the robot in the form of static variables.
 *
 * @author Derek
 */
public class Wiring {

    /*
     * -----------  CAN ASSIGNMENTS -------------------------
     */
    /*
     * Port number for the jaguar that controls the left drive motor
     */
    public final static int bottomShooterJag = 3;
    public final static int lowerBeltJag = 5; 
    public final static int leftDriveJag = 6;
    public final static int upperBeltJag = 7; 
    public final static int rightDriveJag = 8; 
    public final static int topShooterJag = 9;
    public final static int ballReleaseJag = 10;
    /*
     * -----------  DIGITAL INPUTS -------------------------
     */
    //must be changed
    public final static int UltrasonicEcho = 1;
    public final static int UltrasonicPing = 2;
    public final static int ballReleaseEncoderA = 4;
    public final static int ballReleaseEncoderB = 5;
    public final static int shooterBotEncoderA = 8;
    public final static int shooterBotEncoderB = 9;
    public final static int shooterTopEncoderA = 10;
    public final static int shooterTopEncoderB = 11;
    /*
     * -----------  PWM PORTS -------------------------
     */
    public final static int leftBridgeVictor = 1;   
    public final static int rightBridgeVictor = 2;
    public final static int cameraTiltServo = 9;
    public final static int cameraPanServo = 10;
    /*
     * -----------  ANALOG INPUTS -------------------------
     */
    public final static int gyroPort = 1;
    /*
     * -----------  OTHER -------------------------
     */
    public final static int joystick1Port = 1; // drvGamepad
    public final static int joystick2Port = 2; // manGamepad
}
