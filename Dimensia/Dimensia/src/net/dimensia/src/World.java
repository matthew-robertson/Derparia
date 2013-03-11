package net.dimensia.src;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

/**
 * <code>World implements Serializable</code> <br>
 * <code>World</code> implements many of the key features for Dimensia to actually run properly and update. 
 * <br><br>
 * All players, chunks, Biomes, EntityEnemies, EntityLivingItemStacks, TemporaryText, Weather are stored in the 
 * World class. 
 * <br><br>
 * Methods exist to perform hittests between anything requiring it (monster and player, etc); as well as
 * methods to update entities, and everything else in the world. These methods are private and called 
 * though {@link #onWorldTick()}, as such no external source can modify the update rate, or otherwise
 * interfere with it. 
 * <br><br>
 * Other methods of interest, aside from onWorldTick(), relate to block breaking or Block requests.
 * {@link #getBlock(int, int)} and {@link #setBlock(Block, int, int)} implement most of the features 
 * required to "grandfather in" the old world.worldMap[][] style which has since been rendered obsolete 
 * by chunks. These methods are generally considered safe to use, with the same co-ordinates as the
 * previous world.worldMap[][] style. They should perform the division and modular division required 
 * automatically. <b>NOTE: These methods are relatively slow, and unsuitable for large-scale modifications.</b>
 * These large scale operations should be done directly through chunk data, with exact values not requiring
 * division/modular division every single request. 
 * <br><br>
 * The main methods relating to Block breaking and placement are {@link #breakBlock(int, int)} for breaking,
 * and {@link #placeBlock(int, int, Block)} for placing a Block. These methods differ from getBlock(int, int)
 * and setBlock(Block, int, int) significantly. These methods are for block placement and destruction while
 * the game is running (generally the player would do this). As a result, they must obey the rules of 
 * the game. These methods are relatively simple in what they do, instantly breaking and dropping or placing
 * a block upon call, but with regard to the rules of the game. They do not actually decrease inventory 
 * totals, add to them, or interact with the player in any way. As a result, anything in the project
 * can actually request a block placement/destroy using these methods, however it's advised that only the 
 * player actually do this.
 * 
 * @author      Alec Sobeck
 * @author      Matthew Robertson
 * @version     1.0
 * @since       1.0
 */
public class World implements Serializable
{
	private static final long serialVersionUID = 1L;	
	public Hashtable<String, Boolean> chunksLoaded;
	
	public Weather weather;
	public List<EntityLivingItemStack> itemsList;
	public List<TemporaryTextStore> temporaryText; 
	public List<EntityLivingNPCEnemy> entityList;
	public List<EntityLivingNPC> npcList;
	public List<EntityProjectile> projectileList;
	public SpawnManager manager;
	
	private int[] generatedHeightMap;
	private int averageSkyHeight;
	private int totalBiomes;
	private int chunkWidth;
	private int chunkHeight;
	private ChunkManager chunkManager;
	private boolean weatherFinished;
	private EnumDifficulty difficulty;
	private final Random random = new Random();
	private final int GAMETICKSPERDAY = 28800; 
	private final int GAMETICKSPERHOUR = 1200;
	private String worldName;
	private long worldTime;
	private ConcurrentHashMap<String, Chunk> chunks;
	private int width; //Width in blocks, not pixels
	private int height; //Height in blocks, not pixels
	private float previousLightLevel;
	private EntityLivingNPCEnemy[] spawnList;
	private LightUtils utils;
	private boolean lightingUpdateRequired;
	
	/**
	 * Reconstructs a world from a save file. This is the first step.
	 */
	public World()
	{
		setChunks(new ConcurrentHashMap<String, Chunk>(10));
		entityList = new ArrayList<EntityLivingNPCEnemy>(255);
		projectileList = new ArrayList<EntityProjectile>(255);
		npcList = new ArrayList<EntityLivingNPC>(255);
		temporaryText = new ArrayList<TemporaryTextStore>(100);
		itemsList = new ArrayList<EntityLivingItemStack>(250);
		chunksLoaded= new Hashtable<String, Boolean>(25);
		manager = new SpawnManager();
		utils = new LightUtils();
		checkChunks();
		lightingUpdateRequired = true;
	}
	
	/**
	 * Constructs a new instance of World. This involves creation of the EntityLists, and some miscellaneous
	 * fields being initialized, but largely initialization doesnt happen here. Instead things like WorldGen_
	 * initialize most of the important things, such as the 'world map', and ground-level map/averages, 
	 * @param name the world's name, assigned on creation
	 * @param width the width of the world, in blocks
	 * @param height the height of the world, in blocks
	 * @param difficulty the difficulty (EnumDifficulty) of the world
	 */
	public World(String name, int width, int height, EnumDifficulty difficulty)
	{
		setChunks(new ConcurrentHashMap<String, Chunk>(10));
		this.width = width;
		this.height = height; 
		entityList = new ArrayList<EntityLivingNPCEnemy>(255);
		projectileList = new ArrayList<EntityProjectile>(255);
		npcList = new ArrayList<EntityLivingNPC>(255);
		temporaryText = new ArrayList<TemporaryTextStore>(100);
		itemsList = new ArrayList<EntityLivingItemStack>(250);
		chunksLoaded= new Hashtable<String, Boolean>(25);
		worldTime = (long) (6.5 * GAMETICKSPERHOUR);
		worldName = name;
		totalBiomes = 0;
		previousLightLevel = getLightLevel();
		this.difficulty = difficulty;
		chunkManager = new ChunkManager(worldName);
		manager = new SpawnManager();
		chunkWidth = width / Chunk.getChunkWidth();
		chunkHeight = height / height;
		utils = new LightUtils();
		lightingUpdateRequired = true;
		checkChunks();
	}
		
	/**
	 * Finishes reconstructing a world object from disk. This is the 3rd and final step where anything dependent on
	 * variables saved to disk should be created/executed.
	 */
	public void finishWorldReconstruction()
	{
		chunkManager = new ChunkManager(worldName);
	}
	
	/**
	 * Puts the player at the highest YPosition for the spawn XPosition 
	 * @param player the player to be added
	 * @return the player with updated position (x, y)
	 */
	public EntityLivingPlayer spawnPlayer(EntityLivingPlayer player) 
	{
		if(player.inventory.isEmpty())
		{
			player.inventory.pickUpItemStack(this, player, new ItemStack(Item.copperSword));
			player.inventory.pickUpItemStack(this, player, new ItemStack(Item.copperPickaxe));
			player.inventory.pickUpItemStack(this, player, new ItemStack(Item.copperAxe));
			player.inventory.pickUpItemStack(this, player, new ItemStack(Block.craftingTable));
		}
		
		player.respawnXPos = getWorldCenterOrtho();
		
		//requestRequiredChunks((int)(player.respawnXPos / 6), (int)(player.y / 6));
		//chunkManager.addAllLoadedChunks_Wait(this, chunks);
		
		requestRequiredChunks(getWorldCenterBlock(), averageSkyHeight);
		chunkManager.addAllLoadedChunks_Wait(this, getChunks());
		
		
		for(int i = 0; i < height - 1; i++)
		{
			if(getBlock((int)(player.respawnXPos / 6), i).blockID == 0 && getBlock((int)(player.respawnXPos / 6) + 1, i).blockID == 0) 
			{
				continue;
			}
			if(getBlock((int)player.respawnXPos / 6, i).blockID != 0 || getBlock((int) (player.respawnXPos / 6) + 1, i).blockID != 0)
			{	
				player.x = player.respawnXPos;
				player.y = (i * 6) - 18;				
				return player;
			}			
		}
		return player;
	}
		
	/**
	 * Opens the worlddata.dat file and applies all the data to the reconstructed world. Then final reconstruction is performed.
	 * @param BASE_PATH the base path for the Dimensia folder, stored on disk
	 * @param worldName the name of the world to be loaded
	 * @throws FileNotFoundException indicates the worlddata.dat file cannot be found
	 * @throws IOException indicates the load operation has failed to perform the required I/O. This error is critical
	 * @throws ClassNotFoundException indicates casting of an object has failed, due to incorrect class version or the class not existing
	 */
	public void loadAndApplyWorldData(final String BASE_PATH, String worldName, String dir)
			throws FileNotFoundException, IOException, ClassNotFoundException
	{
		//Open an input stream for the file
		ObjectInputStream ois = new ObjectInputStream(new DataInputStream(new GZIPInputStream(new FileInputStream(BASE_PATH + "/World Saves/" + worldName + "/" + dir + "/worlddata.dat")))); 
		
		/**
		Variables are loaded in the following order:
			worldName
			width
			height
			chunkWidth
			chunkHeight
			averageSkyHeight
			generatedHeightMap
			worldTime
			totalTimes
			difficulty
			biomes
			biomesByColumn
		**/
		
		this.worldName = (String)ois.readObject();
		width = Integer.valueOf((ois.readObject()).toString()).intValue();
		height = Integer.valueOf((ois.readObject()).toString()).intValue();
		chunkWidth = Integer.valueOf((ois.readObject()).toString()).intValue();
		chunkHeight = Integer.valueOf((ois.readObject()).toString()).intValue();
		averageSkyHeight = Integer.valueOf((ois.readObject()).toString()).intValue();
		generatedHeightMap = (int[])ois.readObject();
		worldTime = Long.valueOf((ois.readObject()).toString()).longValue();
		totalBiomes = Integer.valueOf((ois.readObject()).toString()).intValue();
		difficulty = (EnumDifficulty)ois.readObject();
		itemsList = (ArrayList<EntityLivingItemStack>)ois.readObject();
		
		System.out.println("Loaded And Applied World Data");
		ois.close();
		//Finalize construction of the world. For example, chunkManager must be constructed here as it depends on variables 
		//from the .dat file
		finishWorldReconstruction();
	}
	
	/**
	 * Adds a player to the world. Currently multiplayer placeholder.
	 * @param player the player to add
	 */
	public void addPlayerToWorld(EntityLivingPlayer player)
	{
		requestRequiredChunks(getWorldCenterBlock(), averageSkyHeight);
		chunkManager.addAllLoadedChunks_Wait(this, getChunks());
		player = spawnPlayer(player);
	}
	
	/**
	 * Loads all nearby chunks for a location, given the Display's size
	 * @param x the x value of the point to load near (in blocks)
	 * @param y the y value of the point to load near (in blocks)
	 */
	private void requestRequiredChunks(int x, int y)
	{
		final int loadDistanceHorizontally = (((int)(Display.getWidth() / 2.2) + 3) > Chunk.getChunkWidth()) ? 
				((int)(Display.getWidth() / 2.2) + 3) : Chunk.getChunkWidth();
		
		//Where to check, in the chunk map (based off loadDistance variables)
		int leftOff = (x - loadDistanceHorizontally) / Chunk.getChunkWidth();
		int rightOff = (x + loadDistanceHorizontally) / Chunk.getChunkWidth();
		
		for(int i = leftOff; i <= rightOff; i++) //Check for chunks that need loaded
		{
			chunkManager.requestChunk("Earth", this, getChunks(), i);
		}
	}
	
	/**
	 * Clears all monsters from entityList, generally invoked after a single player death to provide some mercy to the player.
	 */
	public void clearEntityList()
	{
//		entityList.clear();
		entityList = new ArrayList<EntityLivingNPCEnemy>(255);
	}
	
	/**
	 * Clears all projectiles from projectileList
	 */
	public void clearProjectileList()
	{
		projectileList.clear();
	}
	
	/**
	 * Clears all NPCs from npcList
	 */
	public void clearNPCList()
	{
		npcList.clear();
	}
	
	/**
	 * Keeps track of the worldtime and updates the light if needed
	 */
	public void updateWorldTime(EntityLivingPlayer player)
	{
		//worldTime / GAMETICKSPERHOUR = the hour (from 00:00 to 24:00)
		worldTime++;
		if(worldTime >= GAMETICKSPERDAY)//If the time exceeds 24:00, reset it to 0:00
		{
			worldTime = 0;
		}
		
		if(getLightLevel() != previousLightLevel) //if the sunlight has changed, update it
		{
			previousLightLevel = getLightLevel();
			lightingUpdateRequired = true;
		}		
	}
	
	/**
	 * Gets the light value of the sun. Full light is a value of 1.0f, minimal (night) light is .2f
	 */
	public float getLightLevel()
	{
		//LightLevel of 1.0f is full darkness (it's reverse due to blending mechanics)
		//Light Levels By Time: 20:00-4:00->20%; 4:00-8:00->20% to 100%; 8:00-16:00->100%; 16:00-20:00->20% to 100%;  
		
		float time = (float)(worldTime) / GAMETICKSPERHOUR; 
		
		return (time > 8 && time < 16) ? 1.0f : (time >= 4 && time <= 8) ? MathHelper.roundDownFloat20th(((((time - 4) / 4.0F) * 0.8F) + 0.2F)) : (time >= 16 && time <= 20) ? MathHelper.roundDownFloat20th(1.0F - ((((time - 16) / 4.0F) * 0.8F) + 0.2F)) : 0.2f;
	}
		
	/**
	 * Gets the center of the world, with a block value. This is xsize / 2 
	 * @return xsize / 2 (giving the center block of the world)
	 */
	public final int getWorldCenterBlock()
	{
		return width / 2;
	}
	
	/**
	 * Gets the 'ortho' unit value for the world's center. 1 block = 6 ortho units, so this is xsize * 3
	 * @return xsize * 3 (the world's center in ortho)
	 */
	public final int getWorldCenterOrtho()
	{
		return width * 3;
	}
	
	/**
	 * Adds an EntityLivingNPCEnemy to the entityList in this instance of World
	 * @param enemy the enemy to add to entityList
	 */
	public void addEntityToEnemyList(EntityLivingNPCEnemy enemy)
	{
		entityList.add(enemy);
	}
	
	/**
	 * Adds an EntityLivingNPC to the npcList in this instance of World
	 * @param npc the npc to add to entityList
	 */
	public void addEntityToNPCList(EntityLivingNPC npc)
	{
		npcList.add(npc);
	}
	
	/**
	 * Adds an entityProjectile to the projectileList in this instance of World
	 * @param projectile the projectile to add to projectileList
	 */
	public void addEntityToProjectileList(EntityProjectile projectile)
	{
		projectileList.add(projectile);
	}

	/**
	 * Adds an EntityLivingItemStack to the itemsList in this instance of World
	 * @param stack the EntityLivingItemStack to add to itemsList
	 */
	public void addItemStackToItemList(EntityLivingItemStack stack)
	{
		itemsList.add(stack);
	}
	
	/**
	 * Adds a piece of temporary text to the temporaryText ArrayList. This text is rendered until its time left runs out.
	 * This text is generally from healing, damage, (combat)
	 * @param message the text to display
	 * @param x the x position in ortho units
	 * @param y the y position in ortho units
	 * @param ticksLeft the time (in game ticks) before the text despawns
	 * @param type the type of combat text (affects the colour). For example 'g' makes the text render green
	 */
	public void addTemporaryText(String message, int x, int y, int ticksLeft, char type)
	{
		temporaryText.add(new TemporaryTextStore(message, x, y, ticksLeft, type));
	}
	
	/**
	 * Calls all the methods to update the world and its inhabitants
	 */
	public void onWorldTick(EntityLivingPlayer player)
	{		
	//	spawnMonsters(player);				
		causeWeather();		
		//update the player
		
		player.onWorldTick(this); 
		
		//Update Entities
		updateMonsters(player); 
		updateNPCs(player);
		updateProjectiles(player);
		updateTemporaryText();
		updateEntityLivingItemStacks();
		//Hittests
		performPlayerMonsterHittests(player); 
		performProjectileHittests(player);
		performPlayerItemHittests(player);
		performEnemyToolHittests(player);
		//Update the time
		updateWorldTime(player);
		//checkChunks();
		updateChunks(player);
		updateMonsterStatusEffects();
		applyLightingUpdates(player);
		
		
		if(chunkManager.isAnyLoadOperationDone())
		{
			chunkManager.addAllLoadedChunks(this, getChunks());
		}
		if (Mouse.isButtonDown(0) && player.inventory.getMainInventoryStack(player.selectedSlot) != null) 
		{ //player mining, if applicable
			player.breakBlock(this, ((Render.getCameraX() + MathHelper.getCorrectMouseXPosition()) / 6), ((Render.getCameraY() + MathHelper.getCorrectMouseYPosition()) / 6), (Item.itemsList[player.inventory.getMainInventoryStack(player.selectedSlot).getItemID()]));
		}
	}
	
	/**
	 * Updates (and possibly removes) monster status effects previously registered
	 */
	private void updateMonsterStatusEffects()
	{
		for(int i = 0; i < entityList.size(); i++)
		{
			entityList.get(i).checkAndUpdateStatusEffects(this);
		}
	}	
		
	/**
	 * Gets how many horizontal chunks the world has. This is equal to (width / Chunk.getChunkWidth())
	 * @return the number of horizontal chunks the world has
	 */
	public int getChunkWidth()
	{
		return chunkWidth;
	}
	
	/**
	 * Gets how many vertical chunks the world has. This is equal to (height / height)
	 * @return the number of vertical chunks the world has
	 */
	public int getChunkHeight()
	{
		return chunkHeight;
	}
	
	/**
	 * Applies gravity to all itemstacks entities
	 */
	private void updateEntityLivingItemStacks()
	{
		for(int i = 0; i < itemsList.size(); i++)
		{
			itemsList.get(i).applyGravity(this);
		}		
	}
	
	/**
	 * Picks up itemstacks that the player is standing on (or very near to)
	 */
	private void performPlayerItemHittests(EntityLivingPlayer player)
	{
		for(int i = 0; i < itemsList.size(); i++)
		{
			double distance = MathHelper.distanceBetweenTwoPoints(itemsList.get(i).x + (itemsList.get(i).width / 2), itemsList.get(i).y + (itemsList.get(i).height / 2),
																  player.x + (player.width / 2), player.y + (player.height / 2));
			if(distance <= itemsList.get(i).width * 1.75 && itemsList.get(i).ticksBeforePickup <= 0) 
			{ //is the player near the itemstack, and can it be picked up yet?
				ItemStack stack = player.inventory.pickUpItemStack(this, player, itemsList.get(i).stack); //if so try to pick it up
				
				if(stack == null) //nothing's left, remove the null element
				{
					itemsList.remove(i);
				}
				else //otherwise, put back what's left
				{
					itemsList.get(i).stack = stack;				
				}
			}
			else
			{
				itemsList.get(i).ticksBeforePickup--; 
			}	
		}
	}
	
	/**
	 * applies AI to npcs
	 */
	private void updateNPCs(EntityLivingPlayer player){
		for (int i = 0; i < npcList.size(); i++){
			if (npcList.get(i).isDead()){
				npcList.remove(i);
				continue;
			}
			npcList.get(i).applyAI(this, player);
			
			if(npcList.get(i).inBounds(player.x, player.y, player.width, player.height)){
				npcList.get(i).onPlayerNear();
			}
			npcList.get(i).applyGravity(this);
		}
	}
	
	/**
	 * 
	 * @param player
	 */
	private void updateMonsters(EntityLivingPlayer player)
	{
		final int OUT_OF_RANGE = (int) ((Display.getHeight() > Display.getWidth()) ? Display.getHeight() * 0.75 : Display.getWidth() * 0.75);
		for(int i = 0; i < entityList.size(); i++)
		{
			if(entityList.get(i).isDead()) //if the monster is dead, try to drop items
			{
				ItemStack[] drops = entityList.get(i).getDrops(); //get possible drops
				if(drops != null) //if there're drops
				{
					for(ItemStack stack : drops) //drop each of them
					{
						addItemStackToItemList(new EntityLivingItemStack(entityList.get(i).x - 1, entityList.get(i).y - 1, stack));
					}
				}
				entityList.remove(i);
				continue;
			}
			else if((MathHelper.distanceBetweenTwoPoints(player.x, player.y, entityList.get(i).x, entityList.get(i).y) > OUT_OF_RANGE && !entityList.get(i).isBoss))
			{ //If the monster is dead, or too far away, remove it
				entityList.remove(i);
				//System.out.println("Entity Removed @" + i);
				continue;
			}
			entityList.get(i).applyAI(this, player); //otherwise apply AI
		}
	}
	
	/**
	 * Method designed to handle the updating of all blocks
	 * @param x - x location in the world
	 * @param y - location in the world
	 * @return - updated bitmap
	 */
	
	public int updateBlockBitMap(int x, int y){
		int bit = getBlockGenerate(x,y).getBitMap();
		//If the block is standard
		if (getBlockGenerate(x, y).getTileMap() == 'g'){
			return updateGeneralBitMap(x, y);
		}
		//if the block requires special actions
		//If the block is a pillar
		else if (getBlockGenerate(x, y).getTileMap() == 'p'){
			return updatePillarBitMap(x, y);	
		}
		return bit;
	}
	
	/**
	 * Method to determine the bitmap
	 * @param x - location of the block on the x axis
	 * @param y - location of the block on the y axis
	 * @return bit - the int to be used for calculating which texture to use
	 */
	private int updateGeneralBitMap(int x, int y){
		int bit = 0;
		if (getBlockGenerate(x, y - 1).isSolid){
			bit += 1;
		}
		if (getBlockGenerate(x, y + 1).isSolid){
			bit += 4;
		}
		if (getBlockGenerate(x - 1, y).isSolid){
			bit += 8;
		}
		if (getBlockGenerate(x + 1, y).isSolid){
			bit += 2;
		}
		if (getBlockGenerate(x, y) instanceof BlockGrass && (bit == 15 || bit == 11 || bit == 27 || bit == 31)){
			setBlockGenerate(Block.dirt.setBitMap(bit), x,y);
		}
		return bit;
	}
	
	/**
	 * Subroutine for updateBlockBitMap, specific for pillars.
	 * @param x - location of the block on the x axis
	 * @param y - location of the block on the y axis
	 * @return bit - the int to be used to calculate which texture to use
	 */
	
	private int updatePillarBitMap(int x, int y){
		int bit;
		if (getBlockGenerate(x, y + 1) instanceof BlockPillar){
			bit = 1;
		}
		
		else {
			bit = 2;
		}
		
		if (!getBlockGenerate(x, y - 1).isOveridable && !(getBlockGenerate(x, y - 1) instanceof BlockPillar)){
			//System.out.println(getBlockGenerate(x, y-1).getBlockID());
			bit = 0;					
		}
		
		if (!getBlockGenerate(x, y - 1).isOveridable && !getBlockGenerate(x, y + 1).isOveridable && !(getBlockGenerate(x, y + 1) instanceof BlockPillar) && !(getBlockGenerate(x, y - 1) instanceof BlockPillar)){
			bit = 3;
		}
		return bit;
	}	
	
	/**
	 * Updates (and possibly removes) projectiles
	 * @param player - player to compare distances against
	 */
	private void updateProjectiles(EntityLivingPlayer player)
	{
		final int OUT_OF_RANGE = (int) ((Display.getHeight() > Display.getWidth()) ? Display.getHeight() * 0.75 : Display.getWidth() * 0.75);
		for(int i = 0; i < projectileList.size(); i++)
		{
			if (projectileList.get(i).active){
				projectileList.get(i).moveProjectile(this);
			}
			else if (!projectileList.get(i).active){
				projectileList.get(i).ticksNonActive++;
				//System.out.println(projectileList.get(i).ticksNonActive);
			}
			
			if(((MathHelper.distanceBetweenTwoPoints(player.x, player.y, projectileList.get(i).x, projectileList.get(i).y) > OUT_OF_RANGE) || projectileList.get(i).ticksNonActive > 80) && projectileList.get(i).getType() != 'm')
			{ //If the projectile is too far away, remove it
				if (projectileList.get(i).ticksNonActive > 1 && projectileList.get(i).getDrop() != null){
					addItemStackToItemList(new EntityLivingItemStack(projectileList.get(i).x - 1, projectileList.get(i).y - 1, projectileList.get(i).getDrop()));
				}
				projectileList.remove(i);
				continue;
			}
			else if (((MathHelper.distanceBetweenTwoPoints(player.x, player.y, projectileList.get(i).x, projectileList.get(i).y) > OUT_OF_RANGE) || projectileList.get(i).ticksNonActive > 1) && projectileList.get(i).getType() == 'm'){
				projectileList.remove(i);
				continue;
			}
		}
	}
	
	/**
	 * Sees if any monsters have hit (are in range of) the player
	 */
	private void performPlayerMonsterHittests(EntityLivingPlayer player)
	{
		for(int i = 0; i < entityList.size(); i++)
		{
			if(player.inBounds(entityList.get(i).x, entityList.get(i).y, entityList.get(i).width, entityList.get(i).height))
			{ //If the player is in bounds of the monster, damage them
				player.damageEntity(this, entityList.get(i).damageDone, ((Math.random() < entityList.get(i).criticalStrikeChance) ? true : false));
			}
		}
	}
	
	/**
	 * Sees if any projectiles have hit (are in range of) players or npcs
	 */
	private void performProjectileHittests(EntityLivingPlayer player)
	{
		for (int i = 0; i < projectileList.size(); i++){
			if (projectileList.get(i).isFriendly){
				for(int j = 0; j < entityList.size(); j++)
				{
					if(entityList.get(j).inBounds(projectileList.get(i).x, projectileList.get(i).y, projectileList.get(i).width, projectileList.get(i).height))
					{ //If the projectile is in bounds of the monster, damage them
						entityList.get(j).damageEntity(this, projectileList.get(i).damage, ((Math.random() < projectileList.get(i).criticalStrikeChance) ? true : false));
					}
				}
			}
			if (projectileList.get(i).isHostile){
				if(player.inBounds(projectileList.get(i).x, projectileList.get(i).y, projectileList.get(i).width, projectileList.get(i).height))
				{ //If the projectile is in bounds of the player, damage them
					player.damageEntity(this, projectileList.get(i).damage, ((Math.random() < projectileList.get(i).criticalStrikeChance) ? true : false));
				}
			}
		}
	}
	
	/**
	 * Displays all temporary text in the world, or remove it if it's past its life time
	 */
	private void updateTemporaryText()
	{
		for(int i = 0; i < temporaryText.size(); i++)
		{
			temporaryText.get(i).ticksLeft--; //reduce time remaining
			if(temporaryText.get(i).ticksLeft <= 0)
			{ //remove obsolete text
				temporaryText.remove(i);
			}
		}
	}
	
	/**
	 * Attempts to spawn monsters, based on random numbers
	 * @return number of monsters successfully spawned
	 */
	private int spawnMonsters(EntityLivingPlayer player)
	{
		int totalTries = 2 + random.nextInt(4);
		//int totalTries = 500;		
		//WARNING, THE LINE ABOVE IS VERY, VERY AGGRESSIVE SPAWNING. NOT INTENDED FOR RELEASE BUT TESTING INSTEAD
		
		float time = (float)(worldTime) / GAMETICKSPERHOUR; 
		if(time < 4 || time > 20) //spawn more at night
		{
			totalTries += (3 + random.nextInt(3));
		}	
		
		String active = "";
		int counter = 0;
		int entitych = 0;
		
		for(int i = 0; i < totalTries; i++) 
		{
			if(entityList.size() > 255) //too many monsters spawned
			{
				return counter;
			}			
			
			int xoff = 0;
			int yoff = 0;
			int xscreensize_b = (Display.getWidth() / 22) + 5;
			int yscreensize_b = (Display.getHeight() / 22) + 5;		
			
			//how far away the monster will spawn from the player:
			if(random.nextInt(2) == 0) //spawn to the left
			{ 
				xoff = MathHelper.returnIntegerInWorldMapBounds_X(this, (int)(player.x / 6) - random.nextInt(100) - xscreensize_b);
			}
			else //spawn to the right
			{
				xoff = MathHelper.returnIntegerInWorldMapBounds_X(this, (int)(player.x / 6) + random.nextInt(100) + xscreensize_b);
			}
			if(random.nextInt(2) == 0) //spawn above 
			{
				yoff = MathHelper.returnIntegerInWorldMapBounds_Y(this, (int)(player.y / 6) - random.nextInt(100) - yscreensize_b);
			}
			else //spawn below
			{
				yoff = MathHelper.returnIntegerInWorldMapBounds_Y(this, (int)(player.y / 6) + random.nextInt(60) + yscreensize_b);
			}
			
			active = getBiomeColumn(""+(int)(xoff));
			
			if(active == null) //Should indicate the chunk isnt loaded (good failsafe)
			{
				continue;
			}
			
			active = active.toLowerCase();
			
			if (active.equals("forest")){
				if (time < 4 || time > 20){
					spawnList = manager.getForestNightEnemiesAsArray();
				}
				
				else {
					spawnList = manager.getForestDayEnemiesAsArray();
				}
			}
			
			else if (active.equals("desert")){
				if (time < 4 || time > 20){
					spawnList = manager.getDesertNightEnemiesAsArray();
				}
				
				else {
					spawnList = manager.getDesertDayEnemiesAsArray();
				}
			}
			
			else if (active.equals("arctic")){
			//	System.out.println("hey!");
				if (time < 4 || time > 20){
					spawnList = manager.getArcticNightEnemiesAsArray();
				}
				
				else {
					spawnList = manager.getArcticDayEnemiesAsArray();
				}
			}
			entitych = (int)random.nextInt(spawnList.length);
			//System.out.println(spawnList[entitych].getEnemyName());
			try
			{				
				for(int j = 0; j <spawnList[entitych].getBlockHeight(); j++)//Y
				{
					for(int k = 0; k < spawnList[entitych].getBlockWidth(); k++)//X
					{
						if(!getBlock(xoff + k, yoff + j).isPassable())
						{
							throw new RuntimeException("Dummy");
						}
					}
				}
			}
			catch (Exception e) //if this gets hit, the entity cant actually spawn
			{
				continue;
			}
			
			//So the entity can spawn...
			
			try
			{
				//Ground Entity:
				if((!getBlock(xoff, (yoff + 3)).isPassable() || !getBlock(xoff + 1, (yoff + 3)).isPassable())) //make sure there's actually ground to spawn on
				{
					EntityLivingNPCEnemy enemy = new EntityLivingNPCEnemy(spawnList[entitych]);
					enemy.setPosition(xoff * 6, yoff * 6);
					entityList.add(enemy);
					counter++;
				}	
			}
			catch(Exception e)
			{
				//e.printStackTrace();
			}
		}
		
		return counter;
	}
	
	/**
	 * Tries to make weather happen on each game tick
	 */
	private void causeWeather()
	{
		if(weatherFinished) //clear finished weather
		{
			weather = null;
		}
		if(weather != null) //If there's weather
		{
			if(--weather.ticksLeft <= 0) //decrease time left
			{
				disableWeather();
			}
		}
		
		/*
		for (ConcurrentHashMap.Entry<String, Chunk> entry : chunks.entrySet()) 
		{
		    System.out.println(entry.getValue().getBiome());
		}
		*/
		
		for(ConcurrentHashMap.Entry<String, Chunk> entry: getChunks().entrySet())
		{
			Biome biome = entry.getValue().getBiome();			
			if(biome != null && biome.biomeID == Biome.arctic.biomeID) //if the biome is arctic
			{
				if(random.nextInt(150000) == 0) //and a random chance is met
				{
					if(weather == null) //and it's null (not in use)
					{
						weather = new WeatherSnow(this, biome, averageSkyHeight); //cause weather!
						weatherFinished = false;
					}
				}
			}
		}
	}
	
	private void performEnemyToolHittests(EntityLivingPlayer player) //Work in progress, not yet fully implemented
	{
		if(!player.hasSwungTool || (player.inventory.getMainInventoryStack(player.selectedSlot) == null) || !(Item.itemsList[player.inventory.getMainInventoryStack(player.selectedSlot).getItemID()] instanceof ItemTool))
		{
			return;
		}
		
		int size = 20;		
		int x = 0;
		int y = -size;
		int h = size;
		int w = size;        
		float angle = player.rotateAngle / 57.5f;
		
		double[] x_points = 
		{
			player.x + 9 + (((x) * Math.cos(angle)) - ((y+h) * Math.sin(angle))),
			player.x + 9 + (((x+w) * Math.cos(angle)) - ((y+h) * Math.sin(angle))),
			player.x + 9 + (((x+w) * Math.cos(angle)) - ((y) * Math.sin(angle))),
			player.x + 9 + (((x) * Math.cos(angle)) - (y * Math.sin(angle))) 
		};
		
		double[] y_points = 
		{
			player.y + 9 +(((x) * Math.sin(angle)) + ((y+h) * Math.cos(angle))),	
			player.y + 9 +(((x+w) * Math.sin(angle)) + ((y+h) * Math.cos(angle))),
			player.y + 9 +(((x+w) * Math.sin(angle)) + ((y) * Math.cos(angle))),
			player.y + 9 +(((x) * Math.sin(angle)) + ((y) * Math.cos(angle)))
		};
		
		for(int i = 0; i < entityList.size(); i++)
		{
			for(int j = 0; j < 4; j++)
			{
				if(entityList.get(i).inBounds((float)x_points[j], (float)y_points[j]))
				{
					boolean flag = ((Math.random() < player.criticalStrikeChance) ? true : false);
					int damageDone =((ItemTool) Item.itemsList[player.inventory.getMainInventoryStack(player.selectedSlot).getItemID()]).getDamageDone();
					damageDone = (int) (damageDone * player.allDamageModifier * player.meleeDamageModifier);
					//CURRENTLY DAMAGE DONE IS ALWAYS MELEE
					entityList.get(i).damageEntity(this, damageDone, flag);
					
					//knockback
					/****
					 * ENTITIES NEED STUNNED BRIEFLY AFTER KNOCKBACK
					 * 
					 * 
					 * 
					 * 
					 * 
					 * 
					 * 
					 * 
					 */
					int knockBack = (int) (player.knockbackModifier * 6);
					String direction = player.getDirectionOfQuadRelativeToEntityPosition(entityList.get(i).x, entityList.get(i).y, entityList.get(i).width, entityList.get(i).height);
					
					//System.out.println("@index=" + i + "!knock_back@time= || " + knockBack + " " + direction + " " + ((knockBack % 6 == 0) ? (knockBack / 6) : (knockBack / 6) + 1));
					if(direction.equals("right"))
					{
						for(int k = 0; k < ((knockBack % 6 == 0) ? (knockBack / 6) : (knockBack / 6) + 1); k++)
						{
							entityList.get(i).moveEntityRight(this, knockBack);
						}	
					}
					else if(direction.equals("left"))
					{
						for(int k = 0; k < ((knockBack % 6 == 0) ? (knockBack / 6) : (knockBack / 6) + 1); k++)
						{
							entityList.get(i).moveEntityLeft(this, knockBack);
						}
					}
					

					entityList.get(i).registerStatusEffect(new StatusEffectStun(0.4f, 1));
					
				}
			}
		}

		if(player.isSwingingRight)
		{
            player.rotateAngle = (player.rotateAngle < -120) ? 40 : (player.rotateAngle > 40) ? -120 : player.rotateAngle + 10;	
	        if(player.rotateAngle <= -120 || player.rotateAngle >= 40)
	        {
	        	player.hasSwungTool = false;
	        }
		}
		else
		{
		    player.rotateAngle = (player.rotateAngle < -120) ? 40 : (player.rotateAngle > 40) ? -120 : player.rotateAngle - 10;	
	        if(player.rotateAngle <= -120 || player.rotateAngle >= 40)
	        {
	        	player.hasSwungTool = false;
	        }
		}
	}
	
	/**
	 * Handles block break events, based on what the block is
	 * @param mx x position in the 'world map'
	 * @param my y position in the 'world map'
	 */
	private void handleBlockBreakEvent(EntityLivingPlayer player, int mx, int my)
	{
		if(!getBlock(mx, my).hasMetaData) //normal block
		{
			ItemStack stack = getBlock(mx, my).getDroppedItem();
			if(stack != null) //if there's an item to drop, add it to the list of dropped items
			{
				addItemStackToItemList(new EntityLivingItemStack((mx * 6) - 1, (my * 6) - 2, stack));
			}
			
			if(getBlock(mx, my) instanceof BlockLight)
			{
				//removeLightSource(player, mx, my, ((BlockLight)(getBlock(mx, my))).lightRadius, ((BlockLight)(getBlock(mx, my))).lightStrength);
				setBlock(Block.air, mx, my, EnumEventType.EVENT_BLOCK_BREAK_LIGHT); //replace it with air
			}
			else
			{
				setBlock(Block.air, mx, my, EnumEventType.EVENT_BLOCK_BREAK); //replace it with air
			}
			
		}
		else
		{
			if(getBlock(mx, my) instanceof BlockChest)
			{
				BlockChest chest = ((BlockChest)(getBlock(mx, my)));
				if(chest.metaData != 1)
				{
					int[][] metadata = MetaDataHelper.getMetaDataArray(getBlock(mx, my).blockWidth / 6, getBlock(mx, my).blockHeight / 6); //metadata used by the block of size (x,y)
					int metaWidth = metadata.length; 
					int metaHeight = metadata[0].length;	
					int x1 = 0;
					int y1 = 0;				
					
					for(int i = 0; i < metaWidth; i++) 
					{
						for(int j = 0; j < metaHeight; j++)
						{
							if(metadata[i][j] == getBlock(mx - x1, my - y1).metaData)
							{
								x1 = i; 
								y1 = j;
								break;
							}
						}
					}			
					
					chest = (BlockChest)(getBlock(mx - x1, my - y1));
					mx -= x1;
					my -= y1;
				}	
				
				ItemStack[] stacks = chest.getMainInventory();
				for(int i = 0; i < stacks.length; i++)
				{
					if(stacks[i] != null)
					{
						addItemStackToItemList(new EntityLivingItemStack((mx * 6) + random.nextInt(8) - 2, (my * 6) + random.nextInt(8) - 2, stacks[i])); //drop the item into the world
					}
				}
				
				if(((BlockChest)(getBlock(mx, my))).isAttachedLeft())
				{
					((BlockChest)(getBlock(mx - 2, my))).removeAttachment();
				}
				else if(((BlockChest)(getBlock(mx, my))).isAttachedRight())
				{
					((BlockChest)(getBlock(mx + 2, my))).removeAttachment();
				}
				
				player.clearViewedChest();
			}
			
			ItemStack stack = getBlock(mx, my).getDroppedItem(); //the item dropped by the block
			if(stack != null)
			{			
				int[][] metadata = MetaDataHelper.getMetaDataArray(getBlock(mx, my).blockWidth / 6, getBlock(mx, my).blockHeight / 6); //metadata used by the block of size (x,y)
				int metaWidth = metadata.length; //width of the metadata
				int metaHeight = metadata[0].length; //height of the metadata
	
				int x = 0;
				int y = 0;				
				for(int i = 0; i < metaWidth; i++) //cycle through the metadata until the value of the broken block is matched
				{
					for(int j = 0; j < metaHeight; j++)
					{
						if(metadata[i][j] == getBlock(mx, my).metaData)
						{
							x = i; 
							y = j;
							break;
						}
					}
				}
				
				int xOffset = x * -1; //how far over in the block the player in mining
				int yOffset = y * -1; 
							
				for(int i = 0; i < metaWidth; i++) //break the block
				{
					for(int j = 0; j < metaHeight; j++)
					{
						setBlock(Block.air, mx + i + xOffset, my + j + yOffset, EnumEventType.EVENT_BLOCK_BREAK);
					}					
				}
				
				addItemStackToItemList(new EntityLivingItemStack((mx * 6) - 1, (my * 6) - 2, stack)); //drop the item into the world
			}
		}		
		getBlockGenerate(mx-1,my).setBitMap(updateBlockBitMap(mx-1, my));
		getBlockGenerate(mx,my-1).setBitMap(updateBlockBitMap(mx, my-1));
		getBlockGenerate(mx,my).setBitMap(updateBlockBitMap(mx, my));
		getBlockGenerate(mx+1,my).setBitMap(updateBlockBitMap(mx+1, my));
		getBlockGenerate(mx,my+1).setBitMap(updateBlockBitMap(mx, my+1));
		player.resetMiningVariables();
		//LightingEngine.applySunlight(this);
	}
	
	/**
	 * Handles player block placement
	 * @param mx x position in worldmap array, of the block being placed
	 * @param my y position in the worldmap array, of the block being placed
	 * @param block the block to be placed
	 */
	public void placeBlock(EntityLivingPlayer player, int mx, int my, Block block)
	{
		if(block.hasMetaData) //if the block is large
		{
			int blockWidth = block.getBlockWidth() / 6;
			int blockHeight = block.getBlockHeight() / 6;
			int[][] metadata = MetaDataHelper.getMetaDataArray(blockWidth, blockHeight);
			
			for(int i = 0; i < blockWidth; i++) //is it possible to place the block?
			{
				for(int j = 0; j < blockHeight; j++)
				{
					if(getBlock(mx + i, my + j) != Block.air && !getBlock(mx + i, my + j).getIsOveridable())
					{
						return;
					}
				}
			}
			
			boolean canBePlaced = false;
			//Check for at least one solid block on some side of the placement:
			for(int i = 0; i < blockWidth; i++) //Top
			{
				if(getBlock(mx + i, my - 1).getIsSolid())
				{
					canBePlaced = true;
				}
			}
			for(int i = 0; i < blockWidth; i++) //Bottom
			{
				if(getBlock(mx + i, my + blockHeight).getIsSolid())
				{
					canBePlaced = true;
				}
			}
			for(int i = 0; i < blockHeight; i++) //Left
			{
				if(getBlock(mx - 1, my + i).getIsSolid())
				{
					canBePlaced = true;
				}
			}
			for(int i = 0; i < blockHeight; i++) //Right
			{
				if(getBlock(mx + blockWidth, my + i).getIsSolid())
				{
					canBePlaced = true;
				}
			}
			
			if(!canBePlaced) //If it cant be placed, then give up trying right here
			{
				return;
			}
			
			for(int i = 0; i < metadata.length; i++) //place the block(s)
			{
				for(int j = 0; j < metadata[0].length; j++)
				{
					if(block instanceof BlockChest)
					{
						BlockChest chest = (BlockChest) block.clone();
						if(metadata[i][j] == 1)
						{
							if(getBlock(mx + i - 2, my + j) instanceof BlockChest)
							{
								if(!((BlockChest)(getBlock(mx + i - 2, my + j))).isAttached() && getBlock(mx + i - 2, my + j).metaData == 1)
								{
									chest.attachLeft();
									((BlockChest)(getBlock(mx + i - 2, my + j))).attachRight();
								}
							}
							else if(getBlock(mx + i + 2, my + j) instanceof BlockChest)
							{
								if(!((BlockChest)(getBlock(mx + i + 2, my + j))).isAttached() && getBlock(mx + i + 2, my + j).metaData == 1)
								{
									chest.attachRight();
									((BlockChest)(getBlock(mx + i + 2, my + j))).attachLeft();
								}
							}
						}
						setBlock(chest, mx + i, my + j, EnumEventType.EVENT_BLOCK_PLACE);

					}
					else
					{
						setBlock(block.clone(), mx + i, my + j, EnumEventType.EVENT_BLOCK_PLACE);
						
					}
					getBlock(mx + i, my + j).metaData = metadata[i][j];
				}
			}
			//Make more generic later
			player.inventory.removeItemsFromInventory(this, player, new ItemStack(block, 1)); //take the item from the player's inventory
		}
		else
		{
			if ((getBlock(mx, my).getIsOveridable() == true || getBlock(mx, my) == Block.air) && 
				(getBlock(mx-1, my).getIsSolid() || getBlock(mx, my-1).getIsSolid() || getBlock(mx, my+1).getIsSolid() || getBlock(mx+1, my).getIsSolid())) //can the block be placed
			{
				player.inventory.removeItemsFromInventory(this, player, new ItemStack(block, 1)); //remove the items from inventory	
			
				
				if(block instanceof BlockLight)
				{
					setBlock(block, mx, my, EnumEventType.EVENT_BLOCK_PLACE_LIGHT); //place it
			//		applyLightSource(player, block, mx, my, ((BlockLight)(block)).lightRadius,  ((BlockLight)(block)).lightStrength);
				}
				else
				{
					setBlock(block, mx, my, EnumEventType.EVENT_BLOCK_PLACE); //place it
					//	setBlock(block, mx, my, EnumEventType.EVENT_BLOCK_PLACE); //place it
				}
				
				
			}
		}
		getBlockGenerate(mx-1,my).setBitMap(updateBlockBitMap(mx-1, my));
		getBlockGenerate(mx,my-1).setBitMap(updateBlockBitMap(mx, my-1));
		getBlockGenerate(mx,my).setBitMap(updateBlockBitMap(mx, my));
		getBlockGenerate(mx+1,my).setBitMap(updateBlockBitMap(mx+1, my));
		getBlockGenerate(mx,my+1).setBitMap(updateBlockBitMap(mx, my+1));
	
		
		
	}
	
	/**
	 * Gets the world object's name. worldName is a final field of world and is always the same.
	 * @return the world's name
	 */
	public final String getWorldName()
	{
		return worldName;
	}
	
	/**
	 * Cuts down a tree, if a log was broken
	 * @param mx x position in worldmap array, of the BlockWood
	 * @param my y position in the worldmap array, of the BlockWood
	 */
	public void breakTree(EntityLivingPlayer player, int mx, int my){
		
		do{
			if(my >= 1)
			{
				if (getBlock(mx, my-1).getBlockID() == Block.tree.getBlockID()){ //If there's a tree above, break it
					handleBlockBreakEvent(player, mx, my-1);
				}
			}
			if(mx >= 1)
			{
				if (getBlock(mx-1, my).getBlockID() == Block.treebranch.getBlockID() || getBlock(mx-1, my).getBlockID() == Block.treebase.getBlockID()){
					handleBlockBreakEvent(player, mx - 1, my); //If there is a left branch/base on the same level, break it
				}
			}
			if(mx + 1 < width)
			{
				if (getBlock(mx+1, my).getBlockID() == Block.treebranch.getBlockID() || getBlock(mx+1, my).getBlockID() == Block.treebase.getBlockID()){
					handleBlockBreakEvent(player, mx + 1, my); //Same for right branches/bases
				}
			}
			if(mx + 1 < width && mx >= 1 && my >= 1)
			{
				if (getBlock(mx, my - 1).getBlockID() == Block.treetopc2.getBlockID()){
					handleBlockBreakEvent(player, mx + 1, my - 1); //Break a canopy
					handleBlockBreakEvent(player, mx + 1, my - 2);
					handleBlockBreakEvent(player, mx, my - 1);
					handleBlockBreakEvent(player, mx, my - 2);
					handleBlockBreakEvent(player, mx - 1, my - 1);
					handleBlockBreakEvent(player, mx - 1, my - 2);
				}
			}
			my--; //Move the check upwards 1 block
		}while (my >= 1 && getBlock(mx, my-1).getBlockID() == Block.tree.getBlockID()  //Loop as long as part of the tree is above
			|| getBlock(mx, my-1).getBlockID() == Block.treetopc2.getBlockID());
	}

	/**
	 * Gets the average level of the terrain. After the world object is generated, this should be invoked to map out the top of the terrain.
	 * This allows for simpler application of a background to the world, sunlight, and weather... in theory.
	 */
	public void assessForAverageSky()
	{
		List<Integer> values = new ArrayList<Integer>(width);
		long average = 0;
		
		for(int i = 0; i < width; i++) //Loop though each column
		{
			for(int j = 0; j < height; j++) //and each row
			{
				if(getBlock(i, j) != Block.air) //when something not air is hit, assume it's just the ground
				{
					values.add(j);
					average += j;
					break;
				}				
			}
		}
		
		average /= width; //get Average height
		generatedHeightMap = new int[values.size()];
		averageSkyHeight = (int) average; //save value to the averageSkyHeight field 
		
		for(int i = 0; i < generatedHeightMap.length; i++) //save over the individual data as well
		{
			generatedHeightMap[i] = (Integer)(values.get(i));
		}
		
		System.out.println("Average World Height: " + average);
	}

	/**
	 * Updates the weather object(s) in the world or do nothing if one doesnt exist
	 */
	public void updateWeather()
	{
		if(weather == null) //No weather
		{
			return;
		}
		if(weather instanceof WeatherSnow) //Snow
		{ 
			WeatherSnow weatherSnow = (WeatherSnow)(weather);
			weatherSnow.update(this);
			weather = weatherSnow;
		}
	}
		
	/**
	 * Breaks a cactus, from bottom to top.
	 * @param mx the x position of the first block in worldMap
	 * @param my the y position of the first block in worldMap
	 */
	public void breakCactus(EntityLivingPlayer player, int mx, int my)
	{
		do
		{
			if (getBlock(mx, my-1) == Block.cactus) //If there's a cactus block above, break it
			{ 
				handleBlockBreakEvent(player, mx, my-1);
			}
			my--;
		}while(getBlock(mx, my-1) == Block.cactus);
	}
		
	/**
	 * Makes the weather field null, to stop weather
	 */
	public void disableWeather()
	{
		weatherFinished = true;
	}
	
	/**
	 * Provides access to handlBlockBreakEvent() because that method is private and has a bizzare name, that's hard to both find and
	 * remember
	 * @param x the x position of the block to break (in the 'world map')
	 * @param y the y position of the block to break (in the 'world map')
	 */
	public void breakBlock(EntityLivingPlayer player, int x, int y)
	{
		handleBlockBreakEvent(player, x, y);
		breakCactus(player, x, y);
		breakTree(player, x, y);
	}

	/**
	 * Gets the world's width. This field is a final Integer and will never change.
	 * @return the world's width
	 */
	public final int getWidth()
	{
		return width;
	}
	
	/**
	 * Gets the world's height. This field is a final Integer and will never change.
	 * @return the world's height 
	 */
	public final int getHeight()
	{
		return height;
	}

	/**
	 * Gets the block at the specified (x,y). Useful for easily getting a block at the specified location; Terrible for mass usage,
	 * such as in rendering.
	 * @param x the block's x location in the new world map
	 * @param y the block's y location in the new world map
	 * @return the block at the location specified (this should never be null)
	 */
	public Block getBlock(int x, int y)
	{
		try
		{
			return getChunks().get(""+(x / Chunk.getChunkWidth())).getBlock(x % Chunk.getChunkWidth(), (y));

		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
		
	/**
	 * Sets the block at the specified (x,y). Useful for easily setting a block at the specified location; Terrible for mass usage.
	 * This version of the method does not check if the chunk is actually loaded, therefore it may sometimes fail for bizarre or very, very
	 * far away requests. It will however, simply catch that Exception, should it occur.
	 * @param block the block that the specified (x,y) will be set to
	 * @param x the block's x location in the new world map
	 * @param y the block's y location in the new world map
	 */
	public void setBlock(Block block, int x, int y)
	{
		try
		{
			getChunks().get(""+x / Chunk.getChunkWidth()).setBlock(block, x % Chunk.getChunkWidth(), y);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
		
	/**
	 * Gets the block at the specified (x,y). Useful for easily getting a block at the specified location; Terrible for mass usage, such as in rendering.
	 * This version of the method accepts floats, and casts them to Integers.
	 * @param x the block's x location in the new world map
	 * @param y the block's y location in the new world map
	 * @return the block at the location specified (this should never be null)
	 */
	public Block getBlock(float x, float y)
	{
		return getChunks().get(""+(int)(x / Chunk.getChunkWidth())).getBlock((int)x % Chunk.getChunkWidth(), (int)y);
	}
	
	/**
	 * Method designed to grow (or at least attempt to grow) a tree
	 * @param space - How high/wide the space between trees and terrain must be
	 * @param x - x location on the world
	 * @param y - y location on the world
	 */
	public void growTree(int space, int x, int y){
		boolean isOpen = true;
		int height = (int)(Math.random() * 5 + 4); //Determine the height of the tree
		if (y-height-space <= 0 || x <= 2 || x >= getWidth() - 2){ //If the tree would go off the map
			height = 0; //don't place a tree
			space = 0; //Don't place a tree
		}
		//If there is room for the tree up and to the left/right	
		for (int j = y; j >= y - height - space; j--){
			for (int i = x - space; i <= x + space; i++){
				if (!getBlockGenerate(i, j).getIsOveridable()){
					isOpen = false;
					break;
				}
			}
			if (!isOpen) break;			
		}
		if (isOpen){
			setBlockGenerate(Block.dirt, x, y + 1);
			int count = 1;
			
			if ((getBlockGenerate(x-1, y+1).getBlockID() == Block.grass.getBlockID()|| getBlockGenerate(x-1, y+1).getBlockID() == Block.dirt.getBlockID())){
				setBlockGenerate(Block.treebase.setBitMap(0), x-1, y);
				setBlockGenerate(Block.dirt, x-1, y+1);
			}
			
			if ((getBlockGenerate(x+1, y+1).getBlockID() == Block.grass.getBlockID()|| getBlockGenerate(x+1, y+1).getBlockID() == Block.dirt.getBlockID())){
				setBlockGenerate(Block.treebase.setBitMap(3), x+1, y);
				setBlockGenerate(Block.dirt, x+1, y+1);
			}
			
			for (int k = y; k >= y - height; k--){ //Place the tree
				if (getBlockGenerate(x, k).getBlockID() == Block.air.getBlockID()){ //If the cell is empty
					if (k == y-height){ //If at the top of the tree
						setBlockGenerate(Block.treetopr2, x+1, k); //Place the tree top
						setBlockGenerate(Block.treetopr1, x+1, k-1);
						setBlockGenerate(Block.treetopc2, x, k);
						setBlockGenerate(Block.treetopc1, x, k-1);
						setBlockGenerate(Block.treetopl2, x-1, k);
						setBlockGenerate(Block.treetop, x-1, k-1);
					}
					else{
						setBlockGenerate(Block.tree.setBitMap(1), x, k); //Otherwise, place a tree trunk
					}
					if (count > 2 && k > y - height + 1){ //For each slice of tree, if it is more than the third log, determine if there should be a branch
						int branchl = (int)(Math.random()*60); //Decide if a block should be placed left
						int branchr = (int)(Math.random()*60); //Decide if a branch should be placed right
						
						if (branchl < 5){
							setBlockGenerate(Block.treebranch.setBitMap(branchl * 2), x-1, k);
						}
						if (branchr < 5){
							setBlockGenerate(Block.treebranch.setBitMap(branchr * 2 + 1), x+1, k);
						}															
					}
					count++; //increment the counter 
				}
				else{
					break;
				}
			}
		}
	}
	
	/**
	 * A method designed to convert all exposed dirt above the minimum height to grass
	 * Note: We'll probably want to make it work via light value, rather than via y-value.
	 * @param x - the x-value to start at
	 * @param w - the width of the area to convert
	 * @param minHeight - the lowest y value a dirt block can have and still be converted
	 * @param maxHeight - the highest y value to check
	 */
	public void placeGrass(int x, int w, int minHeight, int maxHeight){
		for(int j = maxHeight; j > minHeight; j--){ //go through the the y-axis of the world
			for(int k = 1; k < x + w; k++){ //x-axis	
				//Search above, left and right of dirt block for air
				if (getBlockGenerate(k, j).getBlockID() == Block.dirt.getBlockID()){
					if (k > 0 && k < getWidth() && j > 0){
						if (getBlockGenerate(k - 1, j).getBlockID() == Block.air.getBlockID()){
							setBlockGenerate(Block.grass, k, j);
						}
					}
					if (k < getWidth()){
						if (getBlockGenerate(k + 1, j).getBlockID() == Block.air.getBlockID()){
							setBlockGenerate(Block.grass, k, j);
						}
					}
					if (j > 0){
						if (getBlockGenerate(k, j-1).getBlockID() == Block.air.getBlockID()){
							setBlockGenerate(Block.grass, k, j);
						}
					}
				}
			}
		}
	}
		
	/**
	 * Sets the block at the specified (x,y). Useful for easily setting a block at the specified location; Terrible for mass usage.
	 * This version of the method does not check if the chunk is actually loaded, therefore it may sometimes fail for bizarre or very, very
	 * far away requests. It will however, simply catch that Exception, should it occur. This version of the method uses floats.
	 * @param block the block that the specified (x,y) will be set to
	 * @param x the block's x location in the new world map
	 * @param y the block's y location in the new world map
	 */
	public void setBlock(Block block, float x, float y)
	{
		try
		{
			getChunks().get(""+x).setBlock(block, (int)x, (int)y);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the chunk based off the block position, so division must occur.
	 * @param x the x position of the chunk (in Blocks)
	 * @param y the y position of the chunk (in Blocks)
	 * @return the chunk at the specified position in the world's chunk map, or null if it doesn't exist or isn't loaded
	 */
	public Chunk getChunk_Division(int x)
	{
		return getChunks().get(""+(int)(x / Chunk.getChunkWidth()));
	}
	
	/**
	 * Gets the chunk based off the chunk-map coordinates. Division is not performed.
	 * @param x the x position of the chunk 
	 * @param y the y position of the chunk 
	 * @return the chunk at the specified position in the world's chunk map, or null if it doesn't exist or isn't loaded
	 */
	public Chunk getChunk(int x)
	{
		return (getChunks().get(""+x) != null) ? getChunks().get(""+x) : new Chunk(Biome.forest, x, height);
	}
	
	/**
	 * Add a new Chunk to the World's Chunk map. Usage of this method is advisable so that the game actually knows the chunk 
	 * exists in memory.
	 * @param chunk the chunk to add to the chunk map of the world
	 * @param x the x position of the chunk
	 * @param y the y position of the chunk
	 */
	public void registerChunk(Chunk chunk, int x)
	{
		chunksLoaded.put(""+x, true);
		chunks.put(""+x, chunk);
	}
	
	/**
	 * Gets the block at the specified (x,y). This method is safe, as all Exceptions are handled in this method. Additionally, 
	 * the (modular) division is performed automatically. The primary intention is that this method is used to generate the 
	 * world, so it MUST be safe, otherwise that code will become dangerous. 
	 * @param x the block's x location in the new world map
	 * @param y the block's y location in the new world map
	 * @return the block at the location specified, or null if there isnt one.
	 */
	public Block getBlockGenerate(int x, int y)
	{
		try
		{
			return getChunks().get(""+x / Chunk.getChunkWidth()).getBlock(x % Chunk.getChunkWidth(), y);
		}
		catch (Exception e)
		{
		}
		return Block.air;
	}
		
	/**
	 * Sets the block at the specified (x,y). This method is safe, as all Exceptions are handled in this method. Additionally,
	 * the (modular) division is performed automatically. The primary intention is that this method is used to generate the world, 
	 * so it MUST be safe, otherwise code will become very dangerous. Chunks are generated if there is non chunk present at that
	 * position.
	 * @param block the block that the specified (x,y) will be set to
	 * @param x the block's x location in the new world map
	 * @param y the block's y location in the new world map
	 */
	public void setBlockGenerate(Block block, int x, int y)
	{
		try
		{ //Ensure the chunk exists
			if(getChunks().get(""+(x / Chunk.getChunkWidth())) == null)
			{
				registerChunk(new Chunk(Biome.forest, (int)(x / Chunk.getChunkWidth()), height), (int)(x / Chunk.getChunkWidth()));
			}
		}
		catch(Exception e) 
		{
			e.printStackTrace();
		}
		
		try 
		{ //Set the block
			chunks.get(""+(x / Chunk.getChunkWidth())).setBlock(block, x % Chunk.getChunkWidth(), y);
		}
		catch(Exception e) 
		{
			e.printStackTrace();
		}
	}
		
	/**
	 * Gets the lighting value of the indicated Block. This may fail if the chunk requested isn't loaded into memory or doesnt exist.
	 * In this case, a lighting value of 1.0f will be returned. All Exceptions are handled in this method.
	 * @param x the x position of the Block to check for light in the world map
	 * @param y the y position of the Block to check for light in the world map
	 * @return the light value of that square, or 1.0f if that square is null or doesnt exist.
	 */
	public float getLight(int x, int y)
	{
		try
		{
			return getChunks().get(""+(int)(x / Chunk.getChunkWidth())).getLight((int)x % Chunk.getChunkWidth(), (int)y);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return 1.0f;
	}
	
	/**
	 * Checks for chunks that need to be loaded or unloaded, based on the player's screen size. The range in which chunks stay loaded increases if the player's 
	 * screen size is larger. (It's about ((width/2.2), (height/2.2))). 
	 */
	private void updateChunks(EntityLivingPlayer player)
	{
		//How far to check for chunks (in blocks)
		final int loadDistanceHorizontally = (((int)(Display.getWidth() / 2.2) + 3) > Chunk.getChunkWidth()) ? ((int)(Display.getWidth() / 2.2) + 3) : Chunk.getChunkWidth();
		//Position to check from
		final int x = (int) (player.x / 6);
		//Where to check, in the chunk map (based off loadDistance variables)
		int leftOff = (x - loadDistanceHorizontally) / Chunk.getChunkWidth();
		int rightOff = (x + loadDistanceHorizontally) / Chunk.getChunkWidth();
		//Bounds checking
		if(leftOff < 0) leftOff = 0;
		if(rightOff > (width / Chunk.getChunkWidth())) rightOff = width / Chunk.getChunkWidth();
		
		Enumeration<String> keys = chunksLoaded.keys();
        while (keys.hasMoreElements()) 
        {
            Object key = keys.nextElement();
            String strKey = (String) key;
            boolean loaded = chunksLoaded.get(strKey);
           
            int cx = Integer.parseInt(strKey);
            
            if(loaded && (cx < leftOff || cx > rightOff) && x != leftOff && x != rightOff)
			{
				//If a chunk isnt needed, request a save.
				chunkManager.saveChunk("Earth", chunks, cx);
				chunksLoaded.put(""+cx, false);
			}
            
		}
		for(int i = leftOff; i <= rightOff; i++) //Check for chunks that need loaded
		{
			if(chunksLoaded.get(""+i) != null && !chunksLoaded.get(""+i)) //If a needed chunk isnt loaded, request it.
			{
				chunkManager.requestChunk("Earth", this, chunks, i);
			}
			
		}
	}
	
	/**
	 * Sets the total number of biomes the world has. Generally this is advisable only during world generation.
	 * @param total the value to set totalBiomes to
	 */
	public void setTotalBiomes(int total)
	{
		totalBiomes = total;
	}
	
	/**
	 * Gets how many biomes the world has. Merged biomes count as a single biome.
	 * @return the total biomes in the world
	 */
	public int getTotalBiomes()
	{
		return totalBiomes;
	}
	
	/**
	 * Saves all chunks loaded in chunks (ConcurrantHashMap) to disk.
	 * @param dir the sub-directory to save the chunks in (ex. "Earth" for the overworld)
	 */
	private void saveAllRemainingChunks(String dir)
	{
		Enumeration<String> keys = getChunks().keys();
        while (keys.hasMoreElements()) 
        {
            Chunk chunk = getChunks().get((String)(keys.nextElement()));
            chunkManager.saveChunk(dir, getChunks(), chunk.getX());		
        }
	}
	
	/**
	 * Saves all the chunks loaded and the important variables in world to disk. The important variables are saved in the world's
	 * main directory under the name "worlddata.dat" and the chunks are saved in the "Earth" directory (or the applicable dimension)
	 * @param dir the sub-directory to save the world data in (Ex. "Earth" for the over-world)
	 */
	public void saveRemainingWorld(String dir)
	{
		saveAllRemainingChunks(dir);
		chunkManager.saveWorldData(this, dir);
	}
	
	/**
	 * Gets the average sky height. This is actually the average Block at which the ground begins. This is measured from the 
	 * top of the screen and should vary based on the world's height.
	 * @return the average block at which the ground begins
	 */
	public int getAverageSkyHeight()
	{
		return averageSkyHeight;
	}
	
	/**
	 * A detailed Integer array of where the ground begins, based on how the world was generated. This is measured from the top of
	 * the screen (sky). The average of these values should equal the value of {@link #getAverageSkyHeight()}.
	 * @return a detailed Integer array of where the ground begins
	 */
	public int[] getGeneratedHeightMap()
	{
		return generatedHeightMap;
	}
	
	public long getWorldTime()
	{
		return worldTime;
	}
	
	public EnumDifficulty getDifficulty()
	{
		return difficulty;
	}
	
	public ConcurrentHashMap<String, Chunk> getChunks() 
	{
		return chunks;
	}

	public void setChunks(ConcurrentHashMap<String, Chunk> chunks) 
	{
		this.chunks = chunks;
	}
	
	public void setChunk(Chunk chunk, int x, int y)
	{
		getChunks().put(""+x, chunk);
	}
	
	private void checkChunks()
	{
		for(int i = 0; i < width / Chunk.getChunkWidth(); i++)
		{
			if(getChunks().get(""+i) == null)
			{
				registerChunk(new Chunk(Biome.forest, i, height), i);
			}
			
		}
	}
	
	/**
	 * Gets the biome for the specified chunk value, NOT block value
	 * @param pos a string in the form of "x", indicating which chunk to check for a biome
	 * @return the chunk's biome if it's loaded, otherwise null if it isnt
	 */
	public Biome getBiome(String pos)
	{
		int x = (Integer.parseInt(pos)) / Chunk.getChunkWidth();
	
		Chunk chunk = getChunks().get(""+x);
		if(chunk != null)
		{
			return chunk.getBiome();
		}
			
		return null;
	}
	
	public String getBiomeColumn(String pos)
	{
		Biome biome = getBiome(pos);
		
		if(biome != null)
		{
			return biome.getBiomeName();
		}
		
		return null;
	}
		
	/**
	 * Gets the background block at the specified (x,y). Useful for easily getting a backgroundblock at the specified location; 
	 * Terrible for mass usage, such as in rendering. This method accepts floats, and casts them to Integers.
	 * @param x the block's x location in the new world map
	 * @param y the block's y location in the new world map
	 * @return the block at the location specified (this should never be null)
	 */
	public Block getBackBlock(float x, float y)
	{
		return getChunks().get(""+(int)(x / Chunk.getChunkWidth())).getBlock((int)x % Chunk.getChunkWidth(), (int)y);
	}
	
	public void setAmbientLight(float x, float y, float strength)
	{
		try
		{
			getChunks().get(""+(int)(x / Chunk.getChunkWidth())).setAmbientLight(strength, (int)x % Chunk.getChunkWidth(), (int)y);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
		
	public void setDiffuseLight(float x, float y, float strength)
	{
		try
		{
			getChunks().get(""+(int)(x / Chunk.getChunkWidth())).setDiffuseLight(strength, (int)x % Chunk.getChunkWidth(), (int)y);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public float getAmbientLight(int x, int y)
	{
		try
		{
			return getChunks().get(""+(int)(x / Chunk.getChunkWidth())).getAmbientLight((int)x % Chunk.getChunkWidth(), (int)y);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return 1.0f;
	}
	
	public float getDiffuseLight(int x, int y)
	{
		try
		{
			return getChunks().get(""+(int)(x / Chunk.getChunkWidth())).getDiffuseLight((int)x % Chunk.getChunkWidth(), (int)y);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return 1.0f;
	}
	
	/**
	 * Updates the ambient lighting based on the world time and light level (from getLightLevel())
	 */
	public void updateAmbientLighting()
	{
		Enumeration<String> keys = chunks.keys();
		//Update the lighting in all the chunks (this is now efficient enough to work)
		while (keys.hasMoreElements()) 
        {
            Chunk chunk = chunks.get((String)keys.nextElement());
        	utils.applyAmbientChunk(this, chunk);
    	}
	}
	
	/**
	 * Sets the block and applies relevant lighting to the area nearby. Includes an EnumEventType to describe the event, although
	 * it is currently not used for anything.
	 * @param block the new block to be placed at (x,y)
	 * @param x the x position where the block will be placed
	 * @param y the y position where the block will be placed
	 * @param eventType
	 */
	public void setBlock(Block block, int x, int y, EnumEventType eventType)
	{
		utils.fixDiffuseLightRemove(this, x, y);
		setBlock(block, x, y);
		utils.blockUpdateAmbient(this, x, y, eventType);		
		utils.fixDiffuseLightApply(this, x, y);
	}
	
	/**
	 * Called on world tick. Updates the lighting if it's appropriate to do so. It is considered appropriate if the light level of
	 * the world has changed, or if the chunk (for whatever reason) has been flagged for a lighting update by a source.
	 * @param player
	 */
	public void applyLightingUpdates(EntityLivingPlayer player)
	{
		//If the light level has changed, update the ambient lighting.
		if(lightingUpdateRequired)
		{
			updateAmbientLighting();
			lightingUpdateRequired = false;
		}
		
		Enumeration<String> keys = chunks.keys();
        while (keys.hasMoreElements()) 
        {
            Object key = keys.nextElement();
            String str = (String) key;
            Chunk chunk = chunks.get(str);
                        
            //If the chunk has been flagged for an ambient lighting update, update the lighting
            if(chunk.isFlaggedForLightingUpdate())
            {
            	utils.applyAmbientChunk(this, chunk);
            	chunk.setFlaggedForLightingUpdate(false);
            }
                    
            //If the light in the chunk has changed, update the light[][] used for rendering
            if(!chunk.isLightUpdated())
            {
            	chunk.updateChunkLight();
            	chunk.setLightUpdated(true);
            }
        }
	}
	
}