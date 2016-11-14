/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import transmission.DataGameCreation;
import transmission.ListAvailableGames;
import transmission.ListPseudo;
import transmission.TransmissionTurn;

/*Thread et a comme attributs un JTextArea
 et un BufferedReader.*/
public class ListenerWaitingRoom extends Thread {

    private WaitingRoom waitingRoom;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private MinesweepingMenu minesweepingMenu;

    public ListenerWaitingRoom(WaitingRoom waitingRoom, ObjectInputStream inputStream, ObjectOutputStream outputStream) {
        this.waitingRoom = waitingRoom;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    @Override
    public void run() {
        System.out.println("run listener waiting room");
        Object message = null;
        try {
            message = inputStream.readObject();
            System.out.println("\n Message 1 listener waiting room");
        } catch (IOException ex) {
            System.out.println("IOException in inputStream.readObject : " + ex.getMessage());
        } catch (ClassNotFoundException ex) {
            System.out.println("ClassNotFoundException in inputStream.readObject : " + ex.getMessage());
            Logger.getLogger(ListenerMainMenu.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("\n LISTENER : message instanceof " + message.getClass());
        while (message != null && !Thread.currentThread().isInterrupted()) {
            
            if (message instanceof ListPseudo) {
                updateListPseudoWaitingRoom(message);
            }
            if (message instanceof DataGameCreation) {
                this.waitingRoom.getjTextArea1().setText(((DataGameCreation) message).toStringInfos());
                this.waitingRoom.getjButton1().setVisible(true);

            }
            if (message instanceof TransmissionTurn) {
                TransmissionTurn transmissionTurn = (TransmissionTurn) message;
                if (transmissionTurn.getActionToDo() instanceof String) {
                    if (transmissionTurn.getActionToDo().equals("Launching")) {
                        
                        DataGameCreation creationGameData = (DataGameCreation) transmissionTurn.getObjetToProcess();
                        minesweepingMenu = new MinesweepingMenu(Integer.parseInt(creationGameData.getSize()), Integer.parseInt(creationGameData.getTurnTimeLimit()), creationGameData.getNumberMines(), creationGameData.getListMines(), outputStream, this.waitingRoom.GetInfoConnection().getPseudo());
                        minesweepingMenu.setTitle(this.waitingRoom.GetInfoConnection().getPseudo());

                        ItsHisTurn(creationGameData);
                        updateListPlayers(creationGameData);

                        minesweepingMenu.setVisible(true);
                        waitingRoom.setVisible(false);
                    }
                    if (transmissionTurn.getActionToDo().equals("mine")) {
                        DataGameCreation creationGameData = (DataGameCreation) transmissionTurn.getObjetToProcess();

                        DefaultListModel defaultListModel = new DefaultListModel();
                        int i = 0;
                        int cond = 0;

                        //Update the list of players, allow to know whose player turn it is
                        for (String Splayer : creationGameData.getListPlayers()) {
                            if (i == creationGameData.getIndex()) {
                                defaultListModel.addElement(Splayer + "(<--)");
                                if (Splayer.equalsIgnoreCase(this.waitingRoom.GetInfoConnection().getPseudo())) {
                                    minesweepingMenu.canPlay = 1;
                                    minesweepingMenu.startTimer();
                                }
                            } else {
                                defaultListModel.addElement(Splayer);
                            }
                            i++;
                            if (Splayer.equalsIgnoreCase(this.waitingRoom.GetInfoConnection().getPseudo())) {
                                //if the name is still in the list, then we didn't lose/exit yet
                                //on contrary, the one that exited/lost will have cond = 0
                                cond = 1;
                            }
                        }
                        this.minesweepingMenu.getjList1().removeAll();
                        this.minesweepingMenu.getjList1().setModel(defaultListModel);

                        //So we exit the window of the waiting room that's still open
                        if (cond == 0) {
                            this.waitingRoom.dispose();
                        }
                    }
                    if (transmissionTurn.getActionToDo().equals("clickOnCell")) {
                        Mine mine = (Mine) transmissionTurn.getObjetToProcess();
                        DataGameCreation dataGameCreation = (DataGameCreation) transmissionTurn.getObjetToProcess2();
                        this.minesweepingMenu.clickOnCell(mine.getIndicex(), mine.getIndicey());

                        //Changing player -> update the list
                        ItsHisTurn(dataGameCreation);
                        updateListPlayers(dataGameCreation);

                    }
                    if (transmissionTurn.getActionToDo().equals("rightClick")) {

                        Mine mine = (Mine) transmissionTurn.getObjetToProcess();
                        DataGameCreation dataGameCreation = (DataGameCreation) transmissionTurn.getObjetToProcess2();
                        this.minesweepingMenu.rightClickOnCell(mine.getIndicex(), mine.getIndicey());

                        //Changing player -> update the list
                        ItsHisTurn(dataGameCreation);
                        updateListPlayers(dataGameCreation);

                    }
                    if (transmissionTurn.getActionToDo().equals("timerStopNextPlayer")) {
                        DataGameCreation dataGameCreation = (DataGameCreation) transmissionTurn.getObjetToProcess();
                        ItsHisTurn(dataGameCreation);
                        updateListPlayers(dataGameCreation);
                    }

                }
            }

            try {
                System.out.println("Waiting for the message");
                message = inputStream.readObject();
            } catch (IOException ex) {
                System.out.println("IOException in myInputStream.readObject : " + ex.getMessage());
            } catch (ClassNotFoundException ex) {
                System.out.println("ClassNotFoundException in myInputStream.readObject : " + ex.getMessage());
                Logger.getLogger(ListenerMainMenu.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("\n Exiting the run of the listener");
    }

    private void updateListPlayers(DataGameCreation dataGameCreation) {
        /**
         * Update the list of all player (and show whose turn it is)
         */
        int i = 0;
        DefaultListModel defaultListModel = new DefaultListModel();

        for (String Splayer : dataGameCreation.getListPlayers()) {
            System.out.println("\nSplayer : " + Splayer);
            if (i == dataGameCreation.getIndex()) {
                defaultListModel.addElement(Splayer + "(<--)");// it's his turn

            } else {
                defaultListModel.addElement(Splayer);
            }
            i++;
        }

        minesweepingMenu.getjList1().removeAll();
        minesweepingMenu.getjList1().setModel(defaultListModel);
    }

    private void ItsHisTurn(DataGameCreation dataGameCreation) {
        /**
         * set the permission to play to 1, and start the turn timer
         */
        if (dataGameCreation.getListPlayers().get(dataGameCreation.getIndex()).equalsIgnoreCase(this.waitingRoom.GetInfoConnection().getPseudo())) {
            minesweepingMenu.canPlay = 1;
            minesweepingMenu.startTimer();
        }
    }

    private void updateListPseudoWaitingRoom(Object message) {
        /**
         * Update the list of pseudo in the Waiting Room
         */
        DefaultListModel defaultListModel = new DefaultListModel();

        for (String pseudo : ((ListPseudo) message).getListPseudo()) {
            defaultListModel.addElement(pseudo);
        }

        this.waitingRoom.getjList1().setModel(defaultListModel);
    }
}
