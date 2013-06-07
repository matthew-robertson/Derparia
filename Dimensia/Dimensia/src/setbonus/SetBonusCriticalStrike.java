package setbonus;

import entities.EntityLivingPlayer;

public class SetBonusCriticalStrike extends SetBonus
{
	protected SetBonusCriticalStrike(float power) 
	{
		super(power);
	}

	public void apply(EntityLivingPlayer player) 
	{
		player.criticalStrikeChance += power;
	}

	public void remove(EntityLivingPlayer player) 
	{
		player.criticalStrikeChance -= power;
	}

}