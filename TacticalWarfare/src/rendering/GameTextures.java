package rendering;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;

import mainframe.GameException;

public class GameTextures {
	
	final static Texture redunit = new Texture("redunit.png");
	final static Texture blueunit = new Texture("blueunit.png");
	final static Texture unitglow = new Texture("unitglow.png");
	final static Texture border = new Texture("border.png");
	final static Texture redflag = new Texture("redflag.png");
	final static Texture blueflag = new Texture("blueflag.png");
	final static Texture projectile = new Texture("projectile_test.png");
	final static Texture mine_unarmed = new Texture("mine_unarmed.png");
	final static Texture mine = new Texture("mine.png");
	final static Texture daynightbar = new Texture("day_night_bar_v2.png");
	final static Texture redspawn = new Texture("redspawn.png");
	final static Texture bluespawn = new Texture("bluespawn.png");
	
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
		case 20:
			projectile.bind();
			break;
		case 21:
			mine_unarmed.bind();
			break;
		case 22:
			mine.bind();
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