/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.Timer;
import transmission.TransmissionTurn;

public class TimerTour {

    private JLabel label;
    Timer countdown;
    int timeLeft = 10;
    MinesweepingMenu minesweepingMenu;

    /**
     * change the time of our timer
     * @param tempsRestant the time of our timer in second
     */
    public void setTimeLeft(int tempsRestant) {
        this.timeLeft = tempsRestant;
    }

    /**
     * Initialise un timerTour
     * @param labelTimeLeft the label showing the time left
     * @param mainWindow the MinesweepingMenu
     */
    public TimerTour(JLabel labelTimeLeft, final MinesweepingMenu mainWindow) {
        countdown = new Timer(1000, (ActionListener) new CountdownTimerListener());
        this.label = labelTimeLeft;
        this.minesweepingMenu = mainWindow;
    }

    
    class CountdownTimerListener implements ActionListener {

        /**
         * Called every second, decrement the time left
         * if the timer reach 0, stop the countdown and send the corresponding message to the server
         * @param e 
         */
        public void actionPerformed(ActionEvent e) {
            if (--timeLeft > 0) {
                label.setText(String.valueOf(timeLeft));
            } else {
                label.setText("out of time !");
                minesweepingMenu.canPlay = 0;
                countdown.stop();
                TransmissionTurn transmissionTurn = new TransmissionTurn("timerStop", null, null);
                minesweepingMenu.sendMessage(transmissionTurn);
            }
        }
    }

}
