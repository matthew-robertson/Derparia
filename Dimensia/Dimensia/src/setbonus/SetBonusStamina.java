package setbonus;

import entities.EntityLivingPlayer;

public class SetBonusStamina extends SetBonus
{
	public SetBonusStamina(float power) 
	{
		super(power);
	}

	public void apply(EntityLivingPlayer player) 
	{
		player.stamina += power;
	}

	public void remove(EntityLivingPlayer player) 
	{
		player.stamina -= power;
	}

}
