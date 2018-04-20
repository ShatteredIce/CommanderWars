package mainframe;

import java.util.Random;

public class Projectile {
	
	Random random = new Random();
	Unit owner;
	String team;
	double[] vertices = new double[8];
	Point[] points; 
		
	Point center;
	double angle;
	double speed;
	int damage;
	double lifetime;
	double current_lifetime = 0;
	int texid;
	
	public Projectile(Unit newowner, String myteam, double spawnx, double spawny, double newangle, double newspeed, int newdamage, double newlifetime, int newtexid) {
		owner = newowner;
		team = myteam;
		center = new Point(spawnx, spawny);
		angle = newangle;
		speed = newspeed;
		damage = newdamage;
		lifetime = newlifetime;
		texid = newtexid;
		createPoints();
		setPoints();
	}
	
	public void createPoints(){
		int width = 1;
		int height = 1;
		if(texid == 20) {
			 width = 2;
			 height = 8;
		}
		else if(texid == 21) {
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
		current_lifetime += 1;
		if(current_lifetime >= lifetime){
			destroy();
			return false;
		}
		return true;
	}
	
	public void destroy() {
		if(texid == 21) {
			owner.setNumMines(owner.getNumMines() - 1);
		}
	}
	
	public int getColor() { //color is texture id
		return texid;
	}
	
	public String getTeam(){
		return team;
	}
	
	public double getAngle(){
		return angle;
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
