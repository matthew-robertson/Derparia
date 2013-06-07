package entities;
import items.Item;

import java.util.Random;
import java.util.Vector;


import render.Render;

import utils.ItemStack;
import utils.MonsterDrop;
import utils.Texture;

import enums.EnumDamageType;
import enums.EnumMonsterType;

/**
 * <code>EntityLivingNPCEnemy extends EntityNPC</code>, 
 * and <code>implements Serializable</code>
 * (as a result of extending <code>EntityLiving</code>)
 * <br> 
 * <code>EntityLivingNPCEnemy</code> implements many of the features and fields needed for a monster.
 * It provides a public implementation of {@link #clone()}, so that monsters
 * can be added to world, from their public final static declarations here.
 * <br><br>
 * The other main method of interest here is {@link #applyAI(World)}. This method is the main method to 
 * make a monster do something, based on where/what the player is doing. It should be called every game tick
 * or at least several times per second, to avoid having laggy monsters.
 * <br><br>
 * Fields and getters/settings are included for storage of:
 * <li>Texture, and texture coordinates
 * <li>Unique id number (this is a non-changing value, therefore there is no setter)
 * <li>Monster type, and monster damage type (these are enums)
 * <li>Damage done
 * <li>Drops
 * <li> where/when the monster can spawn (biome/time)
 * <li>Whether the monster is a boss (this should involve an extension of EntityLivingNPCEnemy)
 * @author      Alec Sobeck
 * @author      Matthew Robertson
 * @version     1.0
 * @since       1.0
 */
public class EntityLivingNPCEnemy extends EntityLivingNPC 
{	
	private static final long serialVersionUID = 1L;	
	public String type;
	public String time;
	protected final Random random = new Random();
	protected MonsterDrop[] possibleDrops;
	protected int iconIndex;
	protected int monsterId;
	public int damageDone;
	protected EnumDamageType damageType;
	protected EnumMonsterType monsterType;
	protected Texture texture;
	public boolean isBoss;
	protected String name;
	
	/**
	 * Constructs a new <code>EntityLivingNPCEnemy</code>. This constructor is protected
	 * because monsters are created in <code>EntityLivingNPCEnemy.java</code> and
	 * then cloned into <code>world.entityList</code>. This reduces the margin of error
	 * greatly, as instead of any number of possible failures, only a simple
	 * clone must be made. It also ensures consistency between monsters
	 * @param i the EntityLivingNPCEnemy's unique ID number, used for getting it from EntityList
	 */
	protected EntityLivingNPCEnemy(int i, String s)
	{
		super(i, s);
		name = s;
		damageDone = 0;
		monsterId = i;
		damageType = EnumDamageType.MELEE;
		monsterType = EnumMonsterType.GROUNDED;
		blockWidth = 2;
		blockHeight = 3;
		textureWidth = 16;
		textureHeight = 16;
		maxHealth = 1;
		health = 1;
		damageDone = 1;
		width = 12;
		height = 18;
		baseSpeed = 2.5f;
		possibleDrops = new MonsterDrop[0];
		time = " ";
		type = " ";
		if (enemyList[i] != null){
			throw new RuntimeException("Entity already exists @" + i);
		}		
		enemyList[i] = this;
	}	

	public EntityLivingNPCEnemy(EntityLivingNPCEnemy entity)
	{
		super(entity);
		this.type = entity.type;
		this.time = entity.time;
		this.possibleDrops = entity.possibleDrops;
		this.iconIndex = entity.iconIndex;
		this.monsterId = entity.monsterId;
		this.damageDone = entity.damageDone;
		this.damageType = entity.damageType;
		this.monsterType = entity.monsterType;
		this.texture = entity.texture;
		this.isBoss = entity.isBoss;
		this.name = entity.name;
	}

	protected EntityLivingNPCEnemy setDamageType(EnumDamageType type)
	{
		damageType = type;
		return this;
	}
	
	protected EntityLivingNPCEnemy setWidthandHeight(int x, int y)
	{
		width = x;
		height = y;
		return this;
	}
	
	protected EntityLivingNPCEnemy setTexture(Texture tex)
	{
		texture = tex;
		return this;
	}
	
	protected EntityLivingNPCEnemy setDamageDone(int i)
	{
		damageDone = i;
		return this;
	}
	
	protected EntityLivingNPCEnemy setMaxHealth(int i)
	{
		maxHealth = i;
		health = i;
		return this;
	}
		
	protected EntityLivingNPCEnemy setWorldDimensions(int i, int j)
	{
		width = i;
		height = j;
		return this;
	}
	
	protected EntityLivingNPCEnemy setBlockDimensions(int i, int j)
	{
		blockWidth = i;
		blockHeight = j;
		return this;
	}
	
	protected EntityLivingNPCEnemy setBlockAndWorldDimensions(int i, int j)
	{
		setWorldDimensions(i * 6, j * 6);
		setBlockDimensions(i, j);
		return this;
	}
	
	protected EntityLivingNPCEnemy setIconIndex(int i, int j)
	{
		iconIndex = i * 16 + j;
		return this;
	}
	
	protected EntityLivingNPCEnemy setTextureDimensions(int i, int j)
	{
		textureWidth = i;
		textureHeight = j;
		return this;
	}
	
	protected EntityLivingNPCEnemy setBaseSpeed(float f)
	{
		baseSpeed = f;
		return this;
	}
		
	public float getWidth()
	{
		return width;
	}
	
	public float getHeight()
	{
		return height; 
	}
	
	public Texture getTexture()
	{
		return texture;
	}

	/**
	 * Sets Monster drops using the following format (constructor) to declare the monster drops:
	 * public MonsterDrop(ItemStack stack, int min, int max, int rollMax)
	 * @param drops the array of possible drops
	 */
	public EntityLivingNPCEnemy setDrops(MonsterDrop[] drops)
	{
		this.possibleDrops = drops;
		return this;
	}	
	
	/**
	 * Gets the drops for the specific enemy kill
	 * @return all the drops for the kill, or null if none are dropped
	 */
	public ItemStack[] getDrops()
	{
		Vector<ItemStack> drops = new Vector<ItemStack>();
		for(MonsterDrop drop : possibleDrops) //for each possible drop
		{
			if(random.nextInt(drop.getRollMaximum()) == 0) //See if the drop will happen (and how much of it)
			{
				drops.add(new ItemStack(drop.getDrop().getItemID(), (drop.getMinimum() + (((drop.getMaximum() - drop.getMinimum()) > 0) ? random.nextInt(drop.getMaximum() - drop.getMinimum()) : 0))));
			}
		}
		ItemStack[] stacks = new ItemStack[drops.size()];
		drops.copyInto(stacks);
		return (drops.size() > 0) ? stacks : null; 
	}
	
	public float getBlockWidth()
	{
		return blockWidth;
	}
	
	public float getBlockHeight()
	{
		return blockHeight;
	}
	
	public String getEnemyName()
	{
		return name;
	}
	
	/**
	 * Makes the monster do something somewhat smart, based on its type. This includes a movement
	 * on the X axis, based on player direction, as well as an attempt to jump if applicable. Gravity 
	 * is also applied if the EntityLivingNPCEnemy is not currently jumping.
	 * @param world the main world Object for the current game
	 */
	/*public void applyAI(World world, EntityLivingPlayer player)
	{
		//if(!isStunned)
		//{
			//ground AI
			if(player.x + 3 < x) //if the player is to the left, move left
			{	        
				moveEntityLeft(world);
			}
			else if(player.x - 3 > x) //if the player is to the right, move right
			{
				moveEntityRight(world);
			}
			
			if(player.y < y) //if the player is above the entity, jump
			{
				hasJumped();
			}
		//}
		//else
		//{
		//	System.out.println("Skipped Entity Movement@>>" + this);
		//}
		
		
		applyGravity(world); //and attempt to apply gravity
	}*/
	public EntityLivingNPCEnemy setIsAlert(boolean alert){
		this.alert = alert;
		return this;
	}
	public EntityLivingNPCEnemy setSpawnVector(String type, String time)
	{
		this.type = type;
		this.time = time;
		return this;
	}
	
	public String getType()
	{
		return type;
	}
	
	public String getTime()
	{
		return time;
	}
	
	public final static EntityLivingNPCEnemy[] enemyList = new EntityLivingNPCEnemy[10];		
	/** EntityLivingNPCEnemy Declarations **/
	public final static EntityLivingNPCEnemy goblin = new EntityLivingNPCEnemy(0, "Goblin").setIsAlert(true).setSpawnVector("forest", " ").setSpawnVector("desert", "night").setTexture(Render.goblin).setDamageDone(7).setMaxHealth(50).setDrops(new MonsterDrop[] { 
			new MonsterDrop(new ItemStack(Item.coal), 1, 2, 5), new MonsterDrop(new ItemStack(Item.healthPotion1), 1, 2, 20)
	});
	public final static EntityLivingNPCEnemy zombie = new EntityLivingNPCEnemy(1, "zombie").setIsAlert(true).setSpawnVector(" ", "night").setDamageDone(16).setMaxHealth(60).setTexture(Render.zombie).setDrops(new MonsterDrop[]{
			new MonsterDrop(new ItemStack(Item.healthPotion2), 1, 1, 50), new MonsterDrop(new ItemStack(Item.ironSword), 1, 1, 100), new MonsterDrop(new ItemStack(Item.bronzeSword), 1, 1, 66)
	});
	public final static EntityLivingNPCEnemy slime = new EntityLivingNPCEnemy(2, "Slime").setIsAlert(true).setSpawnVector(" ", " ").setBlockAndWorldDimensions(1,1).setTexture(Render.slime).setMaxHealth(30).setDamageDone(4).setBaseSpeed(1.4f).setDrops(new MonsterDrop[]{
			new MonsterDrop(new ItemStack(Item.silverIngot), 1, 1, 200), new MonsterDrop(new ItemStack(Item.healingHerb1), 1, 1, 15), new MonsterDrop(new ItemStack(Item.vialOfWater), 1, 1, 15)  
	});
	public final static EntityLivingNPCEnemy dino = new EntityLivingNPCEnemy(3, "Dinosaur").setIsAlert(true).setSpawnVector("forest", "night").setBlockAndWorldDimensions(5,4).setTexture(Render.dino).setMaxHealth(150).setDamageDone(20).setBaseSpeed(3.4f).setDrops(new MonsterDrop[]{
			new MonsterDrop(new ItemStack(Item.ringOfVigor), 1, 1, 100), new MonsterDrop(new ItemStack(Item.coal), 1, 3, 4), new MonsterDrop(new ItemStack(Item.healingHerb2), 1, 1, 20) 
	});
	//public final static EntityLivingNPCEnemy floatingEye = new EntityLivingNPCEnemy(4, "Eyeball").setDamageDone(12).setMaxHealth(60);
	
	
}