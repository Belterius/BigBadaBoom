/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serveur;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import transmission.InfoConnection;
import transmission.ListAvailableGames;
import transmission.ListPseudo;
import transmission.Game;

public class Serveur extends Thread {

    private ServerSocket serverSocket = null;

    //MainMenu
    //all connection to the main menu
    protected static List<ConnectionMainMenu> listConnexionPlayersMainMenu;
    //all pseudo to the main menu
    protected static ListPseudo listPseudoMainMenu;
    //all game data in the main menu
    protected static ListAvailableGames listDisplayGame;
    
    //WaitingRoom
    //all games + their data + their players connection
    protected static List<Game> ListGames;

    private Request rq = null;

    private ObjectInputStream in;
    private ObjectOutputStream out;

    public Serveur(int port) throws IOException {
        rq = new Request();

        serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress("localhost", port));
        serverSocket.setSoTimeout(600000);

        listConnexionPlayersMainMenu = new ArrayList<>();
        listPseudoMainMenu = new ListPseudo();
        ListGames = new ArrayList<>();
        listDisplayGame = new ListAvailableGames();

    }

    @Override
    public void run() {
        Object message;
        try {

            while (!Thread.currentThread().isInterrupted()) {
                // we wait for a connection
                System.out.println("\nWaiting a client");
                Socket sTemp = serverSocket.accept();
                
                out = new ObjectOutputStream(sTemp.getOutputStream());
                in = new ObjectInputStream(sTemp.getInputStream());
                
                message = in.readObject();
                
                if (message instanceof InfoConnection) {
                    /**we create a connection for the MainMenu**/
                    if (((InfoConnection) message).getWindow().equalsIgnoreCase("MainMenu")) {
                        ConnectionMainMenu conn = new ConnectionMainMenu(sTemp, this.rq, in, out, message);
                        conn.start();
                    }
                    /**we create a connection for the WaitingRoom**/
                    if (((InfoConnection) message).getWindow().equalsIgnoreCase("WaitingRoom")) {
                        ConnectionWaitingRoom conn = new ConnectionWaitingRoom(sTemp, this.rq, in, out, ((InfoConnection) message).getPseudo());
                        conn.start();
                    }
                }
            }

        } catch (SocketTimeoutException ste) {
            System.out.println("\nTIMEOUT");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Serveur.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            CloseSocketListener();
        }
    }

    private void CloseSocketListener() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
                System.out.println("Closing server");
            }
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    public ServerSocket getSs() {
        return serverSocket;
    }

    public void setSs(ServerSocket ss) {
        this.serverSocket = ss;
    }

    public static void main(String[] args) throws IOException {

        //Ecoute du serveur sur le port 50000
        Serveur s = new Serveur(50000);
        s.start();
    }

}
