package setbonus;

import entities.EntityLivingPlayer;

public class SetBonusDodge extends SetBonus
{
	protected SetBonusDodge(float power) 
	{
		super(power);
	}

	public void apply(EntityLivingPlayer player) 
	{
		player.dodgeChance += power;
	}

	public void remove(EntityLivingPlayer player) 
	{
		player.dodgeChance -= power;
	}

}