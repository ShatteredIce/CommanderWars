package packets;

public class MapData {
	
	private int[][] data;
	
	public MapData(){
		
	}
	
	public MapData(int[][] newdata){
		data = newdata;
	}

	public int[][] getData() {
		return data;
	}

}
