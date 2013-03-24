package net.dimensia.client;

import java.io.FileNotFoundException;
import java.io.IOException;

import net.dimensia.src.Block;
import net.dimensia.src.EntityLivingPlayer;
import net.dimensia.src.EnumDifficulty;
import net.dimensia.src.ErrorUtils;
import net.dimensia.src.FileManager;
import net.dimensia.src.GuiMainMenu;
import net.dimensia.src.Item;
import net.dimensia.src.ItemStack;
import net.dimensia.src.Keys;
import net.dimensia.src.LightUtils;
import net.dimensia.src.MouseInput;
import net.dimensia.src.RenderGlobal;
import net.dimensia.src.RenderMenu;
import net.dimensia.src.Settings;
import net.dimensia.src.SoundManager;
import net.dimensia.src.World;
import net.dimensia.src.WorldHell;
import net.dimensia.src.WorldSky;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

/**
 * <code>GameEngine</code> is the class responsible for running the main game loop, and other core features of multiple worlds.
 * <code>GameEngine</code> defines 4 important methods. 
 * <br><br>
 * The method {@link #startGame(World, EntityLivingPlayer)} defines the method to actually start the game with the specified World object
 * and player. This will close the main menu and begin rendering based on the Chunk data. 
 * <br><br>
 * Most of the application's life cycle is spent in {@link #run()} method, which contains the main game loop. This handles
 * everything from the main menu to the game, and save menu. Before exiting this method, {@link #saveSettings()} will be called
 * to update any settings changed during runtime. 
 * <br><br>
 * Additional methods of interest: <br>
 * {@link #changeWorld(int)}, {@link #closeGameToMenu()}
 *   
 * 
 * @author      Alec Sobeck
 * @author      Matthew Robertson
 * @version     1.0
 * @since       1.0
 */
public class GameEngine 
{
	/** In single player one render mode can be active. This decides what world is updated, loaded, and rendered. The names
	 * correspond directly to that respective world. */
	public final static int RENDER_MODE_WORLD_EARTH = 1,
							   RENDER_MODE_WORLD_HELL = 2,
							   RENDER_MODE_WORLD_SKY  = 3;
	/** The currently selected RENDER_MODE_WORLD value, detailing what world to update, load, and render.*/
	private int renderMode;
	public SoundManager soundManager;
	public GuiMainMenu mainMenu;
	public World world;
	public WorldHell worldHell;
	public WorldSky worldSky;
	public EntityLivingPlayer player;
	public Settings settings;
	public RenderMenu renderMenu;
	
	/**
	 * Creates a new instance of GameEngine. This includes setting the renderMode to RENDER_MODE_WORLD_EARTH
	 * and loading the settings object from disk. If a settings object cannot be found a new one is created. 
	 */
	public GameEngine()
	{
		renderMode = RENDER_MODE_WORLD_EARTH;
		try 
		{
			loadSettings();
		}
		catch (IOException e) 
		{
			//e.printStackTrace();
			settings = new Settings();
		}
		catch (ClassNotFoundException e) 
		{
			//e.printStackTrace();
			settings = new Settings();
		}
	}
	
	public void setWorld(World world)
	{
		this.world = world;
	}
	
	public void run()
	{
		try
		{
			//Variables for the gameloop cap (20 times / second)
	        final int TICKS_PER_SECOND = 20;
			final int SKIP_TICKS = 1000 / TICKS_PER_SECOND;
			final int MAX_FRAMESKIP = 5;
			long next_game_tick = System.currentTimeMillis();
			long start, end;
			long fps = 0;
			int loops;
			start = System.currentTimeMillis();
			
		    while(!Dimensia.done) //Main Game Loop
		    {
		        loops = 0;
		        while(System.currentTimeMillis() > next_game_tick && loops < MAX_FRAMESKIP) //Update the game 20 times/second 
		        {
		        	if(settings.menuOpen /*&& Dimensia.isMainMenuOpen*/)
		        	{ 
		        	}
		        	else if(!Dimensia.isMainMenuOpen) //Handle game inputs if the main menu isnt open (aka the game is being played)
		        	{
		        		MouseInput.mouse(world, player);
		        		Keys.keyboard(world, player, settings, settings.keybinds);	            
		        		
		        		if(renderMode == RENDER_MODE_WORLD_EARTH) //Player is in the overworld ('earth')
		        		{
		        			world.onWorldTick(player);
		        		}
		        		else if(renderMode == RENDER_MODE_WORLD_HELL) //Player is in the hell dimension 
		        		{
		        			
		        		}
		        		else if(renderMode == RENDER_MODE_WORLD_SKY) //Player is in the sky dimension
		        		{
		        			
		        		}
		        	}
		        	
					if(Dimensia.needsResized || Dimensia.width < 640 || Dimensia.height < 400) //If the window needs resized, resize it
					{
						Dimensia.dimensia.resizeWindow();
						Dimensia.needsResized = false;
					}
		        	 
		        	next_game_tick += SKIP_TICKS;
		            loops++;
		        }
		        
		        //Make sure the game loop doesn't fall very far behind and have to accelerate the 
		        //game for an extended period of time
		        if(System.currentTimeMillis() - next_game_tick > 1000)
		        {
		        	next_game_tick = System.currentTimeMillis();
		        }
		        
		        Display.update();
		    	GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
				GL11.glClearColor(0,0,0,0);
				GL11.glColor4f(1, 1, 1, 1);
				GL11.glPushMatrix();
		        
		        
		        if(Dimensia.isMainMenuOpen) //if the main menu is open, render that, otherwise render the game
		        {
		        	mainMenu.render();
			    }
		        else
		        {
		        	if(renderMode == RENDER_MODE_WORLD_EARTH)
		    		{
		        	 	RenderGlobal.render(world, player, renderMode); //Renders Everything on the screen for the game
		     		}
		    		else if(renderMode == RENDER_MODE_WORLD_HELL)
		    		{
		    		 	RenderGlobal.render(worldHell, player, renderMode); //Renders Everything on the screen for the game
		    		}
		    		else if(renderMode == RENDER_MODE_WORLD_SKY)
		    		{
		    		 	RenderGlobal.render(worldSky, player, renderMode); //Renders Everything on the screen for the game
		    		}
		        	fps++;
		        }
		        
		        if(settings.menuOpen)
		        {
		        	renderMenu.render(world, settings);
		        }
		        
		        GL11.glPopMatrix();
		        
		        Display.swapBuffers(); //allows the display to update when using VBO's, probably
		        Display.update(); //updates the display

				
		        if(System.currentTimeMillis() - start >= 5000)
		        {
		         	//System.out.println("FPS: " + ((double)(fps) / ((double)(System.currentTimeMillis() - start) / 1000.0D)));
		        	start = System.currentTimeMillis();
	        		end = System.currentTimeMillis();     
	        		fps = 0;
		    	}
	        	//     System.out.println(end - start);
		    }     
		}
		catch(Exception e) //Fatal error catching
		{
			e.printStackTrace();			
			ErrorUtils errorUtils = new ErrorUtils();
			errorUtils.writeErrorToFile(e, true);			
		}
		finally
		{
			//Save the settings
		    try 
		    {
				saveSettings();
			}
		    catch (FileNotFoundException e) 
		    {
				e.printStackTrace();
			}
		    catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Starts a game from the main menu.
	 * @param world the world to play on.
	 * @param player the player to play on.
	 */
	public void startGame(World world, EntityLivingPlayer player)
	{
		if(this.world != null)
		{
			throw new RuntimeException("World already exists!");
		}
		this.world = world;
		this.player = player;
		world.addPlayerToWorld(player);
		Dimensia.isMainMenuOpen = false;
		mainMenu = null;
	}
		
	/**
	 * Initializes miscellaneous things within the game engine. This should be called after object creation so 
	 * that the UI thread does not throw an exception while loading textures.
	 */
	public void init()
	{
		mainMenu = new GuiMainMenu();
		renderMenu = new RenderMenu(settings);
		Display.setVSyncEnabled(settings.vsyncEnabled);
		debugCheats();
	}
	/**
	 * Loads the Settings for the entire game upon starting the game.
	 * @throws IOException Indicates the saving operation has failed
	 * @throws ClassNotFoundException Indicates the Settings class does not exist with the correct version
	 */
	public void loadSettings() 
			throws IOException, ClassNotFoundException
	{
		FileManager fileManager = new FileManager();
		this.settings = fileManager.loadSettings();
	}
	/**
	 * Saves the Settings for the entire game to disk, this is called before exiting the run method.
	 * @throws IOException Indicates the saving operation has failed
	 * @throws FileNotFoundException Indicates the desired directory (file) is not found on the filepath
	 */
	public void saveSettings() 
			throws FileNotFoundException, IOException
	{
		FileManager fileManager = new FileManager();
		fileManager.saveSettings(settings);
	}
	
	/**
	 * Inits some cheats and inventory stuff for debugging
	 */
	private void debugCheats()
	{
		if(Dimensia.initInDebugMode)
		{
			Dimensia.isMainMenuOpen = false;
			FileManager fileManager = new FileManager();
			world = fileManager.generateNewWorld("World", 1200, 800, EnumDifficulty.EASY);//EnumWorldSize.LARGE.getWidth(), EnumWorldSize.LARGE.getHeight());
			player = fileManager.generateAndSavePlayer("!!", EnumDifficulty.NORMAL);//new EntityLivingPlayer("Test player", EnumDifficulty.NORMAL);
			world.addPlayerToWorld(player);
			world.assessForAverageSky();
			LightUtils utils = new LightUtils();
			utils.applyAmbient(world);
			
			player.inventory.pickUpItemStack(world, player, new ItemStack(Block.stone, 100));
			player.inventory.pickUpItemStack(world, player, new ItemStack(Block.torch, 100));
			player.inventory.pickUpItemStack(world, player, new ItemStack(Block.chest, 100));

			player.inventory.pickUpItemStack(world, player, new ItemStack(Block.ironChest, 100));
			//*
			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.healthPotion1, 100));
			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.healthPotion2, 100));
			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.manaPotion1, 100));
			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.manaPotion2, 100));
			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.manaStar, 100));
			/**/
			
			player.inventory.pickUpItemStack(world, player, new ItemStack(Block.furnace, 100));
			player.inventory.pickUpItemStack(world, player, new ItemStack(Block.craftingTable, 6));
			player.inventory.pickUpItemStack(world, player, new ItemStack(Block.plank, 100));
			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.goldPickaxe));
		
			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.copperIngot, 100));
			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.ironIngot, 6));

			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.absorbPotion1, 20));
//			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.criticalChancePotion1, 5));
//			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.criticalChancePotion2, 5));
//			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.attackSpeedPotion1, 5));
//			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.attackSpeedPotion2, 5));
//			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.damageBuffPotion1, 5));
//			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.damageBuffPotion2, 5));
//			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.damageSoakPotion1, 5));
//			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.damageSoakPotion2, 5));
//			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.dodgePotion1, 5));
//			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.dodgePotion2, 5));
//			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.manaRegenerationPotion1, 5));
//			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.manaRegenerationPotion2, 5));
//			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.steelSkinPotion1, 5));
//			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.steelSkinPotion2, 5));
//			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.swiftnessPotion1, 5));
//			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.swiftnessPotion2, 5));
			
			
			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.copperOre, 100));
			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.tinOre, 100));
			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.goldOre, 15));
			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.tinIngot, 100));
			
			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.goldHelmet));
			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.goldHelmet));
			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.goldBody));
			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.goldGreaves));
			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.bronzeHelmet));
			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.bronzeBody));
			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.bronzeGreaves));

			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.rocketBoots));
			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.heartCrystal, 2));
			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.manaCrystal, 2));
			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.angelsSigil, 2));
			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.ringOfVigor, 5));
			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.regenerationPotion2, 200));
			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.attackSpeedPotion1, 200));
			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.manaRegenerationPotion2, 200));
		}		
	}
	
	/**
	 * Gets the world object currently in use by the game. This will be null if the main menu is open or something breaks.
	 * @return the world object for the current game, which may be null if on the main menu
	 */
	public World getWorld()
	{
		return world;
	}
	
	/**
	 * Changes the loaded world to something else. For example, changing from Earth to Hell would use this. Calling
	 * this method forces a save of the remaining World and then loads what's required for the new World. The value 
	 * of the param newMode should correspond to the class variables in GameEngine such as RENDER_MODE_WORLD_EARTH or
	 * RENDER_MODE_WORLD_HELL. Supplying an incorrect value will not load a new World.
	 * @param newMode the final integer value for the new world (indicating what object to manipulate)
	 * @throws IOException indicates a general failure to load the file, not relating to a version error
	 * @throws ClassNotFoundException indicates the saved world version is incompatible
	 */
	public void changeWorld(int newMode)
			throws IOException, ClassNotFoundException
	{
		String worldName = "";
		if(renderMode == RENDER_MODE_WORLD_EARTH)
		{
			worldName = world.getWorldName();
			world.saveRemainingWorld("Earth");
			world = null;
		}
		else if(renderMode == RENDER_MODE_WORLD_HELL)
		{
			worldName = worldHell.getWorldName();
			worldHell.saveRemainingWorld("Hell");
			worldHell = null;
		}
		else if(renderMode == RENDER_MODE_WORLD_SKY)
		{
			worldName = worldSky.getWorldName();
			worldSky.saveRemainingWorld("Sky");
			worldSky = null;
		}
		
		this.renderMode = newMode;
		FileManager manager = new FileManager();
		
		if(renderMode == RENDER_MODE_WORLD_EARTH)
		{
			manager.loadWorld("Earth", worldName);
		}
		else if(renderMode == RENDER_MODE_WORLD_HELL)
		{
			manager.loadWorld("Hell", worldName);
		}
		else if(renderMode == RENDER_MODE_WORLD_SKY)
		{
			manager.loadWorld("Sky", worldName);
		}
	}
	
	/**
	 * Saves the remaining world that's loaded and the player to their respective save locations before
	 * exiting to the main menu.
	 * @throws FileNotFoundException indicates a failure to find the save location of the player or world
	 * @throws IOException indicates a general failure to save, not relating to the file
	 */
	public void closeGameToMenu() 
			throws FileNotFoundException, IOException
	{
		FileManager manager = new FileManager();
		manager.savePlayer(player);
	
		if(renderMode == RENDER_MODE_WORLD_EARTH)
		{
			world.saveRemainingWorld("Earth");
		}
		else if(renderMode == RENDER_MODE_WORLD_HELL)
		{
			world.saveRemainingWorld("Hell");
		}
		else if(renderMode == RENDER_MODE_WORLD_SKY)
		{
			world.saveRemainingWorld("Sky");
		}
	}
}
