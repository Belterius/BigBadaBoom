/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serveur;

import client.Mine;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import transmission.DataGameCreation;
import transmission.Game;
import transmission.TransmissionTurn;

public class ConnectionWaitingRoom extends Thread implements Serializable {

    private Socket socket = null;
    private final ObjectInputStream inputStream;
    private final ObjectOutputStream outputStream;
    private Object message = null;
    private final String pseudo;
    private Game gameCurrent = null;

    public ConnectionWaitingRoom(Socket socket, Request request, ObjectInputStream inputStream, ObjectOutputStream outputStream, String pseudo) throws IOException {
        this.socket = socket;
        this.outputStream = outputStream;
        this.inputStream = inputStream;
        this.pseudo = pseudo;
    }

    public ObjectInputStream getInputStream() {
        return inputStream;
    }

    public ObjectOutputStream getOutputStream() {
        return outputStream;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getPseudo() {
        return pseudo;
    }

    private synchronized void sendMessageUpdateGamesMainMenu() {
        /**
         * Update the list of games we can join from the main menu
         */
        synchronized (Serveur.listConnexionPlayersMainMenu) {
            synchronized (Serveur.listDisplayGame) {
                for (ConnectionMainMenu cmp : Serveur.listConnexionPlayersMainMenu) {
                    try {
                        cmp.outputStream.writeObject(Serveur.listDisplayGame);
                        cmp.outputStream.reset();
                    } catch (IOException ex) {
                        Logger.getLogger(ConnectionWaitingRoom.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            }
        }
    }

    private void updateListPersonMainMenu() {
        /**
         * Update the list of person if the main menu
         */
        synchronized (Serveur.listConnexionPlayersMainMenu) {
            synchronized (Serveur.listPseudoMainMenu) {
                for (ConnectionMainMenu c : Serveur.listConnexionPlayersMainMenu) {
                    try {
                        c.outputStream.writeObject(Serveur.listPseudoMainMenu);
                        c.outputStream.reset();
                    } catch (IOException ex) {
                        Logger.getLogger(ConnectionMainMenu.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    private synchronized void sendMessage(Object message) {
        /**
         *Send an object to the other persons of the waiting room
         */
        Game pTemp = gameCurrent;
        synchronized (Serveur.ListGames) {
            for (ConnectionWaitingRoom csa : pTemp.getListConnectionPlayerInGame()) {
                try {
                    csa.outputStream.writeObject(message);
                    csa.outputStream.reset();
                } catch (IOException ex) {
                    Logger.getLogger(ConnectionWaitingRoom.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    }

    private void sendMessageToDoTransmission(String actionToDo, Object objectToProcess, Object objectToProcess2) {
        /**
         * Function creating and sending to the persons in the game the object that will allow to correctly update the state of the game
         */
        TransmissionTurn tt = new TransmissionTurn(actionToDo, objectToProcess, objectToProcess2);
        sendMessage(tt);
    }

    @Override
    @SuppressWarnings("empty-statement")
    public void run() {

        int cond = 0;
        cond = init();

        if (cond == 0) {
            sendMessage(-1);
        } else {
            //While we don't read the end signal (-1), we read the buffer
            while (!(message instanceof Integer && ((Integer) message) == -1)) {
                try {
                    message = inputStream.readObject();
                    processMessage(message);
                } catch (IOException | ClassNotFoundException ex) {
                    Logger.getLogger(ConnectionWaitingRoom.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        removeAndUpdateFromWaitingRoom();
        
    }
    

    private int init() {
        /**
         * init the connection with the waiting room
         * read the first message containing the data of the game
         */
        try {
            // read the game data
            message = inputStream.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ConnectionWaitingRoom.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }

        return processMessage(message);
    }

    private void processDataCreateGame(DataGameCreation message) {
        // link to the current game (if it exist, create it else)
        linkCurrentGame(message);
        //if the person go from the MainMenu to the WaitingRoom -> update MainMenu
        removePseudoListMainMenu();
        updateListPersonMainMenu();
        sendMessage(gameCurrent.listPlayers());
        //Update the game data in the MainMenu ; the number of players increased
        sendMessageUpdateGamesMainMenu();
    }
    
    private void removeAndUpdateFromWaitingRoom()
    {
        /**
         * function deleting a person from the Waiting room
         */
        int cond;
        
        cond = removePseudoListWaitingRoom();
        AddPseudoListMainMenu();
        updateListPersonMainMenu();
        
        if (cond == 1) {
            //we deleted the waiting room, update the MainMenu
            sendMessageUpdateGamesMainMenu();
        } else if (cond == 2) {
            //We deleted a player from the room : update the list of pseudos
            sendMessage(gameCurrent.listPlayers());
        }
    }

    private int removePseudoListWaitingRoom() {

        synchronized (Serveur.ListGames) {
            synchronized (Serveur.listDisplayGame) {
                Game p = gameCurrent;
                if (p.getListConnectionPlayerInGame().size() == 1) {
                    //if it was the last we delete the game
                    Serveur.listDisplayGame.getListDataGameCreation().remove(gameCurrent.getInfosCreaPartie());
                    Serveur.ListGames.remove(gameCurrent);
                    sendMessageUpdateGamesMainMenu();
                    return 1;
                } else {
                    //remove the player from the list of connection to the game
                    p.getListConnectionPlayerInGame().remove(this);
                    // if it was the leader, we set the next one as the leader
                    if (p.getInfosCreaPartie().getLeader().equalsIgnoreCase(pseudo)) {
                        p.getInfosCreaPartie().setLeader(p.getListConnectionPlayerInGame().get(0).getPseudo());
                    }
                    p.getInfosCreaPartie().setNumberOfPlayers(p.getInfosCreaPartie().getNumberPlayers() - 1);
                    sendMessage(p.getInfosCreaPartie());
                    sendMessageUpdateGamesMainMenu();

                    return 2;
                }
            }
        }
    }

    private int processMessage(Object message) {
        if (message instanceof DataGameCreation) {
            processDataCreateGame((DataGameCreation) message);
            return 1;
        }
        if (message instanceof String) {
            if (((String) message).equals("Launch")) {
                launchGame();
                return 1;
            }
        }
        if (message instanceof TransmissionTurn) {
            if (((TransmissionTurn) message).getActionToDo().equals("mine")) {
                removeClientFromCurrentGame((String) ((TransmissionTurn) message).getObjetToProcess());
                return 1;
            }
            if (((TransmissionTurn) message).getActionToDo().equals("clickOnCell")) {
                click("clickOnCell", (Mine) ((TransmissionTurn) message).getObjetToProcess());
                return 1;
            }
            if (((TransmissionTurn) message).getActionToDo().equals("removeFlag") || ((TransmissionTurn) message).getActionToDo().equals("addFlag")) {
                click("rightClick", (Mine) ((TransmissionTurn) message).getObjetToProcess());
                return 1;
            }
            if (((TransmissionTurn) message).getActionToDo().equals("timerStop")) {
                timerFinJoueurSuivant();
                return 1;
            }
        }

        return -1;
    }

    private void timerFinJoueurSuivant() {
        /**
         * called when a player couldn't take an action fast enough
         * we skip his turn
         */
        Game pTemp = gameCurrent;

        if (pTemp.getInfosCreaPartie().getListPlayers().size() - 1 == pTemp.getInfosCreaPartie().getIndex()) {
            pTemp.getInfosCreaPartie().setIndex(0);
        } else {
            pTemp.getInfosCreaPartie().setIndex(pTemp.getInfosCreaPartie().getIndex() + 1);
        }

        sendMessageToDoTransmission("timerStopNextPlayer", pTemp.getInfosCreaPartie(), null);
    }

    private void click(String sTempo, Mine mine) {
        /**
         * change the index of the player that's going to play, and transmit to every players the action that's been taken
         */
        Game pTemp = gameCurrent;
        //Passage au joueur suivant
        if (pTemp.getInfosCreaPartie().getListPlayers().size() - 1 == pTemp.getInfosCreaPartie().getIndex()) {
            pTemp.getInfosCreaPartie().setIndex(0);
        } else {
            pTemp.getInfosCreaPartie().setIndex(pTemp.getInfosCreaPartie().getIndex() + 1);
        }
        sendMessageToDoTransmission(sTempo, mine, pTemp.getInfosCreaPartie());
    }

    private void removeClientFromCurrentGame(String sTempo) {
        /**
         * remove a client from the current game
         * because he clicked on a mine/surrendered/closed the window
         */
        Game pTemp = gameCurrent;
        int oldIndex = pTemp.getInfosCreaPartie().getIndex();
        int sizeListe = pTemp.getInfosCreaPartie().getListPlayers().size();
        // delete the player from the display List
        pTemp.getInfosCreaPartie().getListPlayers().remove(sTempo);
        //if we deleted the last from the list, then we set the next player to index 0
        //if it's not the last then deleting the player act as going to the next player turn, no need to change the index
        if (oldIndex == (sizeListe - 1)) {
            pTemp.getInfosCreaPartie().setIndex(0);
        }
        //send the event to all players
        sendMessageToDoTransmission("mine", pTemp.getInfosCreaPartie(), null);

    }

    private void launchGame() {
        /**
         * Start a new game
         */
        Game pTemp = gameCurrent;
        createMines(pTemp);
        //we get the final list of the players, that we'll send on client side too
        pTemp.getInfosCreaPartie().getListPlayers().addAll(pTemp.listPlayersString());
        sendMessageToDoTransmission("Launching", pTemp.getInfosCreaPartie(), null);
        synchronized (Serveur.listDisplayGame) {
            Serveur.listDisplayGame.getListDataGameCreation().remove(pTemp.getInfosCreaPartie());
        }
        sendMessageUpdateGamesMainMenu();
    }

    public void createMines(Game pTemp) {
        /**
         * Create the mines and add them to the game data
         */
        int xmine;
        int ymine;
        int nbmine = pTemp.getInfosCreaPartie().getNumberMines();
        int sizeMinesweeperGrid = Integer.parseInt(pTemp.getInfosCreaPartie().getSize());
        for (int i = 0; i < nbmine; i++) {
            if (!pTemp.getInfosCreaPartie().getListMines().isEmpty()) {
                int found = 0;
                xmine = ThreadLocalRandom.current().nextInt(0, sizeMinesweeperGrid);
                ymine = ThreadLocalRandom.current().nextInt(0, sizeMinesweeperGrid);
                Mine mine = new Mine(100, 100);
                mine.setIndicex(xmine);
                mine.setIndicey(ymine);
                Iterator iteratorMines = pTemp.getInfosCreaPartie().getListMines().iterator();

                while (iteratorMines.hasNext() && found == 0) {
                    Mine tempo = (Mine) iteratorMines.next();
                    if (mine.equals(tempo)) {
                        found = 1;
                    }
                }
                if (found == 0) {
                    pTemp.getInfosCreaPartie().getListMines().add(mine);
                } else {
                    i--;
                }
            } else {
                xmine = ThreadLocalRandom.current().nextInt(0, sizeMinesweeperGrid);
                ymine = ThreadLocalRandom.current().nextInt(0, sizeMinesweeperGrid);
                Mine mine = new Mine(100, 100);
                mine.setIndicex(xmine);
                mine.setIndicey(ymine);
                pTemp.getInfosCreaPartie().getListMines().add(mine);
            }
        }
    }

    private Game getGame(DataGameCreation icpTemp) {
        /**
         * retrieve the ref of the current game to set it to the client
         * function executed only for the init
         */
        synchronized (Serveur.ListGames) {
            Iterator i = Serveur.ListGames.iterator();
            Game p = null;
            while (i.hasNext()) {
                p = (Game) i.next();
                if (p != null) {
                    System.out.println("\np : " + p);
                    if (p.getInfosCreaPartie().equals(icpTemp)) {
                        return p;
                    }

                }
            }
        }

        return null;
    }

    private int linkCurrentGame(DataGameCreation icpTemp) {
        /**
         * add the current connection to the game or create the game
         */
        synchronized (Serveur.ListGames) {
            Game p = getGame(icpTemp);
            if (p != null) {
                //add a connection to the game
                p.ajouterConnexion(this);
                //add 1 to the number of players
                p.getInfosCreaPartie().setNumberOfPlayers(p.getInfosCreaPartie().getNumberPlayers() + 1);
                this.gameCurrent = p;
                return 1;
            }
            //game doesn't exist, we create it
            p = new Game(icpTemp, this);
            Serveur.ListGames.add(p);
            this.gameCurrent = p;
        }
        synchronized (Serveur.listDisplayGame) {
            //we add it to the list of game in the MainMenu
            Serveur.listDisplayGame.addInfos(icpTemp);
        }
        return 2;

    }

    private void removePseudoListMainMenu() {
        synchronized (Serveur.listPseudoMainMenu) {
            Serveur.listPseudoMainMenu.getListPseudo().remove(this.pseudo);
        }
    }

    private void AddPseudoListMainMenu() {
        synchronized (Serveur.listPseudoMainMenu) {
            Serveur.listPseudoMainMenu.addPseudo(this.pseudo);
        }
    }
}
