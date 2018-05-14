package mainframe;
import java.util.ArrayList;
import java.util.Scanner;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import packets.KeyPress;
import packets.MapData;
import packets.Message;
import packets.MouseClick;
import packets.PlayerInfo;
import packets.ProjectileInfo;
import packets.ProjectilePositions;
import packets.ScoreData;
import packets.TileInfo;
import packets.UnitInfo;
import packets.UnitMovement;
import packets.UnitPositions;
import rendering.Bitmap;
import rendering.GameTextures;
import rendering.Model;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class GameClient extends Listener{
	
	int WINDOW_WIDTH = 840;
	int WINDOW_HEIGHT = 640;
	
	int gameScreenWidth = 640;
	int gameScreenHeight = 640;
	
	int worldWidth = 640;
	int worldHeight = 640;
	
	int tileLength = 64;
	
	//camera variables
	public double viewX = 0;
	public double viewY = 0;
	public double cameraSpeed = 10;
	public double cameraWidth = 840;
	public double cameraHeight = 640;
	
	public int windowXOffset = 0;
	public int windowYOffset = 0;
	
	boolean panLeft = false;
    boolean panRight = false;
    boolean panUp = false;
    boolean panDown = false;
    boolean unitTracking = false;
	
    //map variables
	int mapWidth;
	int mapHeight;
	int[][] map;
	Tile[][] tiles = null;
	
	int tick = 0;
	int ticksPerDay = 24000;
	float lightLevel = 1;
	
	//game data
	ArrayList<Player> players = new ArrayList<>();
	ArrayList<UnitInfo> units = new ArrayList<>();
	ArrayList<ProjectileInfo> projectiles = new ArrayList<>();
	ArrayList<Integer> selectedUnitsId = new ArrayList<>();
	ArrayList<int[]> redSpawns = new ArrayList<>();
	ArrayList<int[]> blueSpawns = new ArrayList<>();
	
	int myPlayerId = -1;
	int teamColor = -1;
	
	//state variables
	int gameState = 1;
	boolean staticFrame = false;
	boolean reloadMap = false;
	
	//music
	File music_path = new File("music/enteringthestronghold.wav");
	AudioInputStream music_input;
	Clip gameMusic;
	boolean sound = true;
	
	int texturePack = 1;
	
	int red_score = 0;
	int blue_score = 0;
	
	boolean aPressed;
	boolean wPressed;
	boolean dPressed;
	boolean sPressed;
	boolean sentMovement = true;
		
	// The window handle
	private long window;
	//rendering variables
	final double[] textureCoords = {0, 0, 0, 1, 1, 0, 1, 1};
	final int[] indices = {0, 1, 2, 2, 1, 3};
	final double[] placeholder = {0, 0, 0, 0, 0, 0, 0, 0};
	
	static GameLogic gamelogic = new GameLogic();
	static GameTextures gametextures;
	
	//networking
	static Client client;
	//ip of server
	static String ip = "localhost";
	static int tcpPort = 27960;
	static int udpPort = 27960;
	
	static Bitmap bitmap;
	
	//clickable buttons
	Button baseButton = new Button(gameScreenWidth + 10, 570, gameScreenWidth + 60, 620);
	int baseIndex = 0;
	Button pauseButton = new Button(gameScreenWidth + 140, 570, gameScreenWidth + 190, 620);
	Button unpauseButton = new Button(430, 150, 460, 180);
	Button soundButton = new Button(220, 250, 420, 305);
	Button textureButton = new Button(220, 310, 420, 365);
	Button exitButton = new Button(220, 370, 420, 425);
	
	public void run() throws IOException {
		
		//create client
		client = new Client(100000, 100000);
		//register packets
		client.getKryo().register(java.util.ArrayList.class);
		client.getKryo().register(double[].class);
		client.getKryo().register(int[].class);
		client.getKryo().register(int[][].class);
		client.getKryo().register(ScoreData.class);
		client.getKryo().register(UnitPositions.class);
		client.getKryo().register(ProjectilePositions.class);
		client.getKryo().register(PlayerInfo.class);
		client.getKryo().register(UnitInfo.class);
		client.getKryo().register(ProjectileInfo.class);
		client.getKryo().register(MouseClick.class);
		client.getKryo().register(KeyPress.class);
		client.getKryo().register(MapData.class);
		client.getKryo().register(Message.class);
		client.getKryo().register(TileInfo.class);
		client.getKryo().register(UnitMovement.class);
		
		//start the client
		client.start();
		
		Scanner scanner = new Scanner(System.in);
		
		if(ip.equals("manual")) {
			System.out.print("Enter Host IP: ");
			ip = scanner.nextLine();
		}
		while(true) {
			try {
				//connect to the server
				client.connect(5000, ip, tcpPort, udpPort);
				break;
			}
			catch(IOException e) {
				System.out.println("Failed to connect to " + ip);
				System.out.print("Enter Host IP: ");
				ip = scanner.nextLine();
			}
			if(ip.equals("")) {
				System.exit(0);
			}
		}
		
		scanner.close();
		
		client.addListener(this);
		myPlayerId = client.getID();
		
		System.out.println("Client Initialized");
		
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
		window = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "Kingdomfall (Client)", NULL, NULL);
		if ( window == NULL )
			throw new RuntimeException("Failed to create the GLFW window");
		
		//setup music
		try {
			music_input = AudioSystem.getAudioInputStream(music_path);
			gameMusic = AudioSystem.getClip();
			gameMusic.open(music_input);
			if (sound) {
				gameMusic.loop(Clip.LOOP_CONTINUOUSLY);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}

		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
				glfwSetWindowShouldClose(window, true); 
			//camera controls
			if ( key == GLFW_KEY_MINUS && action == GLFW_PRESS )
				if(gameState == 1){
					updateZoomLevel(true);
				}
			if ( key == GLFW_KEY_EQUAL && action == GLFW_PRESS )
				if(gameState == 1){
					updateZoomLevel(false);
				}
			if ( key == GLFW_KEY_T && action == GLFW_PRESS ) {
				unitTracking = !unitTracking;
			}
			if ( key == GLFW_KEY_LEFT && action == GLFW_PRESS )
				panLeft = true;
			if ( key == GLFW_KEY_LEFT && action == GLFW_RELEASE )
				panLeft = false;
			
			if ( key == GLFW_KEY_RIGHT && action == GLFW_PRESS )
				panRight = true;
			if ( key == GLFW_KEY_RIGHT && action == GLFW_RELEASE )
				panRight = false;
			//manual unit control
			if ( key == GLFW_KEY_UP && action == GLFW_PRESS )
				panUp = true;
			if ( key == GLFW_KEY_UP && action == GLFW_RELEASE )
				panUp = false;
			
			if ( key == GLFW_KEY_DOWN && action == GLFW_PRESS )
				panDown = true;
			if ( key == GLFW_KEY_DOWN && action == GLFW_RELEASE )
				panDown = false;
			
			if ( key == GLFW_KEY_SPACE && action == GLFW_PRESS )
				client.sendTCP(new KeyPress(GLFW_KEY_SPACE, selectedUnitsId));
			if ( key == GLFW_KEY_R && action == GLFW_PRESS )
				client.sendTCP(new KeyPress(GLFW_KEY_S, selectedUnitsId));
			if ( key == GLFW_KEY_A && action == GLFW_PRESS) {
				aPressed = true;
				sentMovement = false;
			}
			if ( key == GLFW_KEY_A && action == GLFW_RELEASE) {
				aPressed = false;
				sentMovement = false;
			}
			if ( key == GLFW_KEY_W && action == GLFW_PRESS) {
				wPressed = true;
				sentMovement = false;
			}
			if ( key == GLFW_KEY_W && action == GLFW_RELEASE) {
				wPressed = false;
				sentMovement = false;
			}
			if ( key == GLFW_KEY_D && action == GLFW_PRESS) {
				dPressed = true;
				sentMovement = false;
			}
			if ( key == GLFW_KEY_D && action == GLFW_RELEASE) {
				dPressed = false;
				sentMovement = false;
			}
			if ( key == GLFW_KEY_S && action == GLFW_PRESS) {
				sPressed = true;
				sentMovement = false;
			}
			if ( key == GLFW_KEY_S && action == GLFW_RELEASE) {
				sPressed = false;
				sentMovement = false;
			}
		});
		
		//mouse clicks
		glfwSetMouseButtonCallback (window, (window, button, action, mods) -> {
			DoubleBuffer xpos = BufferUtils.createDoubleBuffer(3);
			DoubleBuffer ypos = BufferUtils.createDoubleBuffer(3);
			glfwGetCursorPos(window, xpos, ypos);
			//convert the glfw coordinate to our coordinate system
			xpos.put(0, Math.min(Math.max(xpos.get(0), windowXOffset), WINDOW_WIDTH + windowXOffset));
			ypos.put(0, Math.min(Math.max(ypos.get(0), windowYOffset), WINDOW_HEIGHT + windowYOffset));
			//relative camera coordinates
			xpos.put(1, getWidthScalar() * (xpos.get(0) - windowXOffset) + viewX);
			ypos.put(1, getHeightScalar() * (ypos.get(0) - windowYOffset) + viewY);
			//true window coordinates
			xpos.put(2, xpos.get(0) - windowXOffset);
			ypos.put(2, ypos.get(0) - windowYOffset);
			if ( button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
				if(gameState == 1) {
					if(baseButton.isClicked(xpos.get(2), ypos.get(2))) {
						centerCameraOnBase();
					}
					else if(pauseButton.isClicked(xpos.get(2), ypos.get(2))) {
						client.sendTCP(new Message("Toggle Pause"));
					}
					else {
						for (int u = 0; u < units.size(); u++) {
							if(units.get(u).getOwnerId() == myPlayerId && gamelogic.distance(xpos.get(1), ypos.get(1), units.get(u).getX(), units.get(u).getY()) <= 30){
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
				}
				else if(gameState == 2) {
					if(pauseButton.isClicked(xpos.get(2), ypos.get(2)) || unpauseButton.isClicked(xpos.get(2), ypos.get(2))) {
						client.sendTCP(new Message("Toggle Pause"));
					}
					else if(soundButton.isClicked(xpos.get(2), ypos.get(2))) {
						sound = !sound;
						if(sound) {
							gameMusic.start();
						}
						else {
							gameMusic.stop();
						}
					}
					else if(textureButton.isClicked(xpos.get(2), ypos.get(2))) {
						staticFrame = false;
						if(texturePack == 1) {
							texturePack = 2;
						}
						else {
							texturePack = 1;
						}
					}
					else if(exitButton.isClicked(xpos.get(2), ypos.get(2))) {
						glfwSetWindowShouldClose(window, true);
					}
				}
			}
			else if ( button == GLFW_MOUSE_BUTTON_RIGHT && action == GLFW_PRESS) {
				if(gameState == 1) {
					if(selectedUnitsId.size() > 0){
						client.sendTCP(new MouseClick(xpos.get(1), ypos.get(1), selectedUnitsId));
					}
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
			switch (packet.getAction()) {
			//create player
			case 1:
				players.add(new Player(packet.getColor(), packet.getId()));
//				client.sendTCP(new Message("recieved new player", packet.getId()));
//				System.out.println("recieved player " + packet.getId());
				if(packet.getId() == myPlayerId) {
					teamColor = packet.getColor();
					if(teamColor == 1) {
						viewX = Math.min(worldWidth - cameraWidth * mapWidthScalar(),
								Math.max(0, (redSpawns.get(0)[0] + 1) * (tileLength) - cameraWidth * mapWidthScalar() /2));
						viewY = Math.min(worldHeight - cameraHeight * mapHeightScalar(),
								Math.max(0, (redSpawns.get(0)[1] + 1) * (tileLength) - cameraHeight * mapHeightScalar() /2)); 
					}
					else if(teamColor == 2) {
						viewX = Math.min(worldWidth - cameraWidth * mapWidthScalar(),
								Math.max(0, (blueSpawns.get(0)[0] + 1) * (tileLength) - cameraWidth * mapWidthScalar() /2));
						viewY = Math.min(worldHeight - cameraHeight * mapHeightScalar(),
								Math.max(0, (blueSpawns.get(0)[1] + 1) * (tileLength) - cameraHeight * mapHeightScalar() /2)); 
					}
				}
				break;
			//delete player
			case 2:
				for (int i = 0; i < players.size(); i++) {
					if(players.get(i).getId() == packet.getId()){
						players.remove(i);
						i--;
					}
				}
				break;
			}
		}
		else if(obj instanceof ScoreData) {
			ScoreData data = (ScoreData) obj;
			red_score = data.getRed();
			blue_score = data.getBlue();
			tick = data.getTick();
			lightLevel = data.getLightLevel();
		}
		else if(obj instanceof UnitPositions){
			UnitPositions packet = (UnitPositions) obj;
			units = packet.getUnitdata();
		}
		else if(obj instanceof ProjectilePositions){
			ProjectilePositions packet = (ProjectilePositions) obj;
			projectiles = packet.getProjectiledata();
		}
		else if(obj instanceof MapData){
			MapData packet = (MapData) obj;
			map = packet.getData();
			mapWidth = map.length;
			mapHeight = map[0].length;
			worldWidth = mapWidth * tileLength;
			worldHeight = mapHeight * tileLength;
			redSpawns = packet.getRedSpawns();
			blueSpawns = packet.getBlueSpawns();
			gameState = packet.getState();
			tick = packet.getTick();
			//clear data from previous game
			staticFrame = false;
			projectiles.clear();
			reloadMap = true;
		}
		else if(obj instanceof TileInfo) {
			TileInfo info = (TileInfo) obj;
			map[info.getTileX()][info.getTileY()] = info.getTileId();
			if(tiles != null) {
				tiles[info.getTileX()][info.getTileY()].setId(info.getTileId());
			}
		}
		else if(obj instanceof Message) {
			Message msg = (Message) obj;
			if(msg.getText().equals("Game State Change")) {
				gameState = msg.getValue();
			}
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
		
		gametextures = new GameTextures();
		bitmap = new Bitmap();
		
		Model model = new Model(placeholder, textureCoords, indices);
		
		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		while ( !glfwWindowShouldClose(window) ) {
			
			//reload map if necessary
			if(reloadMap){
				loadMap();
				reloadMap = false;
			}

			// Poll for window events. The key callback above will only be
			// invoked during this call.
			glfwPollEvents();
			
			glEnable(GL_TEXTURE_2D);
			
			if(gameState == 1) {
				if(staticFrame) {
					staticFrame = false;
				}
				glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
				
				drawGame(model);
								
				//send server movement 
				if(sentMovement == false) {
					client.sendTCP(new UnitMovement(selectedUnitsId, aPressed, wPressed, dPressed, sPressed));
					sentMovement = true;
				}
				
				if(unitTracking && selectedUnitsId.size() != 0) {
					double avgX = 0;
					double avgY = 0;
					for (UnitInfo u : units) {
						for (int i = 0; i < selectedUnitsId.size(); i++) {
							if(selectedUnitsId.get(i) == u.getId()){
								avgX += u.getX();
								avgY += u.getY();
							}
						}
					}
					avgX /= selectedUnitsId.size();
					avgY /= selectedUnitsId.size();
							
					viewX = Math.min(worldWidth - cameraWidth * (double) gameScreenWidth / (double) WINDOW_WIDTH,
							Math.max(0, avgX - cameraWidth * mapWidthScalar() /2));
					viewY = Math.min(worldHeight - cameraHeight * (double) gameScreenHeight / (double) WINDOW_HEIGHT,
							Math.max(0, avgY - cameraHeight * mapHeightScalar() /2));
				}
							
				//move camera
				if (panLeft) {
					viewX = Math.max(0, viewX - cameraWidth / 30);
					unitTracking = false;
				}
				if (panRight) {
					viewX = Math.min(worldWidth - cameraWidth * (double) gameScreenWidth / (double) WINDOW_WIDTH, viewX + cameraWidth / 30);
					unitTracking = false;
				}
				if (panDown) {
					viewY = Math.min(worldHeight - cameraHeight * (double) gameScreenHeight / (double) WINDOW_HEIGHT, viewY + cameraHeight / 30);
					unitTracking = false;
				}
				if (panUp) {
					viewY = Math.max(0, viewY - cameraHeight / 30);
					unitTracking = false;
				}
				
				glfwSwapBuffers(window); // swap the color buffers
			}
			else if(gameState == 2) { //game paused
				if(!staticFrame) {
					drawGame(model);
					projectTrueWindowCoordinates();
					gametextures.loadTexture(23);
					model.render(170, 140, 470, 440);
					glfwSwapBuffers(window);
					staticFrame = true;
				}
			}
			else if(gameState == 3) { //red won
				if(!staticFrame) {
					drawGame(model);
					projectTrueWindowCoordinates();
					if(teamColor == 1) {
						gametextures.loadTexture(24);
					}
					else {
						gametextures.loadTexture(25);
					}
					model.render(100, 150, 540, 450);
					glfwSwapBuffers(window);
					staticFrame = true;
				}
			}
			else if(gameState == 4) { //blue won
				if(!staticFrame) {
					drawGame(model);
					projectTrueWindowCoordinates();
					if(teamColor == 2) {
						gametextures.loadTexture(26);
					}
					else {
						gametextures.loadTexture(27);
					}
					model.render(100, 150, 540, 450);
					glfwSwapBuffers(window);
					staticFrame = true;
				}
			}
		}
	}

	public static void main(String[] args) throws IOException {
		new GameClient().run();
	}
	
	public void projectRelativeCameraCoordinates(){
		glMatrixMode(GL_PROJECTION);
        glLoadIdentity(); // Resets any previous projection matrices
        glOrtho((-windowXOffset * getWidthScalar()) + viewX, viewX + cameraWidth + (windowXOffset * getWidthScalar()), viewY + cameraHeight + ((windowYOffset)* getHeightScalar()), viewY + ((-windowYOffset) * getHeightScalar()), 1, -1);
        glMatrixMode(GL_MODELVIEW);
	}
	
	public void projectTrueWindowCoordinates(){
		glMatrixMode(GL_PROJECTION);
        glLoadIdentity(); // Resets any previous projection matrices
        glOrtho(-windowXOffset, WINDOW_WIDTH + windowXOffset, WINDOW_HEIGHT + windowYOffset, -windowYOffset, 1, -1);
        glMatrixMode(GL_MODELVIEW);
	}
	
	public void loadMap(){
		tiles = new Tile[mapWidth][mapHeight];
		for (int x = 0; x < mapWidth; x++) {
			for (int y = 0; y < mapHeight; y++) {
				tiles[x][y] = new Tile(map[x][y], x, y);
			}
		}
	}
	
	public double getWidthScalar(){
		return(double) cameraWidth / (double) WINDOW_WIDTH;
	}
	
	public double getHeightScalar(){
		return(double) cameraHeight / (double) WINDOW_HEIGHT;
	}
	
	public double mapWidthScalar() {
		return (double) gameScreenWidth / (double) WINDOW_WIDTH;
	}
	
	public double mapHeightScalar() {
		return (double) gameScreenHeight / (double) WINDOW_HEIGHT;
	}
	
	public void updateZoomLevel(boolean zoomOut){
		DoubleBuffer xpos = BufferUtils.createDoubleBuffer(3);
		DoubleBuffer ypos = BufferUtils.createDoubleBuffer(3);
		glfwGetCursorPos(window, xpos, ypos);
		//convert the glfw coordinate to our coordinate system
		xpos.put(0, Math.min(Math.max(xpos.get(0), windowXOffset), WINDOW_WIDTH + windowXOffset));
		ypos.put(0, Math.min(Math.max(ypos.get(0), windowYOffset), WINDOW_HEIGHT + windowYOffset));
		//relative camera coordinates
		xpos.put(1, getWidthScalar() * (xpos.get(0) - windowXOffset) + viewX);
		ypos.put(1, getHeightScalar() * (ypos.get(0) - windowYOffset) + viewY);
		//true window coordinates
		xpos.put(2, xpos.get(0) - windowXOffset);
		ypos.put(2, ypos.get(0) - windowYOffset);
		
		boolean mouseInFrame = false;
		double oldX = xpos.get(1);
		double oldY = ypos.get(1);
		double xAxisDistance = 0;
		double yAxisDistance = 0;
		
		if(xpos.get(2) > 0 && xpos.get(2) < gameScreenWidth && ypos.get(2) > 0 && ypos.get(2) < gameScreenHeight){
			mouseInFrame = true;
			xAxisDistance = xpos.get(2)/WINDOW_WIDTH;
			yAxisDistance = ypos.get(2)/WINDOW_HEIGHT;
		}
		
		int MIN_WIDTH = 100;
		int MIN_HEIGHT = 100;
		int MAX_WIDTH = worldWidth * WINDOW_WIDTH / gameScreenWidth;
		int MAX_HEIGHT = worldHeight * WINDOW_HEIGHT / gameScreenHeight;
		
		double zoomLevel = 4d/3d;
		
		if(!mouseInFrame) {
			oldX = viewX + (cameraWidth * gameScreenWidth/WINDOW_WIDTH)/2;
			oldY = viewY + (cameraHeight * gameScreenHeight/WINDOW_HEIGHT)/2;
			xAxisDistance = (gameScreenWidth/2d/WINDOW_WIDTH);
			yAxisDistance = (gameScreenHeight/2d/WINDOW_HEIGHT);
		}
		
		
		if(zoomOut){
			if(cameraWidth * zoomLevel <= MAX_WIDTH && cameraHeight * zoomLevel <= MAX_HEIGHT){
				cameraWidth *= zoomLevel;
				cameraHeight *= zoomLevel;
				viewX = oldX - cameraWidth * xAxisDistance;
				viewY = oldY - cameraHeight * yAxisDistance;
				double gameScreenCameraWidth = cameraWidth * gameScreenWidth / WINDOW_WIDTH;
				double gameScreenCameraHeight = cameraHeight * gameScreenHeight / WINDOW_HEIGHT;
				if(viewX + gameScreenCameraWidth > worldWidth){
					viewX = worldWidth - gameScreenCameraWidth;
				}
				if(viewY + gameScreenCameraHeight > worldHeight){
					viewY = worldHeight - gameScreenCameraHeight;
				}
				if(viewX < 0){
					viewX = 0;
				}
				if(viewY < 0){
					viewY = 0;
				}
			}
		}
		else{
			if(cameraWidth / zoomLevel >= MIN_WIDTH && cameraHeight / zoomLevel >= MIN_HEIGHT){
				cameraWidth /= zoomLevel;
				cameraHeight /= zoomLevel;
				viewX = oldX - cameraWidth * xAxisDistance;
				viewY = oldY - cameraHeight * yAxisDistance;
			}
		}
	}
	
	//center camera view on base
	public void centerCameraOnBase(){
		if(teamColor == 1) {
			viewX = Math.min(worldWidth - cameraWidth * mapWidthScalar(),
					Math.max(0, (redSpawns.get(baseIndex)[0] + 1) * (tileLength) - cameraWidth * mapWidthScalar() /2));
			viewY = Math.min(worldHeight - cameraHeight * mapHeightScalar(),
					Math.max(0, (redSpawns.get(baseIndex)[1] + 1) * (tileLength) - cameraHeight * mapHeightScalar() /2));
			baseIndex++;
			if(baseIndex == redSpawns.size()) {
				baseIndex = 0;
			}
		}
		else if(teamColor == 2) {
			viewX = Math.min(worldWidth - cameraWidth * mapWidthScalar(),
					Math.max(0, (blueSpawns.get(baseIndex)[0] + 1) * (tileLength) - cameraWidth * mapWidthScalar() /2));
			viewY = Math.min(worldHeight - cameraHeight * mapHeightScalar(),
					Math.max(0, (blueSpawns.get(baseIndex)[1] + 1) * (tileLength) - cameraHeight * mapHeightScalar() /2));
			baseIndex++;
			if(baseIndex == blueSpawns.size()) {
				baseIndex = 0;
			}
		}
	}
	
	//draw the game map and sprites
	public void drawMap(Model model) {
		projectRelativeCameraCoordinates();
		
		//display tiles
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[0].length; j++) {
				tiles[i][j].setTexture(texturePack);
				model.render(tiles[i][j].getVertices());
			}
		}
		
		//display spawnpoints
		for (int i = 0; i < redSpawns.size(); i++) {
			int[] current = redSpawns.get(i);
			gametextures.loadTexture(14);
			model.render(current[0] * tileLength, current[1] * tileLength, current[0] * tileLength + 128, current[1] * tileLength + 128);
		}
		
		for (int i = 0; i < blueSpawns.size(); i++) {
			int[] current = blueSpawns.get(i);
			gametextures.loadTexture(15);
			model.render(current[0] * tileLength, current[1] * tileLength, current[0] * tileLength + 128, current[1] * tileLength + 128);
		}
		
		//display projectiles
		for (ProjectileInfo p : projectiles) {
			model.setTextureCoords(p.getTexCoords(lightLevel)); //allow for projectile animations
			gametextures.loadTexture(p.getTexId());
			model.render(p.getVertices());
		}

		//display units
		model.setTextureCoords(textureCoords);
		for (UnitInfo u : units) {
			gametextures.loadTexture(u.getColor());
			model.render(u.getVertices());
		}
		
		//display glow on selected units
		gametextures.loadTexture(0);
		for (UnitInfo u : units) {
			for (int i = 0; i < selectedUnitsId.size(); i++) {
				if(selectedUnitsId.get(i) == u.getId()){
					model.render(u.getOutlineVertices());
				}
			}
		}
	}
		
	//draw the sidebar and all its components
	public void drawSidebar(Model model) {
		projectTrueWindowCoordinates();
		
		//render sidebar
		gametextures.loadTexture(10);
		model.setTextureCoords(textureCoords);
		model.render(gameScreenWidth, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
		
		//render day/night bar
		gametextures.loadTexture(11);
		double shift = ((double) tick / (double) ticksPerDay) - 0.3;
		model.setTextureCoords(new double[] {0 + shift, 0, 0 + shift, 1, 0.2 + shift, 0, 0.2 + shift, 1});
		model.render(gameScreenWidth + 20, 40, gameScreenWidth + 180, 75);
		
		//render red flag and score
		if(texturePack == 1) {
			gametextures.loadTexture(12);
		}
		else if(texturePack == 2) {
			gametextures.loadTexture(32);
		}
		model.setTextureCoords(textureCoords);
		model.render(gameScreenWidth + 10, 100, gameScreenWidth + 60, 150);
		bitmap.drawNumber(gameScreenWidth + 70, 110, gameScreenWidth + 95, 140, red_score);
		
		//render blue flag and score
		if(texturePack == 1) {
			gametextures.loadTexture(13);
		}
		else if(texturePack == 2) {
			gametextures.loadTexture(33);
		}
		model.render(gameScreenWidth + 10, 160, gameScreenWidth + 60, 210);
		bitmap.drawNumber(gameScreenWidth + 70, 170, gameScreenWidth + 95, 200, blue_score);
		
		//render hp bar and unit icon
		UnitInfo selected = null;
		if(selectedUnitsId.size() == 1) {
			for (UnitInfo u : units) {
				if(u.getId() == selectedUnitsId.get(0)) {
					selected = u;
					break;
				}
			}
			gametextures.loadTexture(16);
			model.render(gameScreenWidth + 25, 240, gameScreenWidth + 175, 250);
			gametextures.loadTexture(17);
			model.render(gameScreenWidth + 25, 240, (int) (gameScreenWidth + 25 + (150 * (double) selected.getHealth()/(double) selected.getMaxHealth())), 250);
			gametextures.loadTexture(selected.getColor());
			model.setTextureCoords(new double[] {1, 1, 1, 0, 0, 1, 0, 0});
			model.render(gameScreenWidth + 65, 260, gameScreenWidth + 135, 295);
		}
		
		//render return to base button
		if(teamColor == 1) {
			gametextures.loadTexture(14);
		}
		else if(teamColor == 2) {
			gametextures.loadTexture(15);
		}
		model.setTextureCoords(textureCoords);
		model.render(gameScreenWidth + 10, 570, gameScreenWidth + 60, 620);

		//display gear icon
		gametextures.loadTexture(18);
		model.render(gameScreenWidth + 140, 570, gameScreenWidth + 190, 620);
	}
		
		public void drawLighting(Model model){
			glDisable(GL_TEXTURE_2D);
			
			glColor4f(0f, 0f, 0f, lightLevel);
			
			gametextures.loadTexture(-1);
			model.render(new double[] {0, 0, 0, gameScreenHeight, gameScreenWidth, 0, gameScreenWidth, gameScreenHeight});
			
			glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			glEnable(GL_TEXTURE_2D);
		}
		
		public void drawGame(Model model) {
			drawMap(model);
			drawSidebar(model);
			drawLighting(model);
		}

}

	
	

