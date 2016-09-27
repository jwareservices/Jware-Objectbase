package org.jware.objectbase.util;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * File: PortScanner.java Created On: 03/00/2015
 *
 * @author J. Paul Jackson <jwareservices@gmail.com>
 *
 * Purpose:
 */
import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

class PortScanner {

    public static void main(String[] args) {
        for (int port = 1; port <= 65535; port++) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress("localhost", port), 1000);
                System.out.println("Port: " + port + " is open");
            } catch (IOException ex) {
                 Logger.getLogger(PortScanner.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
