package battleship;

import java.awt.event.*;

import javax.swing.*;///using for GUI

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;

public class GameControl {
    private Model model;
    private View view;
    private ShipName[] ships;
    ButtonMouseListener mouseListener = new ButtonMouseListener();
    ActionListener actionListener = new ButtonListener();
    private boolean serverMode;
    private ObjectOutputStream output; // output stream to client
	private ObjectInputStream input; // input stream from client
	private ServerSocket server; // server socket
	private Socket connection; // connection to client
    private String ip;
    private boolean myTurn;
    private String sentPos;
    private boolean turnHalf;
    private int turnCount;
    private boolean gameOver;

    public GameControl(Model model, View view) {
        this.model = model;
        this.view = view;
        turnHalf = false;
        turnCount = 1;
        
        view.registerGameListeners(mouseListener, actionListener);
        ships = new ShipName[5];
        ships[0] = ShipName.CARRIER;
        ships[1] = ShipName.BATTLESHIP;
        ships[2] = ShipName.DESTROYER;
        ships[3] = ShipName.SUBMARINE;
        ships[4] = ShipName.CRUISER;
    }

    public void connect() {

        serverMode = view.getServerMode();
        try {
            if(serverMode)
            {
                server = new ServerSocket(12345, 100); // create ServerSocket
                myTurn = true;
                connection = server.accept();
            }
            else
            {
                myTurn = false;
                connection = new Socket(InetAddress.getByName(ip), 12345);
            }
            
            output = new ObjectOutputStream(connection.getOutputStream());
            output.flush();
            input = new ObjectInputStream(connection.getInputStream());
            view.log("Connected", FontStyle.SMALL);
            view.playSound("connect");
            view.log("Turn 1: ", FontStyle.BIG);
            start();
        } catch (Exception e) {
            //
        }
    }

    public void start() {
        new Thread(this::processMessages).start();
    }

    private void processMessages() {
        try {
            while (true) {
                String msg = (String) input.readObject();
                
                if(msg.equals("MISS"))
                {
                    int col = Integer.valueOf(sentPos) % 10;
                    int row = Integer.valueOf(sentPos) / 10;
                    view.log("Miss.", FontStyle.MISS);
                    view.drawTile(col, row, false, "TARGET");
                    view.playSound("miss");
                    myTurn = false;
                    if(turnHalf)
                    {
                        turnCount++;
                        view.log("Turn " + turnCount + ": ", FontStyle.BIG);
                    }
                    turnHalf = !turnHalf;
                }
                else if(msg.length() == 1 || msg.length() == 2)
                {
                    int col = Integer.valueOf(msg) % 10;
                    int row = Integer.valueOf(msg) / 10;
                    view.log("Opponent shot at: " + displayPos(col, row), FontStyle.SMALL);
                    sendHit(col, row);
                    //view.log("My Turn!");
                    myTurn = true;
                    if(turnHalf)
                    {
                        turnCount++;
                        view.log("Turn " + turnCount + ": ", FontStyle.BIG);
                    }
                    turnHalf = !turnHalf;
                }
                else 
                {
                    //hit
                    int col = Integer.valueOf(sentPos) % 10;
                    int row = Integer.valueOf(sentPos) / 10;
                    view.log("HIT!", FontStyle.HIT);
                    view.drawTile(col, row, true, "TARGET");
                    myTurn = false;

                    //check if destroyed
                    if(msg.equals("CARRIER"))
                    {
                        view.log("You sunk the opponent's Carrier!", FontStyle.HIT);
                        view.sinkShip(0, "TARGET");
                        view.playSound("targetSunk");
                    }
                    else if(msg.equals("BATTLESHIP"))
                    {
                        view.log("You sunk the opponent's Battleship!", FontStyle.HIT);
                        view.sinkShip(1, "TARGET");
                        view.playSound("targetSunk");
                    }
                    else if(msg.equals("DESTROYER"))
                    {
                        view.log("You sunk the opponent's Destroyer!", FontStyle.HIT);
                        view.sinkShip(2, "TARGET");
                        view.playSound("targetSunk");
                    }
                    else if(msg.equals("SUBMARINE"))
                    {
                        view.log("You sunk the opponent's Submarine!", FontStyle.HIT);
                        view.sinkShip(3, "TARGET");
                        view.playSound("targetSunk");
                    }
                    else if(msg.equals("CRUISER"))
                    {
                        view.log("You sunk the opponent's Cruiser!", FontStyle.HIT);
                        view.sinkShip(4, "TARGET");
                        view.playSound("targetSunk");
                    }
                    else {
                        view.playSound("hit");
                    }

                    if(turnHalf)
                    {
                        turnCount++;
                        view.log("Turn " + turnCount + ": ", FontStyle.BIG);
                    }
                    turnHalf = !turnHalf;
                }
                
           }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String displayPos(int col, int row) {
        char dispCol = (char) (col + 65);
        return dispCol + "" + (row + 1);
    }

    public void sendMove(String pos) {
        if(myTurn)
        {
            try {
                myTurn = false;
                output.writeObject(pos);
                output.flush();

                int col = Integer.valueOf(pos) % 10;
                int row = Integer.valueOf(pos) / 10;
                view.log("You shot at: " + displayPos(col, row), FontStyle.SMALL);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public void sendHit(int col, int row) {
        try {
            boolean hit = model.checkHit(col, row);
            view.drawTile(col, row, hit, "OCEAN");
            if(hit)
            {
                view.log("HIT!", FontStyle.HIT);
                Ship ship = findShipFromTile(col, row);
                if(model.isAlive(ship))
                {
                    output.writeObject("HIT");
                    view.playSound("hit");
                }
                else
                {
                    switch(ship.getName()) {
                        case CARRIER:
                            output.writeObject("CARRIER");
                            view.log("The opponent sunk your Carrier!", FontStyle.HIT);
                            view.sinkShip(0, "OCEAN");
                            view.playSound("oceanSunk");
                            break;
                        case BATTLESHIP:
                            output.writeObject("BATTLESHIP");
                            view.log("The opponent sunk your Battleship!", FontStyle.HIT);
                            view.sinkShip(1, "OCEAN");
                            view.playSound("oceanSunk");
                            break;
                        case DESTROYER:
                            output.writeObject("DESTROYER");
                            view.log("The opponent sunk your Destroyer!", FontStyle.HIT);
                            view.sinkShip(2, "OCEAN");
                            view.playSound("oceanSunk");
                            break;
                        case SUBMARINE:
                            output.writeObject("SUBMARINE");
                            view.log("The opponent sunk your Submarine!", FontStyle.HIT);
                            view.sinkShip(3, "OCEAN");
                            view.playSound("oceanSunk");
                            break;
                        case CRUISER:
                            output.writeObject("CRUISER");
                            view.log("The opponent sunk your Cruiser!", FontStyle.HIT);
                            view.sinkShip(4, "OCEAN");
                            view.playSound("oceanSunk");
                            break;
                    }

                    gameOver = true;
                    for(int i = 0; i < 5; i++)
                    {
                        if(model.isAlive(model.getShip(i)))
                        {
                            gameOver = false;
                        }
                    }
                    if(gameOver)
                    {
                        view.log("Game over!",  FontStyle.BIG);
                    }


                }
            }
            else
            {
                output.writeObject("MISS");
                view.log("Miss.", FontStyle.MISS);
                view.playSound("miss");
            }
            output.flush();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
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
                        System.out.println("Ship found: " + ship.getName());
                        return ship;
                        
                    }
                }
            }
        }
        return null;
    }

    public class ButtonListener implements ActionListener{

        String pos;

        @Override
        public void actionPerformed(ActionEvent e) {

            JButton button = (JButton) e.getSource();
            String command = button.getActionCommand();
            
            
            if(command.equals("FIRE"))
            {
                if(myTurn && pos != null && !gameOver)
                {
                    view.disableButton(pos);
                    view.setEnabled("FIRE", false);
                    //view.playSound("fire");

                    sendMove(pos);
                    sentPos = pos;

                    myTurn = false;
                }
            }
            else if(!gameOver)
            {
                
                view.selectButton(button);
                pos = button.getActionCommand();
                if(myTurn)
                {
                    view.setEnabled("FIRE", true);
                }
            }
        }
    };

    class ButtonMouseListener extends MouseAdapter{
        public void mouseEntered(java.awt.event.MouseEvent evt) {
            JButton button = (JButton) evt.getComponent();
            button.setIcon(new ImageIcon("images/selected.png"));
        }
                
        public void mouseExited(java.awt.event.MouseEvent evt) {
            JButton button = (JButton) evt.getComponent();
            if(button != view.getPrevButton())
                button.setIcon(null);
        }
    }
}
