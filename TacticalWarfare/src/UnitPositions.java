import java.util.ArrayList;

public class UnitPositions {
	
	ArrayList<UnitInfo> unitdata = new ArrayList<>();
	
	public UnitPositions(){
		
	}
	
	public UnitPositions(ArrayList<Unit> units){
		for (int i = 0; i < units.size(); i++) {
			Unit current = units.get(i);
			unitdata.add(new UnitInfo(current.getId(), current.getOwnerId(), current.getTeam(), current.getX(), current.getY(), current.getAngle(), current.getColor(), current.getVertices(), current.getOutlineVertices()));
		}
	}


}
