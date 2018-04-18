package mainframe;
import java.util.ArrayList;

public class Unit {
	
	static GameLogic gamelogic = new GameLogic();
	
	double[] vertices = new double[8];
	double[] outlineVertices = new double[8];
	Point[] points; 
	Point[] outlinePoints;
	
	Point center;
	int id;
	int ownerid;
	int color;
	String team;
	double angle;
	double current_velocity = 0;
	double base_velocity = 10;
	double terrain_movement_modifier = 1;
	double current_turn_speed = 0;
	double max_turn_speed = 2;
	int cooldown = 20;
	int current_cooldown = 0;
	int max_health = 10;
	int health = 10;
	
	Point locationTarget = null;
	ArrayList<Point> tilePath = new ArrayList<>();
	
	public Unit(){
		
	}
	
	public Unit(int myid, int newid, String newteam, double spawnx, double spawny, double spawnangle, int newcolor){
		id = myid;
		ownerid = newid;
		team = newteam;
		center = new Point(spawnx, spawny);
		angle = spawnangle;
		color = newcolor; //1 for red, 2 for blue
		createPoints();
		setPoints();
	}
	
	public void setPoints(){
		angle += current_turn_speed;
		angle = gamelogic.normalizeAngle(angle);
		//set the center point if not offscreen
		Point newcenter = new Point(center.X(), center.Y()+(current_velocity/terrain_movement_modifier));
		newcenter.rotatePoint(center.X(), center.Y(), angle);
		center.setX(newcenter.X());
		center.setY(newcenter.Y()); 
		//Set points for unit facing upward, then rotate points to player angle
		int v_index = 0;
		for (int i = 0; i < points.length; i++) {
			points[i].setX(center.X() + points[i].getXOffset());
			points[i].setY(center.Y() + points[i].getYOffset());
			points[i].rotatePoint(center.X(), center.Y(), angle);
			v_index = 2*i;
			vertices[v_index] = points[i].X();
			vertices[v_index+1] = points[i].Y();	
		}
		for (int i = 0; i < outlinePoints.length; i++) {
			outlinePoints[i].setX(center.X() + outlinePoints[i].getXOffset());
			outlinePoints[i].setY(center.Y() + outlinePoints[i].getYOffset());
			outlinePoints[i].rotatePoint(center.X(), center.Y(), angle);
			v_index = 2*i;
			outlineVertices[v_index] = outlinePoints[i].X();
			outlineVertices[v_index+1] = outlinePoints[i].Y();	
		}
	}
	
	public void setTerrainMovement(int modifier) {
		terrain_movement_modifier = modifier;
	}
	
	public void update(){
		moveToLocation();
		setPoints();
		if(current_cooldown > 0) {
			current_cooldown--;
		}
	}
	
	public void triggerCooldown() {
		current_cooldown = cooldown;
	}
	
	public void moveToLocation(){
		if(locationTarget == null && tilePath.size() > 0){
			locationTarget = tilePath.get(0);
			tilePath.remove(0);
		}
		if(locationTarget != null){
			double targetAngle = gamelogic.angleToPoint(this.getX(), this.getY(), locationTarget.X(), locationTarget.Y());
			double distance = gamelogic.distance(this.getX(), this.getY(), locationTarget.X(), locationTarget.Y());
			double leftBearing = gamelogic.getTurnDistance(angle, targetAngle, true);
			double rightBearing = gamelogic.getTurnDistance(angle, targetAngle, false);
			if(!(Math.min(leftBearing, rightBearing) == 0)){
				if(leftBearing <= rightBearing){ //turn left
					current_velocity = 0;
					current_turn_speed = Math.min(max_turn_speed, (gamelogic.normalizeAngle(targetAngle - angle)));
				}
				else{ //turn right
					current_velocity = 0;
					current_turn_speed = Math.max(-max_turn_speed, -(gamelogic.normalizeAngle(angle - targetAngle)));
				}
			}
			if(distance > 10 && Math.min(leftBearing, rightBearing) < 5){
				current_velocity = base_velocity;
			}
			else if(distance > 10){
				current_velocity = 0;
			}

			else{
				locationTarget = null;
				current_velocity = 0;
				current_turn_speed = 0;
			}
		}
	}
	
	public void setLocationTarget(double x, double y){
		locationTarget = new Point(x, y);
	}
	
	public void setLocationTarget(ArrayList<Point> path){
		locationTarget = null;
		tilePath = path;
		for (int i = 0; i < path.size(); i++) {
			System.out.println(path.get(i).X() + " " + path.get(i).Y());
		}
		System.out.println();
	}
	
	public void setPosition(double newx, double newy, double newangle){
		center.setX(newx);
		center.setY(newy);
		angle = newangle;
		setPoints();
	}
	
	public void createPoints(){
		int width = 28;
		int height = 14;
		Point[] newpoints = new Point[]{
			new Point(-width, -height, true),
			new Point(-width, height, true),
			new Point(width, -height, true),
			new Point(width, height, true),
		};
		points = newpoints;
		int outlineWidth = 30;
		int outlineHeight = 16;
		Point[] newoutlinepoints = new Point[]{
			new Point(-outlineWidth, -outlineHeight, true),
			new Point(-outlineWidth, outlineHeight, true),
			new Point(outlineWidth, -outlineHeight, true),
			new Point(outlineWidth, outlineHeight, true),
		};
		outlinePoints = newoutlinepoints;
	}
	
	public double[] getVertices(){
		return vertices;
	}
	
	public double[] getOutlineVertices(){
		return outlineVertices;
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
		return center.X();
	}
	
	public double getY(){
		return center.Y();
	}
	
	public double getAngle(){
		return angle;
	}
	
	public int getColor() {
		return color;
	}
	
	public int getCurrentCooldown() {
		return current_cooldown;
	}
	
	public void setHealth(int newhealth) {
		health = newhealth;
//		if(health > max_health) {
//			health = max_health;
//		}
	}
	
	public int getHealth() {
		return health;
	}
	
	public int getMaxHealth() {
		return max_health;
	}
	
	public Point[] getPoints() {
		return points;
	}

}
