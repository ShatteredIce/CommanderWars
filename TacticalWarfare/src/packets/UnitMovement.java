package packets;
import java.util.ArrayList;

public class UnitMovement {
	
	private boolean left;
	private boolean up;
	private boolean right;
	private ArrayList<Integer> unitIds;
	
	public UnitMovement(){
		
	}
	
	public UnitMovement(ArrayList<Integer> ids, boolean newleft, boolean newup, boolean newright){
		unitIds = ids;
		left = newleft;
		up = newup;
		right = newright;
	}
	

	public boolean getLeft() {
		return left;
	}
	
	public boolean getUp() {
		return up;
	}

	public boolean getRight() {
		return right;
	}

	public ArrayList<Integer> getUnitIds() {
		return unitIds;
	}

}