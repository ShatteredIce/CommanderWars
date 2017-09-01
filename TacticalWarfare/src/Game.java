import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Game extends Listener{
	
	int WINDOW_WIDTH = 640;
	int WINDOW_HEIGHT = 640;
	
	int mapWidth;
	int mapHeight;
	int[][] map;
	Tile[][] tiles = null;
	int unitId = 0;
	
	ArrayList<Player> players = new ArrayList<>();
	ArrayList<Unit> units = new ArrayList<>();
	ArrayList<Integer> selectedUnitsId = new ArrayList<>();
	
	boolean clientRecieved = false;
	
//	Player debug = new Player(this, "debug", 0);
	int serverPlayerId = 0;
	
	// The window handle
	private long window;
	
	static GameLogic gamelogic = new GameLogic();
	
	//networking
	static Server server;
	static int tcpPort = 27960;
	static int udpPort = 27960;
	
	Random random = new Random();
	
	
	public void run() throws IOException {
		//create server
		server = new Server(32768, 32768);
		//register packets
		server.getKryo().register(java.util.ArrayList.class);
		server.getKryo().register(double[].class);
		server.getKryo().register(int[].class);
		server.getKryo().register(int[][].class);
		server.getKryo().register(UnitPositions.class);
		server.getKryo().register(PlayerInfo.class);
		server.getKryo().register(UnitInfo.class);
		server.getKryo().register(MouseClick.class);
		server.getKryo().register(MapData.class);
		server.getKryo().register(Message.class);
		server.bind(tcpPort, udpPort);
		
		//start server
		server.start();
		System.out.println("server is on");
		
		server.addListener(this);
		
		initGLFW();
		loop();

		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
		server.stop();
	}
	
	//when a connection is recieved
	@Override
	public void connected(Connection c){
		System.out.println("recieved connection from " + c.getRemoteAddressTCP().getHostString());
		//send map
		server.sendToTCP(c.getID(), new MapData(map));
		//add previous players
		for (int i = 0; i < players.size(); i++) {
			Player previous = players.get(i);
			server.sendToTCP(c.getID(), new PlayerInfo(1, previous.getTeam(), previous.getId()));
		}
		Player newplayer = new Player("derp", c.getID());
		players.add(newplayer);
		server.sendToAllTCP(new PlayerInfo(1, newplayer.getTeam(), newplayer.getId()));
		String unitTeam = "derp";
		double unitX = 100*(random.nextInt(6) + 1);
		double unitY = 100*(random.nextInt(6) + 1);
		double unitAngle = random.nextInt(360);
		System.out.println(unitId + " " + newplayer.getId());
		units.add(createUnit(newplayer.getId(), unitTeam, unitX, unitY, unitAngle));
		System.out.println("finished recieving client " + c.getID());
	}
	
	public void updateClients(){
		for (int i = 0; i < players.size(); i++) {
			server.sendToAllTCP(new UnitPositions(units));
		}
	}
	
	//when an object is recieved
	@Override
	public void received(Connection c, Object obj) {
		if(obj instanceof MouseClick){
			MouseClick click = (MouseClick) obj;
			moveOrder(click.x, click.y, click.unitIds);
		}
	}
	
	//when a connection disconnects
	@Override
	public void disconnected(Connection c){
		System.out.println("A client disconnected");
//		//remove player that disconnected
		for (int p = 0; p < players.size(); p++) {
			if(players.get(p).getId() == c.getID()){
				server.sendToAllTCP(new PlayerInfo(2, players.get(p).getTeam(), players.get(p).getId()));
				players.remove(p);
				break;
			}
		}
		//remove units of player that disconnected
		for (int u = 0; u < units.size(); u++) {
			if(units.get(u).getOwnerId() == c.getID()){
				units.remove(u);
				u--;
			}
		}
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
		window = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "My World (Server)", NULL, NULL);
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
					if(units.get(u).getOwnerId() == serverPlayerId && gamelogic.distance(xpos.get(0), ypos.get(0), units.get(u).getX(), units.get(u).getY()) <= 30){
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
				moveOrder(xpos.get(0), ypos.get(0), selectedUnitsId);
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

		// Set the clear color
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		
		//Enable transparency
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		generateMap();
//		loadUnits();
		Player newplayer = new Player("derp", serverPlayerId);
		players.add(newplayer);
		server.sendToAllTCP(new PlayerInfo(1, newplayer.getTeam(), newplayer.getId()));
		String unitTeam = "derp";
//		double unitX = 100*(random.nextInt(6) + 1);
//		double unitY = 100*(random.nextInt(6) + 1);
		double unitAngle = random.nextInt(360);
		Unit serverUnit = createUnit(newplayer.getId(), unitTeam, 100, 100, unitAngle);
		units.add(serverUnit);
		
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
			
			updateClients();
			
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
			for (Unit u : units) {		
				u.update();
				model.render(u.getVertices());
			}
			
			//display glow on selected units
			unitglow.bind();
			for (Unit u : units) {
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
		new Game().run();
	}
	
	public void generateMap(){
		mapWidth = 10;
		mapHeight = 10;
		map = new int[mapWidth][mapHeight];
		for (int i = 0; i < mapWidth; i++) {
			for (int j = 0; j < mapHeight; j++) {
				map[i][j] = random.nextInt(3) + 1;
			}
		}
		loadMap();
	}
	
	public void loadMap(){
		tiles = new Tile[mapWidth][mapHeight];
		for (int x = 0; x < mapWidth; x++) {
			for (int y = 0; y < mapHeight; y++) {
				tiles[x][y] = new Tile(map[x][y], x, y);
			}
		}
	}
	
	public void moveOrder(double xpos, double ypos, ArrayList<Integer> selected){
		ArrayList<Unit> movedUnits = new ArrayList<>();
		//get selected units from their id's
		for (int i = 0; i < selected.size(); i++) {
			for (int u = 0; u < units.size(); u++) {
				if(selected.get(i) == units.get(u).getId()){
					movedUnits.add(units.get(u));
					break;
				}
			}
		}
		//move all the selected units
		for (int i = 0; i < movedUnits.size(); i++) {
			moveUnit(movedUnits.get(i), xpos, ypos);
		}
	}
	
	//finds a path between two tiles using dijkstra's algorithm
	public ArrayList<Point> findPath(int startx, int starty, int endx, int endy){
		int[][] distances = new int[mapWidth][mapHeight];
		boolean[][] visited = new boolean[mapWidth][mapHeight];
		Point[][] previousTile = new Point[mapWidth][mapHeight];
		for (int i = 0; i < mapWidth; i++) {
			for (int j = 0; j < mapHeight; j++) {
				distances[i][j] = -1;
				visited[i][j] = false;
			}
		}
		//set distance of starting tile to 0
		distances[startx][starty] = 0;
		previousTile[startx][starty] = null;
		//loop while target is unvisited
		while(visited[endx][endy] == false){
			//get x and y id of the tile that has the smallest distance value (-1 means no data)
			int minDistX = -1;
			int minDistY = -1;
			int minDistance = -1;
			for (int i = 0; i < mapWidth; i++) {
				for (int j = 0; j < mapHeight; j++) {
					if(visited[i][j] == false && distances[i][j] != -1 && (minDistance == -1 || minDistance > distances[i][j])){
						minDistX = i;
						minDistY = j;
						minDistance = distances[i][j];
					}
				}
			}
			if(minDistX == -1){
				System.out.println("No Path Found: " + startx + " " + starty + " to " + endx + " " + endy);
				return null;
			}
			//remove last distance tile from the unvisited list
			visited[minDistX][minDistY] = true;
			int currentDistance = distances[minDistX][minDistY] + tiles[minDistX][minDistY].getMovement();
			//update top left tile distance
			if(minDistX > 0 && minDistY > 0){
				if((distances[minDistX - 1][minDistY - 1] == -1) || (distances[minDistX - 1][minDistY - 1] > currentDistance + 1)){
					distances[minDistX - 1][minDistY - 1] = currentDistance + 1;
					previousTile[minDistX - 1][minDistY - 1] = new Point(minDistX, minDistY);
				}
			}
			//update top tile distance
			if(minDistY > 0){
				if((distances[minDistX][minDistY - 1] == -1) || (distances[minDistX][minDistY - 1] > currentDistance)){
					distances[minDistX][minDistY - 1] = currentDistance;
					previousTile[minDistX][minDistY - 1] = new Point(minDistX, minDistY);
				}
			}
			//update top right tile distance
			if(minDistX < mapWidth - 1 && minDistY > 0){
				if((distances[minDistX + 1][minDistY - 1] == -1) || (distances[minDistX + 1][minDistY - 1] > currentDistance + 1)){
					distances[minDistX + 1][minDistY - 1] = currentDistance + 1;
					previousTile[minDistX + 1][minDistY - 1] = new Point(minDistX, minDistY);
				}
			}
			//update left tile distance
			if(minDistX > 0){
				if((distances[minDistX - 1][minDistY] == -1) || (distances[minDistX - 1][minDistY] > currentDistance)){
					distances[minDistX - 1][minDistY] = currentDistance;
					previousTile[minDistX - 1][minDistY] = new Point(minDistX, minDistY);
				}
			}
			//update right tile distance
			if(minDistX < mapWidth - 1){
				if((distances[minDistX + 1][minDistY] == -1) || (distances[minDistX + 1][minDistY] > currentDistance)){
					distances[minDistX + 1][minDistY] = currentDistance;
					previousTile[minDistX + 1][minDistY] = new Point(minDistX, minDistY);
				}
			}
			//update bottom left tile distance
			if(minDistX > 0 && minDistY < mapHeight - 1){
				if((distances[minDistX - 1][minDistY + 1] == -1) || (distances[minDistX - 1][minDistY + 1] > currentDistance + 1)){
					distances[minDistX - 1][minDistY + 1] = currentDistance + 1;
					previousTile[minDistX - 1][minDistY + 1] = new Point(minDistX, minDistY);
				}
			}
			//update bottom tile distance
			if(minDistY < mapHeight - 1){
				if((distances[minDistX][minDistY + 1] == -1) || (distances[minDistX][minDistY + 1] > currentDistance)){
					distances[minDistX][minDistY + 1] = currentDistance;
					previousTile[minDistX][minDistY + 1] = new Point(minDistX, minDistY);
				}
			}
			//update bottom right tile distance
			if(minDistX < mapWidth - 1 && minDistY < mapHeight - 1){
				if((distances[minDistX + 1][minDistY + 1] == -1) || (distances[minDistX + 1][minDistY + 1] > currentDistance + 1)){
					distances[minDistX + 1][minDistY + 1] = currentDistance + 1;
					previousTile[minDistX + 1][minDistY + 1] = new Point(minDistX, minDistY);
				}
			}
		}
		ArrayList<Point> path = new ArrayList<>();
		Point backTrace = new Point(endx, endy);
		while(backTrace != null){
			path.add(backTrace);
			backTrace = previousTile[(int) (backTrace.X())][(int) (backTrace.Y())];
		}
		Collections.reverse(path);
		//set points in path to pixel coordinate grid from tile coordinates
		for (int i = 0; i < path.size(); i++) {
			Point current = path.get(i);
			current.setX(current.X()*Tile.getWidth()+Tile.getWidth()/2d);
			current.setY(current.Y()*Tile.getHeight()+Tile.getHeight()/2d);
		}
		path.remove(0);
		return path;
	}
	
	//gives the unit an arraylist of points to move to target destination
	public void moveUnit(Unit u, double x, double y){
		int[] startTile = currentTile(u.getX(), u.getY());
		int[] endTile = currentTile(x, y);
		if(startTile[0] == -1 || startTile[1] == -1 || endTile[0] == -1 || endTile[1] == -1){
			return;
		}
		u.setLocationTarget(findPath(startTile[0], startTile[1], endTile[0], endTile[1]));
	}
	
	//gets the tile a coordinate is in
	public int[] currentTile(double x, double y){
		int[] tilePos = {0,0};
		int tileWidth = Tile.getWidth();
		int tileHeight = Tile.getHeight();
		int xlower = 0;
		int xupper = tileWidth;
		//find tile x position
		while(!(xlower <= x && x < xupper)){
			xlower += tileWidth;
			xupper += tileWidth;
			tilePos[0]++;
			//if x coordinate is out of bounds
			if(tilePos[0] >= mapWidth){
				return new int[]{-1,-1};
			}
		}
		int ylower = 0;
		int yupper = tileHeight;
		//find tile y position
		while(!(ylower <= y && y < yupper)){
			ylower += tileHeight;
			yupper += tileHeight;
			tilePos[1]++;
			//if y coordinate is out of bounds
			if(tilePos[1] >= mapHeight){
				return new int[]{-1,-1};
			}
		}
		return tilePos;
	}
	
	public Unit createUnit(int newownerid, String newteam, double newx, double newy, double newangle){
		Unit u = new Unit(unitId, newownerid, newteam, newx, newy, newangle);
		unitId++;
		return u;
	}

}

	
	

