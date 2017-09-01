import java.util.ArrayList;


public class Player {
	
	String team;
	int id;
	ArrayList<Unit> selectedUnits = new ArrayList<>();
	
	public Player(){

	}
	
	public Player(String myteam, int newid){
		team = myteam;
		id = newid;
	}
	
	public String getTeam(){
		return team;
	}
	
	public int getId(){
		return id;
	}
	
	public ArrayList<Unit> getSelectedUnits(){
		return selectedUnits;
	}

}
