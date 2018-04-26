package packets;

public class PlayerInfo {
	
	private int action;
	private int color;
	private int id;
	
	public PlayerInfo(){
		
	}
	
	public PlayerInfo(int newaction, int newcolor, int newid){
		action = newaction;
		color = newcolor;
		id = newid;
	}

	public int getAction() {
		return action;
	}

	public int getColor() {
		return color;
	}

	public int getId() {
		return id;
	}

}
