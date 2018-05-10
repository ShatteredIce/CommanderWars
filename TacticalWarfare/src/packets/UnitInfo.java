package packets;

public class UnitInfo {

	int id;
	int ownerid;
	String team;
	double unitX;
	double unitY;
	double angle;
	int color;
	int health;
	double[] vertices;
	double[] outlineVertices;
	
	private int max_health = 10;
	
	public UnitInfo(){
		
	}
	
	public UnitInfo(int newid, int newownerid, String myteam, double x, double y, double newangle, int newcolor, int newhealth, double[] newvertices, double[] newoutlinevertices){
		id = newid;
		ownerid = newownerid;
		team = myteam;
		unitX = x;
		unitY = y;
		angle = newangle;
		color = newcolor;
		health = newhealth;
		vertices = newvertices;
		outlineVertices = newoutlinevertices;
	}
	
	public UnitInfo(int newid){
		id = newid;
	}
	
	public int getId(){
		return id;
	}
	
	public int getOwnerId(){
		return ownerid;
	}
	
	public String getTeam(){
		return team;
	}
	
	public double getX(){
		return unitX;
	}
	
	public double getY(){
		return unitY;
	}
	
	public double getAngle(){
		return angle;
	}
	
	public int getColor() {
		return color;
	}
	
	public double[] getVertices(){
		return vertices;
	}
	
	public double[] getOutlineVertices(){
		return outlineVertices;
	}
	
	public int getHealth() {
		return health;
	}
	
	public int getMaxHealth() {
		return max_health;
	}


}
