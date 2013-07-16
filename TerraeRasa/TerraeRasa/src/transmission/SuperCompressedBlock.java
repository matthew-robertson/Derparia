package transmission;

import java.io.Serializable;

import blocks.Block;
import blocks.BlockChest;

import utils.ItemStack;


public class SuperCompressedBlock 
		implements Serializable
{
	private static final long serialVersionUID = 1L;
	public ItemStack[] mainInventory;
	public short id;
	public byte metaData;
	public byte bitMap;
	
	public SuperCompressedBlock()
	{
		
	}	
	
	public SuperCompressedBlock(Block block)
	{
		this.id = (short) block.id;
		this.metaData = (byte) block.metaData;
		this.bitMap = (byte) block.getBitMap();
		mainInventory = (block instanceof BlockChest) ? ((BlockChest)(block)).getMainInventory() : new ItemStack[0];
	}
}
