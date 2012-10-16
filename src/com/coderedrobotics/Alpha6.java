
/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package com.coderedrobotics;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.camera.AxisCamera;
import edu.wpi.first.wpilibj.image.CriteriaCollection;
import edu.wpi.first.wpilibj.image.NIVision.MeasurementType;

//  Drivers Gamepad
//    Left Bumper - start targeting   (Cancel by driving robot)
//    Right Bumper - Shoot (hold until it shoots) 
//    Left Trigger - Bridge down
//    Right Trigger - Bridge up
//
//  Manipulator Gamepad
//      Button A - belts up
//      Button B - belts reverse
//      
//
//    Analog 4 - automatic shooter speed adjustment - start at 1.6 for no effect
//               adjusts boths shooter wheel speeds
//    
//    Overrides
//       Digital 5 - 12' shooter speed (Manual align and shoot)
//       Digital 6
//       Digital 7
//       Digital 8 - ENABLE TOTALLY MANUAL SHOOTER SPEED USING Analog 1 (does both wheels)
public class Alpha6 extends IterativeRobot {

    AxisCamera myAxisCamera;
    CriteriaCollection cc;
    Gamepad drvGamepad = null;
    Gamepad manGamepad = null;
    Drive drive;
    Bridge bridge;
    Belt belt;
    Shooter myShooter;
    ImageObject myImage;
    boolean targetingActive;

    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
        if (Globals.debugLevel > 0) {
            System.out.println("ROBOT INITIALIZING!");
        }

        try {
            myAxisCamera = AxisCamera.getInstance();  // get an instance ofthe camera
        } catch (Exception ex) {
            System.out.println("CAMERA - FAILED TO INITIALIZE");
        }


        drvGamepad = new Gamepad(Wiring.joystick1Port);
        manGamepad = new Gamepad(Wiring.joystick2Port);
        drive = new Drive(Wiring.leftDriveJag, Wiring.rightDriveJag, drvGamepad);
        bridge = new Bridge();
        belt = new Belt(Wiring.upperBeltJag, Wiring.lowerBeltJag, drvGamepad);
        myImage = new ImageObject(myAxisCamera, drvGamepad);
        myShooter = new Shooter(drive, Wiring.topShooterJag, Wiring.bottomShooterJag, manGamepad, myImage);
        targetingActive = false;

        if (Globals.debugLevel > 0) {
            System.out.println("........INITIALIZED");
        }
    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
        if (myShooter.isAligning() && myShooter.isReadyToShoot()) {
            myShooter.turnSignalOn();
        } else {
            myShooter.turnSignalOff();
        }

        if (myShooter.isAligning()) {
            myShooter.Align();
        }

        if (!myShooter.isAligning()) {
            if (drvGamepad.getDpadX() != 0) {
                drive.setLeft(-drvGamepad.getDpadX() / 1.5);
                drive.setRight(drvGamepad.getDpadX() / 1.5);
            } else {
                drive.setLeft(drvGamepad.getLeftY() * Math.abs(drvGamepad.getLeftY()));
                drive.setRight(drvGamepad.getRightY() * Math.abs(drvGamepad.getRightY()));
            }
        }

        bridge.moveManipulator(drvGamepad.getTriggerAxis());

        if (drvGamepad.getButtonRightAnalogStick() && myShooter.isAligning()) {
            myShooter.autoAlignStop();
        }

        if (manGamepad.getButtonA()) {
            belt.enableBelts();
        } else if (manGamepad.getButtonB()) {
            belt.reverseBelts();
        } else {
            belt.disableBelts();
        }

        if (manGamepad.getRawButton(8)) {
            myShooter.setJoysickFudgeSource();
        } else if (manGamepad.getRawButton(9)) {
            myShooter.setCypressFudgeSource();
        }

        if (drvGamepad.getLeftBumper()) {   // START TARGETING
            myShooter.autoAlignStart();
        }

        if (drvGamepad.getRightBumper()) { // SHOOT A BALL
            myShooter.SpinBallRelease();
        } else {
            myShooter.StopBallRelease();
        }

        DriverStation ds = DriverStation.getInstance();

        if (manGamepad.getRawButton(11)) {
            myShooter.setOverride(6);//max power
        } else if (manGamepad.getRawButton(6)) {//short shot
            myShooter.setOverride(3);//short shot
        } else if (manGamepad.getRawButton(10)) {
            myShooter.setOverride(5);//12'
        } else if (manGamepad.getRawButton(9)) {
            myShooter.setOverride(2);//camera
        } else if (manGamepad.getRawButton(8)) {
            myShooter.setOverride(1);//ulra power
        } else if (manGamepad.getRawButton(7)) {
            myShooter.setOverride(0);//off
        }

        myShooter.setShooterSpeed();

        if (myShooter.isReadyToShoot() || manGamepad.getButtonX()) {
            // myShooter.turnSignalOn();
            //light.setDirection(Relay.Direction.kForward);
            // light.set(Relay.Value.kOn);
            // System.out.println("centered");
        } else {
            // myShooter.turnSignalOff();
            //  light.set(Relay.Value.kOff);
            //  System.out.println("not centered");
        }


//        if (gamepad.getButtonY()) {
//            myImage.SetTargetToBall();
//        } else if (gamepad.getButtonX()) {
//            myImage.SetTargetToBucket();

    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {
        myShooter.setOverride(8);
        myShooter.setShooterSpeed();
        belt.enableBelts();
        myShooter.ballRelease.SpinBallRelease();
    }

    public void teleopInit() {
        myShooter.setOverride(0);
        belt.disableBelts();
        myShooter.ballRelease.StopBallRelease();
    }

    public void disabledInit() {
        myShooter.autoAlignStop();
        myShooter.setOverride(0);
    }
}
