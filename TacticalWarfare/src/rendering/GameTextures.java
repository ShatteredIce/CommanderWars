package rendering;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;

import mainframe.GameException;

public class GameTextures {
	
	final static Texture redunit = new Texture("redunit2.png");
	final static Texture blueunit = new Texture("blueunit.png");
	final static Texture unitglow = new Texture("unitglow.png");
	final static Texture border = new Texture("border.png");
	final static Texture redflag = new Texture("redflag.png");
	final static Texture blueflag = new Texture("blueflag.png");
	final static Texture projectile = new Texture("projectile_test.png");
	final static Texture mine = new Texture("mine.png");
	
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
		case 11:
			border.bind();
			break;
		case 12:
			redflag.bind();
			break;
		case 13:
			blueflag.bind();
			break;
		case 20:
			projectile.bind();
			break;
		case 21:
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