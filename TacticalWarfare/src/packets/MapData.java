package packets;

import java.util.ArrayList;

public class MapData {
	
	private int[][] data;
	private int tick;
	private ArrayList<int[]> redSpawns;
	private ArrayList<int[]> blueSpawns;

	
	public MapData(){
		
	}
	
	public MapData(int[][] newdata, int newtick, ArrayList<int[]> newredSpawns, ArrayList<int[]> newblueSpawns){
		data = newdata;
		tick = newtick;
		redSpawns = newredSpawns;
		blueSpawns = newblueSpawns;
	}

	public int[][] getData() {
		return data;
	}
	
	public int getTick() {
		return tick;
	}
	
	public ArrayList<int[]> getRedSpawns(){
		return redSpawns;
	}
	
	public ArrayList<int[]> getBlueSpawns(){
		return blueSpawns;
	}

}
