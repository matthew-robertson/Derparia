package utils;

import items.Item;
import items.ItemAmmo;
import items.ItemArmorAccessory;
import items.ItemArmorBelt;
import items.ItemArmorBody;
import items.ItemArmorBoots;
import items.ItemArmorGloves;
import items.ItemArmorHelmet;
import items.ItemArmorPants;
import transmission.EntityUpdate;
import transmission.ServerUpdate;
import transmission.UpdateWithObject;
import world.World;
import entities.DisplayableEntity;
import entities.EntityItemStack;
import entities.EntityPlayer;

public class MouseItemHelper {

	public static void craftRecipe(World world, EntityPlayer player, int recipeID)
	{
		Recipe whatToCraft = player.getRecipeByID(recipeID);
		
		if(player.getHeldMouseItem() != null && player.getHeldMouseItem().getItemID() != whatToCraft.getResult().getItemID()) //check if it's possible to craft successfully
		{
			return;
		}
	
		if(player.getHeldMouseItem() != null && player.getHeldMouseItem().getItemID() == whatToCraft.getResult().getItemID()) 
		{ //does the mouseItem have the same item being crafted already picked up?
			if(whatToCraft.getResult().getStackSize() + player.getHeldMouseItem().getStackSize() < player.getHeldMouseItem().getMaxStackSize()) //if there's room to pick it up
			{
				for(int i = 0; i < whatToCraft.getRecipe().length; i++) //remove the items from the inventory
				{
					player.inventory.removeItemsFromInventory(player, whatToCraft.getRecipe()[i]);
				}
				player.getHeldMouseItem().addToStack(whatToCraft.getResult().getStackSize()); //pick up the item
				player.flagHeldItem();
			}
		}
		else //the mouseitem is null, so pick up a new itemstack
		{
			for(int i = 0; i < whatToCraft.getRecipe().length; i++) //remove items from inventory
			{
				player.inventory.removeItemsFromInventory(player, whatToCraft.getRecipe()[i]);
			}
			
			player.setHeldMouseItem(new ItemStack(whatToCraft.getResult())); //THIS IS VERY IMPORTANT
			player.flagHeldItem();
		}
	}

	public static void throwMouseItem(ServerUpdate update, World world, EntityPlayer player, String quantity)
	{
		if(player.getHeldMouseItem() != null)
		{
			EntityItemStack entityStack = new EntityItemStack(player.x - 1, player.y - 1, player.getHeldMouseItem());
			world.addItemStackToItemList(entityStack);
			EntityUpdate entityUpdate = new EntityUpdate();
			entityUpdate.action = 'a';
			entityUpdate.entityID = entityStack.entityID;
			entityUpdate.type = 3; 
			entityUpdate.updatedEntity = new DisplayableEntity(entityStack);
			update.addEntityUpdate(entityUpdate);
			player.setHeldMouseItem(null);
			player.flagHeldItem();
		}
	}
	
	//Qty of a number, or "all"
	public static void removeMouseItem(World world, EntityPlayer player, String quantity)
	{
		if(quantity.equals("all"))
		{
			player.setHeldMouseItem(null);
		}
		else
		{
			player.getHeldMouseItem().removeFromStack(Integer.parseInt(quantity));
			if(player.getHeldMouseItem().getStackSize() <= 0)
			{
				player.setHeldMouseItem(null);
			}
		}		
	}

	//IE take from inventory to mouse
	public static void pickupItemInInventory(World world, EntityPlayer player, int inventoryID, int index, String quantity)
	{
		if(inventoryID == 1) //Main Inventory
		{
			ItemStack mouseItem = new ItemStack(player.inventory.getMainInventoryStack(index));
			
			int number = 0;
			if(quantity.equals("1"))
			{
				number = (int)(MathHelper.floorOne(Integer.parseInt(quantity)));
			}
			else if(quantity.equals("1/2"))
			{
				number = (int)(MathHelper.floorOne(mouseItem.getStackSize() / 2));
			}
			else if(quantity.equals("all"))
			{
				number = (int)(MathHelper.floorOne(mouseItem.getStackSize()));
			}
			
			mouseItem.setStackSize(number);
			player.inventory.removeItemsFromInventoryStack(player, mouseItem.getStackSize(), index); 
		
			//Put to mouse item
			player.setHeldMouseItem(mouseItem);
		}
		else if(inventoryID == 2) //Armor Inventory
		{
			ItemStack mouseItem = player.inventory.getArmorInventoryStack(index);
			player.inventory.setArmorInventoryStack(player, null, player.inventory.getArmorInventoryStack(index), index);
			player.setHeldMouseItem(mouseItem);
		
		}
		else if(inventoryID == 3) //Quiver
		{
			ItemStack mouseItem = new ItemStack(player.inventory.getQuiverStack(index));

			int number = 0;
			if(quantity.equals("1"))
			{
				number = (int)(MathHelper.floorOne(Integer.parseInt(quantity)));
			}
			else if(quantity.equals("1/2"))
			{
				number = (int)(MathHelper.floorOne(mouseItem.getStackSize() / 2));
			}
			else if(quantity.equals("all"))
			{
				number = (int)(MathHelper.floorOne(mouseItem.getStackSize()));
			}

			mouseItem.setStackSize(number);
			player.inventory.removeItemsFromQuiverStack(player, mouseItem.getStackSize(), index);
			
			player.setHeldMouseItem(mouseItem);
		
		}
		else if(inventoryID == 4) //Trash
		{
			ItemStack mouseItem = new ItemStack(player.inventory.getTrashStack(index));
			
			int number = 0;
			if(quantity.equals("1"))
			{
				number = (int)(MathHelper.floorOne(Integer.parseInt(quantity)));
			}
			else if(quantity.equals("1/2"))
			{
				number = (int)(MathHelper.floorOne(mouseItem.getStackSize() / 2));
			}
			else if(quantity.equals("all"))
			{
				number = (int)(MathHelper.floorOne(mouseItem.getStackSize()));
			}
			
			
			mouseItem.setStackSize(number);
			player.inventory.removeItemsFromTrashStack(player, mouseItem.getStackSize(), index);
			
			player.setHeldMouseItem(mouseItem);
		
		}		
	}
	
	//Place from mouse -> Inventory
	public static void placeItemInInventory(World world, EntityPlayer player, int inventoryID, int index, String quantity)
	{
		if(player.getHeldMouseItem() == null)
			return;
		
		try {
			if(inventoryID == 1) //Main Inventory
			{
				int number = 0;
				if(quantity.equals("1/2"))
					number = (int) MathHelper.floorOne(player.getHeldMouseItem().getStackSize() / 2);
				else if(quantity.equals("all"))
					number = (int) MathHelper.floorOne(player.getHeldMouseItem().getStackSize());
				else
					number = (int) MathHelper.floorOne(Integer.parseInt(quantity));
				
				if(player.inventory.getMainInventoryStack(index) == null) //There's nothing there, so the mouse doesnt have to pickup something
				{
					player.inventory.putItemStackInSlot(world, player, new ItemStack(player.getHeldMouseItem()).setStackSize(number), index);
					player.getHeldMouseItem().removeFromStack(number);
					if(player.getHeldMouseItem().getStackSize() <= 0)
					{
						player.setHeldMouseItem(null);
					}
				}
				else if(player.inventory.getMainInventoryStack(index).getItemID() == player.getHeldMouseItem().getItemID())
				{
					if(player.inventory.getMainInventoryStack(index).getStackSize() + number
							<= player.inventory.getMainInventoryStack(index).getMaxStackSize())
					{
						player.inventory.combineItemStacksInSlot(world, player, new ItemStack(player.getHeldMouseItem()).setStackSize(number), index);
						player.getHeldMouseItem().removeFromStack(number);
						if(player.getHeldMouseItem().getStackSize() <= 0)
						{
							player.setHeldMouseItem(null);
						}
					}
				}
				else //If there is an item there, swap that slot's item and the mouse's item.
				{
					ItemStack stack = new ItemStack(player.inventory.getMainInventoryStack(index));
					player.inventory.putItemStackInSlot(world, player, player.getHeldMouseItem(), index);
					player.setHeldMouseItem(stack);
				}
			}
			else if(inventoryID == 2) //Armor && Accessories
			{
				Item item = Item.itemsList[player.getHeldMouseItem().getItemID()];			
				//Check if the item is actually valid for the selected slot:
				if(index == InventoryPlayer.HELMET_INDEX) //Helmet
				{
					if(!(item != null) || !(item instanceof ItemArmorHelmet))
					{
						return;
					}	
				}
				else if(index == InventoryPlayer.BODY_INDEX) //Body
				{
					if(!(item != null) || !(item instanceof ItemArmorBody))
					{
						return;
					}	
				}
				else if(index == InventoryPlayer.BELT_INDEX) //Belt
				{
					if(!(item != null) || !(item instanceof ItemArmorBelt))
					{
						return;
					}	
				}			
				else if(index == InventoryPlayer.PANTS_INDEX) //Pants
				{
					if(!(item != null) || !(item instanceof ItemArmorPants))
					{
						return;
					}	
				}
				else if(index == InventoryPlayer.BOOTS_INDEX) //Boots
				{
					if(!(item != null) || !(item instanceof ItemArmorBoots))
					{
						return;
					}	
				}
				else if(index == InventoryPlayer.GLOVES_INDEX) //Gloves
				{
					if(!(item != null) || !(item instanceof ItemArmorGloves))
					{
						return;
					}	
				}
				else //Accessory
				{
					if(!(item != null) || !(item instanceof ItemArmorAccessory))
					{
						return;
					}	
				}
				
				if(player.inventory.getArmorInventoryStack(index) == null) //There's nothing there, so the mouse doesnt have to pickup something
				{
					player.inventory.setArmorInventoryStack(player, player.getHeldMouseItem(), player.inventory.getArmorInventoryStack(index), index);
					player.setHeldMouseItem(null);
				}
				else //If there is an item there, swap that slot's item and the mouse's item.
				{
					ItemStack stack = player.inventory.getArmorInventoryStack(index);
					player.inventory.setArmorInventoryStack(player, player.getHeldMouseItem(), player.inventory.getArmorInventoryStack(index), index);
					player.setHeldMouseItem(stack);
				}
			}
			else if(inventoryID == 3) //Quiver
			{
				Item item = Item.itemsList[player.getHeldMouseItem().getItemID()];
				if(!(item instanceof ItemAmmo))
				{
					return;
				}
				
				int number = 0;
				if(quantity.equals("1/2"))
					number = (int) MathHelper.floorOne(player.getHeldMouseItem().getStackSize() / 2);
				else if(quantity.equals("all"))
					number = (int) MathHelper.floorOne(player.getHeldMouseItem().getStackSize());
				else
					number = (int) MathHelper.floorOne(Integer.parseInt(quantity));
				
				if(player.inventory.getQuiverStack(index) == null) //There's nothing there, so the mouse doesnt have to pickup something
				{
					player.inventory.setQuiverStack(player, new ItemStack(player.getHeldMouseItem()).setStackSize(number), index);
					player.getHeldMouseItem().removeFromStack(number);
					if(player.getHeldMouseItem().getStackSize() <= 0)
					{
						player.setHeldMouseItem(null);
					}
				}
				else if(player.inventory.getQuiverStack(index).getItemID() == player.getHeldMouseItem().getItemID())
				{
					if(player.inventory.getQuiverStack(index).getStackSize() + number
							<= player.inventory.getQuiverStack(index).getMaxStackSize())
					{
						player.inventory.combineItemStacksInQuiverSlot(world, player, new ItemStack(player.getHeldMouseItem()).setStackSize(number), index);
						player.getHeldMouseItem().removeFromStack(number);
						if(player.getHeldMouseItem().getStackSize() <= 0)
						{
							player.setHeldMouseItem(null);
						}
					}
				}
				else //If there is an item there, swap that slot's item and the mouse's item.
				{
					ItemStack stack = new ItemStack(player.inventory.getQuiverStack(index));
					player.inventory.setQuiverStack(player, player.getHeldMouseItem(), index);
					player.setHeldMouseItem(stack);
				}
			}
			else if(inventoryID == 4) //Trash
			{			
				int number = 0;
				if(quantity.equals("1/2"))
					number = (int) MathHelper.floorOne(player.getHeldMouseItem().getStackSize() / 2);
				else if(quantity.equals("all"))
					number = (int) MathHelper.floorOne(player.getHeldMouseItem().getStackSize());
				else
					number = (int) MathHelper.floorOne(Integer.parseInt(quantity));
				
				player.inventory.setTrashStack(player, new ItemStack(player.getHeldMouseItem()).setStackSize(number), index);
				
				player.getHeldMouseItem().removeFromStack(number);
				player.flagHeldItem();
				if(player.getHeldMouseItem().getStackSize() <= 0)
				{
					player.setHeldMouseItem(null);
				}
			}
			else //If something's added to no inventory, then obviously something's wrong.
			{
				throw new RuntimeException("Tried to place item into inventory " + inventoryID + " but failed");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		
		
		
		
	}
	
	

}
