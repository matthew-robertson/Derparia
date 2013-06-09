package setbonus;

import entities.EntityLivingPlayer;

/**
 * SetBonusJumpHeight extends SetBonus to increase the player's jump height by a given amount. This is a fixed amount, indicated
 * by the power value. 1 Power = +1 Block of jump height (etc) 
 * @author      Alec Sobeck
 * @author      Matthew Robertson
 * @version     1.0
 * @since       1.0
 */
public class SetBonusJumpHeight extends SetBonus
{
	private static final long serialVersionUID = 1L;

	public SetBonusJumpHeight(float power) 
	{
		super(power);
	}

	public void apply(EntityLivingPlayer player) 
	{
		player.setUpwardJumpHeight(player.getUpwardJumpHeight() + power);
	}

	public void remove(EntityLivingPlayer player) 
	{
		player.setUpwardJumpHeight(player.getUpwardJumpHeight() - power);
	}

	public String toString()
	{
		return "+" + (int)power + " Jump Height";
	}
}
