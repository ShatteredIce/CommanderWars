import java.util.ArrayList;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;
import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class GameClient extends Listener{
	
	int WINDOW_WIDTH = 640;
	int WINDOW_HEIGHT = 640;
	
	int mapWidth;
	int mapHeight;
	int[][] map;
	Tile[][] tiles = null;
	
	ArrayList<Player> players = new ArrayList<>();
	ArrayList<UnitInfo> units = new ArrayList<>();
	ArrayList<Integer> selectedUnitsId = new ArrayList<>();
	
	int myPlayerId = -1;
		
	// The window handle
	private long window;
	
	GameLogic gamelogic = new GameLogic();
	
	//networking
	static Client client;
	//ip of server
	static String ip = "localhost";
	static int tcpPort = 27960;
	static int udpPort = 27960;
	
	
	public void run() throws IOException {
		//create client
		client = new Client();
		//register packets
		client.getKryo().register(java.util.ArrayList.class);
		client.getKryo().register(double[].class);
		client.getKryo().register(int[].class);
		client.getKryo().register(int[][].class);
		client.getKryo().register(UnitPositions.class);
		client.getKryo().register(PlayerInfo.class);
		client.getKryo().register(UnitInfo.class);
		client.getKryo().register(MouseClick.class);
		client.getKryo().register(MapData.class);
		client.getKryo().register(Message.class);
		
		//start the client
		client.start();
		
		//connect to the server
		client.connect(5000, ip, tcpPort, udpPort);
		
		client.addListener(this);
		myPlayerId = client.getID();
		
		System.out.println("client is ready");
		
		initGLFW();
		loop();

		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
		client.stop();
	}

	private void initGLFW() {
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if ( !glfwInit() )
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

		// Create the window
		window = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "My World (Client)", NULL, NULL);
		if ( window == NULL )
			throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
		});
		
		glfwSetMouseButtonCallback (window, (window, button, action, mods) -> {
			DoubleBuffer xpos = BufferUtils.createDoubleBuffer(1);
			DoubleBuffer ypos = BufferUtils.createDoubleBuffer(1);
			glfwGetCursorPos(window, xpos, ypos);
			if ( button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
				for (int u = 0; u < units.size(); u++) {
					if(units.get(u).getOwnerId() == myPlayerId && gamelogic.distance(xpos.get(0), ypos.get(0), units.get(u).getX(), units.get(u).getY()) <= 30){
						boolean selectUnit = true;
						for (int i = 0; i < selectedUnitsId.size(); i++) {
							if(selectedUnitsId.get(i) == units.get(u).getId()){
								selectedUnitsId.remove(i);
								selectUnit = false;
								break;
							}
						}
						if(selectUnit){
							selectedUnitsId.add(units.get(u).getId());
						}
					}
				}
			}
			else if ( button == GLFW_MOUSE_BUTTON_RIGHT && action == GLFW_PRESS) {
				if(selectedUnitsId.size() > 0){
					client.sendTCP(new MouseClick(xpos.get(0), ypos.get(0), selectedUnitsId));
				}
			}
		});

		// Get the thread stack and push a new frame
		try ( MemoryStack stack = stackPush() ) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(
				window,
				(vidmode.width() - pWidth.get(0)) / 2,
				(vidmode.height() - pHeight.get(0)) / 2
			);
		} // the stack frame is popped automatically

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		// Enable v-sync
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(window);
	}
	
	@Override
	public void received(Connection c, Object obj) {
		if(obj instanceof PlayerInfo){
			PlayerInfo packet = (PlayerInfo) obj;
			switch (packet.action) {
			//create player
			case 1:
				players.add(new Player(packet.team, packet.id));
				client.sendTCP(new Message("recieved new player", packet.id));
				System.out.println("recieved player " + packet.id);
				break;
			//delete player
			case 2:
				for (int i = 0; i < players.size(); i++) {
					if(players.get(i).getId() == packet.id){
						players.remove(i);
						i--;
					}
				}
				break;
			}
		}
		else if(obj instanceof UnitPositions){
			UnitPositions packet = (UnitPositions) obj;
			units = packet.unitdata;
		}
		else if(obj instanceof MapData){
			MapData packet = (MapData) obj;
			map = packet.data;
			mapWidth = map.length;
			mapHeight = map[0].length;
		}
	}
	

	private void loop() {
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();
		
		glMatrixMode(GL_PROJECTION);
        glLoadIdentity(); // Resets any previous projection matrices
        glOrtho(0, WINDOW_WIDTH, WINDOW_HEIGHT, 0, 1, -1);
        glMatrixMode(GL_MODELVIEW);

		//Set the clear color
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		
		//Enable transparency
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		loadMap();
		
		final Texture redunit = new Texture("redunit2.png");
		final Texture unitglow = new Texture("unitglow.png");

		
		final double[] textureCoords = {0, 0, 0, 1, 1, 0, 1, 1};
		final int[] indices = {0, 1, 2, 2, 1, 3};
		final double[] placeholder = {0, 0, 0, 0, 0, 0, 0, 0};
		
		Model model = new Model(placeholder, textureCoords, indices);
		
		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		while ( !glfwWindowShouldClose(window) ) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

			// Poll for window events. The key callback above will only be
			// invoked during this call.
			glfwPollEvents();
			
			glEnable(GL_TEXTURE_2D);
					
			//display tiles
			for (int i = 0; i < tiles.length; i++) {
				for (int j = 0; j < tiles[0].length; j++) {
					tiles[i][j].setTexture();
					model.render(tiles[i][j].getVertices());
				}
			}
			

			//display units
			redunit.bind();
			for (UnitInfo u : units) {
				model.render(u.getVertices());
			}
			
			//display glow on selected units
			unitglow.bind();
			for (UnitInfo u : units) {
				for (int i = 0; i < selectedUnitsId.size(); i++) {
					if(selectedUnitsId.get(i) == u.getId()){
						model.render(u.getOutlineVertices());
					}
				}
			}
			
			glDisable(GL_TEXTURE_2D);
			glfwSwapBuffers(window); // swap the color buffers
		}
	}

	public static void main(String[] args) throws IOException {
		new GameClient().run();
	}
	
	public void loadMap(){
		tiles = new Tile[mapWidth][mapHeight];
		for (int x = 0; x < mapWidth; x++) {
			for (int y = 0; y < mapHeight; y++) {
				tiles[x][y] = new Tile(map[x][y], x, y);
			}
		}
	}

}

	
	

