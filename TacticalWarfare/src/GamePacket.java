import java.util.ArrayList;

public class GamePacket {
	
	ArrayList<Player> players;
	
	public GamePacket(){
		players = null;
	}
	
	public GamePacket(ArrayList<Player> allPlayers){
		players = allPlayers;
	}

}
