/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package transmission;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ListPseudo implements Serializable{
    
    private List<String> listPseudo;

    public ListPseudo() {
        this.listPseudo = new ArrayList() ;
    }

    public List<String> getListPseudo() {
        return listPseudo;
    }
    
    public void addPseudo(String s)
    {
        this.listPseudo.add(s);
    }
    
}
