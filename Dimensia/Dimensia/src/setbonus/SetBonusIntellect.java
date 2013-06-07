package setbonus;

import entities.EntityLivingPlayer;

public class SetBonusIntellect extends SetBonus
{
	protected SetBonusIntellect(float power) 
	{
		super(power);
	}

	public void apply(EntityLivingPlayer player) 
	{
		player.intellect += power;
	}

	public void remove(EntityLivingPlayer player) 
	{
		player.intellect -= power;
	}

}