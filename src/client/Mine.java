/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.Serializable;


public class Mine implements Serializable {
    private int indicex;
    private int indicey;

    public int getIndicex() {
        return indicex;
    }

    public int getIndicey() {
        return indicey;
    }

    public void setIndicex(int indicex) {
        this.indicex = indicex;
    }

    public void setIndicey(int indicey) {
        this.indicey = indicey;
    }

    public Mine(int indicex, int indicey) {
        this.indicex = indicex;
        this.indicey = indicey;
    }

    @Override
    public String toString() {
        return "Mine{" + "indicex=" + indicex + ", indicey=" + indicey + '}';
    }

    public boolean equals(Mine mine) {
        if(this.getIndicex()==mine.getIndicex()){
            if(this.getIndicey() == mine.getIndicey()){
                return true;
            }else{
                return false;
            }
        }
        else{
            return false;
        }
           
    }
    
    
    
    
}
