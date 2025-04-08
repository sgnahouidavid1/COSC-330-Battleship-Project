package battleship;
import java.awt.event.*;

import javax.swing.*;///using for GUI


import java.awt.*;


public class SetupControl {
    private Model model;
    private View view;
    private GameControl gameControl;
    private ShipName[] ships;
    ClickListener clickListener = new ClickListener();
    DragListener dragListener = new DragListener();
    ActionListener actionListener = new ButtonListener();
    
    public SetupControl(Model model, View view, GameControl gameControl) {
        this.model = model;
        this.view = view;
        this.gameControl = gameControl;
        view.registerSetupListeners(clickListener, dragListener, actionListener);
        ships = new ShipName[5];
        ships[0] = ShipName.CARRIER;
        ships[1] = ShipName.BATTLESHIP;
        ships[2] = ShipName.DESTROYER;
        ships[3] = ShipName.SUBMARINE;
        ships[4] = ShipName.CRUISER;
    }

    public boolean placeShip(Ship ship, int col, int row) {

        ship.setPos(col, row);
        if(model.isValidPlacement(ship))
        {
            model.placeShip(ship);
            view.drawShip(ship, 1.0f);
            view.setAllShipsPlaced(model.allShipsPlaced());
            return true;
        }
        else
        {
            view.setAllShipsPlaced(false);
            view.makeVisible(ship.getName());
        }
        return false;
    }

    public Ship findShipFromTile(int col, int row) {
        for(int i = 0; i < 5; i++)
        {
            Ship ship = model.getShip(ships[i]);
            for(int j = 0; j < ship.getLength(); j++)
            {
                if(ship.exists())
                {
                    if(ship.getDispCol(j) == col && ship.getDispRow(j) == row)
                    {
                        return ship;
                    }
                }
            }
        }
        return null;
    }

    class ClickListener extends MouseAdapter{
        public void mousePressed(MouseEvent event) {
            if(view.clickedOnShipPanel(event))
            {
                view.createDnDLabel(event);

                Direction defaultDirection = view.getDefaultDirection();
                Ship ship = model.getShip(view.shipBeingDragged());
                ship.setDirection(defaultDirection);
            }
            else
            {
                int col = view.getColOnGrid(event.getPoint());
                int row = view.getRowOnGrid(event.getPoint());
                if(onGrid(col, row))
                {
                    if(model.getOcean()[col][row] == Tile.SHIP)
                    {
                        Ship ship = findShipFromTile(col, row);
                        view.createDnDLabel(event, ship);

                        for(int k = 0; k < ship.getLength(); k++)
                        {
                            model.setOceanTile(ship.getDispCol(k), ship.getDispRow(k), Tile.OCEAN);
                        }
                    }
                }
            }
        }
        public void mouseReleased(MouseEvent event) {
            if(view.currentlyDragging())
            {
                Point pos = view.getDNDLabelLocation();
                int col = view.pointToCol(pos);
                int row = view.pointToRow(pos);
                ShipName name = view.shipBeingDragged();
                if(onGrid(col, row))
                {
                    Ship ship = model.getShip(name);
                    placeShip(ship, col, row);
                }
                else
                {
                    view.setAllShipsPlaced(false);
                    view.makeVisible(name);
                }
                view.removeDnDLabel();
            }
        }
    };

    class DragListener extends MouseMotionAdapter{
        public void mouseDragged(MouseEvent event) {
            if(view.currentlyDragging())
            {
                ShipName name = view.shipBeingDragged();
                Ship ship = model.getShip(name);
                Point pos = view.getDNDLabelLocation();
                int col = view.pointToCol(pos);
                int row = view.pointToRow(pos);
                
                ship.setPos(col, row);
                view.drawOcean(model.getOcean());
                
                if(model.isValidPlacement(ship))
                {
                    view.setDNDLabelVisible(false);
                    view.drawShip(ship, 0.5f);
                }
                else
                    view.setDNDLabelVisible(true);
                
                view.translateDrag(event);
            }
        }
    };

    public class ButtonListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {

            JButton button = (JButton) e.getSource();
            String command = button.getActionCommand();
            
            if(command == "READY")
            {
                view.displayMainGame();
                for(int i = 0; i < 5; i++)
                {
                    Ship ship = model.getShip(ships[i]);
                    view.drawShip(ship, 1.0f);
                }
            }
            else if(command == "ROTATE")
            {
                view.rotateButton();
            }
            else if(command == "AUTO")
            {
                for(int i = 0; i < 5; i++)
                {
                    Ship ship = model.getShip(ships[i]);
                    if(!model.shipPlaced(ship))
                    {
                        while(true)
                        {
                            int col = (int) (10 * Math.random());
                            int row = (int) (10 * Math.random());
                            
                            int dir = (int) (2 * Math.random());
                            if(dir == 1)
                                ship.setDirection(Direction.VERTICAL);

                            if(placeShip(ship, col, row))
                            {
                                break;
                            }
                        }
                    }
                    view.hideAllShips();
                }
            }
            else if(command == "SERVER")
            {
                view.setServerMode(true);
            }
            else if(command == "CLIENT")
            {
                view.setServerMode(false);

            }
            else if(command == "PLAY")
            {
                view.waitingToConnect();
                gameControl.connect();
                view.displaySelectDialog();
            }
        }
    };

    public boolean onGrid(int col, int row) {
		if(col >= 0 && col < 10 && row >= 0 && row < 10)
			return true;
		return false;
	}
}