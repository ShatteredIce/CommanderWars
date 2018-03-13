package mainframe;

public class GameLogic {
	
	//returns an int from 0 to 359
	public int angleToPoint(double center_x, double center_y, double target_x, double target_y){
    	//first quadrant
    	if(target_x >= center_x && target_y >= center_y){
    		return 360 - (int) Math.round(Math.toDegrees(Math.atan((target_x - center_x)/(target_y - center_y))));
    	}
    	//second quadrant
    	else if(target_x <= center_x && target_y >= center_y){
    		return -(int) Math.round(Math.toDegrees(Math.atan((target_x - center_x)/(target_y - center_y))));
    	}
    	//third quadrant
    	else if(target_x <= center_x && target_y <= center_y){
    		return 180 - (int) Math.round(Math.toDegrees(Math.atan((target_x - center_x)/(target_y - center_y))));
    	}
    	//fourth quadrant
    	else if(target_x >= center_x && target_y <= center_y){
    		return 180 - (int) Math.round(Math.toDegrees(Math.atan((target_x - center_x)/(target_y - center_y))));
    	}
    	else{
    		return 0;
    	}
    }
	
	public double distance(double x1, double y1, double x2, double y2){
    	return Math.sqrt(Math.pow((x2-x1),2) + Math.pow((y2-y1),2));
    }
	
	//get turn distance to angle depending on which direction to turn, 
	public double getTurnDistance(double currentAngle, double targetAngle, boolean toLeft){
		//find which direction to turn is shortest to target
		if(currentAngle >= targetAngle){
			if(toLeft){
				return 360 - currentAngle + targetAngle; //left bearing
			}
			else{
				return currentAngle - targetAngle; //right bearing
			}
		}
		else if(currentAngle < targetAngle){
			if(toLeft){
				return targetAngle - currentAngle; //left bearing
			}
			else{
				 return currentAngle + 360 - targetAngle; //right bearing
			}
		}
		else{
			return 0;
		}
	}
	
	public double normalizeAngle(double angle){
		while(angle < 0){
			angle += 360;
		}
		while(angle >= 360){
			angle -= 360;
		}
		//convert to two decimal places
		angle = ((double) Math.round(angle * 100)) / 100;
		return angle;
	}

}
