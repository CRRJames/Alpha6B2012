/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.coderedrobotics;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Gyro;
import com.sun.squawk.util.MathUtils;

/**
 *
 * @author laptop
 */
public class PlaceTracker {

    private double x, y, dis, oldDis;
    private Encoder encoder;
    private Gyro gyro;
    private Drive drive;

    public PlaceTracker(Encoder encoder, Gyro gyro, Drive drive) {
        this.encoder = encoder;
        this.gyro = gyro;
        this.drive = drive;
    }

    public void step() {
        dis = encoder.get();
        double rot = gyro.getAngle()%360;

        x += Math.cos(Math.toRadians(rot)) * (oldDis - dis);
        y += Math.sin(Math.toRadians(rot)) * (oldDis - dis);

        oldDis = dis;
    }
    
    public boolean goTo(double x, double y, double power, double rotPower) {
        boolean done = false;
        double dir = MathUtils.atan2(y - this.y, x - this.x);
        double speed = 0;
        double rot = gyro.getAngle()%360;

        if (dir - (rot) > 180) {
            dir -= 2 * 180;
        } else if ((rot) - dir > 180) {
            dir += 2 * 180;
        }

        if (Math.abs((rot) - dir) < 90) {
            double dis = Math.sqrt(MathUtils.pow(x - this.x, 2)
                    + MathUtils.pow(y - this.y, 2));
            speed = (90 - Math.abs(rot - dir)) / 90;
            speed *= dis;
            speed *= power;
            if (dis < 10) {
                done = true;
            }
        }

        //drive.setLeft();
        
        return done;
    }
}