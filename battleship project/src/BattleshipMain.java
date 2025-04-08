import java.io.IOException;


import battleship.View;
import battleship.Model;
import battleship.SetupControl;
import battleship.GameControl;
public class BattleshipMain {
	
	public static void main(String[] args) throws IOException {
		Model model = new Model();
		View view = new View(model);
		GameControl gameControl = new GameControl(model, view);
		SetupControl setupControl = new SetupControl(model, view, gameControl);


		view.displayServerModeDialog();


	}
}		
