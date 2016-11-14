/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package transmission;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ListAvailableGames implements Serializable{
    
    private List<DataGameCreation> listeInfosCreaPartie;

    public ListAvailableGames() {
        listeInfosCreaPartie = new ArrayList();
    }
    
    public void addInfos(DataGameCreation i)
    {
        this.listeInfosCreaPartie.add(i);
    }

    public List<DataGameCreation> getListDataGameCreation() {
        return listeInfosCreaPartie;
    }
    

}
