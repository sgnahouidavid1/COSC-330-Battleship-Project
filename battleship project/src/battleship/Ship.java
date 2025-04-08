package battleship;

public class Ship {
        
        private int col, row, length;
        private ShipName name;
        private Direction dir;
        private boolean exists = false;

        public Ship(ShipName name) {
                this.name = name;
                this.dir = Direction.HORIZONTAL;
                switch(name){
                        case CARRIER:
                                length = 5;
                                break;
                        case BATTLESHIP:
                                length = 4;
                                break;
                        case DESTROYER:
                                length = 3;
                                break;
                        case SUBMARINE:
                                length = 3;
                                break;
                        case CRUISER:
                                length = 2;
                }
        }

        public ShipName getName() {
                return name;
        }

        public int getLength() {
                return length;
        }

        public void setPos(int col, int row) {
                this.col = col;
                this.row = row;
                exists = true;
        }
        
        public int getCol() {
                return col;
        }

        public int getRow() {
                return row;
        }

        public boolean exists() {
                return exists;
        }

        public void setDirection(Direction dir) {
                this.dir = dir;
        }

        public Direction getDirection() {
                return dir;
        }

        public int getDispCol(int disp) {
                if(dir == Direction.HORIZONTAL)
                        return (col + disp);
                return col;
        }

        public int getDispRow(int disp) {
                if(dir == Direction.VERTICAL)
                        return (row + disp);
                return row;
        }
}
