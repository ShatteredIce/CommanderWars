package packets;
//Packet for capturepoint information
public class TileInfo {
	
	private int tileX;
	private int tileY;
	private int tileId;
	

	public TileInfo(){
		
	}
	
	public TileInfo(int newx, int newy, int id){
		tileX = newx;
		tileY = newy;
		tileId = id;
	}

	public int getTileX() {
		return tileX;
	}

	public int getTileY() {
		return tileY;
	}

	public int getTileId() {
		return tileId;
	}

}
