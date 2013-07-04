package passivebonuses;

import entities.EntityPlayer;

/**
 * PassiveBonusManaBoost extends PassiveBonus to increase a player's maximum mana without increasing their intellect. 
 * The increase is based on the power value of the PassiveBonus, where the power increases the player's mana point for point.
 * IE 1 power = 1 mana.
 * @author      Alec Sobeck
 * @author      Matthew Robertson
 * @version     1.0
 * @since       1.0
 */
public class PassiveBonusManaBoost extends PassiveBonus
{

	protected PassiveBonusManaBoost(double power) 
	{
		super(power);
	}
	
	public void apply(EntityPlayer player) 
	{
		player.maxMana += power;
	}

	public void remove(EntityPlayer player) 
	{
		player.maxHealth -= power;
	}
}