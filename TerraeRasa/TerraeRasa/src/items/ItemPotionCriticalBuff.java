package items;

import statuseffects.StatusEffectCriticalBuff;
import world.World;
import audio.SoundEngine;
import entities.EntityPlayer;

public class ItemPotionCriticalBuff extends ItemPotion
{
	public ItemPotionCriticalBuff(int i, int duration, int tier, double power, int ticksBetweenEffect) 
	{
		super(i, duration, tier, power, ticksBetweenEffect);
	}
	
	public void onRightClick(World world, EntityPlayer player)
	{
		player.registerStatusEffect(world, new StatusEffectCriticalBuff(durationSeconds, tier, power, ticksBetweenEffect));
		player.inventory.removeItemsFromInventoryStack(player, 1, player.selectedSlot);
		SoundEngine.playSoundEffect(onUseSound);
	}
}
