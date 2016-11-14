/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serveur;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * execute the connection request and unicity of client
 */
public class Request {

    private Connection conn;

    public Request() {
        try {
            connect();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void connect() throws ClassNotFoundException, SQLException {

        // old code for JDBC : BDD JAVA
        /*String driverClass = "org.apache.derby.jdbc.ClientDriver";
        String urlDatabase = "jdbc:derby://localhost:1527/BigBadaBoom2";
        String user = "maxime";
        String pwd = "maxime";*/

        //Class.forName(driverClass);
        //this.conn = DriverManager.getConnection(urlDatabase, user, pwd);
        
        // BDD sur SQLite
        this.conn = DriverManager.getConnection("jdbc:sqlite:BigBadaBoom.db");

    }

    /**
     *
     * @param pseudo
     * @return true if doesn't exist yet, false else
     */
    public boolean unicityConnexion(String pseudo) {

        String requete = "SELECT id FROM connexion WHERE UPPER(pseudo)=?";
        PreparedStatement pstmt;
        try {
            pstmt = conn.prepareStatement(requete);
            pstmt.setString(1, pseudo.toUpperCase());
            ResultSet res = pstmt.executeQuery();
            if (res.next()) {
                return false;
            }
            return true;
        } catch (SQLException ex) {
            System.out.println("\nREQUETE : Problem in unicity");
            Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;

    }

    /**
     *
     * @param pseudo
     * @param password
     * @return id (int) or -1 if doesn't exist
     */
    public int connectionClient(String pseudo, String password) {
        Md5 pass = new Md5(password);

        String requete = "SELECT id FROM connexion WHERE UPPER(pseudo)=? AND password=?";
        PreparedStatement pstmt;
        try {
            pstmt = conn.prepareStatement(requete);
            pstmt.setString(1, pseudo.toUpperCase());
            pstmt.setString(2, pass.codeGet());
            ResultSet res = pstmt.executeQuery();
            if (res.next()) {
                int tempo = res.getInt("id");
                pstmt.close();
                return tempo;
            }
            pstmt.close();
            return -1;
        } catch (SQLException ex) {
            System.out.println("\nREQUETE : Problem in connection");
            Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    /**
     *
     * @param pseudo
     * @param password
     * @return true if success, false else
     */
    public boolean createClient(String pseudo, String password) {

        try {
            Md5 pass = new Md5(password);

            String requete = "INSERT INTO connexion (pseudo, password) VALUES (?,?)";
            PreparedStatement pstmt;
            pstmt = conn.prepareStatement(requete);
            pstmt.setString(1, pseudo);
            pstmt.setString(2, pass.codeGet());
            System.out.println("Pre exec");
            int ret = pstmt.executeUpdate();
            pstmt.close();
            System.out.println("Post exec");
            if (ret == 1) {
                JOptionPane d = new JOptionPane();
                String message = "Created a new Client !!";
                d.showMessageDialog(null, message, "Success", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane d = new JOptionPane();
                String message = "Error creating client !!";
                d.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (SQLException ex) {
            System.out.println("Problem Creating client");
            Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex);
        }
            return false;
    }

    public static void main(String[] args) throws SQLException {
        Request rq = new Request();

    }

}
