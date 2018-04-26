package packets;

public class ProjectileInfo {
	
	int color;
	double unitX;
	double unitY;
	double angle;
	int texId;
	double[] vertices;
	double[] outlineVertices;
	
	public ProjectileInfo() {
		
	}
	
	public ProjectileInfo(int newcolor, double x, double y, double newangle, int newtexId, double[] newvertices){
		color = newcolor;
		unitX = x;
		unitY = y;
		angle = newangle;
		texId = newtexId;
		vertices = newvertices;
	}
	
	public int getColor() {
		return color;
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
	
	public int getTexId(){
		return texId;
	}
	
	public double[] getVertices(){
		return vertices;
	}
	
	public double[] getOutlineVertices(){
		return outlineVertices;
	}

}
