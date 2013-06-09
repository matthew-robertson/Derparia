package setbonus;

import entities.EntityLivingPlayer;
/**
 * SetBonusStamina extends SetBonus to increase the player's stamina by a given amount. This is a fixed amount, indicated
 * by the power value. 1 Power = +1 stamina (etc) 
 * @author      Alec Sobeck
 * @author      Matthew Robertson
 * @version     1.0
 * @since       1.0
 */
public class SetBonusStamina extends SetBonus
{
	private static final long serialVersionUID = 1L;

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
