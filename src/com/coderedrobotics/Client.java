/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.coderedrobotics;

import com.sun.squawk.io.BufferedReader;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.command.PIDCommand;
import java.io.*;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

public class Client implements Runnable {

    private SocketConnection connection;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private InputStreamReader inputStreamReader;
    private BufferedReader bufferedReader;
    private Thread thread;
    private PIDController pIDController;
    private static int port = 2771;
    private int myport;

    public Client(PIDController pid) {
        myport = port;
        port++;
        pIDController = pid;
        thread = new Thread(this);
        thread.start();
    }

    public void run() {
        reconnect();//initial connection
        while (true) {
            try {
                //System.out.println("sending data");
                pIDController.setPID(
                        dataInputStream.readDouble(), //P//
                        dataInputStream.readDouble(), //I//
                        dataInputStream.readDouble());//D//
                pIDController.setSetpoint(
                        dataInputStream.readDouble());//set setpoint//
                //System.out.println(pIDController.getError());
                //System.out.println(pIDController.get());
                dataOutputStream.writeDouble(
                        pIDController.getError());//send back error//
                dataOutputStream.writeDouble(
                        pIDController.get());//send back output//
                dataOutputStream.flush();
            } catch (IOException ex) {
                reconnect();
            }
        }
    }

    private void reconnect() {
        boolean retry = true;
        while (retry) {
            retry = false;
            try {
                if (dataInputStream != null) {//close if open
                    dataInputStream.close();
                }
                if (connection != null) {//close if open
                    connection.close();
                }
            } catch (IOException ex) {
            }
            connection = null;
            while (connection == null) {//keeps trying to connect           
                try {
                    connection = (SocketConnection) Connector.open(
                            "socket://10.27.71.64:1180", Connector.READ_WRITE);
                } catch (IOException ex) {
                    connection = null;
                }
            }
            //setup the reader and writer objects
            try {
                dataOutputStream = connection.openDataOutputStream();
                dataInputStream = connection.openDataInputStream();
            } catch (IOException ex) {
                retry = true;
            }
        }
    }
}
