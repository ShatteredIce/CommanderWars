package packets;
import java.util.ArrayList;

import mainframe.Projectile;

public class ProjectilePositions {
	
	private ArrayList<ProjectileInfo> projectiledata = new ArrayList<>();
	
	public ProjectilePositions(){
		
	}
	
	public ProjectilePositions(ArrayList<Projectile> projectiles){
		for (int i = 0; i < projectiles.size(); i++) {
			Projectile p = projectiles.get(i);
			getProjectiledata().add(new ProjectileInfo(p.getColor(), p.getX(), p.getY(), p.getAngle(), p.getTexId(), p.getVertices()));
		}
	}

	public ArrayList<ProjectileInfo> getProjectiledata() {
		return projectiledata;
	}

}

