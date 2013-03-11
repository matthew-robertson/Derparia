package net.dimensia.src;

public class EntityLivingNPC extends EntityLiving 
{
	private static final long serialVersionUID = 1L;
	protected String name;
	protected int npcID;
	protected int iconIndex;
	protected Texture texture;
	protected String[] speech;
	protected int i;
	
	public EntityLivingNPC (int i, String s){
		super();
		name = s;
		npcID = i;
		blockWidth = 2;
		blockHeight = 3;
		textureWidth = 16;
		textureHeight = 16;
		maxHealth = 1;
		health = 1;
		width = 12;
		height = 18;
		ticksSinceLastWander = 0;
		baseSpeed = 2.5f;
		speech = new String[10];
		
		if (npcList[i] != null){
			throw new RuntimeException("NPC already exists @" + i);
		}		
		npcList[i] = this;
	}
	
	public EntityLivingNPC(EntityLivingNPC entity)
	{
		super(entity);

		this.name = entity.name;
		this.npcID = entity.npcID;
		this.iconIndex = entity.iconIndex;
		this.texture = entity.texture;
		this.speech = entity.speech;
		this.i = entity.i;
	}
		
	/**
	 * Basic method to control AI. If the NPC isn't stunned, will chase the player if alert. Will wander otherwise. Gravity is applied 
	 * after any actions are taken
	 * @param world - current world
	 * @param target - entity to chase/retreat from
	 */
	public void applyAI(World world, EntityLivingPlayer target){
		if(!isStunned){
			if (alert){
				AIManager.AIChaseAndRetreat(world, this, target, true );
			}
			else {
				AIManager.AIWander(world, this);
			}
		}
		
		applyGravity(world);	
	}	
	
	/**
	 * triggers when the actor approaches a player.
	 * Currently only makes them speak and rotates through their lines
	 */
	public void onPlayerNear(){
		i++;
		if (i >= speech.length){
			i = 0;
		}
		else if (speech[i] == null){
			i = 0;
		}
		AIManager.AISpeech(speech[i]);
	}		
	
	//Setters
	
	protected EntityLivingNPC setName(String s){
		name = s;
		return this;
	}
	
	protected EntityLivingNPC setWidthandHeight(int x, int y)
	{
		width = x;
		height = y;
		return this;
	}
	
	protected EntityLivingNPC setTexture(Texture t){
		texture = t;
		return this;
	}
	
	protected EntityLivingNPC setSpeech(String[] s){
		speech = s;
		return this;
	}
	
	protected EntityLivingNPC setMaxHealth(int i)
	{
		maxHealth = i;
		health = i;
		return this;
	}
		
	protected EntityLivingNPC setWorldDimensions(int i, int j)
	{
		width = i;
		height = j;
		return this;
	}
	
	protected EntityLivingNPC setBlockDimensions(int i, int j)
	{
		blockWidth = i;
		blockHeight = j;
		return this;
	}
	
	protected EntityLivingNPC setBlockAndWorldDimensions(int i, int j)
	{
		setWorldDimensions(i * 6, j * 6);
		setBlockDimensions(i, j);
		return this;
	}
	
	protected EntityLivingNPC setIconIndex(int i, int j)
	{
		iconIndex = i * 16 + j;
		return this;
	}
	
	protected EntityLivingNPC setTextureDimensions(int i, int j)
	{
		textureWidth = i;
		textureHeight = j;
		return this;
	}
	
	protected EntityLivingNPC setBaseSpeed(float f)
	{
		baseSpeed = f;
		return this;
	}
	
	public EntityLivingNPC setUpwardJumpHeight(int i){
		upwardJumpHeight = i;
		return this;
	}
	
	public EntityLivingNPC setJumpSpeed(float f){
		jumpSpeed = f;
		return this;
	}
	
	public EntityLivingNPC setIsAlert(boolean bool){
		alert = bool;
		return this;
	}
	
	//getters
	
	public String getName(){
		return name;
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
	
	public float getBlockWidth()
	{
		return blockWidth;
	}
	
	public float getBlockHeight()
	{
		return blockHeight;
	}
	
	public float getUpwardJumpHeight(){
		return upwardJumpHeight;
	}
	
	public float getJumpSpeed(){
		return jumpSpeed;
	}
	
	private EntityLivingNPC[] npcList = new EntityLivingNPC[5];
	//npc declarations
	public static EntityLivingNPC test = new EntityLivingNPC(0, "Test").setIsAlert(true).setTexture(Render.dino).setBaseSpeed(2.5f).setUpwardJumpHeight(30).setJumpSpeed(6).setSpeech(new String[] { "Hai!", "I am a test NPC.", "I can't give you any quests."});
}
