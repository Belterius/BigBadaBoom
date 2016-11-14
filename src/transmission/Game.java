/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package transmission;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import serveur.ConnectionMainMenu;
import serveur.ConnectionWaitingRoom;

public class Game implements Serializable{
    
    private DataGameCreation infosCreaPartie;
    private List<ConnectionWaitingRoom> listConnectionPlayerInGame;
    

    public Game(DataGameCreation ifc, ConnectionWaitingRoom c) {
        infosCreaPartie = ifc;
        listConnectionPlayerInGame = new ArrayList<>();
        listConnectionPlayerInGame.add(c);
    }
    
    public List<ConnectionWaitingRoom> getListConnectionPlayerInGame() {
        return listConnectionPlayerInGame;
    }
    
    public void ajouterConnexion(ConnectionWaitingRoom c)
    {
        this.listConnectionPlayerInGame.add(c);
    }

    public DataGameCreation getInfosCreaPartie() {
        return infosCreaPartie;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.infosCreaPartie);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Game other = (Game) obj;
        if (!Objects.equals(this.infosCreaPartie, other.infosCreaPartie)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        String s = "Game{" + "infosCreaPartie=" + this.infosCreaPartie.toStringInfos()+ ", listConnectionPlayerInGame=";
        for(ConnectionWaitingRoom c : this.listConnectionPlayerInGame)
            s+="\n\t "+c.getPseudo();
        return   s;
    }
    
    public ListPseudo listPlayers()
    {
        ListPseudo lp = new ListPseudo();
        for(ConnectionWaitingRoom c : listConnectionPlayerInGame)
            lp.addPseudo(c.getPseudo());
        return lp;
    }
    
    public List<String> listPlayersString()
    {
        List<String> lp = new ArrayList<>();
        for(ConnectionWaitingRoom c : listConnectionPlayerInGame)
            lp.add(c.getPseudo());
        return lp;
    }
    
    
    
}
