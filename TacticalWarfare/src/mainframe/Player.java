package mainframe;
import java.util.ArrayList;


public class Player {
	int color;
	int id;
	ArrayList<Unit> selectedUnits = new ArrayList<>();
	
	public Player(){

	}
	
	public Player(int newcolor, int newid){
		color = newcolor;
		id = newid;
	}
	
	public int getColor(){
		return color;
	}
	
	public int getId(){
		return id;
	}
	
	public ArrayList<Unit> getSelectedUnits(){
		return selectedUnits;
	}

}
