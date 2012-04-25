/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.coderedrobotics;

import edu.wpi.first.wpilibj.Encoder;

/**
 *
 * @author laptop
 */
public class SpeedEncoder extends Encoder {

    long lastTime = 0;
    double lastPos = 0;
    double currentSpeed = 0;
    
    public SpeedEncoder(int a, int b) {
        super(a, b);

        start();
        lastTime = System.currentTimeMillis();
        setPIDSourceParameter(Encoder.PIDSourceParameter.kRate);
    }

    /**
     * Changes PID Source parameter to rate. Obsolete - please replace
     * @deprecated Use setPIDSourceParameter() instead.
     */
    public void setParameterRate() {
        setPIDSourceParameter(Encoder.PIDSourceParameter.kRate);
    }

    /**
     * Changes PID Source parameter to distance. Obsolete - please replace.
     * @deprecated Use setPIDSourceParameter() instead.
     */
    public void setParameterDistance() {
        setPIDSourceParameter(Encoder.PIDSourceParameter.kDistance);
    }

    public double pidGet() {
        currentSpeed = (lastPos - getRaw()) / ((int) (lastTime - System.currentTimeMillis()));
        lastPos = getRaw();
        lastTime = System.currentTimeMillis();
        return currentSpeed;
    }
    
    public double getSpeed() {
        return currentSpeed;
    }
}
