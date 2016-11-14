/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package transmission;

import client.Mine;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DataGameCreation implements Serializable{
    
    private String name;
    private String leader;
    private String size;
    private String timePerTurn;
    private int numberOfMines;
    private int maxNumberOfPlayers;
    private int numberOfPlayers;
    private List<Mine> listMines;
    private List<String> listPlayers;
    private int index;//index of the player that is playing
    

    public DataGameCreation(String leader, String name, String size, String timeTurn, int numberMines, int maxNumberOfPlayers) {
        this.leader = leader;
        this.name = name;
        this.size = size;
        this.timePerTurn = timeTurn;
        this.numberOfMines = numberMines;
        this.maxNumberOfPlayers = maxNumberOfPlayers;
        this.numberOfPlayers = 1;
        listMines = new ArrayList<>();
        listPlayers = new ArrayList<>();
        index = 0;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.leader);
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
        final DataGameCreation other = (DataGameCreation) obj;
        if (!Objects.equals(this.leader, other.leader)) {
            return false;
        }
        return true;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public String getLeader() {
        return leader;
    }
    
    public String getSize() {
        return size;
    }

    public String getTurnTimeLimit() {
        return timePerTurn;
    }

    public int getNumberMines() {
        return numberOfMines;
    }

    public int getMaxNumberOfPlayers() {
        return maxNumberOfPlayers;
    }

    public String getName() {
        return name;
    }

    public int getNumberPlayers() {
        return numberOfPlayers;
    }

    public void setNumberOfPlayers(int numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;
    }

    public List<Mine> getListMines() {
        return listMines;
    }

    public List<String> getListPlayers() {
        return listPlayers;
    }

    public void setListPlayers(List<String> listPlayers) {
        this.listPlayers = listPlayers;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
    
    
    @Override
    public String toString() {
        return name + "(" + leader + ")" + "\t : "+ this.numberOfPlayers + " / " + this.maxNumberOfPlayers;
    }
    
    public String toStringInfos() {
        return "Name of the game = " + name + "\nLeader : "+this.leader+"\nSize = " + size + "\nTime limit per turn = " + timePerTurn + "\nNumber of mines = " + numberOfMines + "\nNumber of players max = " + maxNumberOfPlayers;
    }
    
}
