package battleship;
import javax.swing.*;///using for GUI
import javax.swing.border.Border;

import battleship.SetupControl.ClickListener;
import battleship.SetupControl.DragListener;
import battleship.GameControl.ButtonMouseListener;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;

import javax.sound.sampled.*;
import java.io.File;

public class View {

    private JFrame frame = new JFrame("Battleship"); //frame for main game
    private JButton[][] target;
    private JLabel[][] ocean;
    private JLabel log = new JLabel();
    private JDialog placementDialog;
    private Border blackline = BorderFactory.createLineBorder(Color.black);
    Point prevPoint;

    private JLayeredPane dndPanel;
    private JPanel shipPanel;
    private Component clickTarget;
    private ShipLabel dndLabel;
    private JPanel placementGrid;
    private ShipLabel[] ships, yourShips, myShips;
    private JLabel[][] placementTile;
    private Point imageUpperLeft;
    private Direction defaultDirection;

    private JButton dialogButton, autoButton;
    private JPanel logPanel, targetGrid, oceanGrid, targetOverlayGrid, oceanOverlayGrid;
    private JLabel[][] activeGrid;
    private JButton serverButton, clientButton, playButton;
    private JTextField ipField;
    private JDialog serverModeFrame, waitDialog;
    private JButton prevButton;
    private JButton fireButton;
    private boolean serverMode;
    private JLabel[][] targetOverlay, oceanOverlay;

    class ShipLabel extends JLabel {
        private ShipName name;
        private ImageIcon icon;
        private int width;
        private int height = 32;

        ShipLabel(ShipName name) {
            this.name = name;
            switch(name) {
                case CARRIER:
                    icon = new ImageIcon("images/CARRIER_FULL.png");
                    width = height * 5;
                    break;
                case BATTLESHIP:
                    icon = new ImageIcon("images/BATTLESHIP_FULL.png");
                    width = height * 4;
                    break;
                case DESTROYER:
                    icon = new ImageIcon("images/DESTROYER_FULL.png");
                    width = height * 3;
                    break;
                case SUBMARINE:
                    icon = new ImageIcon("images/SUBMARINE_FULL.png");
                    width = height * 3;
                    break;
                case CRUISER:
                    icon = new ImageIcon("images/CRUISER_FULL.png");
                    width = height * 2;
            }
            icon = rotateImageIcon(icon, defaultDirection);
            if(defaultDirection == Direction.VERTICAL)
            {
                int tmp = width;
                width = height;
                height = tmp;
            }
            
            icon = resizeIcon(icon, width, height);
            setIcon(icon);
            setSize(width, height);
        }

        public ShipName getShipName() {
            return name;
        }

        public void setImageIcon(ImageIcon icon) {
            this.icon = icon;
            setIcon(icon);
        }

        public ImageIcon getImageIcon() {
            return icon;
        }
        
    }
    
    class ImagePanel extends JComponent {
        private Image image;
        public ImagePanel(Image image) {
            this.image = image;
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, this);
        }
    }
    //constructor
	public View(Model model) {

		//frame.setSize(784, 776);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(800, 776));

        defaultDirection = Direction.HORIZONTAL;

        dndPanel = new JLayeredPane();
        dialogButton = new JButton("Rotate ships");
        autoButton = new JButton("Place automatically");
        serverButton = new JButton("Server");
        clientButton = new JButton("Client");
        ipField = new JTextField("127.0.0.1");
        playButton = new JButton("Play");
        prevButton = null;
        fireButton = new JButton("Fire");


        JLayeredPane gamePane = new JLayeredPane();
        JLabel bg = new JLabel();
        bg.setIcon(new ImageIcon("images/bg.png"));
        bg.setBounds(0, 0, 800, 736);
        gamePane.add(bg, 8);

        //create opponent's side of the board
		targetGrid = new JPanel(new GridLayout(10, 10)); 
        targetGrid.setBounds(16, 16, 320, 320);
        targetGrid.setOpaque(false);
        target = new JButton[10][10];

        targetOverlayGrid = new JPanel(new GridLayout(10, 10));
        targetOverlayGrid.setBounds(16, 16, 320, 320);
        targetOverlayGrid.setOpaque(false);
        targetOverlay = new JLabel[10][10];

        oceanGrid = new JPanel(new GridLayout(10, 10));
        oceanGrid.setBounds(192, 352, 320, 320);
        oceanGrid.setOpaque(false);
        ocean = new JLabel[10][10];

        oceanOverlayGrid = new JPanel(new GridLayout(10, 10));
        oceanOverlayGrid.setBounds(192, 352, 320, 320);
        oceanOverlayGrid.setOpaque(false);
        oceanOverlay = new JLabel[10][10];

		for (int row = 0; row < 10; row++) {
			for (int col = 0; col < 10; col++) {
				target[col][row] = new JButton();
                target[col][row].setContentAreaFilled(false);
                target[col][row].setBorderPainted(false);
                target[col][row].addMouseListener(new java.awt.event.MouseAdapter() {});
				targetGrid.add(target[col][row]);

                targetOverlay[col][row] = new JLabel();
                targetOverlayGrid.add(targetOverlay[col][row]);

                ocean[col][row] = new JLabel();
                oceanGrid.add(ocean[col][row]);

                oceanOverlay[col][row] = new JLabel();
                oceanOverlayGrid.add(oceanOverlay[col][row]);
			}
		}

        gamePane.add(targetGrid, 0);
        gamePane.add(targetOverlayGrid, 1);
        gamePane.add(oceanGrid, 1);
        gamePane.add(oceanOverlayGrid, 0);

        fireButton.setBounds(216, 688, 96, 32);
        fireButton.setEnabled(false);

        gamePane.add(fireButton, 2);

        //create chat log
        logPanel = new JPanel();
        logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.PAGE_AXIS));
        log = new JLabel("Progress Log");
        logPanel.add(log);

        JScrollPane logPane = new JScrollPane(logPanel);
        logPane.setBounds(528, 16, 240, 656);
        gamePane.add(logPane, 0);

        //create status panels

        ships = new ShipLabel[5];
        ships[0] = new ShipLabel(ShipName.CARRIER);
        ships[1] = new ShipLabel(ShipName.BATTLESHIP);
        ships[2] = new ShipLabel(ShipName.DESTROYER);
        ships[3] = new ShipLabel(ShipName.SUBMARINE);
        ships[4] = new ShipLabel(ShipName.CRUISER);

        JPanel myStatusPanel = new JPanel(new GridBagLayout());
        myStatusPanel.setBounds(16, 352, 160, 320);
        myStatusPanel.setOpaque(false);

        JPanel yourStatusPanel = new JPanel();
        yourStatusPanel.setBounds(352, 16, 160, 320);
        yourStatusPanel.setLayout(new GridBagLayout());
        yourStatusPanel.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;

        myShips = new ShipLabel[5];
        yourShips = new ShipLabel[5];

        Font labelFont = new Font("Dialog", Font.BOLD, 17);
        JLabel myLabel = new JLabel("Your Ships");
        myLabel.setFont(labelFont);
        myStatusPanel.add(myLabel, c);

        JLabel yourLabel = new JLabel("Opponent's Ships");
        yourLabel.setFont(labelFont);
        yourStatusPanel.add(yourLabel, c);
        
        for(int i = 0; i < 5; i++)
        {
            myShips[i] = new ShipLabel(ships[i].getShipName());
            yourShips[i] = new ShipLabel(ships[i].getShipName());
            c.gridy++;
            myStatusPanel.add(myShips[i], c);
            yourStatusPanel.add(yourShips[i], c);
        }

        gamePane.add(myStatusPanel, 0);
        gamePane.add(yourStatusPanel, 0);

        frame.add(gamePane);
        frame.setVisible(true);
	}

    public void playSound(String fileName) {
        try {
            File soundFile = new File("sounds/" + fileName + ".wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
            System.out.println(fileName + " audio played ?");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sinkShip(int index, String player) {
        ShipLabel[] playerShips = new ShipLabel[5];
        if(player.equals("TARGET"))
            playerShips = yourShips;
        else
            playerShips = myShips;

        playerShips[index].setVisible(false);
    }

    public void log(String text, FontStyle style) {
        JLabel log = new JLabel(text);
        Font font;
        if(style == FontStyle.SMALL)
        {
            font = new Font("Dialog", Font.PLAIN, 12);
        }
        else if(style == FontStyle.HIT)
        {
            font = new Font("Dialog", Font.BOLD, 12);
            log.setForeground(new Color(214, 108, 100));
        }
        else if(style == FontStyle.MISS)
        {
            font = new Font("Dialog", Font.BOLD, 12);
        }
        else
        {
            font = new Font("Dialog", Font.BOLD, 14);
        }
        log.setFont(font);
        logPanel.add(log);
    }

    public void gameOver() {
        
    }

    public void displayServerModeDialog() {
        serverModeFrame = new JDialog(frame, "Battleship");
        serverModeFrame.setSize(300, 200);
        serverModeFrame.setResizable(false);
        JPanel serverModePanel = new JPanel();
        serverModePanel.setLayout(new FlowLayout());
        
        JPanel buttonPanel = new JPanel();

        
        serverButton.setFocusPainted(false);

        
        clientButton.setFocusPainted(false);

        ButtonGroup group = new ButtonGroup();
        group.add(serverButton);
        group.add(clientButton);
        buttonPanel.add(serverButton);
        buttonPanel.add(clientButton);
        buttonPanel.setPreferredSize(new Dimension(300, 50));

        
        ipField.setPreferredSize(new Dimension(150, 24));
        ipField.setEnabled(false);
        JPanel ipPanel = new JPanel();
        ipPanel.setBackground(Color.BLUE);
        ipPanel.add(ipField);
        ipPanel.setPreferredSize(new Dimension(200, 50));
        
        playButton.setFocusPainted(false);

        serverModePanel.add(buttonPanel);
        serverModePanel.add(ipField);
        serverModePanel.add(playButton);
        serverModeFrame.add(serverModePanel);
        serverModeFrame.setLocationRelativeTo(frame);
        serverModeFrame.setVisible(true);
    }

    public void setServerMode(boolean bool) {
        serverMode = bool;
        ipField.setEnabled(!bool);
        serverButton.setEnabled(!bool);
        clientButton.setEnabled(bool);
    }

    public boolean getServerMode() {
        return serverMode;
    }

    public void waitingToConnect() {
        serverModeFrame.dispose();
        waitDialog = new JDialog(frame, "Waiting to connect...");
        waitDialog.setSize(new Dimension(617, 392));
        waitDialog.setResizable(false);
        waitDialog.setLocationRelativeTo(frame);
        waitDialog.setVisible(true);
    }

    public String getIP() {
        return ipField.getText();
    }

    public void displaySelectDialog() {

        JPanel buttonPanel, autoButtonPanel;

        waitDialog.dispose();
        placementDialog = new JDialog(frame, "Place your ships!");
        placementDialog.setSize(new Dimension(617, 392));
        placementDialog.setResizable(false);
        
        
        
        dndPanel.setBackground(Color.BLACK); 

        placementGrid = new JPanel(new GridLayout(10, 10));
        
        placementTile = new JLabel[10][10];
        activeGrid = placementTile;
        for(int row = 0; row < 10; row++)
        {
            for(int col = 0; col < 10; col++)
            {
                placementTile[col][row] = new JLabel();
                placementTile[col][row].setBorder(blackline);
                placementTile[col][row].setPreferredSize(new Dimension(32, 32));
                placementGrid.add(placementTile[col][row]);
            }
        }


        dndPanel.add(placementGrid, 2);
        placementGrid.setBounds(16, 16, 320, 320);
        
        
        dialogButton.setFocusPainted(false);

        
        buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.add(dialogButton);
        dndPanel.add(buttonPanel, 1);
        buttonPanel.setBounds(352, 16, 216, 48);

        
        autoButton.setFocusPainted(false);

        autoButtonPanel = new JPanel(new GridBagLayout());
        autoButtonPanel.add(autoButton);
        dndPanel.add(autoButtonPanel, 4);
        autoButtonPanel.setBounds(352, 288, 216, 48);

        shipPanel = new JPanel();
        shipPanel.setLayout(new BoxLayout(shipPanel, BoxLayout.PAGE_AXIS));
        
        Dimension rigid = new Dimension(10, 10);
        shipPanel.add(Box.createRigidArea(rigid));
        for(int i = 0; i < 5; i++)
        {
            ships[i].setAlignmentY(Component.TOP_ALIGNMENT);
            shipPanel.add(ships[i]);
            shipPanel.add(Box.createRigidArea(rigid));
        }
        
        dndPanel.add(shipPanel, 1);
        shipPanel.setBounds(352, 68, 216, 216);
        placementDialog.add(dndPanel);
        frame.setVisible(true);
        placementDialog.setLocationRelativeTo(frame);
        placementDialog.setVisible(true);
    }

    public void displayMainGame() {
        placementDialog.dispose();
        activeGrid = ocean;

    }

    public void selectButton(JButton button) {
        if(prevButton != null)
        {
            prevButton.setIcon(null);
        }
        fireButton.setEnabled(true);
        button.setIcon(new ImageIcon("images/selected.png"));
        prevButton = button;
    }

    public JButton getPrevButton() {
        return prevButton;
    }

    public void registerSetupListeners(ClickListener clickListener, DragListener dragListener, ActionListener buttonListener) {
        dndPanel.addMouseListener(clickListener);
        dndPanel.addMouseMotionListener(dragListener);
        dialogButton.addActionListener(buttonListener);
        dialogButton.setActionCommand("ROTATE");
        autoButton.addActionListener(buttonListener);
        autoButton.setActionCommand("AUTO");
        serverButton.addActionListener(buttonListener);
        serverButton.setActionCommand("SERVER");
        clientButton.addActionListener(buttonListener);
        clientButton.setActionCommand("CLIENT");
        playButton.addActionListener(buttonListener);
        playButton.setActionCommand("PLAY");
        
    }

    public void registerGameListeners(ButtonMouseListener mouseListener, ActionListener actionListener) {
        fireButton.addActionListener(actionListener);
        fireButton.setActionCommand("FIRE");
        for (int row = 0; row < 10; row++) {
            for(int col = 0; col < 10; col++) {
			    target[col][row].addActionListener(actionListener);
                target[col][row].addMouseListener(mouseListener);
			    target[col][row].setActionCommand(Integer.toString((10 * row) + col));
            }
		}
    }

    public void setEnabled(String name, boolean bool) {
        JButton button = new JButton();
        if(name == "FIRE")
            button = fireButton;

        button.setEnabled(bool);
    }

    public void disableButton(String pos) {
        int x = Integer.valueOf(pos);
        int col = x % 10;
        int row = x / 10;
        target[col][row].setEnabled(false);
    }

    public void rotateButton() {
        switch(defaultDirection) {
            case VERTICAL:
                defaultDirection = Direction.HORIZONTAL;
                shipPanel.setLayout(new BoxLayout(shipPanel, BoxLayout.PAGE_AXIS));
                break;
            case HORIZONTAL:
                defaultDirection = Direction.VERTICAL;
                shipPanel.setLayout(new BoxLayout(shipPanel, BoxLayout.LINE_AXIS));
        }
        for(int i = 0; i < 5; i++)
        {
            ImageIcon icon = ships[i].getImageIcon();
            icon = rotateImageIcon(icon, defaultDirection);
            ships[i].setIcon(icon);
        }
    }

    public void setDNDLabelVisible(boolean bool) {
        dndLabel.setVisible(bool);
    }

    public Direction getDefaultDirection() {
        return defaultDirection;
    }

    public boolean clickedOnShipPanel(MouseEvent event) {
        if(dndPanel.getComponentAt(event.getPoint()) == shipPanel) {
            if(shipPanel.getComponentAt(shipPanel.getMousePosition()) instanceof ShipLabel) {
                return true;
            }
        }
        return false;
    }

    public int getColOnGrid(Point point) {
        return Math.floorDiv(((int)point.getX() - placementGrid.getX()), 32);
    }

    public int getRowOnGrid(Point point) {
        return Math.floorDiv(((int)point.getY() - placementGrid.getY()), 32);
    }

    public void setAllShipsPlaced(boolean bool) {
        if(bool)
        {
            dialogButton.setText("Ready to play!");
            dialogButton.setActionCommand("READY");
            autoButton.setEnabled(false);
        }
        else
        {
            dialogButton.setText("Rotate ships");
            dialogButton.setActionCommand("ROTATE");
            autoButton.setEnabled(true);
        }
    }

    public ShipName shipBeingDragged() {
        return dndLabel.getShipName();
    }

    public void removeDnDLabel() {
        dndPanel.remove(dndLabel);
        dndLabel = null;
        dndPanel.repaint();
    }

    public void createDnDLabel(MouseEvent event) {

        clickTarget = (shipPanel.getComponentAt(shipPanel.getMousePosition()));
        clickTarget.setVisible(false);
        dndLabel = new ShipLabel(((ShipLabel)clickTarget).getShipName());
        
        int row = (int) (clickTarget.getY() + shipPanel.getY());
        int col = (int) (clickTarget.getX() + shipPanel.getX());

        //sets position to top right corner of ship being clicked
        dndLabel.setLocation(col, row);
        prevPoint = event.getPoint();
        dndPanel.add(dndLabel, 0);
        imageUpperLeft = dndLabel.getLocation();
    }

    public void createDnDLabel(MouseEvent event, Ship ship) {

        dndLabel = new ShipLabel(ship.getName());
        dndLabel.setIcon(null);

        int col = ship.getCol() * 32;
        int row = ship.getRow() * 32;

        //sets position to top right corner of ship being clicked
        dndLabel.setLocation(col, row);
        prevPoint = event.getPoint();
        dndPanel.add(dndLabel, 0);
        imageUpperLeft = dndLabel.getLocation();
    }

    public void hideAllShips() {
        for(int i = 0; i < 5; i++)
        {
            ships[i].setVisible(false);
        }
    }

    public boolean currentlyDragging() {
        if(dndLabel == null)
            return false;
        return true;
    }

    public void makeVisible(ShipName name) {
        for(int i = 0; i < 5; i++)
        {
            if(ships[i].getShipName() == name)
                ships[i].setVisible(true);
        }
    }

    public void translateDrag(MouseEvent event) {
        Point currPoint = event.getPoint();
        int dx = (int) (currPoint.getX() - prevPoint.getX());
        int dy = (int) (currPoint.getY() - prevPoint.getY());

        imageUpperLeft.translate(dx, dy);
        prevPoint = currPoint;
        dndLabel.setLocation(imageUpperLeft);

    }

    public Point getDNDLabelLocation() {
        return dndLabel.getLocation();
    }

    public int pointToCol(Point point) {
        double col = (imageUpperLeft.getX() - placementGrid.getX()) / 32;
        return (int) Math.round(col);
    }

    public int pointToRow(Point point) {
        double row = (imageUpperLeft.getY() - placementGrid.getY()) / 32;
        return (int) Math.round(row);
    }

    ImageIcon resizeIcon(ImageIcon icon, int width, int height) {
        Image image = icon.getImage();
        image = image.getScaledInstance(width,height, java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    void drawShip(Ship ship, float opacity) {
        for(int i = 0; i < ship.getLength(); i++)
        {
            String filename = "images/" + ship.getName() + "_" + (i+1) + ".png";
            ImageIcon icon = new ImageIcon(filename);

            if(opacity != 1.0)
            {
                Image image = icon.getImage();
                BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = bufferedImage.createGraphics();
                float alpha = 0.5f;
                AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
                g.setComposite(alphaComposite);
                g.drawImage(image, 0, 0, null);
                g.dispose();
                icon = new ImageIcon(bufferedImage);
            }

            
            icon = rotateImageIcon(icon, ship.getDirection());

            icon = resizeIcon(icon, 32, 32);

            activeGrid[ship.getDispCol(i)][ship.getDispRow(i)].setIcon(icon);
        }
    }

    void undrawShip(Ship ship) {
        for(int i = 0; i < ship.getLength(); i++)
        {
            placementTile[ship.getDispCol(i)][ship.getDispRow(i)].setIcon(null);
        }
    }

    void drawOcean(Tile[][] ocean) {
        for(int row = 0; row < 10; row++)
        {
            for(int col = 0; col < 10; col++)
            {
                if(placementTile[col][row].getIcon() != null)
                {
                    if(ocean[col][row] == Tile.OCEAN)
                    {
                        placementTile[col][row].setIcon(null);
                    }
                }
            }
        }
    }

    void drawTile(int col, int row, boolean hit, String boardName) {
        JLabel[][] board = new JLabel[10][10];
        if(boardName.equals("OCEAN"))
            board = oceanOverlay;
        else
            board = targetOverlay;
        if(hit)
            board[col][row].setIcon(new ImageIcon("images/hit.png"));
        else 
            board[col][row].setIcon(new ImageIcon("images/miss.png"));
    }

    ImageIcon rotateImageIcon(ImageIcon original, Direction dir) {

        if(dir == Direction.HORIZONTAL)
            return original;

        Image image = original.getImage();
        int width = image.getHeight(null);
        int height = image.getWidth(null);
        
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        AffineTransform at = new AffineTransform();
       
        at.rotate(Math.PI / 2, width / 2, height / 2);
        
        int x = (width - height) / 2;
        int y = (height - width) / 2;
        at.translate(x, y);

        g2d.setTransform(at);
        g2d.drawImage(image, 0, 0, null);
        return new ImageIcon(bufferedImage);
    }
}
