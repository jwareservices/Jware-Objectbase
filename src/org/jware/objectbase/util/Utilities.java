package org.jware.objectbase.util;

/*
 * Copyright (C) 2014 J. Paul Jackson <jwareservices@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.PrintStream;
import java.util.Random;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * File: Utilities.java Created On: 05/00/2014
 *
 * @author J. Paul Jackson <jwareservices@gmail.com>
 *
 * Purpose: To provide a set of general utility features for the library.
 */
public final class Utilities {

    /**
     * Most methods called via static reference except Clock. To gain access to
     * the Clock class use: Utility.getTimer();
     */
    public final static Utilities Utility = new Utilities();

    public final static PrintStream out = System.out;


    static final boolean DEBUG = false;

    public static final Logger getLogger(final String loggerClass) {
        return Logger.getLogger(loggerClass);
    }

     /**
     * Print to standard system out.
     *
     * @param msg Message to be printed.
     */
    public final static void printMsg(final String msg) {
        out.println(msg);
    }

     /**************************************************************************
                               Swing alertDialog handling
     * @param msg
     * @param title
    **************************************************************************/
 
    public final static void alertDialog(String msg, String title) {
        Object[] options = {"OK"};
        JOptionPane.showOptionDialog(null, msg, title,
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, options, options[0]);
    }

    /**
     *
     * @param msg
     * @param title
     * @return
     */
    public final static boolean confirmDialog(String msg, String title) {
        return JOptionPane.showConfirmDialog(null, msg, title,
                JOptionPane.YES_NO_OPTION) == JFileChooser.APPROVE_OPTION;
    }
    
    /**************************************************************************
                               Exception handling
    **************************************************************************/

    /**
     * @param obj
     * @param msg
     */
    public final static void assertNotNull(final Object obj, final String msg) throws IllegalArgumentException {
        if (null == obj) {
            throw new IllegalArgumentException(msg);
        }
    }
    
    /**
     * @param obj
     * @param msg
     */
     public final static void assertNull(final Object obj, final String msg) throws IllegalArgumentException {
        if (null != obj) {
            throw new IllegalArgumentException(msg);
        }
    }
    // End exception handling.
    
    /**
     * A singleton clock to used for timing sequences.
     */
    public static final class Clock {

        long startTime = 0;

        private Clock() {
        }

        /**
         * Current time.
         */
        public final void startTime() {
            startTime = System.currentTimeMillis();
        }

        /**
         * How long have we been running in seconds.
         *
         * @return Length of time in seconds since start.
         */
        public final long step() {
            return ((System.currentTimeMillis() - startTime));
        }
    }

    /**
     *
     * @return The timer.
     */
    public final Clock getTimer() {
        return new Clock();
    }
    // Clock end.

    
    /**************************************************************************
                                    Math Stuff
    **************************************************************************/

    /**
     * Find the polar coordinates for the ending points of a vector starting at
     * 0 ,0.
     *
     * @param radius
     * @param theta
     * @return
     */
    public static final float[] getPolarCoorindates(final int radius, final float theta) {

        final float[] points = new float[2];
        points[0] = (float) (radius * Math.cos(theta));
        points[1] = (float) (radius * Math.sin(theta));

        return points;
    }

    public static final int byteToUnsignedInt(final byte b) {
        return (b >= 0) ? ((int) b) : ((int) (256 + b));
    }

    public static final int fourBytesToInt(final byte b[], int offset) {
        int value;

        value = byteToUnsignedInt(b[offset++]);
        value |= (byteToUnsignedInt(b[offset++]) << 8);
        value |= (byteToUnsignedInt(b[offset++]) << 16);
        value |= (byteToUnsignedInt(b[offset++]) << 24);

        return (value);
    }

    public static final void intToFourBytes(final int iValue, final byte b[], int offset) {
        b[offset++] = (byte) ((iValue) & 0xff);
        b[offset++] = (byte) ((iValue >>> 8) & 0xff);
        b[offset++] = (byte) ((iValue >>> 16) & 0xff);
        b[offset++] = (byte) ((iValue >>> 24) & 0xff);
    }

    /**************************************************************************
                                Random Stuff
    **************************************************************************/
    
    final static Random r = new Random();

    public final static int getRandom(final int aStart, final int aEnd) {
        //get the range, casting to long to avoids overflow problems
        final long range = (long) aEnd - (long) aStart + 1;
        // compute a fraction of the range, 0 <= frac < range
        final long fraction = (long) (range * r.nextDouble());
        return (int) (fraction + aStart);
    }

    public final static int getNextRandom(final int limit) {
        return r.nextInt(limit);
    }

    public final static int getRandomBetween(final int lowerBound, final int upperBound) {
        return (int) (Math.random() * upperBound) + lowerBound;
    }

    public final static char getRandomCharacter(){
        final char[] chars = {'a','b','c','d','e','f','g','h','i','j',
                              'k','l','m','n','o','p','q','r','s','t',
                              'u','v','w','x','y','z'};
        return chars[getNextRandom(26)];
    }
    
    public final static String getRandomString(final int size) {
        final StringBuilder randomString = new StringBuilder(size);
        for(int i=0; i<size;i++){
            randomString.append(getRandomCharacter());
        }
        return randomString.toString();
    }
    
    
    //***********************  The RGB stuff will likely move.
    
    public final RGB getRGB(final double r, final double g, final double b) {
        return new RGB (r, g, b);
    }
    
    public final class RGB {

        double r, g, b;

        public RGB(final double r, final double g, final double b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        /**
         * Really need to implement the alpha channel too.
         * @param rgb 
         */
        public RGB(final int rgb) {
            r = (double) (rgb >> 16 & 0xff) / 255;
            g = (double) (rgb >> 8 & 0xff) / 255;
            b = (double) (rgb & 0xff) / 255;
        }

        public final void scale(final double scale) {
            r *= scale;
            g *= scale;
            b *= scale;
        }

        public final void add(final RGB texel) {
            r += texel.r;
            g += texel.g;
            b += texel.b;
        }

        public final int toRGB() {
            return 0xff000000 | (int) (r * 255.99) << 16
                    | (int) (g * 255.99) << 8 | (int) (b * 255.99);
        }
    }
    
    /*
    public static void main(String[] args) {
        System.out.println(getRandomString(200));
    }
    */
}


/*
    public static final Color BLACK = Color.BLACK;
    public static final Color BLUE = Color.BLUE;
    public static final Color CYAN = Color.CYAN;
    public static final Color DARK_GRAY = Color.DARK_GRAY;
    public static final Color GRAY = Color.GRAY;
    public static final Color GREEN = Color.GREEN;
    public static final Color LIGHT_GRAY = Color.LIGHT_GRAY;
    public static final Color MAGENTA = Color.MAGENTA;
    public static final Color ORANGE = Color.ORANGE;
    public static final Color PINK = Color.PINK;
    public static final Color RED = Color.RED;
    public static final Color WHITE = Color.WHITE;
    public static final Color YELLOW = Color.YELLOW;

*/