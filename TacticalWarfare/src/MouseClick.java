import java.util.ArrayList;

public class MouseClick {
	
	double x;
	double y;
	ArrayList<Integer> unitIds;
	
	public MouseClick(){
		
	}
	
	public MouseClick(double newx, double newy, ArrayList<Integer> ids){
		x = newx;
		y = newy;
		unitIds = ids;
	}

}