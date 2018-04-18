package packets;

public class ProjectileInfo {
	
	String team;
	double unitX;
	double unitY;
	double angle;
	int color;
	double[] vertices;
	double[] outlineVertices;
	
	public ProjectileInfo() {
		
	}
	
	public ProjectileInfo(String myteam, double x, double y, double newangle, int newcolor, double[] newvertices){
		team = myteam;
		unitX = x;
		unitY = y;
		angle = newangle;
		color = newcolor;
		vertices = newvertices;
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

}
