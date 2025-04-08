package battleship;

public class Model { 

	private Tile[][] ocean;
	private Tile[][] target;
	private Ship[] ships;

	public Model() {

		ocean = new Tile[10][10];
		target = new Tile[10][10];

		for (int row = 0; row < 10; row++ ) {
			for (int col = 0; col < 10; col++) {
				ocean[col][row] = Tile.OCEAN;
				target[col][row] = Tile.OCEAN;
			}
		}

		ships = new Ship[5];
		ships[0] = new Ship(ShipName.CARRIER);
		ships[1] = new Ship(ShipName.BATTLESHIP);
		ships[2] = new Ship(ShipName.DESTROYER);
		ships[3] = new Ship(ShipName.SUBMARINE);
		ships[4] = new Ship(ShipName.CRUISER);
	}

	public boolean gameOver() {
		return false;
	}

	public Ship getShip(ShipName name) {
		
		for(int i = 0; i < 5; i++)
		{
			if(ships[i].getName() == name)
			{
				return ships[i];
			}
		}
		return null;
	}

	public Ship getShip(int i) {
		return ships[i];
	}

	public boolean isValidTile(int col, int row) {
		if(onGrid(col, row))
		{
			if(ocean[col][row] == Tile.OCEAN)
			{
				return true;
			}
		}
		return false;
	}

	public boolean isValidPlacement(Ship ship) {

		for(int i = 0; i < ship.getLength(); i++)
		{
			if(!isValidTile(ship.getDispCol(i), ship.getDispRow(i)))
				return false;
		}
		return true;
	}

	public void placeShip(Ship ship) {
		for(int i = 0; i < ship.getLength(); i++)
		{
			ocean[ship.getDispCol(i)][ship.getDispRow(i)] = Tile.SHIP;
		}
	}

	public boolean checkHit(int col, int row) {
		if(ocean[col][row] == Tile.SHIP)
		{
			ocean[col][row] = Tile.HIT;
			return true;
		}
		ocean[col][row] = (Tile.MISS);
		return false;
	}

	public boolean isAlive(Ship ship) {
		
		for(int i = 0; i < ship.getLength(); i++)
		{
			if(ocean[ship.getDispCol(i)][ship.getDispRow(i)] != Tile.HIT)
				return true;
		}
		return false;
	}

	public Tile[][] getOcean() {
		return ocean;
	}

	public Tile[][] getTarget() {
		return target;
	}

	public void setOceanTile(int col, int row, Tile tile)
	{
		ocean[col][row] = tile;
	}

	public boolean allShipsPlaced() {
        for(int i = 0; i < 5; i++)
        {
			boolean bool = shipPlaced(ships[i]);
			if(!bool)
				return false;
        }
		return true;
    }

	public boolean shipPlaced(Ship ship) {
		if(ship.exists())
		{
        	return onGrid(ship.getCol(), ship.getRow());
		}
		return false;
	}

	public boolean onGrid(int col, int row) {
		if(col >= 0 && col < 10 && row >= 0 && row < 10)
			return true;
		return false;
	}
} 

