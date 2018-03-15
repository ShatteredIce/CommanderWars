package packets;

public class MapData {
	
	private int[][] data;
	private int tick;
	
	public MapData(){
		
	}
	
	public MapData(int[][] newdata, int newtick){
		data = newdata;
		tick = newtick;
	}

	public int[][] getData() {
		return data;
	}
	
	public int getTick() {
		return tick;
	}

}
