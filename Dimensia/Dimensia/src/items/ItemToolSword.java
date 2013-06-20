package items;

public class ItemToolSword extends ItemTool
{
	protected ItemToolSword(int i)
	{
		super(i, "New Item");
		setXBounds(new double[] { 13F/16, 16F/16, 16F/16, 4F/16, 1F/16 });
		setYBounds(new double[] { 1F/16, 1F/16 ,  4F/16, 16F/16, 13F/16 });	
	}
}
