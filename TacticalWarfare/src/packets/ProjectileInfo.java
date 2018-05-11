package packets;

public class ProjectileInfo {
	
	int color;
	double unitX;
	double unitY;
	double angle;
	int texId;
	double[] vertices;
	double[] outlineVertices;
	int current_lifetime;
	boolean animating = false;
	
	public ProjectileInfo() {
		
	}
	
	public ProjectileInfo(int newcolor, double x, double y, double newangle, int newtexId, int mylifetime, double[] newvertices){
		color = newcolor;
		unitX = x;
		unitY = y;
		angle = newangle;
		texId = newtexId;
		current_lifetime = mylifetime;
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
	
	public double[] getTexCoords(float lightLevel) {
		if(texId == 20) {
			return new double[] {0, 1, 1, 1, 0, 0, 1, 0};
		}
		else if(texId == 22) {
			double offset = 0;
			if(current_lifetime % 200 > 180 && lightLevel < 0.2 && !animating) {
				animating = true;
			}
			if(current_lifetime % 200 > 180 && animating) {
				 if(current_lifetime % 2 == 0) { //even 
					 offset = ((current_lifetime % 100) - 80) / 20d;
				 }
				 else { //odd
					 offset = ((current_lifetime % 100) - 79) / 20d;
				 }
			}
			else if(animating) {
				animating = false;
			}
			return new double[] {0 + offset, 0, 0 + offset, 1, 0.1 + offset, 0, 0.1 + offset, 1}; 
		}
		return new double[] {0, 0, 0, 1, 1, 0, 1, 1};
	}
	
	public double[] getVertices(){
		return vertices;
	}
	
	public double[] getOutlineVertices(){
		return outlineVertices;
	}

}
