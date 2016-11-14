/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;


/*
    Contain the cell and it's x/y location
*/
public class CellGrid {
    MineSweeperGrid.CellPane CellGrid;
    int x;
    int y;

    public MineSweeperGrid.CellPane getCellGrid() {
        return CellGrid;
    }

    public void setCellGrid(MineSweeperGrid.CellPane CaseGrille) {
        this.CellGrid = CaseGrille;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public CellGrid(MineSweeperGrid.CellPane cellGrid, int x, int y) {
        this.CellGrid = cellGrid;
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "CellGrid{" + "CellGrid=" + CellGrid + ", x=" + x + ", y=" + y + '}';
    }
    
    
}
