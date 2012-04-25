/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.coderedrobotics;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PIDController;

/**
 * Class which controls the rotating ball release mechanism.
 *
 * @author Derek
 */
public class BallRelease {

    SafeJaguar jaguar;
//    Encoder encoder;
//    PIDController pid;
//    DigitalInput sensorC;

    /**
     * Class which controls the rotating ball release mechanism.
     */
    public BallRelease() {
//        sensorC = new DigitalInput(Wiring.sensorCChannel);
        jaguar = new SafeJaguar(Wiring.ballReleaseJag);
//        encoder = new Encoder(Wiring.ballReleaseEncoderA,
//                Wiring.ballReleaseEncoderB);
//        encoder.setPIDSourceParameter(Encoder.PIDSourceParameter.kDistance);
//        encoder.setDistancePerPulse(1);
//        encoder.start();
//        pid = new PIDController(0.1, 0, 0, encoder, jaguar);
//        pid.enable();
    }

    /**
     * Release the Kraken!
     */
    public void SpinBallRelease() {
        jaguar.set(1);
//        pid.enable();
//        Rotate(180);
//        double startTime = System.currentTimeMillis();
//        while(System.currentTimeMillis() <= (startTime + 1000)){
//            jaguar.set(-0.5);
//        }
    }
    public void StopBallRelease() {
        jaguar.set(0);
    }
   
    /**
     * Rotates the motor to a specific degree
     *
     * @param double s is the setpoint in degrees
     */
//    private void Rotate(double s) {
//        double setpoint = pid.getSetpoint() + s;
//        pid.setSetpoint(setpoint);
//        System.out.println("setpoint: " + setpoint);
//        System.out.println("encoder: " + encoder.getRaw());
//    }
}
