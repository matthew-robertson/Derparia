package transmission;

import io.Chunk;

import java.io.Serializable;

import utils.DisplayableItemStack;
import utils.ItemStack;
import world.Biome;
import blocks.Block;
import blocks.MinimalBlock;

public class ChunkCompressor 
		implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static SuperCompressedChunk compressChunk(Chunk chunk)
	{
//		long time = System.currentTimeMillis();
		SuperCompressedChunk compressedChunk = new SuperCompressedChunk();
		compressedChunk.biome = new Biome(chunk.getBiome());
		compressedChunk.backWalls = compress(chunk.backWalls, false);
		compressedChunk.blocks = compress(chunk.blocks, true);
		compressedChunk.x = chunk.getX();
		compressedChunk.wasChanged = chunk.getChanged();
		compressedChunk.height = chunk.getHeight();
		compressedChunk.lightSources = chunk.getLightSourcesAsArray();
		compressedChunk.weather = chunk.weather;
//		System.out.println("Compression time = " + (System.currentTimeMillis() - time));
		return compressedChunk;
	}

	private static SuperCompressedBlock[][] compress(MinimalBlock[][] blocks, boolean front)
	{
		SuperCompressedBlock[][] compressed = new SuperCompressedBlock[blocks.length][blocks[0].length];
		if(front)
		{
			for(int i = 0; i < compressed.length; i++)
			{
				for(int k = 0; k < compressed[0].length; k++)
				{
					if(blocks[i][k].id == Block.air.id)
					{
						compressed[i][k] = null;
					}
					else
					{
						SuperCompressedBlock cblock = new SuperCompressedBlock();
						cblock.bitMap = blocks[i][k].bitMap;
						cblock.id = blocks[i][k].id;
						cblock.metaData = blocks[i][k].metaData;
						cblock.mainInventory = blocks[i][k].mainInventory.length == 0 ? null : convert(blocks[i][k].mainInventory);
						compressed[i][k] = cblock;			
					}
				}
			}
		}
		else
		{
			for(int i = 0; i < compressed.length; i++)
			{
				for(int k = 0; k < compressed[0].length; k++)
				{
					if(blocks[i][k].id == Block.backAir.id)
					{
						compressed[i][k] = null;
					}
					else
					{
						SuperCompressedBlock cblock = new SuperCompressedBlock();
						cblock.bitMap = blocks[i][k].bitMap;
						cblock.id = blocks[i][k].id;
						cblock.metaData = blocks[i][k].metaData;
						cblock.mainInventory = blocks[i][k].mainInventory.length == 0 ? null : convert(blocks[i][k].mainInventory);
						compressed[i][k] = cblock;			
					}
				}
			}
		}
		return compressed;		
	}
	
	public static DisplayableItemStack[] convert(ItemStack[] stacks)
	{
		DisplayableItemStack[] displayables = new DisplayableItemStack[stacks.length];
		for(int i = 0; i < stacks.length; i++)
		{
			if(stacks[i] != null)
			{
				displayables[i] = new DisplayableItemStack(stacks[i]);
			}
		}
		return displayables;
	}
	
}
