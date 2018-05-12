package mainframe;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.io.File;
import java.io.IOException;
import java.nio.*;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Launcher {
	
	// The window handle
	private long window;
	
	int WINDOW_WIDTH = 640;
	int WINDOW_HEIGHT = 640;
	
	int exitState = 0;
	
	//music
	File music_path = new File("music/rainopeningtheme.wav");
	AudioInputStream music_input;
	Clip menuMusic;
	boolean sound = true;
	
	public void run() {

		init();
		loop();

		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
		
		menuMusic.close();
		
		if(exitState == 1) { //launch server
			try {
				new Game().run();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if(exitState == 2) { //launch client
			try {
				new GameClient().run();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void init() {
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
		window = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "Game Launcher", NULL, NULL);
		if ( window == NULL )
			throw new RuntimeException("Failed to create the GLFW window");
		
		//setup music
		try {
			music_input = AudioSystem.getAudioInputStream(music_path);
			menuMusic = AudioSystem.getClip();
			menuMusic.open(music_input);
			if (sound) {
				menuMusic.loop(Clip.LOOP_CONTINUOUSLY);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}

		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
		});
		
		//mouse clicks
		glfwSetMouseButtonCallback (window, (window, button, action, mods) -> {
			DoubleBuffer xpos = BufferUtils.createDoubleBuffer(1);
			DoubleBuffer ypos = BufferUtils.createDoubleBuffer(1);
			glfwGetCursorPos(window, xpos, ypos);
			if ( button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
				System.out.println(xpos.get(0) + " " + ypos.get(0));
				if(xpos.get(0) > 140 && xpos.get(0) < 540 && ypos.get(0) > 100 && ypos.get(0) < 300) {
					exitState = 1;
					glfwSetWindowShouldClose(window, true);
				}
				else if(xpos.get(0) > 140 && xpos.get(0) < 540 && ypos.get(0) > 400 && ypos.get(0) < 600) {
					exitState = 2;
					glfwSetWindowShouldClose(window, true);
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

	private void loop() {
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();

		// Set the clear color
		glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		while ( !glfwWindowShouldClose(window) ) {
			
//			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
//			glfwSwapBuffers(window); // swap the color buffers

			// Poll for window events. The key callback above will only be
			// invoked during this call.
			glfwPollEvents();
		}
	}

	public static void main(String[] args) {
		new Launcher().run();

	}

}
