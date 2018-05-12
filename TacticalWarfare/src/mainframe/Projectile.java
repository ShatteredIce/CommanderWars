package mainframe;

import java.util.Random;

public class Projectile {
	
	Random random = new Random();
	Unit owner;
	int color;
	double[] vertices = new double[8];
	Point[] points; 
		
	Point center;
	double angle;
	double speed;
	int damage;
	int lifetime;
	int current_lifetime = 0;
	int texId;
	boolean animating = false;
	
	public Projectile(Unit newowner, int newcolor, double spawnx, double spawny, double newangle, double newspeed, int newdamage, int newlifetime, int newtexId) {
		owner = newowner;
		color = newcolor;
		center = new Point(spawnx, spawny);
		angle = newangle;
		speed = newspeed;
		damage = newdamage;
		lifetime = newlifetime;
		texId = newtexId;
		createPoints();
		setPoints();
	}
	
	public void createPoints(){
		int width = 1;
		int height = 1;
		if(texId == 19) {
			width = 2;
			height = 8;
		}
		else if(texId == 20) {
			 width = 5;
			 height = 22;
		}
		else if(texId == 21) {
			width = 32;
			height = 32;
		}
		Point[] newpoints = new Point[]{
			new Point(-width, -height, true),
			new Point(-width, height, true),
			new Point(width, -height, true),
			new Point(width, height, true),
		};
		points = newpoints;
	}
	
	public boolean setPoints(){
		Point newcenter = new Point(center.X(), center.Y()+speed);
		newcenter.rotatePoint(center.X(), center.Y(), angle);
		center.setX(newcenter.X());
		center.setY(newcenter.Y()); 
		int v_index = 0;
		for (int i = 0; i < points.length; i++) {
			points[i].setX(center.X() + points[i].getXOffset());
			points[i].setY(center.Y() + points[i].getYOffset());
			points[i].rotatePoint(center.X(), center.Y(), angle);
			v_index = 2*i;
			vertices[v_index] = points[i].X();
			vertices[v_index+1] = points[i].Y();	
		}
		if(updateLifetime()){
			return true;
		}
		else{
			return false;
		}
	}
	
	//returns false if projectile is destroyed
	public boolean updateLifetime(){
		current_lifetime++;
		if(current_lifetime >= lifetime){
			destroy();
			return false;
		}
		if(texId == 21 && current_lifetime > 100) { //arm mines
			texId = 22;
		}
		return true;
	}
	
	public void destroy() {
		if(texId == 22) {
			owner.setNumMines(owner.getNumMines() - 1);
		}
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
	
	public int getTexId() {
		return texId;
	}
	
	public int getColor() {
		return color;
	}
	
	public double getAngle(){
		return angle;
	}
	
	public int getCurrentLifetime() {
		return current_lifetime;
	}
	
	public double[] getVertices(){
		return vertices;
	}
	
	public double getX(){
		return center.X();
	}
	
	public double getY(){
		return center.Y();
	}
	
	public int getDamage() {
		return damage;
	}
	
	public Point[] getPoints() {
		return points;
	}
	
}
