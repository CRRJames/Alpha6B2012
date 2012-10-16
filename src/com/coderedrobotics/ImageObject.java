package com.coderedrobotics;

import edu.wpi.first.wpilibj.camera.AxisCamera;
import edu.wpi.first.wpilibj.camera.AxisCameraException;
import edu.wpi.first.wpilibj.image.BinaryImage;
import edu.wpi.first.wpilibj.image.ColorImage;
import edu.wpi.first.wpilibj.image.CriteriaCollection;
import edu.wpi.first.wpilibj.image.NIVision.MeasurementType;
import edu.wpi.first.wpilibj.image.NIVisionException;
import edu.wpi.first.wpilibj.image.ParticleAnalysisReport;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class ImageObject implements Runnable {

    AxisCamera myCamera;
    Servo servoCameraPan;
    Servo servoCameraTilt;
    CriteriaCollection cc;
    ParticleAnalysisReport[] particles;
    Thread thread;
    Gamepad gamepad;
    Filter filter;
    int targetType; // BUCKET or BALL
    static int TARGET_IS_BUCKET = 1;
    static int TARGET_IS_BALL = 2;
    long imageAcquisitionMS; // milliseconds to acquire and process image
    private boolean getingImage = false;
    private boolean imageReady = false;
    private double distance;
    private double angle;
    private double gyroReading;

    public ImageObject(AxisCamera camera, Gamepad g) {
        thread = new Thread(this);
        gamepad = g;
        myCamera = camera;
        servoCameraPan = new Servo(Wiring.cameraPanServo);
        servoCameraTilt = new Servo(Wiring.cameraTiltServo);
        cc = new CriteriaCollection();      // create the criteria for the particle filter
        cc.addCriteria(MeasurementType.IMAQ_MT_BOUNDING_RECT_WIDTH, 30, 400, false);
        cc.addCriteria(MeasurementType.IMAQ_MT_BOUNDING_RECT_HEIGHT, 40, 400, false);
        targetType = TARGET_IS_BUCKET;
        thread.start();
        if (Globals.debugLevel > 0) {
            System.out.println("IMAGE THREAD STARTED " + thread.toString());
        }
    }

    public void run() {
        while (true) {

            if (getingImage) {
                SetTargetToBucket();
                if (Globals.debugLevel > 0) {
                    System.out.println("getting image");
                }
                GetImage();
                //  System.out.println("Image Acquisition Time: " + GetAcquisitionTime());
                if (ParticleCount() > 0) {

                    //  PrintParticles();

                    double offset = GetOffsetPercent();
                    offset = offset * 20;
                    double angle = gyroReading + offset; // added the .5 on 2/29 to adjust for camera skew
                    //    System.out.println("Target Gyro: " + angle);System.out.println("Distance: " + GetDistance());
                    this.angle = angle;
                    distance = GetDistance();

                    if (Globals.debugLevel > 0) {
                        System.out.println("Target Gyro: " + angle + "  Current Gyro: " + gyroReading + "  Distance: " + GetDistance());
                    }

                    getingImage = false;
                    imageReady = true;
                }
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void GetImage() {

        if (myCamera != null) {

            ColorImage image = null;
            imageAcquisitionMS = 0;
            long startTime = 0;
            long endTime = 0;

            startTime = System.currentTimeMillis();

            try {
                image = myCamera.getImage();
            } catch (AxisCameraException ex) {
                ex.printStackTrace();
            } catch (NIVisionException ex) {
                ex.printStackTrace();
            }

            try {
                //System.out.println("applying threshold");
                Thread.yield();
                BinaryImage thresholdImage = image.thresholdRGB(25, 95, 0, 24, 0, 34);   // keep only red objects
//                BinaryImage thresholdImage = image.thresholdRGB(0, 108, 0, 78, 0, 78);   // keep only red objects
                Thread.yield();
                //System.out.println("applying convexHull");
                BinaryImage convexHullImage = thresholdImage.convexHull(false);          // fill in rectangles with color
                Thread.yield();
                //System.out.println("applying another convexHull");
                BinaryImage bigObjectsImage = convexHullImage.removeSmallObjects(false, 3);  // remove small artifacts (2 iterations)
                Thread.yield();
                //System.out.println("applying bigObjects");
                BinaryImage filteredImage = bigObjectsImage.particleFilter(cc);
                Thread.yield();

                particles = filteredImage.getOrderedParticleAnalysisReports();  // get list of "particles" (image objects found)
                endTime = System.currentTimeMillis();

                imageAcquisitionMS = endTime - startTime;

                //System.out.println("particles found: " + particles.length);

                // Sort the particles so the one highest in the image is first
                ParticleAnalysisReport temp;
                if (particles.length > 0) {
                    for (int i = 0; i < particles.length - 1; ++i) {
                        for (int j = i + 1; j < particles.length; ++j) {
                            if (particles[i].boundingRectTop > particles[j].boundingRectTop) {
                                temp = particles[i];
                                particles[i] = particles[j];
                                particles[j] = temp;
                            }
                        }
                    }
                }
                Thread.yield();

                filteredImage.free();
                convexHullImage.free();
                bigObjectsImage.free();
                thresholdImage.free();
                image.free();
            } catch (NIVisionException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void requestImage(double gyroAngle) {
        gyroReading = gyroAngle;
        imageReady = false;
        getingImage = true;
    }

    public void cancelRequest() {
        getingImage = false;
    }

    public double getAngle() {
        imageReady = false;
        return angle;
    }

    public double getDistance() {
        imageReady = false;
        return distance;
    }

    public boolean isReady() {
        return imageReady;
    }

    public void SetTargetToBall() {
        targetType = TARGET_IS_BALL;
        servoCameraPan.set(.5);
        servoCameraTilt.set(.5);

        //  System.out.println("TARGET SET TO BALL: TiltServo: " + servoCameraTilt.get() + "   PAN: " + servoCameraPan.get());

    }

    public void SetTargetToBucket() {
        targetType = TARGET_IS_BUCKET;
        servoCameraPan.set(.5);
        servoCameraTilt.set(.5);
        //   System.out.println("TARGET SET TO BUCKET: TiltServo: " + servoCameraTilt.get() + "   PAN: " + servoCameraPan.get());
    }

    private double GetDistance() {
        double DISTANCE_FUDGE_FACTOR = -1;

        double dst = 0;
        if (GetMaxHeight() != 0) {
            // le original fancy formula
            dst = (((326.04 - (1.6 * GetMaxHeight())) / 12) + DISTANCE_FUDGE_FACTOR); //calculating inches, returning feet - Fudge factor is a manual refinement
        }
        return dst;
    }

    public long GetAcquisitionTime() {
        return imageAcquisitionMS;
    }

    public int GetHeight() {
        return particles[0].boundingRectHeight;
    }

    public int GetHeight(int idx) {
        if (idx < 0) {
            return 0;
        } else {
            return particles[idx].boundingRectHeight;
        }
    }

    public int GetMaxHeight() {
        int maxHeight = 0;
        if (particles.length > 0) {
            for (int i = 0; i < particles.length; i++) {
                if (particles[i].boundingRectHeight > maxHeight) {
                    maxHeight = particles[i].boundingRectHeight;
                }
            }
        }
        return maxHeight;
    }

    public int GetWidth() {
        return particles[0].boundingRectWidth;
    }

    public int GetWidth(int idx) {
        if (idx < 0) {
            return 0;
        } else {
            return particles[idx].boundingRectWidth;
        }
    }

    public double GetXPos() {
        return particles[0].boundingRectLeft;
    }

    public double GetXPos(int idx) {
        if (idx < 0) {
            return 0;
        } else {
            return particles[idx].boundingRectLeft;
        }
    }

    public int ParticleCount() {
        return particles.length;
    }

    public int GetParticleMidPoint() {
        return particles[0].boundingRectLeft + (particles[0].boundingRectWidth / 2);
    }

    public int GetParticleMidPoint(int idx) {
        if (idx < 0) {
            return 0;
        } else {
            return particles[idx].boundingRectLeft + (particles[idx].boundingRectWidth / 2);
        }
    }

    public int GetImageWidth() {
        return particles[0].imageWidth;
    }

    public int GetImageWidth(int idx) {
        if (idx < 0) {
            return 0;
        } else {
            return particles[idx].imageWidth;
        }
    }

    public int GetParticleOffset() {
        return GetParticleMidPoint() - (particles[0].imageWidth / 2);
    }

    public int GetParticleOffset(int idx) {
        return GetParticleMidPoint(idx) - (particles[idx].imageWidth / 2);
    }

    public double GetOffsetPercent() {
        return ((double) GetParticleOffset()) / ((double) GetImageWidth() / 2);
    }

    public double GetOffsetPercent(int idx) {
        return ((double) GetParticleOffset(idx)) / ((double) GetImageWidth(idx) / 2);
    }

    public void PrintParticles() {
        if (ParticleCount() > 0) {
            for (int i = 0; i < ParticleCount(); i++) {
                System.out.println("Particular: " + i);
                System.out.println("X: " + particles[i].boundingRectLeft);
                System.out.println("Y: " + particles[i].boundingRectTop);
                System.out.println("H: " + particles[i].boundingRectHeight);
                System.out.println("AREA: " + particles[i].particleArea);
                System.out.println("MAX PART HEIGHT: " + GetMaxHeight());
            }
        }
    }

    private class Filter {

        double val = 0;
        double weight = 0;

        public void filter(double input) {
            val = ((val * weight) + input) / (weight + 1);
            weight += 1;
        }

        public double get() {
            return val;
        }
    }
}
// public double pidGet() {
//    double offset;
//     GetImage();
//     System.out.println("Image Acquisition Time: " + GetAcquisitionTime());
//     if (ParticleCount() > 0) {
//         PrintParticles();
//         System.out.println("Midpoint of target: " + GetParticleMidPoint(0));
// System.out.println("Height of 0: " + GetHeight(BigPartIdx()));
// System.out.println("Width of 0: " + GetWidth(BigPartIdx()));
// System.out.println("MidPoint of 0: " + GetParticleMidPoint());
// System.out.println("Offset from Center: " + GetParticleOffset());
// System.out.println("Distance: " + GetDistance());
//         offset = GetOffsetPercent(0);
//         System.out.println("PID Get - TURN PERCENT: " + offset);
//     } else {
//         offset = 0;
//         System.out.println("NO PARTICLES FOUND");
//    }
//     return offset;
   // }