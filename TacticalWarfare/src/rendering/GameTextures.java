package rendering;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;

import mainframe.GameException;

public class GameTextures {
	
	final static Texture redunit = new Texture("redunit.png");
	final static Texture blueunit = new Texture("blueunit.png");
	final static Texture unitglow = new Texture("unitglow.png");
	final static Texture border = new Texture("lightgray.png");
	final static Texture redflag = new Texture("tower_red.png");
	final static Texture blueflag = new Texture("tower_blue.png");
	final static Texture redflag2 = new Texture("redflag.png");
	final static Texture blueflag2 = new Texture("blueflag.png");
	final static Texture projectile = new Texture("projectile_arrow.png");
	final static Texture trap_unarmed = new Texture("trap_unarmed.png");
	final static Texture trap_armed = new Texture("trap_armed.png");
	final static Texture daynightbar = new Texture("day_night_bar_v2.png");
	final static Texture redspawn = new Texture("redspawn.png");
	final static Texture bluespawn = new Texture("bluespawn.png");
	final static Texture hpred = new Texture("red.png");
	final static Texture hpgreen = new Texture("lightgreen.png");
	final static Texture gear = new Texture("gear.png");
	
	public void loadTexture(int id){
		switch (id) {
		case -1:
			glBindTexture(GL_TEXTURE_2D, 0);
		case 0:
			unitglow.bind();
			break;
		case 1:
			redunit.bind();
			break;
		case 2:
			blueunit.bind();
			break;
		case 10:
			border.bind();
			break;
		case 11:
			daynightbar.bind();
			break;
		case 12:
			redflag.bind();
			break;
		case 13:
			blueflag.bind();
			break;
		case 14:
			redspawn.bind();
			break;
		case 15:
			bluespawn.bind();
			break;
		case 16:
			hpred.bind();
			break;
		case 17:
			hpgreen.bind();
			break;
		case 18:
			gear.bind();
			break;
		case 20:
			projectile.bind();
			break;
		case 21:
			trap_unarmed.bind();
			break;
		case 22:
			trap_armed.bind();
			break;
		case 32:
			redflag2.bind();
			break;
		case 33:
			blueflag2.bind();
			break;
		default:
			try {
				throw new GameException("Requested Texture ID does not exist");
			} catch (GameException e) {
				e.printStackTrace();
			}
		}
	}

}