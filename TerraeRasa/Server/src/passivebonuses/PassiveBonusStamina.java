package passivebonuses;

import entities.EntityPlayer;
/**
 * PassiveBonusStamina extends PassiveBonus to increase the player's stamina by a given amount. This is a fixed amount, indicated
 * by the power value. 1 Power = +1 stamina (etc) 
 * @author      Alec Sobeck
 * @author      Matthew Robertson
 * @version     1.0
 * @since       1.0
 */
public class PassiveBonusStamina extends PassiveBonus
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PassiveBonusStamina(double power) 
	{
		super(power);
	}

	public void apply(EntityPlayer player) 
	{
		player.stamina += power;
	}

	public void remove(EntityPlayer player) 
	{
		player.stamina -= power;
	}

	public String toString()
	{
		return "+" + (int)power + " Stamina";
	}
}
