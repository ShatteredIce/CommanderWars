package packets;

public class ScoreData {
	
	private int red;
	private int blue;
	
	public ScoreData() {
		
	}
	
	public ScoreData(int newred, int newblue){
		red = newred;
		blue = newblue;
	}
	
	public int getRed(){
		return red;
	}
	
	public int getBlue(){
		return blue;
	}

}
