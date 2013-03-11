package net.dimensia.src;

import java.io.Serializable;
import java.util.Random;

/**
 * <code>Block implements Serializable, Cloneable</code>
 * <br>
 * <code>Block</code> defines the class for placable ItemStacks in the player's inventory, and 
 * the Objects used to render the world and determine where <code>Entities</code> are able to move.
 * All possible <code>Blocks</code> must extend <code>Block</code>, to ensure general consistancy and storage 
 * of all Blocks. All <code>Blocks</code> are stored in <code>Block.blocksList[]</code> upon creation,
 * at the index of their <code>blockID</code>, so that they may easily be accessed later.
 * <br><br>
 * <code>Blocks</code> are generally initialized using <code>protected Block(int)</code>, but 
 * <code>Block.air</code> is uniquely created using <code>protected Block()</code> due to there being 
 * irregular settings and no need for it to be stored in <code>blocksList[]</code>. <code>Block(int)</code> 
 * provides a large amount of standard <code>Block</code> settings that can be changed with 
 * the use of setters. An example block declaration (in this case for <code>Block.chest</code>):
 * <br>
 * <code>public static Block chest = new BlockChest(56).setBlockName("chest").setBothBlockWidthAndHeight(2, 2).setBlockHardness(40.0f).setIconIndex(11, 1).setBlockType(2);</code>
 * <br>
 * To explain this example, a <code>Block</code> is created as an <code>instanceof BlockChest</code> with: 
 * <li>A blockID of 56
 * <li>A blockName of "chest"
 * <li>A render width and texture width of 2x2 blocks (so it renders 12x12 ortho units, and has a texture of 32x32 pixels)
 * <li>A blockHardness of 40.0f (about 20% weaker than stone)
 * <li>An icon position on terrain.png of (11,1)
 * <li>A blockType of 2 (breakable with an axe)
 * <br>
 * In addition to the default settings that otherwise are unmodified. This allows for <code>Block</code>, like
 * <code>Item</code>, to be a very versitile class with the simple use of several different setters.
 * <br><br>
 * The constructor of <code>Block</code> is protected for the same reason as <code>Item</code>'s, to prevent 
 * random redeclaration of a <code>Block</code>. This could cause corruption of the world save file, or cause 
 * the game to crash. With a protected constructor, a <code>Block</code> can only be declared here; hopefully
 * preventing corrupt except due to extreme stupid.
 * <br><br> 
 * There is a point of interest regarding rendering of <code>Block</code>. The "universal block constant" 
 * width and height is 12 pixels (6 'ortho'(render) units), per block in the 'world map'. This does not change, 
 * and likely never will. This size does not even change when the screen is resized, instead more 
 * <code>Blocks</code> are displayed. <b>NOTE: this is a 'magic number' not a constant.</b>
 * @author      Alec Sobeck
 * @author      Matthew Robertson
 * @version     1.0
 * @since       1.0
 */
public class Block
		implements Serializable, Cloneable
{
	private static final long serialVersionUID = 1L;
	/** (constant) Block width in pixels, currently this value is 6. Corresponds to the value of Render.BLOCK_SIZE.*/
	public final static int BLOCK_WIDTH = 6;
	/** (constant) Block height in pixels, currently this is 6. Corresponds to the value of Render.BLOCK_SIZE.*/
	public final static int BLOCK_HEIGHT = 6;
	protected static final Random random = new Random();
	protected ItemStack droppedItem;
	protected int maximumDropAmount;
	protected int minimumDropAmount;
	protected int metaData;
	protected boolean isOveridable;
	protected boolean hasMetaData;
	protected char tileMap;
	protected int bitMap;
	public int iconY;
	public int iconX;
	protected int blockWidth; //Render Width of a texture (in the world)
	protected int blockHeight; //Render Height of a texture (in the world)
	protected int maxStackSize;
	protected double textureWidth; //Pixel Width of texture
	protected double textureHeight; //Pixel Height of texture
	protected int iconIndex;
	protected int gradeOfToolRequired;
	protected int blockType;
	protected int blockTier;
	protected EnumBlockMaterial material;
	protected boolean breakable;
	protected float hardness;
	protected int blockID;
	protected String blockName;
	protected boolean passable;
	protected boolean isSolid;
	protected int hRange;
	protected int lRange;
	protected String extraTooltipInformation;
	
	/**
	 * Constructs a special Block- air. This constructor should only ever be used for the initial declaration
	 * of Block.air.
	 */
	protected Block()
	{		
		blockName = "air";
		extraTooltipInformation = "";
		isSolid = false;
		isOveridable = true;
	}
	
	/**
	 * Constructs a new <code>Block</code> that will be stored in <code>blocksList[]</code> at the index of the blockID,
	 * allowing for easy access to the <code>Block</code> created later. <code>Block</code> has a protected 
	 * constructor, so that a <code>Block</code> can't be randomly declared outside of <code>Block.java</code>.
	 * This should prevent random <code>Blocks</code> from overwriting random parts of the <code>blocksList[]</code>,
	 * possibly crashing the game or corrupting the save file for the world.
	 * <br><br>
	 * Additional customization can be performed after creation of a <code>Block</code> through
	 * the use of setters (see the class comment for an example).
	 * @param i the unique blockID of the <code>Block</code> being created
	 */
	protected Block(int i)
	{
		blockID = i;
		breakable = true;
		blockWidth = 6;
		blockHeight = 6;
		textureWidth = 16; 
		textureHeight = 16;
		passable = false;
		tileMap = ' ';
		bitMap = 0;
		maxStackSize = 250;
		gradeOfToolRequired = 0;
		blockTier = 0;
		droppedItem = new ItemStack(this);
		maximumDropAmount = 1;
		minimumDropAmount = 1;
		isOveridable = false;
		isSolid = true;
		extraTooltipInformation = "";
		
		if(blocksList[i] != null)
		{
			System.out.println(new StringBuilder().append("Conflict@ BlockID ").append(i).toString());
			throw new RuntimeException(new StringBuilder().append("Conflict@ BlockID ").append(i).toString());
		}
		blocksList[i] = this;		
	}
	
	/**
	 * @deprecated
	 * Overrides Object.clone() to provide better and public cloning functionality for Block. Cloned objects, using Block.clone()
	 * should return a deep copy, at a new reference.
	 * @return a deep copy of the current Block
	 */
	public Block clone()
	{
		try 
		{
			return (Block) super.clone();
		}
		catch(CloneNotSupportedException e) 
		{
			System.out.println("Cloning not allowed.");
			return this;
		}
	}
	
	/**
	 * @deprecated replaced by clone(). This method is not maintained very well and likely is outdated.
	 * Generally it's recommended to use clone(), as there's a good chance this will fail and cause Block corruption. 
	 * @param block the Block to be copied
	 * @return a new Block with the same properties as the cloned one 
	 */
	public Block(Block block)
	{
		this.droppedItem = block.droppedItem;
		this.maximumDropAmount = block.maximumDropAmount;
		this.minimumDropAmount = block.minimumDropAmount;
		this.hasMetaData = block.hasMetaData;
		this.blockWidth = block.blockWidth; 
		this.blockHeight = block.blockHeight; 
		this.maxStackSize = block.maxStackSize;
		this.textureWidth = block.textureWidth; 
		this.textureHeight = block.textureHeight; 
		this.iconIndex = block.iconIndex;
		this.gradeOfToolRequired = block.gradeOfToolRequired;
		this.blockType = block.blockType;
		this.blockTier = block.blockTier;
		this.material = block.material;
		this.breakable = block.breakable;
		this.hardness = block.hardness;
		this.blockID = block.blockID;
		this.blockName = block.blockName;
		this.passable = block.passable;
		this.extraTooltipInformation = block.extraTooltipInformation;
	}
		
	protected Block setBlockName(String s)
	{
		blockName = s;
		return this;
	}
	
	protected Block setBlockHardness(float f)
	{
		hardness = f;
		return this;
	}
	
	protected Block setBreakable(boolean flag)
	{
		breakable = flag;
		return this;
	}
	
	protected Block setIconIndex(int x, int y)
	{
		iconY = y;
		iconX = x;
		iconIndex = x + y * Render.ICONS_PER_COLUMN;
		return this;
	}
	
	protected Block setExtraTooltipInformation(String info)
	{
		this.extraTooltipInformation = info;
		return this;
	}
	
	protected Block setBlockMaterial(EnumBlockMaterial mat)
	{
		material = mat;
		//gradeOfToolRequired....
		return this;
	}
		
	protected Block setPassable(boolean flag)
	{
		passable = flag;
		return this;
	}
	
	protected Block setMaxStackSize(int i)
	{
		maxStackSize = i;
		return this;
	}
	
	protected Block setGradeOfToolRequired(int i)
	{
		gradeOfToolRequired = i;
		return this;
	}
	
	protected Block setBothBlockWidthAndHeight(int x, int y)
	{
		setOnlyBlockWorldWidthAndHeight(x * 6, y * 6);
		setOnlyBlockTextureWidthAndHeight(x * 16, y * 16);
		return this;
	}
	
	protected Block setOnlyBlockWorldWidthAndHeight(int w, int h)
	{
		blockWidth = w;
		blockHeight = h;
		hasMetaData = true;
		//metaData = MetaDataHelper.getBlockMetaDataId(w, h);		
		return this;
	}
	
	protected Block setOnlyBlockTextureWidthAndHeight(int w, int h)
	{
		textureWidth = w;
		textureHeight = h;
		return this;
	}
	
	protected Block setBlockTier(int i)
	{
		blockTier = i;
		return this;
	}
	
	protected Block setBlockType(int i)
	{
		blockType = i;
		return this;
	}
	// Set if the block allows blocks to be placed by it
	protected Block setIsSolid(boolean flag){
		isSolid = flag;
		return this;
	}
	
	protected Block setHRange(int i){
		hRange = i;
		return this;
	}
	
	protected Block setLRange(int i){
		lRange = i;
		return this;
	}	
	
	protected Block setDroppedItem(ItemStack stack, int min, int max)
	{
		droppedItem = stack;
		minimumDropAmount = min;
		maximumDropAmount = max;
		return this;
	}
	
	protected Block setBitMap(int i){
		bitMap = i;
		//If the block is a general case
		if (this.getTileMap() == 'g'){
			if (i <= 15){
				this.setIconIndex(i, this.iconY);
			}
			else{
				this.setIconIndex(i - 16, this.iconY + 1);
			}
		}
		//If the block is a pillar
		else if (this.getTileMap() == 'p'){
			this.setIconIndex(i, this.iconY);
		}
		//If the block is a tree
		else if (this.getTileMap() == 't'){
			this.setIconIndex(i, this.iconY);
		}
		//If the block is a branch
		else if(this.getTileMap() == 'b'){
			//If the branch is a regular branch
			if (i <= 11){
				this.setIconIndex(4 + i, this.iconY);
			}
			//If the branch is covered in snow
			else{
				this.setIconIndex(4 + (i - 12), this.iconY + 1);
			}
		}
		//If the block is a treetop
		else if (this.getTileMap() == 'T'){
			this.setIconIndex( this.iconX + 3*i, this.iconY);
		}
		return this;
	}
	
	protected Block setTileMap(char c){
		tileMap = c;
		return this;
	}
	
	protected Block setIsOveridable(boolean flag) 
	{
		isOveridable = flag;
		return this;
	}
	
	public int getBlockID()
	{
		return blockID;
	}

	public String getBlockName()
	{
		return blockName;
	}
	
	public float getBlockHardness()
	{
		return hardness;
	}
	
	public boolean getBreakable()
	{
		return breakable;
	}
	
	public int getIconIndex()
	{
		return iconIndex;
	}
	
	public String getExtraTooltipInformation()
	{
		return extraTooltipInformation;
	}
		
	public EnumBlockMaterial getMaterial()
	{
		return material;
	}
		
	public boolean getPassable()
	{
		return passable;
	}
	
	public int getMaxStackSize()
	{
		return maxStackSize;
	}

	public int getBlockWidth()
	{
		return blockWidth;
	}
	
	public int getBlockHeight()
	{
		return blockHeight;
	}
	
	public double getTextureWidth()
	{
		return textureWidth;
	}
	
	public double getTextureHeight()
	{
		return textureHeight;
	}	
	
	public int getGradeOfToolRequired()
	{
		return gradeOfToolRequired;
	}
	
	public int getBlockTier()
	{
		return blockTier;
	}
	
	public int getBlockType()
	{
		return blockType;
	}
	
	public boolean getIsOveridable()
	{
		return isOveridable;
	}
	
	public int getHRange(){
		return hRange;
	}
	
	public int getLRange(){
		return lRange;
	}
	
	public boolean isPassable() //Is it possible to walk through this block? 
	{
		return (blockID == 0 || passable || this == null);
	}
	
	public int getBitMap(){
		return bitMap;
	}
	
	public char getTileMap(){
		return tileMap;
	}
	public ItemStack getDroppedItem()
	{
		return (droppedItem != null) ? new ItemStack(droppedItem.getItemID(), (minimumDropAmount + (((maximumDropAmount - minimumDropAmount) > 0) ? random.nextInt(maximumDropAmount - minimumDropAmount) : 0))) : null;
	}
	
	public boolean getIsSolid(){
		return isSolid;
	}
		
	public static final Block[] blocksList = new Block[2048];
	/** Block Declarations **/
	
	public static Block air = new Block();
	public static Block dirt = new Block(1).setBlockName("Dirt").setTileMap('g').setBlockHardness(40.0f).setIconIndex(0, 3).setBlockType(1);
	public static Block stone = new Block(2).setBlockName("Stone").setTileMap('g').setBlockHardness(70.0f).setIconIndex(0, 0).setBlockType(1);
	public static Block grass = new BlockGrass(3).setBlockName("Grass").setTileMap('g').setBlockHardness(40.0f).setIconIndex(0, 4).setBlockType(1);
	public static Block sand = new Block(6).setBlockName("Sand").setTileMap('g').setBlockHardness(30.0f).setIconIndex(0, 2).setBlockType(1);
	public static Block sandstone = new Block(7).setBlockName("Sandstone").setTileMap('g').setIconIndex(0,1).setBlockHardness(70.0f).setBlockType(1);
	public static Block cactus = new Block(8).setBlockName("Cactus").setBlockHardness(1.0f).setIconIndex(2, 3).setPassable(true).setBlockType(2).setIsSolid(false);
	public static Block tree = new BlockWood(9).setBlockName("Tree").setTileMap('t').setBlockHardness(60.0f).setIconIndex(1, 24).setPassable(true).setBlockType(2).setIsSolid(false);
	public static Block treebase = new BlockWood(10).setBlockName("tree base").setTileMap('t').setBlockHardness(60.0f).setIconIndex(0, 24).setPassable(true).setBlockType(2).setIsSolid(false);
	public static Block treebranch = new BlockWood(12).setBlockName("tree branch").setTileMap('b').setBlockHardness(5.0f).setIconIndex(4, 22).setPassable(true).setIsSolid(false);
	public static Block treetop = new BlockLeaves(20).setBlockName("tree top left top").setTileMap('T').setBlockHardness(5.0f).setIconIndex(4, 24).setPassable(true).setIsSolid(false);
	public static Block treetopl2 = new BlockLeaves(21).setBlockName("tree top left bottom").setTileMap('T').setBlockHardness(5.0f).setIconIndex(4, 25).setPassable(true).setIsSolid(false);
	public static Block treetopc1 = new BlockLeaves(22).setBlockName("tree top center top").setTileMap('T').setBlockHardness(5.0f).setIconIndex(5, 24).setPassable(true).setIsSolid(false);
	public static Block treetopc2 = new BlockLeaves(23).setBlockName("tree top center bottom").setTileMap('T').setBlockHardness(5.0f).setIconIndex(5, 25).setPassable(true).setIsSolid(false);
	public static Block treetopr1 = new BlockLeaves(24).setBlockName("tree top right top").setTileMap('T').setBlockHardness(5.0f).setIconIndex(6, 24).setPassable(true).setIsSolid(false);
	public static Block treetopr2 = new BlockLeaves(25).setBlockName("tree top right bottom").setTileMap('T').setBlockHardness(5.0f).setIconIndex(6, 25).setPassable(true).setIsSolid(false);
	public static Block gold = new BlockOre(26).setBlockName("Bold Ore Block").setBlockHardness(10.0f).setHRange(0).setLRange(3).setIconIndex(15, 12).setBlockType(1).setDroppedItem(new ItemStack(Item.goldOre), 1,1);
	public static Block iron = new BlockOre(27).setBlockName("Iron Ore Block").setBlockHardness(10.0f).setHRange(5).setLRange(10).setIconIndex(15, 8).setBlockType(1).setDroppedItem(new ItemStack(Item.ironOre), 1,1);
	public static Block coal = new BlockOre(28).setBlockName("Coal Ore Block").setTileMap('g').setHRange(20).setLRange(10).setBlockHardness(10.0f).setIconIndex(15,7).setBlockType(1).setDroppedItem(new ItemStack(Item.coal), 1,1);
	public static Block diamond = new BlockOre(29).setBlockName("Diamond Ore Block").setBlockHardness(10.0f).setHRange(0).setLRange(1).setIconIndex(15, 15).setBlockType(1).setDroppedItem(new ItemStack(Item.diamond), 1,1);
	public static Block copper = new BlockOre(30).setBlockName("Copper Ore Block").setBlockHardness(55.0f).setHRange(20).setLRange(0).setIconIndex(15,10).setBlockType(1).setDroppedItem(new ItemStack(Item.copperOre), 1,1);
	public static Block silver = new BlockOre(31).setBlockName("Silver Ore Block").setBlockHardness(80.0f).setHRange(0).setLRange(5).setIconIndex(15, 11).setBlockType(1).setDroppedItem(new ItemStack(Item.silverOre), 1,1);
	public static Block tin = new BlockOre(32).setBlockName("Tin Ore Block").setBlockHardness(55.0f).setHRange(10).setLRange(0).setIconIndex(15, 9).setBlockType(1).setDroppedItem(new ItemStack(Item.tinOre), 1,1);
	public static Block redflower = new Block(33).setBlockName("Red Flower").setBlockHardness(10.0f).setIconIndex(0, 1).setPassable(true).setIsOveridable(true).setIsSolid(false);
	public static Block yellowflower = new Block(34).setBlockName("Yellow Flower").setBlockHardness(10.0f).setIconIndex(0, 1).setPassable(true).setIsOveridable(true).setIsSolid(false);
	public static Block tallgrass = new Block(35).setBlockName("Tall Grass").setBlockHardness(10.0f).setIconIndex(0, 1).setPassable(true).setIsOveridable(true).setIsSolid(false);
	public static Block snowCover = new Block(44).setBlockName("Snow Cover").setBlockHardness(10.0f).setIconIndex(9, 10).setPassable(true).setIsOveridable(true).setDroppedItem(new ItemStack(Item.snowball), 1, 1).setIsSolid(false);
	public static Block torch = new BlockLight(48).setLightStrengthAndRadius(1.0F, 10).setBlockName("Torch").setBlockHardness(0.0f).setIconIndex(0, 0).setPassable(true).setIsSolid(false);	
	public static Block adminium = new Block(49).setBlockName("Adminium").setBlockHardness(8000.0f).setIconIndex(0,1);
	public static Block plank = new Block(50).setBlockName("Plank").setTileMap('g').setBlockHardness(30.0f).setIconIndex(0, 17).setBlockType(2);
	public static Block sapling = new Block(51).setBlockName("Sapling").setBlockHardness(5.0f).setIconIndex(10, 4).setBlockType(2).setIsOveridable(true).setIsSolid(false); 
	public static Block furnace = new Block(52).setBlockName("Furnace").setBlockHardness(50.0f).setIconIndex(12, 2).setBlockType(1).setBothBlockWidthAndHeight(2, 2);
	public static Block craftingTable = new Block(53).setBlockName("Crafting table").setBlockHardness(50.0f).setIconIndex(11, 3).setBlockType(2).setBothBlockWidthAndHeight(2, 2);
	public static Block snow = new Block(54).setBlockName("Snow Block").setBlockHardness(10.0f).setIconIndex(4, 1).setBlockType(1).setIsOveridable(true);
	public static Block heartCrystal = new Block(55).setBlockName("Heart Crystal Block").setBlockHardness(50.0f).setIconIndex(1,1).setBlockType(3).setBothBlockWidthAndHeight(2,2).setDroppedItem(new ItemStack(Item.heartCrystal), 1, 1);
	public static Block chest = new BlockChest(56).setBlockName("Chest").setBothBlockWidthAndHeight(2, 2).setBlockHardness(40.0f).setIconIndex(11, 1).setBlockType(2);

	//Needs recipe
	public static Block bookshelf = new Block(57).setBlockName("Bookshelf").setIconIndex(4, 3).setBlockHardness(20.0f).setBlockType(2);
	public static Block woodTable = new Block(58).setBlockName("Wooden Table").setPassable(true).setBlockHardness(50.0f).setIconIndex(11, 1).setBlockType(2).setBothBlockWidthAndHeight(2, 1);
	public static Block stoneTable = new Block(59).setBlockName("Stone Table").setPassable(true).setBlockHardness(50.0f).setIconIndex(11, 0).setBlockType(1).setBothBlockWidthAndHeight(2, 1);
	public static Block fenceLeft = new Block(60).setBlockName("fence end left").setBlockHardness(50.0f).setIconIndex(2, 4).setBlockType(2);
	public static Block fence = new Block(61).setBlockName("Fence").setPassable(true).setBlockHardness(50.0f).setIconIndex(3, 4).setBlockType(2);
	public static Block fenceRight = new Block(62).setBlockName("fence end right").setBlockHardness(50.0f).setIconIndex(4, 4).setBlockType(2);
	public static Block woodChairRight = new Block(63).setBlockName("Right facing wood chair").setPassable(true).setBlockHardness(50.0f).setIconIndex(13,1).setBlockType(2);
	public static Block woodChairLeft = new Block(64).setBlockName("Left facing wood chair").setPassable(true).setBlockHardness(50.0f).setIconIndex(14,1).setBlockType(2);
	public static Block stoneChairRight = new Block(65).setBlockName("Right facing stone chair").setPassable(true).setBlockHardness(50.0f).setIconIndex(13,0).setBlockType(1);
	public static Block stoneChairLeft = new Block(66).setBlockName("Left facing stone chair").setPassable(true).setBlockHardness(50.0f).setIconIndex(14,0).setBlockType(1);
		
	public static Block stonePillar = new BlockPillar(68).setBlockName("Stone Pillar Block").setTileMap('p').setPassable(true).setBlockHardness(75.0f).setIconIndex(0,20).setBlockType(1);
	public static Block marblePillar = new BlockPillar(71).setBlockName("Marble Pillar Block").setTileMap('p').setPassable(true).setBlockHardness(75.0f).setIconIndex(0,21).setBlockType(1);
	public static Block goldPillar = new BlockPillar(74).setBlockName("Gold Pillar Block").setTileMap('p').setPassable(true).setBlockHardness(75.0f).setIconIndex(0,22).setBlockType(1);
	public static Block diamondPillar = new BlockPillar(77).setBlockName("Diamond Pillar Block").setTileMap('p').setPassable(true).setBlockHardness(75.0f).setIconIndex(0,23).setBlockType(1);
	
	public static Block glass = new Block(79).setBlockName("Glass Block").setBlockHardness(10.0f).setIconIndex(1, 3).setBlockType(1);
	
	//Backwalls
	public static Block backAir = new BlockBackWall(128).setIconIndex(3, 0).setBlockName("backwall air");
	public static Block backDirt = new BlockBackWall(129).setBlockName("backwall dirt");
	
}
