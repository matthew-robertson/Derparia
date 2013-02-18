package net.dimensia.src;

import java.util.ArrayList;
import java.util.List;

/**
 * <code>EntityLiving</code> extends <code>Entity</code> and implements Serializable (as an extension of Entity)
 * <br>
 * It provides most of the features required for an Entity
 * that is alive. The list of methods for EntityLiving is extensive. It includes methods to: 
 * <br><br>
 * <li>Apply gravity: {@link #applyGravity(World)}
 * <li>Check for nearby blocks: {@link #blockInBounds(World, int, int, int, int, Block)}
 * <li>Check for vertexes inside the Entity: {@link #inBounds(float, float)}, or {@link #inBounds(float, float, float, float)}
 * <li>Check if the entity has health left: {@link #isDead()}
 * <li>Damage the entity: {@link #damageEntity(World, int, boolean)} 
 * <li>Handle a jump: {@link #hasJumped()}
 * <li>Handle movement: {@link #moveEntityDown(World)}, {@link #moveEntityUp(World)}, {@link #moveEntityLeft(World)}, {@link #moveEntityRight(World)}
 * <li>Determine movement: {@link #isWalkingSafe(World, boolean)}, {@link #isJumpNeeded(World, boolean, boolean)}, {@link #isJumpPossibleAndNeeded(World, boolean, boolean)}
 * <li>Launch projectiles: {@link #launchProjectile(World, EntityProjectile)}
 * <li>Heal the entity: {@link #healEntity(World, int)}
 * <li>Implement custom death behaviour: {@link #onDeath()}
 * <br><br>
 * More advanced features can be added by further extending the class. All methods in EntityLiving
 * are designed to scale with the entity's size, health, etc.
 * <br>
 * @author      Alec Sobeck
 * @author      Matthew Robertson
 * @version     1.0
 * @since       1.0
 */
public class EntityLiving extends Entity
{
	private static final long serialVersionUID = 1L;
	public boolean isFireImmune;
	public float attackSpeedModifier;
	public float knockbackModifier;
	public float damageSoakModifier;
	public float meleeDamageModifier;
	public float rangeDamageModifier;
	public float magicDamageModifier;
	public float allDamageModifier;
	public boolean isStunned;
	public List<StatusEffect> statusEffects;
	public int ticksFallen;
	/** (chance to crit / 100) */
	public float criticalStrikeChance; 
	/** (Chance to dodge / 100) */
	public float dodgeChance;
	public boolean isImmuneToCrits;
	public boolean isImmuneToFallDamage;
	public boolean isImmuneToFireDamage;
	public int invincibilityTicks;
	public int textureWidth;
	public int textureHeight;
	public float width;
	public float height;
	public float blockWidth;
	public float blockHeight;
	public float distanceFallen;
	public float maxHeightFallenSafely;
	public float baseSpeed;
	public float movementSpeedModifier;
	public int maxHealth;
	public int maxMana;
	public float mana;
	public float defense;
	public float health;
	public int wanderLeft;
	public int wanderRight;
	public boolean alert;
	public int ticksSinceLastWander;
	public int ticksSinceLastProjectile;
	
	/**
	 * Overrides Entity's constructor, and constructs a new EntityLiving. 
	 * In addition to Entity's Constructor, this
	 * also sets a large number of values to a base value. The list is extensive
	 * and includes (but many not be exclusively limited to) the following initializations:
	 * <br> <br>
	 * <li>(max)Health is set to 100
	 * <li>(max)Mana is set to 0
	 * <li>defense is set to 0
	 * <li>movementSpeedModifier is set to 1.0f
	 * <li>baseSpeed is set to 1.0f
	 * <li>Jump distance(upwardJumpHeight) is set to 7 blocks (42 ortho)
	 * <li>maxHeightFallSafely is set to 12 blocks (72 ortho)
	 * <li>fallSpeed is set to 3.0f
	 * <li>jumpSpeed is set to 6.0f
	 * <li>dodge and critical strike chance are set to 5%
	 * <li>position is set to (0,0)
	 * <li>critical strike, fall damage, are fire immunity are disabled (set false)
	 */
	public EntityLiving()
	{
		super();
		defense = 0;
		health = 100;
		maxHealth = 100;
		mana = 0;
		maxMana = 0;
		movementSpeedModifier = 1.0f;
		baseSpeed = 1.0f;
		upwardJumpHeight = 42; //7 blocks
		upwardJumpCounter = 0;
		distanceFallen = 0;
		maxHeightFallenSafely = 72;
		jumpSpeed = 6;
		fallSpeed = 3;
		isJumping = false;
		canJumpAgain = true;
		isImmuneToFallDamage = false;
		isImmuneToFireDamage = false;
		dodgeChance = 0.05f;
		attackSpeedModifier = 1.0f;
		knockbackModifier = 1;
		damageSoakModifier = 1;
		meleeDamageModifier = 1;
		rangeDamageModifier = 1;
		magicDamageModifier = 1;
		allDamageModifier = 1;
		wanderLeft = 0;
		wanderRight = 0;
		alert = false;
		isImmuneToCrits = false;
		criticalStrikeChance = 0.05f;
		statusEffects = new ArrayList<StatusEffect>();
		
		x = 0;
		y = 0;	
		isStunned = false;
	}
	
	public EntityLiving(EntityLiving entity)
	{
		super(entity);

		this.isFireImmune = entity.isFireImmune;
		this.attackSpeedModifier = entity.attackSpeedModifier;
		this.knockbackModifier = entity.knockbackModifier;
		this.damageSoakModifier = entity.damageSoakModifier;
		this.meleeDamageModifier = entity.meleeDamageModifier;
		this.rangeDamageModifier = entity.rangeDamageModifier;
		this.magicDamageModifier = entity.magicDamageModifier;
		this.allDamageModifier = entity.allDamageModifier;
		this.isStunned = entity.isStunned;
	
		this.statusEffects = new ArrayList<StatusEffect>();
		
		for(int i = 0; i < entity.statusEffects.size(); i++)
		{
			this.statusEffects.add(new StatusEffect(entity.statusEffects.get(i)));
		}
		this.ticksFallen = entity.ticksFallen;
		this.criticalStrikeChance = entity.criticalStrikeChance; 
		this.dodgeChance = entity.dodgeChance;
		this.isImmuneToCrits = entity.isImmuneToCrits;
		this.isImmuneToFallDamage = entity.isImmuneToFallDamage;
		this.isImmuneToFireDamage = entity.isImmuneToFireDamage;
		this.invincibilityTicks = entity.invincibilityTicks;
		this.textureWidth = entity.textureWidth;
		this.textureHeight = entity.textureHeight;
		this.width = entity.width;
		this.height = entity.height;
		this.blockWidth = entity.blockWidth;
		this.blockHeight = entity.blockHeight;
		this.distanceFallen = entity.distanceFallen;
		this.maxHeightFallenSafely = entity.maxHeightFallenSafely;
		this.baseSpeed = entity.baseSpeed;
		this.movementSpeedModifier = entity.movementSpeedModifier;
		this.maxHealth = entity.maxHealth;
		this.maxMana = entity.maxMana;
		this.mana = entity.mana;
		this.defense = entity.defense;
		this.health = entity.health;
		this.wanderLeft = entity.wanderLeft;
		this.wanderRight = entity.wanderRight;
		this.alert = entity.alert;
		this.ticksSinceLastWander = entity.ticksSinceLastWander;
		this.ticksSinceLastProjectile = entity.ticksSinceLastProjectile;
	}
	
	/** 
	 * launch NPC projectiles
	 * @param world - current world
	 * @param angle - the angle at which to launch the projectile
	 * @param projectile - the projectile to launch
	 */
	public void launchProjectile(World world, int angle, EntityProjectile projectile){
		world.addEntityToProjectileList(new EntityProjectile(projectile).setXLocAndYLoc(x, y)
				.setDirection(angle).setDamage(projectile.getDamage()));
	}
	
	/**
	 * Checks if a jump is required to progress
	 * @param world - current world
	 * @param direction - true if moving right, false if left
	 * @return true if jump is needed, false if not
	 */
	public int isJumpRequired(World world, boolean direction, boolean up){
		float heightCheck;
		
		if ((alert) || (up && !alert)){
			heightCheck = upwardJumpHeight / 6;
		}
		else {
			heightCheck = blockHeight;
		}
		
		//If checking the right side
		if (direction){
			//Check all the blocks in a line up to the npc's block height if any are solid, return true
			for (int i = 0; i <= heightCheck; i++){
					if (world.getBlockGenerate((int)(x + width) / 6, (int)(y + height) / 6 - 1 - i).getIsSolid()){
						if (up){
							return i;
						}
						else return -1;
					}
			}
		}
		//if checking the left side
		else if (!direction){
			//Check all the blocks in a line up to the npc's block height if any are solid, return true
			for (int i = 0; i <= heightCheck; i++){						
				 if (world.getBlockGenerate((int)(x) / 6 - 1, (int)(y + height) / 6 - 1 - i).getIsSolid()){
					if (up){
						return i;
					}
					else return -1;
				 }
			}
		}
		//Else return false
		return -1;
	}
	
	/**
	 * Checks to see if a jump is possible on the given side
	 * @param world - current world
	 * @param direction - true if moving right, false if left
	 * @return true if jump is possible, false if not
	 */
	public boolean isJumpPossibleAndNeeded(World world, boolean direction, boolean up){
		int start = isJumpRequired(world, direction, up);
		if (start != -1){
			int maxBlockHeight = 0;
			int blockHeight = 0;
			//If checking the right side
			if (direction){
				//Check all the blocks in a line up to the maximum jump height + the npc's block height
				for (int i = start; i < (upwardJumpHeight + height) / 6; i++){
					 if (!world.getBlockGenerate((int)(x + width) / 6, (int)(y + height) / 6 - 1 - i).getIsSolid()){
						 blockHeight++;
						 if (blockHeight > maxBlockHeight){
							 maxBlockHeight = blockHeight;
						 }
					 }
					 else{
						 blockHeight = 0;
					 }
					 if (maxBlockHeight >= blockHeight){
						  return true;
					 }
				}
			}
			//if checking the left side
			else if (!direction){
				//Check all the blocks in a line up to the maximum jump height + the npc's block height
				for (int i = start; i < (upwardJumpHeight + height / 6); i++){
					 if (!world.getBlockGenerate((int)(x) / 6 - 1, (int)(y + height) / 6 - 1 - i).getIsSolid()){
						 blockHeight++;
						 if (blockHeight > maxBlockHeight){
							 maxBlockHeight = blockHeight;
						 }
					 }
					 else{
						 blockHeight = 0;
					 }
					 if (maxBlockHeight >= blockHeight){
						 return true;
					 }
				}
			}
		}
		return false;
	}
	
	/**
	 * Checks to see if it is safe for the actor to move in the provided direction
	 * @param world - the current world
	 * @param direction - true if attempting to move right, false if moving left
	 * @return - true if it's safe to walk (a block is solid and the actor will land on it, up to it's maximum safe fall distance down, and it's width wide)
	 */
	public boolean isWalkingSafe(World world, boolean direction){
		//If checking the right side
		if (direction){
			//Check all the blocks in a line up to the npc's block width if any are solid, return true
			for (int i = 0; i <= blockWidth; i++){
				for (int j = 0; j <= (maxHeightFallenSafely / 6); j++)
				if (world.getBlockGenerate((int)(x + width) / 6 - 1 + i, (int)(y + height) / 6 + j).getIsSolid()){
					return true;							 
				}
			}
		}
		
		//if checking the left side
		else if (!direction){
			//Check all the blocks in a line up to the npc's block width if any are solid, return true
			for (int i = 0; i <= blockWidth; i++){						
				for (int j = 0; j <= (maxHeightFallenSafely / 6); j++){ 
					if (world.getBlockGenerate((int)(x) / 6 - i, (int)(y + height) / 6 + j).getIsSolid()){
						return true;
					}
				 }
			}
		}
		//Else return false
		return false;
	}
	
	/**
	 * Damages the entity for the specified amount
	 * @param d the amount damage
	 * @param isCrit was the hit critical? (2x damage)
	 */
	public void damageEntity(World world, int d, boolean isCrit)
	{
		if(invincibilityTicks <= 0) //can the entity actually be damaged?
		{	
			double dodgeRoll = Math.random();
			if(dodgeRoll < dodgeChance || dodgeChance >= 1.0f) //Is it a dodge
			{
				world.addTemporaryText("Dodge", (int)x - 2, (int)y - 3, 20, 'g'); //add temporary text to be rendered, for the damage done
			}
			else if(!isCrit) //Is it Normal Damage
			{
				invincibilityTicks = 5; //set them invincible for 250ms
				float damageAfterArmor = ((d - (defense / 2)) > 0) ? (d - (defense / 2)) : 1; //determine damage after armour
				health -= damageAfterArmor; //do the damage
				world.addTemporaryText(""+(int)damageAfterArmor, (int)x - 2, (int)y - 3, 20, 'w'); //add temporary text to be rendered, for the damage done
			}
			else //It was a critical hit
			{
				d *= 2;
				invincibilityTicks = 5; //set them invincible for 250ms
				float damageAfterArmor = ((d - (defense / 2)) > 0) ? (d - (defense / 2)) : 1; //determine damage after armour
				health -= damageAfterArmor; //do the damage
				world.addTemporaryText(""+(int)damageAfterArmor, (int)x - 2, (int)y - 3, 20, 'c'); //add temporary text to be rendered, for the damage done
			}		
		}
		else //the entity can't be damaged, so reduce their invincibility
		{
			invincibilityTicks--;
		}
		
		if(isDead()) //is the entity has died, perform the appropriate action
		{
			onDeath(world);
		}		
	}
	
	/**
	 * Heals the entity for the specified amount
	 * @param h the amount healed
	 */
	public void healEntity(World world, int h)
	{
		health += h; 
		world.addTemporaryText(""+(int)h, (int)x - 2, (int)y - 3, 20, 'h'); //add temperary text to be rendered, for the healing done
		if(health > maxHealth) //if health exceeds the maximum, set it to the maximum
		{
			health = maxHealth;
		}
	}
	
	/**
	 * Determines if the EntityLiving has died
	 * @return whether the entity is dead or not (true = dead)
	 */
	public boolean isDead()
	{
		return health <= 0;
	}	
		
	/**
	 * Handles special death events. Generally this should be done by extending EntityLiving and overriding this method
	 */
	public void onDeath(World world)
	{		
	}
	
	/**
	 * Determines if any corner point of the compared quad falls inside this entity's hitbox
	 * @param x x position (in ortho) of the entity to compare against this entity
	 * @param y y position (in ortho) of the entity to compare against this entity
	 * @param w width of the entity to compare against this entity
	 * @param h height of the entity to compare against this entity
	 * @return Whether a point falls in bounds or not
	 */
	public boolean inBounds(float x, float y, float w, float h)
	{
		if ((x >= this.x && x <= this.x + 12 && y >= this.y && y <= this.y + 18) //top left
		|| (x + w >= this.x && x + w <= this.x + 12 && y >= this.y && y <= this.y + 18) //top right
		|| (x + w >= this.x && x + w <= this.x + 12 && y + h >= this.y && y + h <= this.y + 18) //bottom right
		|| (x >= this.x && x <= this.x + 12 && y + h >= this.y && y + h <= this.y + 18)){ //bottom left
			return true;
		}			
		return false;
	}
	
	/**
	 * Determines if the point is inside the EntityLiving
	 * @param x the x point to test against this entity
	 * @param y the y point to test against this entity
	 * @return whether the point is in bounds or not
	 */
	public boolean inBounds(float x, float y)
	{
		return (x >= this.x && x <= this.x + width && y >= this.y && y <= this.y + height);
	}
	
	/**
	 * Determines if a block from the 'world map' is nearby
	 * @param leftOffset how far left the block can be detected
	 * @param rightOffset how far right the block can be detected
	 * @param upOffset how far up the block can be detected
	 * @param downOffset how far down the block can be detected
	 * @param block what block to search for
	 * @return whether the block is nearby or not
	 */	
	public boolean blockInBounds(World world, int leftOffset, int rightOffset, int upOffset, int downOffset, Block block)
	{
		int x = (int)(this.x / 6);
		int y = (int)(this.y / 6);
		for(int i = leftOffset; i < rightOffset + blockWidth; i++) //for each block horizontally
		{
			for(int j = upOffset; j < downOffset + blockHeight; j++) //and each block vertically
			{
				if(world.getBlock(MathHelper.returnIntegerInWorldMapBounds_X(world, x + i), MathHelper.returnIntegerInWorldMapBounds_Y(world, y + j)).blockID == block.blockID)
				{ //see if the block matches the specified block
					return true;				
				}
			}
		}
		return false;
	}
	
	public String getDirectionOfQuadRelativeToEntityPosition(float x, float y, float width, float height) //NYI
	{
		if((x + (width * .5)) < this.x)
			return "left";
		if((x + (width * .5)) >= this.x)
			return "right";
		
		//CONTINUE HERE
		
		return "";
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
					damageEntity(world, (int)fallDamage, ((Math.random() < 0.1f) ? true : false)); //damage the entity
				}
			}
			
			ticksFallen = 0;
			canJumpAgain = true;
			distanceFallen = 0;
		}		
	}
	
	/**
	 * Sees if it's possible to jump. If so then the jump will be performed
	 */
	public void hasJumped()
	{
		if(canJumpAgain)// If the player can jump, let them
		{
			isJumping = true;
			upwardJumpCounter = 0;
			canJumpAgain = false;
		}	
	}
	
	/**
	 * Determines whether the entity standing on something solid?
	 */
	public boolean isOnGround(World world)		
	{
		int xOffset = (int) ((x % 6 == 0) ? blockWidth : (blockWidth + 1));
		try
		{
			for(int i = 0; i < xOffset; i++) //for each block below the entity, check if any are solid
			{
				if(!world.getBlock((int)(x / 6) + i, (int)((y / 6) + Math.ceil(blockHeight))).isPassable())
				{
					return true; //if one is solid, they entity is on the ground
				}
			}
			return false;
		}
		catch(Exception e) //if there's an out of bounds error, assume the player is standing on something solid
		{
			return true;
		}
	}
	
	/**
	 * Applies a jump to the entity's Y Position. Disables jumping if (y < 0) or the upwardJumpCounter exceeds the upwardJumpHeight.
	 */
	public void moveEntityUp(World world, float jumpSpeed)
	{		
		float movementValue = jumpSpeed;
		int loops = (int) (movementValue / 6) + 1;
		
		if(isStunned)
		{
			isJumping = false;
			return;
		}
		
		for(int i = 0; i < loops; i++)
		{
			float f = canMoveUp(world);
			
			if((y - f) >= 0 && f >= movementValue) //full movement
			{
				upwardJumpCounter += f;
				y -= f;
			}
			else if(f > 0) //partial movement
			{
				upwardJumpCounter += f;
				y -= f;
			}
			
			if(upwardJumpCounter >= upwardJumpHeight) //If the player has exceeded the jump height, stop jumping
			{
				if(upwardJumpCounter > upwardJumpHeight)
				{
					y += upwardJumpCounter - upwardJumpHeight;
				}
				isJumping = false;
			}	
			
			if(y < 0) 
			{
				y = 0;
				isJumping = false;
				break;
			}
			if(y > (world.getHeight() * 6) - height - 6)
			{
				y = (world.getHeight() * 6) - height - 6;
				break;
			}
			
			movementValue -= f;
		}
	}
	
	/**
	 * Gets how far up the entity can move, upto 1 block (6 ortho units). Use multiple times to go further. 
	 * @return how far the entity can move, upto 6 ortho units (1 block)
	 */
	private float canMoveUp(World world)
	{
		if((int)(y / 6) <= 0) //bounds check
		{
			isJumping = false;
			return 6;
		}
		
		Block[] blocks = new Block[(int) (blockWidth + ((x % 6 == 0) ? 0 : 1))]; //blocks to check	
		boolean flag = true;
		
		for(int i = 0; i < blocks.length; i++) //get blocks to check
		{
			blocks[i] = world.getBlock((x / 6) + i, ((y - jumpSpeed) / 6));
		}
		
		for(int i = 0; i < blocks.length; i++)
		{
			if(!blocks[i].isPassable()) //hittest has failed
			{
				flag = false;
				break;
			}
			else
			{
				flag = true;
			}
		}
		
		if(flag && y - jumpSpeed >= 0) //Normal upward movement
		{
			return jumpSpeed;
		}
		else if(y % 6 != 0) //Partial jump
		{
			isJumping = false;
			return y % 6;
		}
		else //Anything else means the player cant jump anymore
		{
			isJumping = false;
		}

		return 0.0f;
	}
	
	/**
	 * Applies downward gravity to the entity 	
	 */
	public void moveEntityDown(World world, float fallSpeed)  
	{		
		float movementValue = fallSpeed;
		int loops = (int) (movementValue / 6) + 1;
		
		for(int i = 0; i < loops; i++)
		{
			float f = canMoveDown(world);
			
			if(y + f < (world.getHeight() * 6) && f >= movementValue) //full movement
			{
				distanceFallen += f;
				y += f;
			}
			else if(f > 0) //partial movement
			{
				distanceFallen += f;
				y += f;
			}
			
			if(y > (world.getHeight() * 6) - height - 6)
			{
				y = (world.getHeight() * 6) - height - 6;
				break;
			}

			movementValue -= f;
		}
	}
	
	/**
	 * Tries to move the entity right
	 */
	public void moveEntityRight(World world)
	{
		float movementValue = baseSpeed * movementSpeedModifier;
		int loops = (int) (movementValue / 6) + 1;
		
		if(isStunned)
		{
			return;
		}
		
		for(int i = 0; i < loops; i++)
		{
			float possibleMovement = canMoveRight(world);
			
			float actualMovement = movementValue % 6;
			
			if(x + possibleMovement < (world.getWidth() * 6) && possibleMovement >= movementValue) //full movement
				x += actualMovement;
			else if(possibleMovement > 0) //partial movement
				x += actualMovement;
			
			if(x > world.getWidth() * 6 - width - 6) //bounds check
				x = (world.getWidth() * 6) - width - 6;

			movementValue -= actualMovement;
		}
	}
	
	/**
	 * Tries to move the entity left
	 */
	public void moveEntityLeft(World world) 
	{
		float movementValue = baseSpeed * movementSpeedModifier;
		int loops = (int) (movementValue / 6) + 1;
		
		if(isStunned)
		{
			return;
		}
		
		for(int i = 0; i < loops; i++)
		{
			float possibleMovement = canMoveLeft(world);
			float actualMovement = movementValue % 6;
			
			if((x - possibleMovement) >= 0 && possibleMovement >= movementValue) //full movement
				x -= actualMovement;
			else if(possibleMovement > 0) //partial movement
				x -= actualMovement;
			
			if(x < 0)//out of bounds check
				x = 0;
			
			movementValue -= actualMovement;
		}
	}
	
	/**
	 * Tries to move the entity right. This version of the method dictates how far to move right. Basespeed is NOT Applied!
	 * @param movementValue the distance to move right, if possible
	 */
	public void moveEntityRight(World world, float movementValue)
	{
		int loops = (int) (movementValue / 6) + 1;
		
		if(isStunned)
		{
			return;
		}
		
		for(int i = 0; i < loops; i++)
		{
			float possibleMovement = canMoveRight(world);
			
			float actualMovement = movementValue % 6;
			
			if(x + possibleMovement < (world.getWidth() * 6) && possibleMovement >= movementValue) //full movement
				x += actualMovement;
			else if(possibleMovement > 0) //partial movement
				x += actualMovement;
			
			if(x > world.getWidth() * 6 - width - 6) //bounds check
				x = (world.getWidth() * 6) - width - 6;

			movementValue -= actualMovement;
		}
	}
	
	/**
	 * Tries to move the entity left. This version of the method dictates how far to move left. Basespeed is NOT Applied!
	 * @param movementValue the distance to move left, it possible
	 */
	public void moveEntityLeft(World world, float movementValue) 
	{
		int loops = (int) (movementValue / 6) + 1;
		
		if(isStunned)
		{
			return;
		}
		
		for(int i = 0; i < loops; i++)
		{
			float possibleMovement = canMoveLeft(world);
			float actualMovement = movementValue % 6;
			
			if((x - possibleMovement) >= 0 && possibleMovement >= movementValue) //full movement
				x -= actualMovement;
			else if(possibleMovement > 0) //partial movement
				x -= actualMovement;
			
			if(x < 0)//out of bounds check
				x = 0;
			
			movementValue -= actualMovement;
		}
	}
	
	/**
	 * Gets how far right can the entity move (0-6 ortho)
	 * @return the distance that can be moved right (from 0-6 ortho)
	 */
	private float canMoveRight(World world)
	{
		if(!isAffectedByWalls) //no point in performing a hittest
			return 6;
		
		boolean flag = false;
		int offset = (int) ((x % 6 == 0) ? blockWidth : (blockWidth + 1)); //How far right are the blocks for the hit test, if the player is perfectly on a block 2, otherwise 3		
		Block[] blocks = new Block[(int) (blockHeight + ((y % 6 == 0) ? 0 : 1))]; //blocks to check
		
		for(int i = 0; i < blocks.length; i++) //get the blocks to check against
		{
			blocks[i] = world.getBlock(MathHelper.returnIntegerInWorldMapBounds_X(world, (int)(x / 6) + offset), MathHelper.returnIntegerInWorldMapBounds_Y(world, (int)(y / 6) + i));
		}
		
		for(int i = 0; i < blocks.length; i++)
		{
			if(!blocks[i].isPassable()) //the hittest has failed
			{
				flag = false;
				break;
			}
			else
			{
				flag = true;
			}
		}
		
		return (flag) ? 6 : (offset == blockWidth) ? (x % 6) : (6 - (x % 6));//6-complete success; offset==2-0; otherwise, remaining value to move over
	}
	
	/**
	 * Gets how far left can the entity move (0-6 ortho)
	 * @return the distance that can be moved left (from 0-6 ortho)
	 */
	private float canMoveLeft(World world)
	{
		if(!isAffectedByWalls) //no point in performing a hittest
			return 6;
		
		boolean flag = false;				
		Block[] blocks = new Block[(int) (blockHeight + ((y % 6 == 0) ? 0 : 1))]; //blocks to check

		for(int i = 0; i < blocks.length; i++) //get the blocks to check against
		{
			blocks[i] =  world.getBlock(MathHelper.returnIntegerInWorldMapBounds_X(world, (int)(x / 6) - 1), MathHelper.returnIntegerInWorldMapBounds_Y(world, ((int)(y / 6) + i)));
		}
		
		for(int i = 0; i < blocks.length; i++)
		{
			if(!blocks[i].isPassable()) //is the block isnt passable, the hittest has failed
			{
				flag = false;
				break;
			}
			else
			{
				flag = true;
			}
		}	

		return (flag) ? 6 : (x % 6); //6-> complete success, otherwise the remaining amount it's possible to move
	}

	/**
	 * Gets how far the entity can fall, upto 1 block (use multiple times to go further)
	 * @return the distance it's possible to fall, upto 6 ortho units (1 block)
	 */
	private float canMoveDown(World world)
	{
		int offset = (int) ((y % 6 == 0) ? blockHeight : (blockHeight + 1)); 
		
		if((int)((y + fallSpeed) / 6) + offset + 1 > world.getHeight() - 1) //bounds check
		{
			return 6.0f;
		}
		
		Block[] blocks = new Block[(int) (blockWidth + ((x % 6 == 0) ? 0 : 1))]; //blocks to check
		boolean flag = true;
		
		for(int i = 0; i < blocks.length; i++) //get blocks to check
		{
			blocks[i] = world.getBlock((x / 6) + i, ((y + fallSpeed) / 6) + offset); 
		}		

		for(int i = 0; i < blocks.length; i++)
		{
			if(!blocks[i].isPassable()) //hittest has failed
			{
				flag = false;
				break;
			}
			else
			{
				flag = true;
			}
		}
		
		
		
		if(flag && y + fallSpeed < world.getHeight() * 6) //Normal Gravity
		{
			return fallSpeed;
		}
		else if(y % 6 != 0) //Near ground partial gravity 
		{
			return (6 - (y % 6));
		}		
		return 0.0f;
	}
	
	/**
	 * Heals the entity for a certain amount of health, possibly periodic, that does not incur cooldown or text to screen.
	 * @param health the amount of health to heal
	 */
	public void heal_Textless(int health)
	{
		health += health; 
		if(health > maxHealth) //if health exceeds the maximum, set it to the maximum
		{
			health = maxHealth;
		}
	}
	
	public void checkAndUpdateStatusEffects(World world)
	{
		if(isStunned)
		{
		//	System.out.println("Stunned Entity@" + this);
			System.out.print("");
		}
		for(int i = 0; i < statusEffects.size(); i++)
		{
			if(!(this instanceof EntityLivingPlayer))
			{
				System.out.println(i + ">" + statusEffects.get(i) + " TIME_REMAINING = " + statusEffects.get(i).ticksLeft);
			}
			statusEffects.get(i).applyPeriodicBonus(world, this);
			if(statusEffects.get(i).isExpired())
			{
				statusEffects.get(i).removeInitialEffect(this);
				statusEffects.remove(i);
			}
		}
	}
	
	public boolean registerStatusEffect(StatusEffect effect)
	{
		for(int i = 0; i < statusEffects.size(); i++)
		{
			if(statusEffects.get(i).toString().equals(effect.toString()))
			{
				System.out.println("StatusEffectMatch=" + effect.toString());
				
				if(statusEffects.get(i).tier <= effect.tier)
				{
					if(statusEffects.get(i).tier == effect.tier && statusEffects.get(i).ticksLeft > effect.ticksLeft)
					{
						return false;
					}
					else
					{
						if(!statusEffects.get(i).reapplicationSkipsRemovalEffect)
						{
							statusEffects.get(i).removeInitialEffect(this);
						}
						statusEffects.remove(i);
						
						effect.applyInitialEffect(this);
						statusEffects.add(effect);
						System.out.println("Effect registered>>" + effect);
						return true;
					}
				}
				else
				{
					return false;
				}
			}
		}
		
		effect.applyInitialEffect(this);
		statusEffects.add(effect);
		System.out.println(this + "->Effect registered>>" + effect);
		return true;
	}
	
	public EntityLiving setUpwardJumpHeight(int i){
		upwardJumpHeight = i;
		return this;
	}
}
