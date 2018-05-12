package packets;

public class ScoreData {
	
	private int red;
	private int blue;
	private int tick;
	private float lightLevel;
	
	public ScoreData() {
		
	}
	
	public ScoreData(int newred, int newblue, int newtick, float newlevel){
		red = newred;
		blue = newblue;
		tick = newtick;
		lightLevel = newlevel;
	}
	
	public int getRed(){
		return red;
	}
	
	public int getBlue(){
		return blue;
	}
	
	public int getTick() {
		return tick;
	}
	
	public float getLightLevel() {
		return lightLevel;
	}

}
