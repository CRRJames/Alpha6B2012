/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.coderedrobotics;

import edu.wpi.first.wpilibj.Ultrasonic;

public class UltrasonicRangefinder {

    private Ultrasonic ultrasonic;
    private Thread thread;
    private double range;

    public UltrasonicRangefinder(int ping, int echo) {
        ultrasonic = new Ultrasonic(ping, echo);
        ultrasonic.setAutomaticMode(false);
        ultrasonic.setEnabled(true);
        thread = new Thread(new Pinger(this));
    }

    public double getRange() {
        return range;
    }

    private class Pinger implements Runnable {

        UltrasonicRangefinder ur;

        private Pinger(UltrasonicRangefinder ur) {
            this.ur = ur;
        }

        public void run() {
            while (true) {
                ur.ultrasonic.ping();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                double d = ur.ultrasonic.getRangeMM();
                if (d < 5500) {
                    ur.range = d * 0.0032808399;//convert to feet
                }
            }
        }
    }
}
