/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.coderedrobotics;

import com.sun.cldc.jna.Windows;
import com.sun.squawk.microedition.io.FileConnection;
import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.can.CANTimeoutException;
import edu.wpi.first.wpilibj.camera.AxisCamera;
import edu.wpi.first.wpilibj.camera.AxisCameraException;

/**
 * Class which controls the firing mechanism on the ball shooter.
 *
 * @author dvanvoorst
 */
public class Shooter implements PIDSource {

    AxisCamera myCamera;
    Drive myDriveTrain;
    SafeJaguar jagShooterTop = null;
    SafeJaguar jagShooterBottom = null;
    SpeedEncoder encShooterTop = null;
    SpeedEncoder encShooterBottom = null;
    PIDController pidTop = null;
    PIDController pidBottom = null;
    Relay onTargetSignal;
    private PIDController pidGyro;
    Gyro gyro;
    BallRelease ballRelease;
    UltrasonicRangefinder ultrasonicRangefinder;
    private double cameraDistance;
    private double tsp;
    private double bsp;
    private int override = 0;
    private Client client;
    private boolean isAligning = false;
    private boolean cypressFudgeSource = false;
    private Joystick joystick;
    private ImageObject imageObject;
    private double angle;

    /**
     * Class which controls the firing mechanism on the ball shooter.
     *
     * @param driveTrain - Drive train object
     * @param shooterTopPort - Port number for the top shooter jaguar
     * @param shooterBotPort - Port number for the bottom shooter jaguar
     */
    public Shooter(Drive driveTrain, int shooterTopPort, int shooterBotPort,
            Joystick j, ImageObject imageObject) {
        ultrasonicRangefinder = new UltrasonicRangefinder(
                Wiring.UltrasonicPing,
                Wiring.UltrasonicEcho);
        this.imageObject = imageObject;
        isAligning = false;
        gyro = new Gyro(Wiring.gyroPort);
        gyro.reset();
        myDriveTrain = driveTrain;
        pidGyro = new PIDController(0.28, 0, 0.2, this, driveTrain);
        pidGyro.setOutputRange(-1, 1);

        encShooterTop = new SpeedEncoder(
                Wiring.shooterTopEncoderA,
                Wiring.shooterTopEncoderB);
        encShooterBottom = new SpeedEncoder(
                Wiring.shooterBotEncoderA,
                Wiring.shooterBotEncoderB);

        jagShooterTop = new SafeJaguar(shooterTopPort, encShooterTop);
        jagShooterBottom = new SafeJaguar(shooterBotPort, encShooterBottom);

        encShooterTop.setPIDSourceParameter(
                Encoder.PIDSourceParameter.kDistance);
        encShooterBottom.setPIDSourceParameter(
                Encoder.PIDSourceParameter.kDistance);

        // these PID values are overwritten in the test code
        pidTop = new PIDController(-0.02, 0, -0.13,
                encShooterTop, jagShooterTop);
        pidBottom = new PIDController(0.06, 0, 0.1,
                encShooterBottom, jagShooterBottom);

        jagShooterTop.setRelative();
        jagShooterBottom.setRelative();

        pidTop.setSetpoint(0);
        pidBottom.setSetpoint(0);

        pidTop.enable();
        pidBottom.enable();

        ballRelease = new BallRelease();

        onTargetSignal = new Relay(1, Relay.Direction.kForward);
        onTargetSignal.set(Relay.Value.kOff);

        joystick = j;
    }

    public void setCypressFudgeSource() {
        cypressFudgeSource = true;
    }

    public void setJoysickFudgeSource() {
        cypressFudgeSource = false;
    }

    /**
     * Sets the speed of the shooter.
     *
     * @param double distance - Distance from camera used to calculate speeds
     */
    public void setShooterSpeed() {

        if (override != 0) {
            //  System.out.println("Override: " + override + "\tFudge: " + getFudgeFactor());
        }

        if (override == 5) { // 12' shot
            tsp = calcTopSpeed(12);
            bsp = calcBottomSpeed(12);
        } else if (override == 6) {
            tsp = 60;
            bsp = 60;
        } else if (override == 3) { // short shot to side basket
            tsp = 7;
            bsp = 27;
        } else if (override == 8) { // auto shot 15'
            tsp = calcTopSpeed(18);
            bsp = calcBottomSpeed(18);
        } else if (override == 9) {// 16' Shot
            tsp = calcTopSpeed(16);
            bsp = calcBottomSpeed(16);
        } else if (override == 2) {
            if (imageObject.isReady()) {
                angle = imageObject.getAngle();
                cameraDistance = imageObject.getDistance();
            }
            if (cameraDistance != 0) {
                tsp = calcTopSpeed(cameraDistance);
                bsp = calcBottomSpeed(cameraDistance);
            } else {
                tsp = 0;
                bsp = 0;
            }
            if (Math.abs(pidTop.getError()) < 1.5) {
                imageObject.requestImage(getGyroAngle());
                tsp = calcTopSpeed(12);
            }
        } else if (override == 1) {
            tsp = calcTopSpeed(ultrasonicRangefinder.getRange());
            bsp = calcBottomSpeed(ultrasonicRangefinder.getRange());
        }

        if (override != 0) {
            pidTop.setSetpoint(-tsp);
            pidBottom.setSetpoint(bsp);
            jagShooterTop.setFailSpeed(-tsp / 23);
            jagShooterBottom.setFailSpeed(bsp / 33);
            if (Globals.debugLevel > 0) {
                System.out.println("Top Motor Voltage: " + jagShooterTop.getOutputVoltage() + "  Current: " + jagShooterTop.getOutputCurrent());
            }
        } else {
            pidBottom.setSetpoint(0);
            pidTop.setSetpoint(0);
            jagShooterTop.setFailSpeed(0);
            jagShooterBottom.setFailSpeed(0);
        }

        //report if the jaugars are maxing out
        if (Math.abs(jagShooterTop.getSpeed()) == 1) {
            DriverStation.getInstance().setDigitalOut(2, true);
        } else {
            DriverStation.getInstance().setDigitalOut(2, false);
        }
        if (Math.abs(jagShooterBottom.getSpeed()) == 1) {
            DriverStation.getInstance().setDigitalOut(3, true);
        } else {
            DriverStation.getInstance().setDigitalOut(3, false);
        }
//        System.out.print(
//                " Top Setpoint: " + tsp
//                + "  Bottom SetPoint: " + bsp
//                + "     P: " + p);
//        System.out.println(
//                " Top encoder: " + encShooterTop.getSpeed() +
//                "  Bottom: " + encShooterBottom.getSpeed());
    }

    /**
     * Sets the setpoint of both shooters to 0, effectively stopping the
     * shooter.
     */
    public void setOverride(int override) {
        this.override = override;
        if (override == 2) {
            imageObject.requestImage(getGyroAngle());
        }
    }

    public void turnSignalOn() {
        onTargetSignal.set(Relay.Value.kOn);
        //    System.out.println("SIGNAL LIGHT ON");
    }

    public void turnSignalOff() {
        onTargetSignal.set(Relay.Value.kOff);
        //  System.out.println("SIGNAL LIGHT OFF");
    }

    public void SpinBallRelease() {
        ballRelease.SpinBallRelease();
    }

    public void StopBallRelease() {
        ballRelease.StopBallRelease();
    }

    private double calcTopSpeed(double distance) {
        return (7.56 /*7.2,7.92*/ + ((distance + getFudgeFactor()) * 0.255 /*0.35,0.16*/));
    }

    private double calcBottomSpeed(double distance) {
        return 27;
    }

    private double getFudgeFactor() {
        double fudge = 0;

        fudge = -joystick.getRawAxis(3);
        fudge = fudge * 3;
        return fudge;
    }

    //--------------------------------------------------------------------------
    //----------------------------- Aligning Code ------------------------------
    //--------------------------------------------------------------------------
    public boolean isAligning() {
        return isAligning;
    }

    public boolean isReadyToShoot() {
        return Math.abs(pidGyro.getError()) < 3;
    }

    public double pidGet() {
        return getGyroAngle();
    }

    /**
     * Enables targeting mode and enables the gyro.
     */
    public void autoAlignStart() {
        isAligning = true;
        imageObject.requestImage(getGyroAngle());
        if (Globals.debugLevel > 0) {
            System.out.println("Targeting ACTIVE - Joystick DISABLED");
        }
    }

    /**
     * Disables the gyro and turns off targeting mode.
     */
    public void autoAlignStop() {
        imageObject.cancelRequest();
        pidGyro.disable();
        isAligning = false;
        turnSignalOff();
        angle = 0;
    }

    public void Align() {
        if (imageObject.isReady()) {
            angle = imageObject.getAngle();
            cameraDistance = imageObject.getDistance();
            if (Globals.debugLevel > 0) {
                System.out.println("Image found - particles: " + imageObject.ParticleCount() + "\tAngle change: " + (getGyroAngle() - angle) + "\tDistance: " + cameraDistance);
            }
        }
        if (angle != 0) {
            pidGyro.setSetpoint(angle);
            pidGyro.enable();
        }
        if (pidGyro.isEnable() && isReadyToShoot()) {
            //imageObject.requestImage(getGyroAngle());
        }
    }

    private synchronized double getGyroAngle() {
        return gyro.getAngle();
    }
    //--------------------------------------------------------------------------
    //-------------------------- End of Aligning Code --------------------------
    //--------------------------------------------------------------------------
}
