package net.dimensia.src;
import java.util.Vector;

import net.dimensia.client.Dimensia;

import org.lwjgl.input.Mouse;

/**
 * <code>EntityLivingPlayer</code> extends <code>EntityLiving</code> and implements Serializable
 * <br><br>
 * EntityLivingPlayer implements all the features needed for the user's character. 
 * <br><br>
 * There are several methods of significance in EntityLivingPlayer, serving different purposes. 
 * The first is {@link #onWorldTick(World)}. This method should be called every single world tick,
 * and this is how the player gets updated regularly. This should apply things like regen, gravity,
 * and update cooldowns or invincibility (things based on world ticks). onWorldTick(World) can also
 * do things like check nearby blocks, to see if recipes need updated. Anything that should be 
 * done regularly, but not every frame, essentially.
 * <br><br>
 * A second major component of <code>EntityLivingPlayer</code> is recipe updating. The Recipe[] used are based off 
 * of what craftManager decides are possible for the inventory object passed to it. 
 * <code>EntityLivingPlayer</code> provides a method to check for nearby blocks, and update recipes as required 
 * {@link #checkForNearbyBlocks(World)}, based on the result for craftingManager. 
 * <br><br>
 * The third major component of <code>EntityLivingPlayer</code> is armour management. This is done almost 
 * automatically through a single method - {@link #onArmorChange()}. This method recalculates the armor
 * bonuses of the armour, as well as the set bonuses. It then proceeds to cancel the previous ones. 
 * <b>NOTE: This method does dangerous things, and more than likely is going to terribly ruin any attempt 
 * at adding a +defense potion, or -defense debuff. It WILL have to be recoded, with little doubt.</b>
 * <br><br>
 * The final major component of <code>EntityLivingPlayer</code> is block breaking, through the method 
 * {@link #breakBlock(World, int, int, Item)}. This method is what handles mining, and any 
 * gradual block breaking done by the player. A block break is automatically send to the 'world map' upon
 * completion of the mining.
 * @author      Alec Sobeck
 * @author      Matthew Robertson
 * @version     1.0 
 * @since       1.0
 */
public class EntityLivingPlayer extends EntityLiving
{
	private static final long serialVersionUID = 2L;
	private final static int HEALTH_FROM_STAMINA = 10;
	private final static int MANA_FROM_INTELLECT = 10;
	public int viewedChestX;
	public int viewedChestY;
	public boolean isViewingChest;	
	
	public int strength;
	public int dexterity;
	public int intellect;
	public int stamina;
	public int baseMaxHealth;
	public int baseMaxMana;
	
	public float respawnXPos;
	public float respawnYPos;
	public boolean isSwingingRight;
	public int selectedRecipe;
	public int selectedSlot;
	public boolean hasSwungTool;
	public float rotateAngle;
	public boolean isFacingRight;
	public boolean isInventoryOpen;	
	public InventoryPlayer inventory;
	public boolean heavensReprieve;
	
	private int ticksSinceLastCast = 0;
	private int ticksInCombat;
	private int ticksOfHealthRegen;
	private boolean isInCombat;
	private boolean isReloaded;
	private int ticksreq;
	private int sx;
	private int sy;		
	private int ticksBeforeNextHeal;
	private int ticksBeforeNextManaRestore;
	private EnumSetBonuses[] setBonuses;
	private CraftingManager craftingManager;
	private Recipe[] allPossibleRecipes;
	private final String playerName;
	private boolean inventoryChanged;
	private boolean isNearCraftingTable;
	private boolean isNearFurnace;
	private final EnumDifficulty difficulty;
	private Recipe[] possibleCraftingRecipes;
	private Recipe[] possibleFurnaceRecipes;
	private Recipe[] possibleInventoryRecipes;
	private final int MAXIMUM_BASE_MANA;
	private final int MAXIMUM_BASE_HEALTH;
	private final int MAX_BLOCK_PLACE_DISTANCE;
	
	/**
	 * Constructs a new instance of EntityLivingPlayer with default settings, inventory (includes 3 basic tools),
	 * and the specified name/difficulty. The default reset position is set to (50, 0), but this
	 * value is essentially worthless, and should be reset.
	 * @param name the player's name
	 * @param difficulty the difficulty setting the player has selected (EnumDifficulty)
	 */
	public EntityLivingPlayer(String name, EnumDifficulty difficulty)
	{
		super();		
		stamina = 0;
		isJumping = false;
		movementSpeedModifier = 1.0f;		
		baseSpeed = 5.0f;
		width = 12;
		height = 18;
		blockHeight = 3;
		blockWidth = 2;
		setRespawnPosition(50, 0);		
		upwardJumpHeight = 48;
		upwardJumpCounter = 0;
		jumpSpeed = 5;
		fallSpeed = 3;
		canJumpAgain = true;
		inventory = new InventoryPlayer();
		playerName = name;
		rotateAngle = -120.0f;		
		isInCombat = false;
		ticksInCombat = 0;
		ticksSinceLastCast = 0;
		health = 100;
		maxHealth = 100;
		baseMaxHealth = 100;
		baseMaxMana = 0;
		maxMana = 0;
		if(Dimensia.initInDebugMode)
		{
			health = 1;
			//maxHealth = 400;
			mana = 1000;
			baseMaxMana = 20;
			maxMana = 20;
			isAffectedByWalls = true; //pretty much no-clip
			isImmuneToFallDamage = false;
		}
		this.difficulty = difficulty;
		invincibilityTicks = 10;
		selectedSlot = 0;
		MAX_BLOCK_PLACE_DISTANCE = 42;
		MAXIMUM_BASE_HEALTH = 400;
		MAXIMUM_BASE_MANA = 200;
		ticksBeforeNextHeal = 0;
		viewedChestX = 0;
		viewedChestY = 0;
		isImmuneToCrits = false;
		setBonuses = new EnumSetBonuses[0];		
		craftingManager = new CraftingManager();
		isNearCraftingTable = false;
		isNearFurnace = false;
		inventoryChanged = true;	
		selectedRecipe = 0;
		knockbackModifier = 1;
		damageSoakModifier = 1;
		meleeDamageModifier = 1;
		rangeDamageModifier = 1;
		magicDamageModifier = 1;
		allDamageModifier = 1;
		heavensReprieve = false;
		isReloaded = false;
	}
	
	public EntityLivingPlayer(EntityLivingPlayer entity)
	{
		throw new RuntimeException("Support Not Implemented - Was this action actually required?");
	}
	
	/**
	 * Fixes issues from loading a player from disk. This generally relates to applying newer recipes and content, so that the
	 * player can actually view and use them.
	 */
	public void reconstructPlayerFromFile()
	{	
		isReloaded = true;
		rotateAngle = -120.0f;		
		if(Dimensia.initInDebugMode)
		{
			isAffectedByWalls = false; //pretty much no-clip
			isImmuneToFallDamage = true;
		}		
		invincibilityTicks = 10;
		selectedSlot = 0;
		ticksBeforeNextHeal = 0;
		viewedChestX = 0;
		viewedChestY = 0;
		isViewingChest = false;
		inventory.initializeInventoryTotals(isReloaded);
		craftingManager = new CraftingManager();
		isNearCraftingTable = false;
		isNearFurnace = false;
		inventoryChanged = true;	
		selectedRecipe = 0;
	}
	
	/**
	 * Updates the player, should only be called each world tick.
	 */
	public void onWorldTick(World world)
	{		
		invincibilityTicks = (invincibilityTicks > 0) ? --invincibilityTicks : 0;
		ticksSinceLastCast++;
		ticksBeforeNextHeal = (ticksBeforeNextHeal > 0) ? --ticksBeforeNextHeal : 0;
		ticksBeforeNextManaRestore = (ticksBeforeNextManaRestore > 0) ? --ticksBeforeNextManaRestore : 0;
		checkForCombatStatus();
		checkAndUpdateStatusEffects(world);
		applyGravity(world); //Apply Gravity to the player (and jumping)
		applyHealthRegen();
		applyManaRegen();
		checkForNearbyBlocks(world);	
		verifyChestRange();
	}
	
	/**
	 * Updates time in combat, and if the time has exceeded 120 ticks (6 seconds) since the last combat action, combat status is removed.
	 */
	private void checkForCombatStatus()
	{
		ticksInCombat++;
		
		if(ticksInCombat > 120)
		{
			ticksInCombat = 0;
			isInCombat = false;
		}
	}
	
	/**
	 * Checks if the player is still near their selected chest (should there be one selected). If the player is not near that
	 * chest, it is cleared and no longer rendered.
	 */
	private void verifyChestRange() 
	{
		if(isViewingChest)
		{
			if(MathHelper.distanceBetweenTwoPoints(viewedChestX * 6 + 6, viewedChestY * 6 + 6, x + (blockWidth / 2), y + (blockHeight / 2)) >= 48)
			{
				clearViewedChest();
			}			
		}
	}

	/**
	 * Gets the player's name, assigned upon player creation
	 * @return the player's name
	 */
	public final String getName()
	{
		return playerName;
	}
	
	/**
	 * Get every recipe the player is able to craft, based on what they're standing by and what's in 
	 * their inventory
	 * @return the allPossibleRecipes[], indicating possible craftable recipes
	 */
	public final Recipe[] getAllPossibleRecipes()
	{
		return allPossibleRecipes;
	}
	
	/**
	 * Sets the respawn position of the player (this may be broken between world sizes)
	 * @param x the x position in ortho units
	 * @param y the y position in ortho units
	 */
	public void setRespawnPosition(int x, int y) 
	{
		respawnXPos = x;		
		if(x % 6 != 0)
		{
			x = (int)(x / 6);
		}		
		respawnYPos = y;
	}
	
	/**
	 * Inflicts damage that does not trigger combat. This damage will not interrupt things like health regeneration which is dependent on whether 
	 * or not combat is active. This damage cannot be critical, or dodged, can be affected by the players armor, but is affected by invincibility (including
	 * triggering it).
	 * @param damage the damage inflicted
	 * @param penetratesArmor whether or not the damage penetrates the player's armor points (defense)
	 */
	public void inflictNonCombatDamage(World world, int damage, boolean penetratesArmor)
	{
		if(invincibilityTicks <= 0)
		{
			if(penetratesArmor)
			{
				float damageAfterArmor = ((damage - (defense / 2)) > 0) ? (damage - (defense / 2)) : 1;
				health -= damageAfterArmor;
				world.addTemporaryText(""+(int)damageAfterArmor, (int)x - 2, (int)y - 3, 20, 'd');
				invincibilityTicks = 15; //750ms
			}
			else
			{
				health -= damage;
				world.addTemporaryText(""+(int)damage, (int)x - 2, (int)y - 3, 20, 'd');
			}
			
			if(isDead()) //if the player has died
			{
				onDeath(world);
				world.clearEntityList();
			}	
		}
	}
	
	/**
	 * Applies gravity or a jump upward, depending on if the entity is jumping
	 */
	public void applyGravity(World world) 
	{
		if(isJumping) //If the entity is jumping upwards, move them up
		{
			moveEntityUp(world, jumpSpeed * movementSpeedModifier);			
		}
		else if(!isOnGround(world) && isAffectedByGravity) //otherwise, if the entity is in the air, make them fall
		{
			moveEntityDown(world, MathHelper.getFallSpeed(fallSpeed * movementSpeedModifier, ticksFallen));
			ticksFallen++;
		}	
		
		if(isOnGround(world)) //Is the entity on the ground? If so they can jump again
		{
			if((isJumping || !canJumpAgain) && isAffectedByGravity && !isImmuneToFallDamage) //if the entity can take fall damage
			{
				float fallDamage = MathHelper.getFallDamage(distanceFallen, maxHeightFallenSafely); //calculate the fall damage
				if(fallDamage > 0)
				{
					inflictNonCombatDamage(world, (int)fallDamage, true); //damage the player with non-combat defense affected damage
				}
			}
			
			ticksFallen = 0;
			canJumpAgain = true;
			distanceFallen = 0;
		}		
	}
	
	/**
	 * Overrides the entity damage taken to account for different invincibility and death
	 * @param d damage inflicted against the player
	 * @param isCrit was the hit critical? (2x damage)
	 */
	public void damageEntity(World world, int d, boolean isCrit)
	{
		if(invincibilityTicks <= 0) //If it's possible to take damage
		{
			double dodgeRoll = Math.random();
			d *= damageSoakModifier;
			if(dodgeRoll < dodgeChance || dodgeChance >= 1.0f) //Is it a dodge
			{
				world.addTemporaryText("Dodge", (int)x - 2, (int)y - 3, 20, 'g'); //add temperary text to be rendered, for the dodge
			}
			else if(!isCrit || isImmuneToCrits) //Is it Normal Damage
			{
				float damageAfterArmor = ((d - (defense / 2)) > 0) ? (d - (defense / 2)) : 1;
				health -= damageAfterArmor;
				world.addTemporaryText(""+(int)damageAfterArmor, (int)x - 2, (int)y - 3, 20, 'd');
				invincibilityTicks = 15; //750ms
			}
			else //It was a critical hit
			{
				d *= 2;
				invincibilityTicks = 16; //set the player invincible for 800ms
				float damageAfterArmor = ((d - (defense / 2)) > 0) ? (d - (defense / 2)) : 1; //determine damage after armour
				health -= damageAfterArmor; //do the damage
				world.addTemporaryText(""+(int)damageAfterArmor, (int)x - 2, (int)y - 3, 20, 'c'); //add temperary text to be rendered, for the damage done
			}	
			
			isInCombat = true;
		}
		
		if(isDead()) //if the player has died
		{
			onDeath(world);
			world.clearEntityList();
		}		
	}
	
	/**
	 * Heals the player, without a cooldown
	 * @param h the amount healed
	 */
	public boolean healPlayer_NoCooldown(World world, int h)
	{
		if(health < maxHealth)
		{
			health += h; 
			world.addTemporaryText(""+(int)h, (int)x - 2, (int)y - 3, 20, 'h'); //add temperary text to be rendered, for the healing done
			if(health > maxHealth) //if health exceeds the maximum, set it to the maximum
			{
				health = maxHealth;
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Heals the player, upto 1 heal / second
	 * @param h the amount healed
	 */
	public boolean healPlayer(World world, int h)
	{
		if(ticksBeforeNextHeal <= 0 && health < maxHealth)
		{
			health += h; 
			world.addTemporaryText(""+(int)h, (int)x - 2, (int)y - 3, 20, 'h'); //add temperary text to be rendered, for the healing done
			if(health > maxHealth) //if health exceeds the maximum, set it to the maximum
			{
				health = maxHealth;
			}
			ticksBeforeNextHeal = 20;
			return true;
		}
		return false;
	}
	
	/**
	 * Restore mana (capped at maxMana). upto 1 restore / second
	 * @param m the amount of mana to restore
	 */
	public boolean restorePlayerMana(World world, int m)
	{
		if(ticksBeforeNextManaRestore <= 0 && mana < maxMana)
		{
			mana += m; 
			world.addTemporaryText(""+(int)m, (int)x - 2, (int)y - 3, 20, 'b');
			if(mana > maxMana) //if mana exceeds the maximum, set it to the maximum
			{
				mana = maxMana;
			}
			ticksBeforeNextManaRestore = 20;
			return true;
		}
		return false;
	}
	
	/**
	 * Resets ticksSinceLastCast to 0, preventing mana regen for 6 seconds and restarting the regeneration cycle.
	 */
	public void resetCastTimer()
	{
		ticksSinceLastCast = 0;
	}
	
	/**
	 * Applies mana regen (currently a static amount)
	 */
	private void applyManaRegen()
	{
		if(ticksSinceLastCast > 120)
		{
			mana += ((ticksSinceLastCast - 120) / 1000 < 0.60f) ? ((ticksSinceLastCast - 120) / 1000) : 0.60f;
		}
		
		mana += (maxMana * 0.0002f);
		
		if(mana > maxMana)
		{
			mana = maxMana;
		}
	}
	
	/**
	 * Applies health regen that scales over time, until reaching the maximum of (11 HP/sec)
	 */
	private void applyHealthRegen()
	{
		if(!isInCombat)
		{
			float amountHealed = (ticksOfHealthRegen / 1800f < 0.55f) ? (ticksOfHealthRegen / 1800f) : 0.55f;
			health += amountHealed;
			ticksOfHealthRegen++;	
		}
	
		health += (maxHealth * 0.0002f);
		
		if(health > maxHealth)
		{
			health = maxHealth;
		}
	}
	
	/**
	 * Recalculates defense and set bonuses when armour changes
	 */
	public void onArmorChange()
	{
		recalculateStatsFromArmor();
		applyDefenseFromArmor();
		applySetBonus();
	}

	/**
	 * Attempts to recalculate stats and damage modifiers. <b>NOTE: WIP resets modifiers and stats! </b>
	 */
	private void recalculateStatsFromArmor()
	{
		dexterity = 0;
		intellect = 0;
		strength = 0;
		stamina = 0;
				
		for(int i = 0; i < inventory.getArmorInventoryLength(); i++) //for each slot, reapply the stats
		{
			if(inventory.getArmorInventoryStack(i) != null)
			{
				ItemArmor item = (ItemArmor) Item.itemsList[inventory.getArmorInventoryStack(i).getItemID()]; 				
				dexterity += item.getDexterity();
				intellect += item.getIntellect();
				strength += item.getStrength();
				stamina += item.getStamina();
			}
		}
		
		rangeDamageModifier = 1.0f + 0.04f * dexterity;
		meleeDamageModifier = 1.0f + 0.04f * strength;
		magicDamageModifier = 1.0f + 0.04f * intellect;
		maxHealth = baseMaxHealth + (stamina * HEALTH_FROM_STAMINA);
		maxMana = baseMaxMana + (intellect * MANA_FROM_INTELLECT);
	}
	
	/**
	 * Recalculates defense values and default bonuses for armour 
	 */
	private void applyDefenseFromArmor()
	{
		defense = 0; //dangerous line of code to clear armour benefits
		
		for(int i = 0; i < inventory.getArmorInventoryLength(); i++) //for each slot, reapply the defense value
		{
			if(inventory.getArmorInventoryStack(i) != null)
			{
				ItemArmor item = (ItemArmor) Item.itemsList[inventory.getArmorInventoryStack(i).getItemID()]; 				
				defense += item.getDefense();					
			}
		}
	}
	
	/**
	 * Overrides EntityLiving onDeath() to provide special things like hardcore (mode) and itemdrops
	 */
	public void onDeath(World world)
	{			
		if(heavensReprieve) //If this special modifier is in place, it's not yet the player's time... 
		{
			health = maxHealth * 0.15f; //give the player 15% health back
			invincibilityTicks = 120; //6 seconds immunity
			heavensReprieve = false;
			inventory.removeSavingRelic(); //destory something with that modifier (first thing to occur in the inventory) 
			onArmorChange(); //flag the armour as changed
		}
		else //Otherwise they actually have died
		{
			//this is where things would be dropped if that was added.
			health = (health < 100) ? 100 : health;
			world.spawnPlayer(this);
		}
	}
	
	/**
	 * Flag recipes for recalculation
	 */
	public void onInventoryChange()
	{
		inventoryChanged = true; 
	}

	/**
	 * Recreates the allPossibleRecipes[] based on what the player is standing by.
	 */
	private void setAllRecipeArray()
	{
		//'int size' indicates how many recipes are able to be created, in total
		int size = possibleInventoryRecipes.length; //InventoryRecipes are always possible, add their total no matter what
		
		if(isNearFurnace) //if the player is near a furnace, add the total length of the possibleFurnaceRecipes[]
		{
			size += possibleFurnaceRecipes.length;
		}
		if(isNearCraftingTable) //if the player is near a crafting table, add the total length of the possibleCraftingTable[]
		{
			size += possibleCraftingRecipes.length;
		}
		Recipe[] recipes = new Recipe[size]; //create a temperary Recipe[] to store stuff in.S
		
		int i = 0; //used for standard looping. declared here so its value can be known after the loop
		int k = 0; //the index in recipes[] to begin saving recipes
		
		if(isNearCraftingTable) //If the player is near a crafting table, add the recipes for that in.
		{
			for(i = 0; i < possibleCraftingRecipes.length; i++)
			{
				recipes[k + i] = possibleCraftingRecipes[i];
			}
			k += i;
		}
		if(isNearFurnace) //If the player is near a furnace, add the recipes for that in
		{
			for(i = 0; i < possibleFurnaceRecipes.length; i++)
			{
				recipes[k + i] = possibleFurnaceRecipes[i];
			}
			k += i;		
		}
		for(i = 0; i < possibleInventoryRecipes.length; i++) //Add inventory recipes in
		{
			recipes[k + i] = possibleInventoryRecipes[i];
		}
		
		allPossibleRecipes = recipes; //set the possible recipes to the temperary Recipe[]
		
		if(selectedRecipe >= allPossibleRecipes.length) //Fix the selectedRecipe Integer so that the crafting scroller doesnt go out of bounds
		{
			selectedRecipe = (allPossibleRecipes.length > 0) ? allPossibleRecipes.length - 1 : 0;
		}
	}
	
	/**
	 * Serves for detecting what can be crafting, currently
	 */
	private void checkForNearbyBlocks(World world) 
	{
		boolean recalculateRecipes = false;
		final int detectionRadius = 2; //blocks away a player can detect a crafting_table/furnace/etc
		
		boolean nearCraftingTable = blockInBounds(world, -detectionRadius, detectionRadius, -detectionRadius, detectionRadius, Block.craftingTable); //if the player is near a crafting table
		if(isNearCraftingTable != nearCraftingTable) //and they weren't just near one
		{ 
			recalculateRecipes = true; //recipes need recalculated
		}
	
		boolean nearFurnace = blockInBounds(world, -detectionRadius, detectionRadius, -detectionRadius, detectionRadius, Block.furnace); //if the player is near a furnace
		if(isNearFurnace != nearFurnace) //and they weren't just near one
		{
			recalculateRecipes = true; //recipes need recalculated
		}
		
		if(recalculateRecipes || inventoryChanged) //if the recipes need recalulated or the inventory has changed
		{	
			if(isNearCraftingTable != nearCraftingTable || inventoryChanged)
			{
				isNearCraftingTable = nearCraftingTable; 
				updateCraftingRecipes(); //recalculate the crafting recipes
			}	
	
			if(isNearFurnace != nearFurnace || inventoryChanged)
			{
				isNearFurnace = nearFurnace;
				updateFurnaceRecipes(); //recalculate the furnace recipes
			}
		
			updateInventoryRecipes(); //update inventory recipes
			setAllRecipeArray(); //set the crafting recipe array to the new recipes
			inventoryChanged = false;
		}
	}
		
	/**
	 * Emergency or init function to brute force calculate all recipes
	 */
	public void updateAllPossibleRecipes()
	{
		updateCraftingRecipes();
		updateFurnaceRecipes();
		updateInventoryRecipes();		
		setAllRecipeArray();
	}
	
	/**
	 * Updates the possibleCraftingRecipes[] to be accurate, after an inventoryChange generally
	 */
	private void updateCraftingRecipes()
	{
		possibleCraftingRecipes = craftingManager.getPossibleCraftingRecipes(inventory);		
	}
	
	/**
	 * Updates the possibleFurnaceRecipes[] to be accurate, after an inventoryChange generally
	 */
	private void updateFurnaceRecipes()
	{
		possibleFurnaceRecipes = craftingManager.getPossibleFurnaceRecipes(inventory);
	}
	
	/**
	 * Updates the possibleInventoryRecipes[] to be accurate, after an inventoryChange generally
	 */
	private void updateInventoryRecipes()
	{
		possibleInventoryRecipes = craftingManager.getPossibleInventoryRecipes(inventory);
	}
	
	/**
	 *  Applies all (set) bonuses from armour equipped. Important note: % modifiers (such as +10%) multiply the current value 
	 *  by 10% (1.1), and are not additive. This may cause things to behave slightly different than expected, or otherwise behave wierdly
	 *  This is subject to individual change, if balance requires it. Currently this is not the case though.
	 */
	private void applySetBonus()
	{
		Vector<EnumSetBonuses> otherBonuses = new Vector<EnumSetBonuses>(10);
		EnumSetBonuses[] set = EnumSetBonuses.getSetBonus(inventory);
		
		for(int i = 0; i < set.length; i++) //Add set bonus from armour
		{
			otherBonuses.add(set[i]);
		}
		
		for(int i = 0; i < inventory.getArmorInventoryLength(); i++) //For each individual armour item
		{
			if(inventory.getArmorInventoryStack(i) != null) 
			{
				ItemArmor armor = (ItemArmor) Item.itemsList[inventory.getArmorInventoryStack(i).getItemID()]; 
				for(EnumSetBonuses bonus : armor.bonuses) //Add all the bonuses from that piece
				{
					otherBonuses.add(bonus);
				}
			}
		}
		
		EnumSetBonuses[] bonuses = new EnumSetBonuses[otherBonuses.size()];
		otherBonuses.copyInto(bonuses);
		
		//Apply all the bonuses:
		for(int i = 0; i < bonuses.length; i++)
		{
			//IMPORTANT: +% defense increases should happen only to the base defense, not the set bonuses!
			if(bonuses[i] == EnumSetBonuses.DEFENSE_P_10) // apply +10% base defense bonus
			{
				this.defense *= 1.1;
				continue;
			}			
			if(bonuses[i] == EnumSetBonuses.DEFENSE_P_20) // Apply +20% base defense bonus
			{
				this.defense *= 1.2;
				continue;
			}			
			if(bonuses[i] == EnumSetBonuses.DEFENSE1) // Apply +1 defense bonus
			{
				this.defense += 1;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.DEFENSE2) // Apply +2 defense bonus
			{
				this.defense += 2;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.DEFENSE3) // Apply +3 defense bonus
			{
				this.defense += 3;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.DEFENSE4) // Apply +4 defense bonus
			{
				this.defense += 4;
				continue;
			}			
			if(bonuses[i] == EnumSetBonuses.DAMAGE_MAGIC_DONE_10) // Apply +10% magic damage
			{
				this.magicDamageModifier *= 1.1;
				continue;
			}			
			if(bonuses[i] == EnumSetBonuses.DAMAGE_MAGIC_DONE_20) // Apply +20% magic damage
			{
				this.magicDamageModifier *= 1.2;
				continue;
			}			
			if(bonuses[i] == EnumSetBonuses.DAMAGE_RANGE_DONE_10) // Apply +10% range damage
			{
				this.rangeDamageModifier *= 1.1;
				continue;
			}			
			if(bonuses[i] == EnumSetBonuses.DAMAGE_RANGE_DONE_20) // Apply +20% range damage
			{
				this.rangeDamageModifier *= 1.2;
				continue;
			}			
			if(bonuses[i] == EnumSetBonuses.DAMAGE_MELEE_DONE_10) // Apply +10% melee damage
			{
				this.meleeDamageModifier *= 1.1;
				continue;
			}			
			if(bonuses[i] == EnumSetBonuses.DAMAGE_MELEE_DONE_20) // Apply +20% melee damage
			{
				this.meleeDamageModifier *= 1.2;
				continue;
			}			
			if(bonuses[i] == EnumSetBonuses.DAMAGE_DONE_10) // Apply +10% all damage
			{
				this.allDamageModifier *= 1.1;
				continue;
			}			
			if(bonuses[i] == EnumSetBonuses.DAMAGE_DONE_20) // Apply +10% all damage
			{
				this.allDamageModifier *= 1.2;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.DAMAGE_SOAK_5) // Apply -5% (all) damage
			{
				this.damageSoakModifier *= 0.95;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.DAMAGE_SOAK_10) // Apply -10% (all) damage 
			{
				this.damageSoakModifier *= 0.9;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.DAMAGE_SOAK_15) // Apply -15% (all) damage
			{
				this.damageSoakModifier *= 0.85;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.DAMAGE_SOAK_20) // Apply -20% (all) damage
			{
				this.damageSoakModifier *= 0.8;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.MOVEMENT_SPEED_10) // Apply +10% movement speed
			{
				this.movementSpeedModifier *= 1.1;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.MOVEMENT_SPEED_20) // Apply +20% movement speed
			{
				this.movementSpeedModifier *= 1.2;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.MOVEMENT_SPEED_30) // Apply +30% movement speed
			{
				this.movementSpeedModifier *= 1.3;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.FALL_IMMUNE) // Apply Fall Damage Immunity
			{
				this.isImmuneToFallDamage = true;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.FIRE_IMMUNE) // Apply Fire (and lava) Damage Immunity 
			{
				this.isImmuneToFireDamage = true;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.JUMP3) // Apply +3 block jump height and fall immunity
			{
				this.maxHeightFallenSafely += 18;
				this.upwardJumpHeight += 18;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.JUMP3) // Apply +8 block jump height and fall immunity
			{
				this.maxHeightFallenSafely += 48;
				this.upwardJumpHeight += 48;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.CRITICAL10) // Apply +10% critical strike chance
			{
				this.criticalStrikeChance *= 1.1;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.CRITICAL20) // Apply +20% critical strike chance
			{
				this.criticalStrikeChance *= 1.2;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.CRITICAL_IMMUNE) // Apply Critical Hit Immunity
			{
				this.isImmuneToCrits = true;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.KNOCKBACK_20) // Apply +20% knockback power
			{
				this.knockbackModifier *= 1.2;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.KNOCKBACK_40) // Apply +40% knockback power
			{
				this.knockbackModifier *= 1.4;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.KNOCKBACK_60) // Apply +60% knockback power
			{
				this.knockbackModifier *= 1.6;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.HEAVENS_REPRIEVE) // Apply the HEAVENS_REPRIEVE effect
			{
				this.heavensReprieve = true;
				continue;
			}
		}
		
		removeOldBonuses();
		this.setBonuses = bonuses;		
	}
		
	/**
	 * Removes all the set effects previously applied to the player, before their armour changed.
	 * <br><br>
	 * [WARNING (From Alec)]: This is probably going to break at very least when dealing with immunities, because 
	 * they're applied earlier on... so if duplicates of an item are present it's very possible things will go terribly wrong
	 * and immunity wont be achievable (includes things like heavensReprieve too)
	 */	
	private void removeOldBonuses()
	{
		EnumSetBonuses[] bonuses = this.setBonuses;
		
		//Remove all the bonuses:
		for(int i = 0; i < bonuses.length; i++)
		{
			//DONT do anything with the defense modifiers, they're already taken care of!
			if(bonuses[i] == EnumSetBonuses.DAMAGE_MAGIC_DONE_10) // Remove +10% magic damage
			{
				this.magicDamageModifier /= 1.1;
				continue;
			}			
			if(bonuses[i] == EnumSetBonuses.DAMAGE_MAGIC_DONE_20) // Remove +20% magic damage
			{
				this.magicDamageModifier /= 1.2;
				continue;
			}			
			if(bonuses[i] == EnumSetBonuses.DAMAGE_RANGE_DONE_10) // Remove +10% range damage
			{
				this.rangeDamageModifier /= 1.1;
				continue;
			}			
			if(bonuses[i] == EnumSetBonuses.DAMAGE_RANGE_DONE_20) // Remove +20% range damage
			{
				this.rangeDamageModifier /= 1.2;
				continue;
			}			
			if(bonuses[i] == EnumSetBonuses.DAMAGE_MELEE_DONE_10) // Remove +10% melee damage
			{
				this.meleeDamageModifier /= 1.1;
				continue;
			}			
			if(bonuses[i] == EnumSetBonuses.DAMAGE_MELEE_DONE_20) // Remove +20% melee damage
			{
				this.meleeDamageModifier /= 1.2;
				continue;
			}			
			if(bonuses[i] == EnumSetBonuses.DAMAGE_DONE_10) // Remove +10% all damage
			{
				this.allDamageModifier /= 1.1;
				continue;
			}			
			if(bonuses[i] == EnumSetBonuses.DAMAGE_DONE_20) // Remove +10% all damage
			{
				this.allDamageModifier /= 1.2;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.DAMAGE_SOAK_5) // Remove -5% (all) damage
			{
				this.damageSoakModifier /= 0.95;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.DAMAGE_SOAK_10) // Remove -10% (all) damage 
			{
				this.damageSoakModifier /= 0.9;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.DAMAGE_SOAK_15) // Remove -15% (all) damage
			{
				this.damageSoakModifier /= 0.85;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.DAMAGE_SOAK_20) // Remove -20% (all) damage
			{
				this.damageSoakModifier /= 0.8;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.MOVEMENT_SPEED_10) // Remove +10% movement speed
			{
				this.movementSpeedModifier /= 1.1;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.MOVEMENT_SPEED_20) // Remove +20% movement speed
			{
				this.movementSpeedModifier /= 1.2;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.MOVEMENT_SPEED_30) // Remove +30% movement speed
			{
				this.movementSpeedModifier /= 1.3;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.FALL_IMMUNE) // Remove Fall Damage Immunity
			{
				this.isImmuneToFallDamage = false;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.FIRE_IMMUNE) // Remove Fire (and lava) Damage Immunity 
			{
				this.isImmuneToFireDamage = false;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.JUMP3) // Remove +3 block jump height and fall immunity
			{
				this.maxHeightFallenSafely -= 18;
				this.upwardJumpHeight -= 18;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.JUMP3) // Remove +8 block jump height and fall immunity
			{
				this.maxHeightFallenSafely -= 48;
				this.upwardJumpHeight -= 48;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.CRITICAL10) // Remove +10% critical strike chance
			{
				this.criticalStrikeChance /= 1.1;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.CRITICAL20) // Remove +20% critical strike chance
			{
				this.criticalStrikeChance /= 1.2;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.CRITICAL_IMMUNE) // Remove Critical Hit Immunity
			{
				this.isImmuneToCrits = false;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.KNOCKBACK_20) // Remove +20% knockback power
			{
				this.knockbackModifier /= 1.2;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.KNOCKBACK_40) // Remove +40% knockback power
			{
				this.knockbackModifier /= 1.4;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.KNOCKBACK_60) // Remove +60% knockback power
			{
				this.knockbackModifier /= 1.6;
				continue;
			}
			if(bonuses[i] == EnumSetBonuses.HEAVENS_REPRIEVE) // Remove the HEAVENS_REPRIEVE effect
			{
				this.heavensReprieve = false;
				continue;
			}
		}
	}	
	
	/**
	 * Function called when the player is mining a block
	 * @param mx x position in worldmap array, of the block being mined
	 * @param my y position in the worldmap array, of the block being mined
	 * @param item the tool mining the block
	 */
	public void breakBlock (World world, int mx, int my, Item item)
	{
		if (((item instanceof ItemToolPickaxe && world.getBlock(mx, my).getBlockType() == 1) 
		  || (item instanceof ItemToolAxe && world.getBlock(mx, my).getBlockType() == 2) 
		  || (item instanceof ItemToolHammer && world.getBlock(mx, my).getBlockType() == 3) 
	      || world.getBlock(mx, my).getBlockType() == 0) && Mouse.isButtonDown(0))
		{ //If the left-mouse button is pressed && they have the correct tool to mine
			EnumToolMaterial material;
			if (item instanceof ItemTool)
			{
				material = item.getToolMaterial();
			}
			else
			{
				material = EnumToolMaterial.FIST;
			}
			double distance = MathHelper.distanceBetweenTwoPoints((MathHelper.getCorrectMouseXPosition() + Render.getCameraX()), (MathHelper.getCorrectMouseYPosition() + Render.getCameraY()), (this.x + ((isFacingRight) ? 9 : 3)), (this.y + 9));
				      
			if(distance <= material.getDistance() && material.getToolTier() >= world.getBlock(mx, my).getBlockTier()) //If the block is within range
			{ 	
				if(ticksreq == 0 || sx != mx || sy != my) //the mouse has moved or they arent mining anything
				{				
					sx = mx; //save the co-ords
					sy = my;
					ticksreq = (int) (world.getBlock(mx, my).getBlockHardness() / material.getStrength()) + 1; //Determine how long to break
				}	
				else if(ticksreq == 1 && Mouse.isButtonDown(0)) //If the block should break
				{
					if(world.getBlock(mx, my) == Block.cactus) 
					{
						world.breakCactus(this, mx, my);
					}
					else if(world.getBlock(mx, my) == Block.tree)
					{
						world.breakTree(this, mx, my);					
					}
					world.breakBlock(this, mx, my);
					
					//Overwrite snow/flowers/etc...
					if (world.getBlock(mx, my-1).getIsOveridable() == true && world.getBlock(mx, my-1) != Block.air)
					{
						world.breakBlock(this, mx, my-1);
					}
				}		
				else if (Mouse.isButtonDown(0)) //mining is in progress, decrease remaining time.
				{
					ticksreq--;			
				}	
			}
		}		
	}	
	
	/**
	 * Increases the maximum health of the player
	 * @param increase the amount to increase maxHealth
	 * @return success of the operation
	 */
	public boolean boostMaxHealth(int increase) 
	{
		if(baseMaxHealth + increase <= MAXIMUM_BASE_HEALTH)//if the player can have that much of a max health increase
		{
			maxHealth += increase; //increase max health
			baseMaxHealth += increase;
			return true;
		}
		return false;
	}
	
	/**
	 * Increases the maximum mana of the player
	 * @param increase the amount to increase maxMana
	 * @return success of the operation
	 */
	public boolean boostMaxMana(int increase) 
	{
		if(baseMaxMana + increase <= MAXIMUM_BASE_MANA) //if the player can have that much of a max mana increase
		{
			baseMaxMana += increase;
			maxMana += increase; //increase max mana
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the player's difficulty setting. This field is final, and unchanging
	 * @return the player's difficulty setting
	 */
	public final EnumDifficulty getDifficulty()
	{
		return difficulty;
	}
	
	/**
	 * Gets the maximum distance the player can place a block, measured in ortho units. this field is final
	 * @return MAX_BLOCK_PLACE_DISTANCE field
	 */
	public final int getMaximumBlockPlaceDistance()
	{
		return MAX_BLOCK_PLACE_DISTANCE;
	}
	
	/**
	 * Sets the position of the viewed chest (from 'world map'), and changes isViewingChest to true.
	 * @param x the x position of the chest, in the 'world map'
	 * @param y the y position of the chest, in the 'world map'
	 */
	public void setViewedChest(int x, int y)
	{
		isViewingChest = true;
		viewedChestX = x;
		viewedChestY = y;
	}
	
	/**
	 * Clears the currently viewed chest position to 0,0, and sets isViewingChest to false
	 */
	public void clearViewedChest()
	{
		isViewingChest = false;
		viewedChestX = -1;
		viewedChestY = -1;
	}
	
	/**
	 * Resets all variables relating to mining, to a default value so the player can mine again
	 */
	public void resetMiningVariables()
	{
		ticksreq = 0;
		sy = 0;
		sx = 0;
	}
	
	/**
	 * Launch a player projectile
	 * @param world = current world
	 * @param mouseX = x position to create the projectile at
	 * @param mouseY = y position to create the projectile at
	 * @param item = Item to be used to launch projectile (what projectile is needed)
	 */
	public void launchProjectile(World world, float mouseX, float mouseY, Item item){
		if (item instanceof ItemRanged){
			ItemStack[] ammo = item.getAmmoAsArray();
			for (int i = 0; i < ammo.length; i++){				
				if (inventory.doesPartialStackExist(ammo[i]) != -1){
					int angle = MathHelper.angleMousePlayer(mouseX, mouseY, x, y) - 90;
					if (angle < 0){
						angle += 360;
					}
					if (isFacingRight){
						world.addEntityToProjectileList(new EntityProjectile(Item.itemsList[ammo[i].getItemID()].getProjectile()).setDrop(Item.itemsList[ammo[i].getItemID()]).setXLocAndYLoc(x, y)
								.setDirection(angle).setDamage((Item.itemsList[ammo[i].getItemID()].getProjectile().getDamage() + item.getDamage())));
					}
					else{
						world.addEntityToProjectileList(new EntityProjectile(Item.itemsList[ammo[i].getItemID()].getProjectile()).setDrop(Item.itemsList[ammo[i].getItemID()]).setXLocAndYLoc(x, y)
								.setDirection(angle).setDamage((Item.itemsList[ammo[i].getItemID()].getProjectile().getDamage() + item.getDamage())));
					}
					inventory.removeItemsFromInventoryStack(1, inventory.doesPartialStackExist(ammo[i]));
				}
			}
		}
		if (item instanceof ItemMagic){
			if (mana >= item.getManaReq()){
				int angle = MathHelper.angleMousePlayer(mouseX, mouseY, x, y) - 90;
				if (angle < 0){
					angle += 360;
				}
				world.addEntityToProjectileList(new EntityProjectile(item.getProjectile()).setXLocAndYLoc(x, y)
						.setDirection(angle).setDamage(item.getProjectile().getDamage()));
				mana -= item.getManaReq();
			}
		}
		
	}
	
	/**
	 * Gets the Block the player is holding, if it's a Block and it's an instanceof BlockLight. Used for handheld lighting.
	 * @return an instanceof BlockLight if the player is holding one, otherwise null
	 */
	public Block getHandheldLight()
	{
		ItemStack stack = inventory.getMainInventoryStack(selectedSlot);
		if(stack != null)
		{
			if(stack.getItemID() < Item.shiftedIndex && (Block.blocksList[stack.getItemID()]) instanceof BlockLight)
			{
				return Block.blocksList[stack.getItemID()];
			}
		}
		return null;
	}
}