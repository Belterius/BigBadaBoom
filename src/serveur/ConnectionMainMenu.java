/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serveur;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import transmission.InfoConnection;
import static serveur.Serveur.listConnexionPlayersMainMenu;

public class ConnectionMainMenu extends Thread implements Serializable {

    private Socket socket = null;
    private ObjectInputStream inputStream;
    ObjectOutputStream outputStream;
    private Object message = null;
    private String pseudo;

    public ConnectionMainMenu(Socket socket, Request request, ObjectInputStream inputStream, ObjectOutputStream outputStream, Object message) throws IOException {
        // pour la connexion en générale
        this.socket = socket;
        this.outputStream = outputStream;
        this.inputStream = inputStream;
        this.message = message;
        this.pseudo = "";
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

    private synchronized void sendMessage(Object message) throws IOException {
         /**
          * send an object to the current person
         */
        synchronized (Serveur.listConnexionPlayersMainMenu) {
            this.outputStream.writeObject(message);
            this.outputStream.reset();
        }
    }

    @Override
    @SuppressWarnings("empty-statement")
    public void run() {

        init();

        //read the buffer
        while (!(message instanceof Integer && ((Integer) message) == -1)) {
            try {
                message = inputStream.readObject();
                //process the message
                processMessage(message);
            } catch (IOException ex) {
                Logger.getLogger(ConnectionMainMenu.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ConnectionMainMenu.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        closeConnection();
    }

    private int init() {
        /**
         * We first check if the client wasn't connected :
         * if yes, we close the connection, we don't allow similar clients !
         * if not, we add it to the list of connection, we add his pseudo to the list and we send that to every person in the main menu
         */
        if (message instanceof InfoConnection) {
            synchronized (Serveur.listConnexionPlayersMainMenu) {
                for (ConnectionMainMenu co : Serveur.listConnexionPlayersMainMenu) {
                    if (co.pseudo.equalsIgnoreCase(((InfoConnection) message).getPseudo())) {
                        // already exist : we close !
                        try {
                            sendMessage(-1);
                            return -1;
                        } catch (IOException ex) {
                            System.out.println("CONNEXION : Problème IOexception : envoyermessage -1");;
                        }
                    }
                }
                listConnexionPlayersMainMenu.add(this);
            }
            pseudo = ((InfoConnection) message).getPseudo();
            synchronized (Serveur.listPseudoMainMenu) {
                Serveur.listPseudoMainMenu.addPseudo(pseudo);
            }
            //we send an update msg of the list in the MainMenu
            updateListPersonMenu();
            updateListRoomMenu();
            return 1;
        }
        try {
            sendMessage(-1);
        } catch (IOException ex) {
            Logger.getLogger(ConnectionMainMenu.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;

    }

    private void closeConnection() {
        /**
         * we are closing the program, we remove it from the list to update everywhere
         */
        System.out.println("\n\tWe close the related window : closing the connection !!! ");
        
        removePseudoListMenu();
        removeConnectionFromList();
        updateListPersonMenu();
    }

    private void updateListPersonMenu() {
        /**
         * Update the list of pseudo is the MainMenu
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

    private void updateListRoomMenu() {
        /**
         * Update list of game/rooms in the MainMenu
         */
        synchronized (Serveur.listConnexionPlayersMainMenu) {
            synchronized (Serveur.listDisplayGame) {
                for (ConnectionMainMenu c : Serveur.listConnexionPlayersMainMenu) {
                    try {
                        c.outputStream.writeObject(Serveur.listDisplayGame);
                        c.outputStream.reset();
                    } catch (IOException ex) {
                        Logger.getLogger(ConnectionMainMenu.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    private void removePseudoListMenu() {
        /**
         * Delete the pseudo from the List of players of the MainMenu
         */
        synchronized (Serveur.listPseudoMainMenu) {
            Serveur.listPseudoMainMenu.getListPseudo().remove(this.pseudo);
        }
    }

    private void removeConnectionFromList() {
        /**
         * Delete the connection from the list of connection of the MainMenu
         */
        synchronized (Serveur.listConnexionPlayersMainMenu) {
            Serveur.listConnexionPlayersMainMenu.remove(this);
        }
    }

    private int processMessage(Object message) {
        /**
         * Process the different server messages
         */
        if (message instanceof Integer) {
            if ((Integer) message == -2) {
                // need to update the pseudoList
                updateListPersonMenu();
                return 1;
            }
        }
        return -1;
    }

}
