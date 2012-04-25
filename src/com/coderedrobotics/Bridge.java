/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.coderedrobotics;

import edu.wpi.first.wpilibj.Victor;

/**
 * Class that contains two victors that control the motors in the arm
 *
 * @author Derek
 */
public class Bridge {

    Victor left;
    Victor right;

    /**
     * Class that contains two victors that control the motors in the arm
     * @param int l - Left Jaguar port 
     * @param int r - Right Jaguar port
     */
    public Bridge() {
        left = new Victor(Wiring.leftBridgeVictor);
        right = new Victor(Wiring.rightBridgeVictor);
    }

    /**
     * Sets the speed of both of the manipulator's victors
     *
     * @param double s is the value assigned to the speed
     */
    public void moveManipulator(double s) {
        left.set(s);
        right.set(s);
    }
}
