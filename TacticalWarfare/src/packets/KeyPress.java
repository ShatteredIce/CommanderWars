package packets;
import java.util.ArrayList;

public class KeyPress {
	
	private int key;
	private ArrayList<Integer> unitIds;
	
	public KeyPress(){
		
	}
	
	public KeyPress(int newkey, ArrayList<Integer> ids){
		key = newkey;
		unitIds = ids;
	}

	public double getKey() {
		return key;
	}

	public ArrayList<Integer> getUnitIds() {
		return unitIds;
	}

}