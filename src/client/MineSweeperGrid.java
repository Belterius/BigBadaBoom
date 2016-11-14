/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;

public class MineSweeperGrid {

    List<CellGrid> GridCells = new ArrayList<>();

    /**
     *Create our Minesweeper grid
     * @param frame the frame that will contain all our Cells
     * @param sizeGrid the size of our Grid
     * @param mainPanel our main MinesweepingMenu
     */
    public MineSweeperGrid(final JInternalFrame frame, int sizeGrid, final MinesweepingMenu mainPanel) {
        final int size = sizeGrid;

        //We prevent from closing our minesweeping grid
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        //We add a mainPanel that will contain all our internal panels
        //we still pass our mainPanel
        frame.add(new MainInternalPanel(size, mainPanel));
        frame.pack();
        frame.setVisible(true);
    }

    public class MainInternalPanel extends JPanel {

        /**
         * Create our internal Minesweeping grid
         *
         * @param size the size of our grid
         * @param mainPanel our main MinesweepingMenu
         */
        public MainInternalPanel(int size, final MinesweepingMenu mainPanel) {

            //Force a display of type grid (align every components)
            setLayout(new GridBagLayout());

            //set the constraints for our cells
            GridBagConstraints gridBagConstraints = new GridBagConstraints();

            //Create our grid
            for (int row = 0; row < size; row++) {
                for (int col = 0; col < size; col++) {
                    //Creating all the Cells from the row

                    //x pos
                    gridBagConstraints.gridx = col;
                    //y pos
                    gridBagConstraints.gridy = row;

                    //We create our cell, passing the size of the grid in parameter so we can always have the same size of grid for the display, and only change the size of the cell
                    //We pass the x and y pos for later usage
                    //And the mainPanel to call the click functions
                    CellPane cellPane = new CellPane(size, col, row, mainPanel);

                    Border border = null;
                    /*
                     set the borders
                    Warning : we don't want double borders, so we have to be carefull to only have one of the adjacent cell doing the border and not both
                    To do that each cell have a Top and Left border, and we add the Right border for the last column, Bottom border for the last row
                     */
                    if (row < size - 1) {
                        //we're not on the last row
                        if (col < size - 1) {
                            //we're not on the last column
                            border = new MatteBorder(1, 1, 0, 0, Color.gray);
                        } else {
                            //last column, not last row
                            border = new MatteBorder(1, 1, 0, 1, Color.GRAY);
                        }
                    } else {
                        //last row
                        if (col < size - 1) {
                            //not last column
                            border = new MatteBorder(1, 1, 1, 0, Color.GRAY);
                        } else {
                            //last column + last row
                            border = new MatteBorder(1, 1, 1, 1, Color.GRAY);
                        }
                    }
                    //We set the assigned borders
                    cellPane.setBorder(border);
                    //We add our Cell
                    add(cellPane, gridBagConstraints);
                    //We create a CellGrid, that contains both our Cell, and it's x & y pos, will be way easier to work with that rather than pixel location                    
                    CellGrid cgrille = new CellGrid(cellPane, col, row);
                    //We add that to a List that contain all our Cells
                    GridCells.add(cgrille);
                }
            }

        }
    }

    /**
     * A Cell of our grid
     * We need a listener on each one to retrieve a click action
     */
    public class CellPane extends JPanel {

        int sizeGrid = 350;
        int sizeCell = 0;
        int xpos = 0;
        int ypos = 0;
        private Image image;

        /**
         * allow to display an image on the cell
         *
         * @param image correspond to the state of the cell
         * can be the number of adjacent mines/a flag/a clean cell
         */
        public void setImage(Image image) {
            this.image = scaleImage(image, sizeCell, sizeCell);

            repaint();
        }

        /**
         * Scale our image to the size of the cell
         *
         * @param source the image to resize
         * @param width the new width of the image
         * @param height the new height of the image
         * @return
         */
        public Image scaleImage(Image source, int width, int height) {
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) img.getGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(source, 0, 0, width, height, null);
            g.dispose();
            return img;
        }

        @Override
        /**
         * set our img on the repain call repaint()
         */
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, null);
        }

        /**
         * Create our grid cell
         *
         * @param size the size of our GRID
         * @param x x pos of the cell
         * @param y y pos of the cell
         * @param panelPrincipal our main MinesweepingMenu
         */
        public CellPane(int size, int x, int y, final MinesweepingMenu panelPrincipal) {
            xpos = x;
            ypos = y;
            //we want to have a grid that has sizeGrid size in pixel, we divide by the number of column/row to have the size in pixel of our cell
            this.sizeCell = sizeGrid / size;
            addMouseListener(new MouseAdapter() {
                /*
                 We create our listener       
                 */
                @Override
                public void mouseEntered(MouseEvent e) {
                    //not used atm
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //not used atm
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        //Reveal a Cell
                        if (panelPrincipal.canPlay == 1) {
                            //We call our MainPanel clickOnCell function, with our cell x & y pos as parameter
                            panelPrincipal.clickOnCell(xpos, ypos);
                        }

                    }
                    //
                    if (SwingUtilities.isRightMouseButton(e)) {
                        //Setting or removing a Flag
                        if (panelPrincipal.canPlay == 1) {
                            panelPrincipal.rightClickOnCell(xpos, ypos);
                        }
                    }
                }

            });
        }

        @Override
        /**
         * The prefered dimension of the cell (called in frame.pack earlier)
         * depend on the size of our cell, depending on the size of our grid
         * So we have a fixed grid size, a dynamic cell size, but the same for each of our cells
         */
        public Dimension getPreferredSize() {
            return new Dimension(this.sizeCell, this.sizeCell);
        }
    }

}
