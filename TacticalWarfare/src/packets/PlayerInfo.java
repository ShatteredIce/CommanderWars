package packets;

public class PlayerInfo {
	
	private int action;
	private String team;
	private int id;
	
	public PlayerInfo(){
		
	}
	
	public PlayerInfo(int newaction, String newteam, int newid){
		action = newaction;
		team = newteam;
		id = newid;
	}

	public int getAction() {
		return action;
	}

	public String getTeam() {
		return team;
	}

	public int getId() {
		return id;
	}

}
