/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import transmission.DataGameCreation;
import transmission.ListAvailableGames;
import transmission.ListPseudo;


/*Thread et a comme attributs un JTextArea
 et un BufferedReader.*/
public class ListenerMainMenu extends Thread {

    private MainMenu myMainMenu;
    private ObjectInputStream myInputStream;

    public ListenerMainMenu(MainMenu mainMenu, ObjectInputStream inputStream) {
        this.myMainMenu = mainMenu;
        this.myInputStream = inputStream;
    }

    @Override
    public void run() {
        Object message = null;
        try {
            System.out.println("Waiting for the message");
            message = myInputStream.readObject();
        } catch (IOException ex) {
            System.out.println("IOException in myInputStream.readObject : " + ex.getMessage());
        } catch (ClassNotFoundException ex) {
            System.out.println("ClassNotFoundException in myInputStream.readObject : " + ex.getMessage());
            Logger.getLogger(ListenerMainMenu.class.getName()).log(Level.SEVERE, null, ex);
        }

        //while we are not done with the questions : ie. the message != a specific value
        //or while we don't leave the client app
        while (message != null && !Thread.currentThread().isInterrupted()) {
            
            //Update all pseudos
            if (message instanceof ListPseudo) {
                this.myMainMenu.getjList1().removeAll();

                DefaultListModel defaultListModel = new DefaultListModel();
                for (String pseudo : ((ListPseudo) message).getListPseudo()) {
                    defaultListModel.addElement(pseudo);
                }
                this.myMainMenu.getjList1().setModel(defaultListModel);
            }
            //Update all available games
            if (message instanceof ListAvailableGames) {
                this.myMainMenu.getjList2().removeAll();
                DefaultListModel defaultListModel = new DefaultListModel();
                for (DataGameCreation gameCreationData : ((ListAvailableGames) message).getListDataGameCreation()) {
                    System.out.println("\n Game Data : " + gameCreationData);
                    defaultListModel.addElement(gameCreationData);
                }
                this.myMainMenu.getjList2().setModel(defaultListModel);
                this.myMainMenu.getjList2().setSelectedIndex(-1);
                this.myMainMenu.getjTextArea1().setText("");
            }
            //On -1 : close the client
            if (message instanceof Integer) {
                if ((Integer) message == -1) {
                    this.myMainMenu.dispose();
                }
            } else {
                // read the buffer
                try {
                    System.out.println("\n Waiting for the message");
                    message = myInputStream.readObject();
                } catch (IOException ex) {
                    System.out.println("IOException in myImputStream.readObject : " + ex.getMessage());
                } catch (ClassNotFoundException ex) {
                    System.out.println("ClassNotFoundException in myImputStream.readObject : " + ex.getMessage());
                    Logger.getLogger(ListenerMainMenu.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        System.out.println("\n Sortie du run de l'écouteur principal");
    }

}
