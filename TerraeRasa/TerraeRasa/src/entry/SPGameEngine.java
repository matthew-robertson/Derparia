package entry;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;

import client.ClientConnectionThread;
import client.render.Render;


/**
 * TerraeRasa.java defines the entry class of the application. After calling the main method of the program,
 * a new instance of the TerraeRasa class is created in order to handle all the data and responsibilities of the
 * application. The constructor of this class is private, and an instance of TerraeRasa should only be created
 * in the main method of the application.
 * @author      Alec Sobeck
 * @author      Matthew Robertson
 * @version     1.0
 * @since       1.0
 */
public class SPGameEngine extends Thread
{
	private final static Object connectionThreadLock = new Object();
	private final static String WINDOW_TITLE = "Terrae Rasa " + TerraeRasa.getVersion();
	private final static int DEFAULT_RESOLUTION_WIDTH = 900;
	private final static int DEFAULT_RESOLUTION_HEIGHT = 520;
	private Frame terraeRasaFrame;
	private Canvas terraeRasaCanvas;
	private int width;
	private int height;
	private boolean needsResized;
	private static String osName;
	public static boolean done;
	public static boolean isMainMenuOpen;
	public static boolean initInDebugMode;
	public static SPGameEngine terraeRasa;
	public SPGameLoop gameEngine;
	private ClientConnectionThread thread;
	private String[] args;
	
	/**
	 * Creates a new instance of the container for all game objects, and a new GameEngine. 
	 * This is private to prevent destructive calls in other parts of the code. A new 
	 * instance of this class should only be created in the main method, implemented in TerraeRasa.java.
	 * @param w the width of the display at the start
	 * @param h the height of the display at the start
	 */
	public SPGameEngine(String[] args)
	{
		setName("SPGameEngine");
		this.args = args;
	}
	
	/**
	 * Initializes the canvas, display, and JFrame used by the game
	 */
	private void createWindow()
	{
		try {
			//Frame and canvas			
			terraeRasaCanvas = new Canvas();
			terraeRasaCanvas.setPreferredSize(new Dimension(width, height));
			
			terraeRasaFrame = new Frame(WINDOW_TITLE);
			final Image icon = Toolkit.getDefaultToolkit().getImage(SPGameEngine.class.getResource("/Resources/icon.png"));
			terraeRasaFrame.setIconImage(icon);
			terraeRasaFrame.setBackground(Color.black);
			terraeRasaFrame.setLayout(new BorderLayout());
			terraeRasaFrame.add(terraeRasaCanvas, "Center");
			terraeRasaFrame.pack();
			terraeRasaFrame.setLocationRelativeTo(null);
			terraeRasaFrame.setVisible(true);
			
			//Opengl display
			Display.setParent(terraeRasaCanvas); //The display is inside an awtCanvas to allow for resizing
	        Display.setTitle(WINDOW_TITLE); //sets the title of the window
		    Display.setDisplayMode(new DisplayMode(width, height)); //creates the window with the correct size
		    Display.setFullscreen(true);
		    Display.setResizable(true); //The display is resizable
			Display.create((new PixelFormat()).withDepthBits(24)); //finally creates the window
		    
	        initGL(); //Initialize the generic 2D opengl settings
	    	resizeWindow(); //To remain consistent, resize the window to use slightly different calculations immediately
	    	
	    	Component myComponent = terraeRasaFrame;
			myComponent.addComponentListener(new ComponentAdapter() //JFrame listener for a window resize event
			{
			    public void componentResized(ComponentEvent e)
			    {
		    		needsResized = true; //Flags the window for recalculations on the next game update
			    }
			});
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}	
	
	/**
	 * Checks if the window needs resized, and will resize it if needed
	 */
	public void checkWindowSize()
	{
		//Minimum game window size is 640 x 480
		if(needsResized || width < 640 || height < 400) 
		{
			resizeWindow();
			needsResized = false;
		}
	}
	
	/**
	 * Resizes the JFrame and all components when applicable 
	 */
	private void resizeWindow()
	{
		width = (terraeRasaFrame.getWidth() > 740) ? terraeRasaFrame.getWidth() : 740; //740 = arbitrary value for things not to look bad
		height = (terraeRasaFrame.getHeight() > 480) ? terraeRasaFrame.getHeight() : 480; //same with 480
		
		int width_fix = (int)(width / Render.BLOCK_SIZE) * Render.BLOCK_SIZE + 1 * Render.BLOCK_SIZE;
		int height_fix = (int)(height / Render.BLOCK_SIZE) * Render.BLOCK_SIZE + 1 * Render.BLOCK_SIZE;
		
		if(osName.contains("Window")) //Windows Resize
		{
			terraeRasaFrame.setSize(new Dimension(width_fix, height_fix));
			terraeRasaCanvas.setSize(new Dimension(width, height));
		}
		else if(osName.toLowerCase().contains("linux"))
		{
			terraeRasaCanvas.setSize(new Dimension(width_fix, height_fix));
			terraeRasaFrame.setSize(new Dimension(width, height));
		}
		else //Mac resize
		{
			terraeRasaFrame.setSize(new Dimension(width, height));
			terraeRasaCanvas.setSize(new Dimension(width, height));
			terraeRasaCanvas.setLocation(0, 22);
			Display.setLocation(0, 22);
		}

		GL11.glMatrixMode(GL11.GL_PROJECTION); 
        GL11.glLoadIdentity(); //Reset to the origin        
        GL11.glOrtho(0.0D, width_fix / 2, height_fix / 2, 0.0D, 1000D, 3000D);
        GL11.glMatrixMode(GL11.GL_MODELVIEW); 
        GL11.glViewport(0, 0, width_fix, height_fix); //Sets up the second required element for 2D projection					
        
        System.out.println("Resizing window to: (" + width + ", " + height + ")");		
  	}
	
	/**
	 * Initializes the default openGL settings
	 */	
	private void initGL() 
	{		
		int width_fix = (int)(width / Render.BLOCK_SIZE) * Render.BLOCK_SIZE + Render.BLOCK_SIZE;
		int height_fix = (int)(height / Render.BLOCK_SIZE) * Render.BLOCK_SIZE + Render.BLOCK_SIZE;
		GL11.glClear(16640); //Clear the screendad
        GL11.glMatrixMode(GL11.GL_PROJECTION); 
        GL11.glLoadIdentity(); //Reset to the origin
        GL11.glOrtho(0.0D, width_fix / 2, height_fix / 2, 0.0D, 1000D, 3000D); //initialize the 2D drawing area
        GL11.glMatrixMode(5888 /*GL_MODELVIEW0_ARB*/); 
        GL11.glLoadIdentity(); //Reset position to the origin
        GL11.glTranslatef(0.0F, 0.0F, -2000F); //Move out on the screen so stuff is actually visible
        GL11.glViewport(0, 0, width_fix, height_fix); //Setup the second element for 2D projection
        GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F); //Clear the colour
        GL11.glDisable(GL11.GL_LIGHTING); //Ensure lighting isnt enabled
        GL11.glEnable(GL11.GL_TEXTURE_2D); //Allow flat textures to be drawn
        GL11.glDisable(GL11.GL_FOG); //Ensure there isnt fog enabled
	}	
	
	private void initializeConstants()
	{
		osName = System.getProperty("os.name").toLowerCase();
		
		try {
			System.out.println(args[0]);
			if(args[0].equals("no-debug"))
			{
				initInDebugMode = false;
			}
			else
			{
				initInDebugMode = true;			
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			initInDebugMode = true;
		}
		
		//initInDebugMode = false;
		
		System.out.println("Debug = " + initInDebugMode);
				
		width = DEFAULT_RESOLUTION_WIDTH;
		height = DEFAULT_RESOLUTION_HEIGHT;
		this.gameEngine = new SPGameLoop();
	}
	
	/**
	 * Creates the window, loads resources, initializes the game engine,
	 * and then beings to run the game engine and main game loop. When quitting, 
	 * this method will destroy the display and then call System.exit(1) to ensure 
	 * the VM deallocates all resources and terminates.
	 */
	public void run()
	{
		initializeConstants();
		
		createWindow(); //Create the display and embed it in an awtcanvas	
		isMainMenuOpen = true;       
		gameEngine.init();
		gameEngine.run();
	    Display.destroy(); //Cleans up  
        System.exit(1); //remember to exit the system and release resources
	} 		
	
	/**
	 * Flags a game join as being a MP game session. Things will behave a bit differently in MP.
	 */
	public static void startMPGame()
	{
		terraeRasa.gameEngine.startMPGame();
	}
	
	/**
	 * Gets the name of the user's operating system.
	 * @return the name of the user's operating system
	 */
	public static final String getOSName()
	{
		return osName;
	}
	
	/**
	 * Registers a new connection to a server. This will forcibly throw a runtime exception if one such 
	 * connection already exists.
	 * @param thread the new ClientConnectionThread to register.
	 */
	public static void registerClientThread(ClientConnectionThread thread)
	{
		//TODO [CRITICAL_BUG]: ensure that a disconnect terminates this thread and makes it null.
		synchronized(connectionThreadLock)
		{
			if(terraeRasa.thread != null)
			{
				throw new RuntimeException("Not-null thread was almost replaced by a null thread.");
			}
			terraeRasa.thread = thread;
		}
	}
	
	/**
	 * Kills the active clientConnectionThread if it is not null and is still alive.
	 */
	public static void terminateClientConnection()
	{
		synchronized(connectionThreadLock)
		{
			if(terraeRasa.thread != null && terraeRasa.thread.isAlive())
			{
				try {
					terraeRasa.thread.kill();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(terraeRasa.thread != null)
			{
				terraeRasa.thread = null;
			}
		}
	}
}
