package org.jware.objectbase.util;

import java.awt.Toolkit;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingWorker;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * File: WaitCountDownTimerTask.java Created On: 25/00/2015
 *
 * @author J. Paul Jackson <jwareservices@gmail.com>
 *
 * Purpose:
 */
public class WaitCountDownTimerTask extends SwingWorker<List, Integer> {

    int currentTime;
    int timeIn;
    int timeOut;
    List lst = new ArrayList();

    public WaitCountDownTimerTask(LocalDate dt) {
        LocalDate ld = dt;
    }
    /*
     * Main task. Executed in background thread.
     */

    @Override
    public List<Integer> doInBackground() {
        while (!isCancelled()) {
            publish(timeOut);
            setProgress(timeOut);
        }
        return lst;
    }

    @Override
    protected void process(List<Integer> chunks) {
        for (int number : chunks) {
//             textArea.append(number + "\n");
        }
    }
    /*
     * Executed in event dispatching thread
     */

    @Override
    public void done() {
        Toolkit.getDefaultToolkit().beep();
        //           setCursor(null); //turn off the wait cursor
    }
}
