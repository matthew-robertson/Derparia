package items;


public class ItemTool extends Item
{
	/** Damage the weapon does */
	public int damageDone; 	
	public double[] xBounds;
	public double[] yBounds;
	//Render size - ortho (1/2 pixel)
	public double size;
	public double swingSpeed;
		
	protected ItemTool(int i)
	{
		super(i);
		damageDone = 1;
		maxStackSize = 1;
		setXBounds(new double[] { 13F/16, 16F/16, 16F/16, 4F/16, 1F/16 });
		setYBounds(new double[] { 1F/16, 1F/16 ,  4F/16, 16F/16, 13F/16 });		
		size = 20;
		swingSpeed = 1.0D;
	}
	
	/**
	 * -- In sec
	 * @param speed
	 * @return
	 */
	protected ItemTool setSwingSpeed(double speed)
	{
		this.swingSpeed = speed;
		return this;
	}
		
	protected ItemTool setSize(int size)
	{
		this.size = size;
		return this;
	}
	
	protected ItemTool setDamageDone(int i)
	{
		damageDone = i;
		return this;
	}
	
	public int getDamageDone()
	{
		return damageDone;
	}
	
	protected ItemTool setXBounds(double[] bounds)
	{
		this.xBounds = bounds;
		return this;
	}
	
	protected ItemTool setYBounds(double[] bounds)
	{
		this.yBounds = bounds;
		return this;
	}
	
	/**
	 * Gets the stats of the given item. ItemTools should have at least a damage value.
	 * @return an array of this ItemTool's stats, which should be at least of size 1
	 */
	public String[] getStats()
	{
		return new String[] { "Damage: "+damageDone };
	}
}