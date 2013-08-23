package items;

import world.World;
import audio.SoundEngine;
import entities.EntityPlayer;

public class ItemPotionSteelSkin extends ItemPotion
{
	public ItemPotionSteelSkin(int i, int duration, int tier, int power, int ticksBetweenEffect) 
	{
		super(i, duration, tier, power, ticksBetweenEffect);
	}
	
	public void onRightClick(World world, EntityPlayer player)
	{
		SoundEngine.playSoundEffect(onUseSound);
	}
}
