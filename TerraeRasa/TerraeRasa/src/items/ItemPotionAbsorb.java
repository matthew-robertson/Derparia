package items;

import statuseffects.StatusEffectAbsorb;
import world.World;
import audio.SoundEngine;
import entities.EntityPlayer;

public class ItemPotionAbsorb extends ItemPotion
{
	public ItemPotionAbsorb(int i, int duration, int tier, int power, int ticksBetweenEffect) 
	{
		super(i, duration, tier, power, ticksBetweenEffect);
	}
	
	public void onRightClick(World world, EntityPlayer player)
	{
		if(!player.isOnCooldown(id))
		{
			player.registerStatusEffect(world, new StatusEffectAbsorb(durationSeconds, tier, (int)power));
			player.inventory.removeItemsFromInventoryStack(player, 1, player.selectedSlot);		
			player.putOnCooldown(id, 600);
			SoundEngine.playSoundEffect(onUseSound);
		}		
	}
}
