package packets;
import java.util.ArrayList;

public class MouseClick {
	
	private double x;
	private double y;
	private ArrayList<Integer> unitIds;
	
	public MouseClick(){
		
	}
	
	public MouseClick(double newx, double newy, ArrayList<Integer> ids){
		x = newx;
		y = newy;
		unitIds = ids;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public ArrayList<Integer> getUnitIds() {
		return unitIds;
	}

}