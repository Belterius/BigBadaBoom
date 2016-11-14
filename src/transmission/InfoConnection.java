/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package transmission;

import java.io.Serializable;

public class InfoConnection implements Serializable {
    
    private String pseudo;
    private String window;
    

    public InfoConnection(String pseudo, String fenetre) {
        this.pseudo = pseudo;
        this.window = fenetre;
    }

    public String getPseudo() {
        return pseudo;
    }

    public String getWindow() {
        return window;
    }

    @Override
    public String toString() {
        return "InfoConnection{" + "pseudo=" + pseudo + ", window=" + window + '}';
    }
    
    
}
