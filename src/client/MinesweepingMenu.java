/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import com.sun.java.swing.plaf.windows.WindowsInternalFrameUI;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import transmission.TransmissionTurn;

public class MinesweepingMenu extends javax.swing.JFrame {

    int sizeMinesweepingGrid = 20;
    int timePerTurn = 5;
    int numberOfMines = 0;
    List<Mine> listMines = new ArrayList<>();//contain the placement of all the mines

    List<Mine> listCellClicked = new ArrayList<>();
    int canPlay = 0;// is it my turn ?
    int changeListPlayer = 0;

    // static    
    int numberOfFlags = 0;

    MineSweeperGrid minesweeperGrid;
    List<Mine> listFlagMine = new ArrayList<>();
    TimerTour timerEndTime;
    Image oneMine, zeroMine, twoMines, treeMines, fourMines, fiveMines, sixMines, sevenMines, eightMines, boom, flag, background;

    //OUT
    ObjectOutputStream outpuStream;
    TransmissionTurn transmissionTurn;
    String pseudo = "";

    /**
     * Create the MineSweepingMenu
     *
     * @param sizeGrid x & y size
     * @param timePerTurn time per turn to do an action
     * @param numberOfMines number of mines in the grid
     * @param listMines pos of the mines
     * @param outputStream communication channel with the server
     * @param pseudo pseudo of the player
     */
    public MinesweepingMenu(int sizeGrid, int timePerTurn, int numberOfMines, List<Mine> listMines, ObjectOutputStream outputStream, String pseudo) {

        this.sizeMinesweepingGrid = sizeGrid;
        this.timePerTurn = timePerTurn;
        this.numberOfMines = numberOfMines;
        this.listMines.addAll(listMines);
        this.outpuStream = outputStream;
        this.pseudo = pseudo;

        this.initComponents();
        initMinesweeperGrid();
        init();
    }

    /*
     Create the minesweeping grid
     */
    public void initMinesweeperGrid() {
        
        //jInternalFrame1.setUI(null);
        minesweeperGrid = new MineSweeperGrid(jInternalFrame1, sizeMinesweepingGrid, this);
        //Get the title of our internalFrame
        BasicInternalFrameTitlePane titlePane = (BasicInternalFrameTitlePane) ((BasicInternalFrameUI) jInternalFrame1.getUI()).getNorthPane();
        //Remove the not really pretty title ...
        jInternalFrame1.remove(titlePane);
        jInternalFrame1.repaint();
        
    }

    /**
     * Called when left clicking on a Cell
     * 2 possibilities : 
     * -Cell already revealed, we don't do anything
     * -Cell isn't revealed yet, we reveal it and take the relevant action (mine/0/1-9)
     *
     * @param x horizontal pos of the Cell
     * @param y vertical pos of the Cell
     */
    public void clickOnCell(int x, int y) {
        /*
        This function is called in MinesweeperGrid, with have the x and y pos of the clicked cell, we can then compare with our listMines to check the dangerousness of the cell/if it exploded
        */

        Mine mine = new Mine(x, y);
        Iterator iteratorClickedCell = listCellClicked.iterator();
        Iterator iteratorListMines = listFlagMine.iterator();
        int found = 0;
        while (iteratorClickedCell.hasNext()) {
            Mine tempo = (Mine) iteratorClickedCell.next();
            //if our cell is already revealed, we don't do anything
            if (tempo.equals(mine)) {
                found = 1;
            }
        }
        while (iteratorListMines.hasNext()) {
            Mine tempo = (Mine) iteratorListMines.next();
            //if our cell is already "flagged", then a left click dooesn't do anything
            if (tempo.equals(mine)) {
                found = 1;
            }
        }
        if (found == 0) {
            //we reveal and take the appropriate action
            for (CellGrid myCell : minesweeperGrid.GridCells) {
                if (myCell.getX() == x) {
                    if (myCell.getY() == y) {
                        mine.setIndicex(x);
                        mine.setIndicey(y);
                        if (canPlay == 0) {
                            listCellClicked.add(mine);
                            //we put the relevant image on the cell, depending on the dangerousness
                            setDangerosityImage(myCell.getCellGrid(), DangerosityCell(x, y));
                        }
                        if (canPlay == 1) {
                            if (DangerosityCell(x, y) == 10) {
                                JOptionPane d = new JOptionPane();
                                String message = "You clicked on a mine !! Goodbye.";
                                d.showMessageDialog(this, message, "BOOM", JOptionPane.WARNING_MESSAGE);
                                this.dispose();
                            } else {
                                // send the click data
                                transmissionTurn = new TransmissionTurn("clickOnCell", mine, null);
                                sendMessage(transmissionTurn);
                            }
                        }

                    }
                }
            }
            //if we flagged as much cell as there are mines, we check for victory
            if (numberOfFlags == numberOfMines) {
                if (listCellClicked.size() == (sizeMinesweepingGrid * sizeMinesweepingGrid - numberOfMines)) {
                    checkIfVictory();
                }
            }
        }
    }

    /**
     * Handle the right click on a cell, so either setting or removing a flag
     * 3 possibilities :
     *  -cell already revealed, we don't do anything, doesn't count as an action
     *  -cell isn't revealed and isn't flagged, we flag it
     *  -cell isn't revealed and is flagged, we remove the flag
     * @param x x pos of the cell
     * @param y y pos of the cell
     */
    public void rightClickOnCell(int x, int y) {
        Mine clickedCell = new Mine(x, y);
        Iterator iteratorFlaggedCell = listFlagMine.iterator();
        Iterator iteratorClickedCell = listCellClicked.iterator();
        int found = 0;
        int alreadyRevealed = 0;
        while (iteratorClickedCell.hasNext()) {
            Mine tempo2 = (Mine) iteratorClickedCell.next();
            if (tempo2.equals(clickedCell)) {
                alreadyRevealed = 1;
            }
        }

        if (alreadyRevealed == 0) {
            while (iteratorFlaggedCell.hasNext()) {
                Mine tempo = (Mine) iteratorFlaggedCell.next();
                //if our cell is already flagged then we remove the flag (and the cell from the flag list)
                if (tempo.equals(clickedCell)) {
                    found = 1;
                    if (canPlay == 0) {
                        iteratorFlaggedCell.remove();
                    }
                    for (CellGrid cg : minesweeperGrid.GridCells) {
                        if (cg.getX() == x) {
                            if (cg.getY() == y) {
                                //We are on the right cell
                                //We put back the default background
                                cg.getCellGrid().setImage(background);
                                cg.getCellGrid().repaint();
                                if (canPlay == 1) {
                                    //we remove a flag
                                    transmissionTurn = new TransmissionTurn("removeFlag", tempo, null);
                                    sendMessage(transmissionTurn);
                                } else {
                                    numberOfFlags--;
                                    jLabel9.setText(String.valueOf(listFlagMine.size()));
                                    //Check if the grid is complete
                                    if (numberOfFlags == numberOfMines) {
                                        if (listCellClicked.size() == (sizeMinesweepingGrid * sizeMinesweepingGrid - numberOfMines)) {
                                            checkIfVictory();
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }

            if (found == 0) {
                //we put a flag and add the cell to the flag list
                if (canPlay == 0) {
                    listFlagMine.add(clickedCell);
                }
                for (CellGrid cg : minesweeperGrid.GridCells) {
                    if (cg.getX() == x) {
                        if (cg.getY() == y) {
                            //On est sur la bonne case
                            cg.getCellGrid().setImage(flag);
                            cg.getCellGrid().repaint();
                            //on ajoute un flag
                            if (canPlay == 1) {
                                transmissionTurn = new TransmissionTurn("addFlag", new Mine(cg.getX(), cg.getY()), null);
                                sendMessage(transmissionTurn);
                            } else {
                                numberOfFlags++;
                                jLabel9.setText(String.valueOf(listFlagMine.size()));
                                if (numberOfFlags == numberOfMines) {
                                    if (listCellClicked.size() == (sizeMinesweepingGrid * sizeMinesweepingGrid - numberOfMines)) {
                                        checkIfVictory();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * retrieve all the cell already revealed
     * @param revealedCells list of all the revealed cells
     */
    public void CaseRevelee(List<Mine> revealedCells) {
     /*
        analyse all cells for all revealed cells
        is used when the server himself reveal new cells (when revealing a "0" dangerousness cell) without a player click
     */
        if (!revealedCells.isEmpty()) {
            for (Mine mn : revealedCells) {
                //for each of our cell we'll color
                for (CellGrid cg : minesweeperGrid.GridCells) {
                    if (cg.getX() == mn.getIndicex()) {
                        if (cg.getY() == mn.getIndicey()) {
                            //we are on the right cell
                            setDangerosityImage(cg.getCellGrid(), DangerosityCell(mn.getIndicex(), mn.getIndicey()));
                        }
                    }
                }
            }
        }
    }

/**
 * Leave/surrender the game
 */
    public void Abandonner() {
        this.dispose();
    }

/**
 * return the number of mines adjacent to the cell, if the cell itself is a mine return 10
 * @param x horizontal pos of the cell
 * @param y vertical pos of the cell
 * @return the number of mines adjacent to the cell, if the cell itself is a mine return 10
 */
    public int DangerosityCell(int x, int y) {
            
        int dangerousness = 0;
        if (!listMines.isEmpty()) {

            for (Mine mn : listMines) {
                if (mn.getIndicex() == x) {
                    //same column
                    if (mn.getIndicey() == y) {
                        //BOOM, the cell is a mine
                        return 10;
                    }

                    if (mn.getIndicey() == y + 1) {
                        //mine under our cell
                        dangerousness++;
                    }
                    if (mn.getIndicey() == y - 1) {
                        //mine above our cell
                        dangerousness++;
                    }
                }
                if (mn.getIndicey() == y) {
                    //same column
                    if (mn.getIndicex() == x + 1) {
                        //mine on the left of our cell
                        dangerousness++;
                    }
                    if (mn.getIndicex() == x - 1) {
                        //mine on the right of our cell
                        dangerousness++;
                    }
                }
                if (mn.getIndicex() == x - 1) {
                    if (mn.getIndicey() == y + 1) {
                        //mine on the bottom left corner of our cell
                        dangerousness++;
                    }
                    if (mn.getIndicey() == y - 1) {
                        //mine on the upper left corner of our cell
                        dangerousness++;
                    }
                }
                if (mn.getIndicex() == x + 1) {
                    if (mn.getIndicey() == y + 1) {
                        //mine on the bottom right corner of our cell
                        dangerousness++;
                    }
                    if (mn.getIndicey() == y - 1) {
                        //mine on the upper right corner of our cell
                        dangerousness++;
                    }
                }
            }
            return dangerousness;

        }
        return dangerousness;
    }


    /**
     * depending on the dangerousness of the cell, set the corresponding image
     * if the dangerousness is 0, we set the image and try a reveal for every adjacent cell
     * @param cell the jPanel corresponding to our cell
     * @param dangerousness the dangerousness of the cell
     */
    public void setDangerosityImage(client.MineSweeperGrid.CellPane cell, int dangerousness) {
        if (dangerousness == 0) {
            //On effectue un clicSurCase des 8 cases adjacentes
            cell.setImage(zeroMine);
            clickOnCell(cell.xpos - 1, cell.ypos - 1);
            clickOnCell(cell.xpos - 1, cell.ypos);
            clickOnCell(cell.xpos - 1, cell.ypos + 1);
            clickOnCell(cell.xpos, cell.ypos - 1);
            clickOnCell(cell.xpos, cell.ypos + 1);
            clickOnCell(cell.xpos + 1, cell.ypos - 1);
            clickOnCell(cell.xpos + 1, cell.ypos);
            clickOnCell(cell.xpos + 1, cell.ypos + 1);

        }
        if (dangerousness == 1) {
            cell.setImage(oneMine);
        }
        if (dangerousness == 2) {
            cell.setImage(twoMines);
        }
        if (dangerousness == 3) {
            cell.setImage(treeMines);
        }
        if (dangerousness == 4) {
            cell.setImage(fourMines);
        }
        if (dangerousness == 5) {
            cell.setImage(fiveMines);
        }
        if (dangerousness == 6) {
            cell.setImage(sixMines);
        }
        if (dangerousness == 7) {
            cell.setImage(sevenMines);
        }
        if (dangerousness == 8) {
            cell.setImage(eightMines);
        }
        if (dangerousness == 10) {
            cell.setImage(boom);
        }

    }


    /**     
     * We are in a config where it is possible to win, we check if everything is correct or not
     */
    public void checkIfVictory() {
        /*
         We check that each mine is in the flag list (and we know that numberOfMines == numberOfFlags)
         */
        Iterator iteratorMine = listMines.iterator();
        int foundFinal = 1;
        while (iteratorMine.hasNext()) {
            Mine tempo = (Mine) iteratorMine.next();
            int found = 0;
            for (Mine mine : listFlagMine) {
                if (mine.equals(tempo)) {
                    found = 1;
                }
            }
            if (found == 0) {
                foundFinal = 0;
            }
        }
        //if a mine hasn't been found, there's a problem
        if (foundFinal == 0) {
            return;
        }
        //else, victory is assured !
        jButton1.setEnabled(false);
        // POPUP
        JOptionPane d = new JOptionPane();
        String message = "You have won !!";
        d.showMessageDialog(this, message, "Congratz", JOptionPane.INFORMATION_MESSAGE);
        this.dispose();

    }

   
    /**
     * This function is done server side, it is only here as a legacy for testing purpose
     * We randomly generate the appropriate number of mines, all at unique spots
     * @param nbmine number of mines to create
     */
    public void generationmines(int nbmine) {
        int xmine;
        int ymine;
        for (int i = 0; i < nbmine; i++) {
            if (!listMines.isEmpty()) {
                int found = 0;
                xmine = ThreadLocalRandom.current().nextInt(0, sizeMinesweepingGrid);
                ymine = ThreadLocalRandom.current().nextInt(0, sizeMinesweepingGrid);
                Mine mine = new Mine(100, 100);
                mine.setIndicex(xmine);
                mine.setIndicey(ymine);
                Iterator iteratormines = listMines.iterator();

                //we check that our random mine doesn't already exist
                while (iteratormines.hasNext() && found == 0) {
                    Mine tempo = (Mine) iteratormines.next();
                    if (mine.equals(tempo)) {
                        found = 1;
                    }
                }

                //if it doesn't exist, we add it
                if (found == 0) {
                    listMines.add(mine);
                } else {
                    //if it doesn't, we don't add it, and we don't forget to decrement our i, because it didn't do anything relevant
                    i--;
                }
            } else {
                //if we don't have any mine yet, we add it straight up
                xmine = ThreadLocalRandom.current().nextInt(0, sizeMinesweepingGrid);
                ymine = ThreadLocalRandom.current().nextInt(0, sizeMinesweepingGrid);
                Mine mine = new Mine(100, 100);
                mine.setIndicex(xmine);
                mine.setIndicey(ymine);
                listMines.add(mine);
            }
        }
    }

    /**
     * restart our turn timer
     */
    public void startTimer() {
        //On paramètre notre timer de tour
        timerEndTime.timeLeft = timePerTurn;
        timerEndTime.countdown.restart();

    }

    /**
     * We load the images corresponding to each dangerousness (+ flag)
     * We init our turn timer
     */
    public void init() {

        oneMine = new ImageIcon(this.getClass().getResource("/images/1.jpg")).getImage();
        zeroMine = new ImageIcon(this.getClass().getResource("/images/0.jpg")).getImage();
        twoMines = new ImageIcon(this.getClass().getResource("/images/2.jpg")).getImage();
        treeMines = new ImageIcon(this.getClass().getResource("/images/3.jpg")).getImage();
        fourMines = new ImageIcon(this.getClass().getResource("/images/4.jpg")).getImage();
        fiveMines = new ImageIcon(this.getClass().getResource("/images/5.jpg")).getImage();
        sixMines = new ImageIcon(this.getClass().getResource("/images/6.jpg")).getImage();
        sevenMines = new ImageIcon(this.getClass().getResource("/images/7.jpg")).getImage();
        eightMines = new ImageIcon(this.getClass().getResource("/images/8.jpg")).getImage();
        boom = new ImageIcon(this.getClass().getResource("/images/x.jpg")).getImage();
        flag = new ImageIcon(this.getClass().getResource("/images/drapeau.jpg")).getImage();
        background = new ImageIcon(this.getClass().getResource("/images/fond.jpg")).getImage();

        
        timerEndTime = new TimerTour(jLabel5, this);
        timerEndTime.setTimeLeft(timePerTurn);
        timerEndTime.countdown.stop();

        jLabel8.setText(String.valueOf(numberOfMines));
        jLabel9.setText(String.valueOf(listFlagMine.size()));

    }

    /**
     * Retourn the list of players still in the game
     * @return the list of players still in the game
     */
    public JList getjList1() {
        return jList1;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jInternalFrame1 = new javax.swing.JInternalFrame();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jInternalFrame1.setBorder(null);
        jInternalFrame1.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        jInternalFrame1.setTitle("MineSweeping grid, be carefull !");
        jInternalFrame1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jInternalFrame1.setFocusable(false);
        jInternalFrame1.setPreferredSize(new java.awt.Dimension(400, 400));
        jInternalFrame1.setVisible(true);

        javax.swing.GroupLayout jInternalFrame1Layout = new javax.swing.GroupLayout(jInternalFrame1.getContentPane());
        jInternalFrame1.getContentPane().setLayout(jInternalFrame1Layout);
        jInternalFrame1Layout.setHorizontalGroup(
            jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jInternalFrame1Layout.setVerticalGroup(
            jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 367, Short.MAX_VALUE)
        );

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel1.setText("Time Left :");

        jList1.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        jList1.setFocusable(false);
        jList1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jList1MouseClicked(evt);
            }
        });
        jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList1ValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        jLabel2.setText("Players Left :");

        jLabel3.setText("Data Mines");

        jButton1.setText("Surrender");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N

        jLabel6.setText("Total number of Mines :");

        jLabel7.setText("Number of Mines flagged :");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(90, 90, 90)
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel5))
                    .addComponent(jInternalFrame1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jInternalFrame1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 69, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(23, 23, 23)
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel8)
                .addGap(12, 12, 12)
                .addComponent(jLabel7)
                .addGap(18, 18, 18)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        Abandonner();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jList1ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList1ValueChanged

    }//GEN-LAST:event_jList1ValueChanged

    private void jList1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jList1MouseClicked

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        // TODO add your handling code here:
        transmissionTurn = new TransmissionTurn("mine", pseudo, null);
        sendMessage(transmissionTurn);
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        /* tt = new TransmissionTour("mine", pseudo, null);
         envoyerMessage(tt);*/
    }//GEN-LAST:event_formWindowClosing

    /**
     * Communication with the server, called after a click or at the end of our turn timer
     * once the communication started, we disable the right to take actions
     * @param message 
     */
    public void sendMessage(Object message) {
        canPlay = 0;
        timerEndTime.countdown.stop();
        jLabel5.setText("");
        try {
            outpuStream.writeObject(message);
            outpuStream.reset();
        } catch (IOException ex) {
            Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MinesweepingMenu.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MinesweepingMenu.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MinesweepingMenu.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MinesweepingMenu.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MinesweepingMenu(0, 0, 0, null, null, null).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JInternalFrame jInternalFrame1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
