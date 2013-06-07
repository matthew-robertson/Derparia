package setbonus;

import entities.EntityLivingPlayer;

public class SetBonusDefense extends SetBonus
{
	protected SetBonusDefense(float power) 
	{
		super(power);
	}

	public void apply(EntityLivingPlayer player) 
	{
		player.defense += power;
	}

	public void remove(EntityLivingPlayer player) 
	{
		player.defense -= power;
	}

}