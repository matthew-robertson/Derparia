package blocks;

import java.util.Vector;

import math.MathHelper;
import utils.Position;
import world.Biome;
import world.Weather;

/**
 * <br>
 * <code>Chunk</code> implements something that is similar to a C(++) struct. A Chunk stores data relating to
 * Backwalls, Blocks, Biomes, and lighting data(including ambient, diffuse, and total). Upon initialization, 
 * all Blocks and Backwalls are set to be air, a Biome must be assigned, and lighting values are set to 0. 
 * Additionally, each chunk stores its own position in the chunk array. 
 * <br><br>
 * Each method in <code>Chunk</code> is either synchronized or final to make <code>Chunk</code> 
 * relatively Thread-Safe overall. All setters (for example: <code>{@link #setBlock(Block, int, int)}, 
 * {@link #setChanged(boolean)}, {@link #setLight(double, int, int)}</code>) are synchronized, all getters are 
 * final. All fields in <code>Chunk</code> are final.
 * 
 * <br><br>
 * <b>Chunk sizes are subject to change. NEVER use magic numbers and always use 
 * {@link #getChunkWidth()} or {@link #getChunkHeight()} when performing chunk
 * size operations.</b>
 * 
 * @author      Alec Sobeck
 * @author      Matthew Robertson
 * @version     1.1
 * @since       1.0
 */
public class ChunkClient extends Chunk
{
	private final Object lightLock = new Object();
	private final Object backWallLock = new Object();
	private final Object frontBlockLock = new Object();
	/** Light is the total of ambient and diffuse light. This value is inverted (0.0F becomes 1.0F, etc) to optimize rendering */
	public float[][] light;
	/** Diffuse light is light from light sources*/
	public float[][] diffuseLight;
	/** Ambient light is light from the sun */
	public float[][] ambientLight;
	public ClientMinimalBlock[][] backWalls;
	public ClientMinimalBlock[][] blocks;
	private volatile boolean wasChanged;
	private volatile boolean lightUpdated;
	private volatile boolean requiresAmbientLightingUpdate;
	private volatile boolean requiresDiffuseApplied;
	public Weather weather;
	
	/**
	 * Constructs a new Chunk. Chunks are initialized with blocks[][] fully set to air, and backwalls[][]
	 * fully set to air as well. All light values are 0.0f (no light).
	 * @param biome the biome type of the chunk
	 * @param x the x position of the chunk in the chunk grid
	 * @param y the y position of the chunk in the chunk grid
	 * @param height the height of the chunk - which should be the world's height
	 */
	public ChunkClient(Biome biome, int x, final int height)
	{
		this.height = height;
		this.biome = new Biome(biome);
		blocks = new ClientMinimalBlock[CHUNK_WIDTH][height];
		backWalls = new ClientMinimalBlock[CHUNK_WIDTH][height];
		for(int i = 0; i < CHUNK_WIDTH; i++)
		{
			for(int j = 0; j < height; j++)
			{
				blocks[i][j] = new ClientMinimalBlock(true);
				backWalls[i][j] = new ClientMinimalBlock(false);
			}
		}
		setRequiresAmbientLightingUpdate(false);
		light = new float[CHUNK_WIDTH][height];
		diffuseLight = new float[CHUNK_WIDTH][height];
		ambientLight = new float[CHUNK_WIDTH][height];
		this.x = x;
		this.lightSources = new Vector<Position>();
		this.setRequiresDiffuseApplied(true);
	}
	
	/**
	 * Gets the light[][] stored in this instanceof Chunk
	 * @return the light array for this Chunk
	 */
	public final float[][] getLight()
	{
		return light;
	}
	
	/**
	 * Gets the block at position (x,y) of the chunk, NOT the world 
	 * @param x the x position of the block requested
	 * @param y the y position of the block requested
	 * @return the block at the specified position, which should never be null
	 */
	public final ClientMinimalBlock getBlock(int x, int y)
	{
		return blocks[x][y];
	}
	
	/**
	 * Gets the backwall at position (x,y) of the chunk, NOT the world 
	 * @param x the x position of the block requested
	 * @param y the y position of the block requested
	 * @return the block at the specified position, which should never be null
	 */
	public final ClientMinimalBlock getBackWall(int x, int y)
	{
		return backWalls[x][y];
	}
	
	/**
	 * Gets the currently calculated light value for the block at position (x,y) in the chunk. A value of
	 * 1.0F is full darkness, 0.0F is full light (this reversal is an optimization for rendering)
	 * @param x a value from 0 to ChunkWidth, to retrieve from the light[][]
	 * @param y a value from 0 to ChunkHeight, to retrieve from the light[][]
	 * @return the currently calculated light value of light[x][y]
	 */
	public final double getLight(int x, int y)
	{
		return light[x][y];
	}
	
	/**
	 * Gets whether or not this Chunk has been flagged as having been changed, for some reason. This is generally not very descriptive
	 * and may not even happen at all. 
	 * @return whether or not this Chunk has been changed
	 */
	public final boolean getChanged()
	{
		return wasChanged;
	}
	
	/**
	 * Replaces the current block at backWalls[x][y] with the given Block parameter.
	 * @param block the new Block for position (x,y)
	 * @param x a value from 0 to ChunkWidth	
	 * @param y a value from 0 to ChunkHeight
	 */
	public void setBackWall(ClientMinimalBlock block, int x, int y)
	{
		synchronized(backWallLock)
		{
			backWalls[x][y] = block;
		}
	}
	
	/**
	 * Replaces the current block at blocks[x][y] with the given Block parameter.
	 * @param block the new Block for position (x,y)
	 * @param x a value from 0 to ChunkWidth	
	 * @param y a value from 0 to ChunkHeight
	 */
	public void setBlock(ClientMinimalBlock block, int x, int y)
	{
//		if(Block.blocksList[blocks[x][y].id].lightStrength > 0)
//		{
//			removeLightSource(x, y);
//		}
//		if(block.lightStrength > 0)
//		{
//			addLightSource(x, y);
//		}
		synchronized(frontBlockLock)
		{
			blocks[x][y] = block;
		}
	}
	
	/**
	 * Sets the diffuseLight[x][y] value to the given strength. 
	 * @param strength a value from 0.0F to 1.0F, indicating the % of diffuse light strength
	 * @param x a value from 0 to ChunkWidth	
	 * @param y a value from 0 to ChunkHeight
	 */
	public void setDiffuseLight(double strength, int x, int y)
	{
		synchronized(lightLock)
		{
			diffuseLight[x][y] = (float) strength;
		}
	}
	
	/**
	 * Sets the ambientLight[x][y] value to the given strength. 
	 * @param strength a value from 0.0F to 1.0F, indicating the % of ambient light strength
	 * @param x a value from 0 to ChunkWidth	 
	 * @param y a value from 0 to ChunkHeight
	 */
	public void setAmbientLight(double strength, int x, int y)
	{
		synchronized(lightLock)
		{
			ambientLight[x][y] = (float) strength;
		}
	}
	
	/**
	 * Sets the wasChanged variable of this chunk to the given boolean
	 * @param flag the new value for this Chunk's wasChanged field
	 */
	public void setChanged(boolean flag)
	{
		wasChanged = flag;
	}
	
	/**
	 * Gets the diffuse (artificial) light value from diffuseLight[x][y]. A value of 1.0F is full light, 
	 * 0.0F is no light (This value may exceed 1.0F, in which case it is simply full light).
	 * @param x a value from 0 to ChunkWidth
	 * @param y a value from 0 to ChunkHeight
	 * @return the diffuse light value from diffuseLight[x][y]
	 */
	public final double getDiffuseLight(int x, int y)
	{
		synchronized(lightLock)
		{
			return diffuseLight[x][y];
		}
	}

	/**
	 * Gets the ambient(sun) light value from ambientLight[x][y]. A value of 1.0F is full light, 
	 * 0.0F is no light (This value may not 1.0F, unlike diffuse light values).
	 * @param x a value from 0 to ChunkWidth
	 * @param y a value from 0 to ChunkHeight
	 * @return the diffuse light value from ambientLight[x][y]
	 */
	public final double getAmbientLight(int x, int y)
	{
		synchronized(lightLock)
		{
			return ambientLight[x][y];
		}
	}
	
	/**
	 * Updates the light[][] for rendering. A value of 0.0F is full light, 1.0F is full darkness. This is inverted 
	 * for optimization reasons.
	 */
	public void updateChunkLight()
	{
		synchronized(lightLock)
		{
			for(int i = 0; i < CHUNK_WIDTH; i++)
			{
				for(int k = 0; k < height; k++)
				{
					light[i][k] = (float) MathHelper.sat(1.0F - ambientLight[i][k] - diffuseLight[i][k]);
				}
			}
		}
	}
	
	/**
	 * Sets all the ambientlight[][] to 0.0F (no light)
	 */
	public void clearAmbientLight()
	{
		synchronized(lightLock)
		{
			for(int i = 0; i < CHUNK_WIDTH; i++)
			{
				for(int k = 0; k < height; k++)
				{
					ambientLight[i][k] = 0.0F;
				}
			}
		}
	}

	/**
	 * Gets the Chunk's flaggedForLightUpdate field
	 * @return this Chunk's flaggedForLightUpdate field
	 */
	public final boolean requiresAmbientLightingUpdate() 
	{
		return requiresAmbientLightingUpdate;
	}

	/**
	 * Sets this Chunk's flaggedForLightingUpdate field to the given boolean
	 * @param flaggedForLightingUpdate the new value for this Chunk's flaggedForLightingUpdate field
	 */
	public void setRequiresAmbientLightingUpdate(boolean requiresAmbientLightingUpdate) 
	{
		this.requiresAmbientLightingUpdate = requiresAmbientLightingUpdate;
	}

	/**
	 * Gets whether or not the light was updated
	 * @return a boolean describing whether or not the light has been updated
	 */
	public final boolean isLightUpdated() 
	{
		return lightUpdated;
	}

	/**
	 * Sets this Chunk's lightUpdated field to the given boolean
	 * @param lightUpdated the new value for this Chunk's lightUpdated field
	 */
	public void setLightUpdated(boolean lightUpdated) 
	{
		this.lightUpdated = lightUpdated;
	}	
	
//	/**
//	 * Registers a light source at the given position.
//	 * @param x the x position, in blocks, relative to the start of this chunk (IE a value 0 <= x < Chunk_width)
//	 * @param y the y position, in blocks
//	 */
//	private void addLightSource(int x, int y)
//	{
//		getLightSources().add(new Position((this.x * CHUNK_WIDTH) + x, y));
//	}
//	
//	/**
//	 * Removes a light source at the given position. This will make the game mad if it fails to locate the given light source.
//	 * @param x the x position, in blocks, relative to the start of this chunk (IE a value 0 <= x < Chunk_width)
//	 * @param y the y position, in blocks
//	 */
//	private void removeLightSource(int x, int y)
//	{
//		int adjustedX = this.x * CHUNK_WIDTH + x;
//		//(adjustedX,y)
//		Iterator<Position> it = getLightSources().iterator();
//		while(it.hasNext())
//		{
//			Position position = it.next();
//			if(position.equals(adjustedX, y))
//			{
//				it.remove();
//				return;
//			}
//		}		
//		throw new RuntimeException("Illegal light source removal at (" + (adjustedX) + "," + y + ")");
//	}
	
	public void addLightSources(Position[] lightSources)
	{
		if(lightSources == null)
		{
			return;
		}
		for(Position position : lightSources)
		{
			this.lightSources.add(position);
		}
	}

	public Vector<Position> getLightSources() 
	{
		return lightSources;
	}

	public boolean requiresDiffuseApplied() 
	{
		return requiresDiffuseApplied;
	}

	public void setRequiresDiffuseApplied(boolean requiresDiffuseApplied) 
	{
		this.requiresDiffuseApplied = requiresDiffuseApplied;
	}
}