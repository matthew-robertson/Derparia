package auras;

import server.entities.EntityPlayer;
import world.World;
import entry.MPGameLoop;

/**
 * AuraSmartHeal extends Aura to implement healing to the player below a certain threshold. When the player's health is below a certain 
 * amount, and the aura is not on cooldown, they will be healed for the given amount - which may be percentile. 
 * @author      Alec Sobeck
 * @author      Matthew Robertson
 * @version     1.0
 * @since       1.0
 */
public class AuraSmartHeal extends Aura 
{
	private static final long serialVersionUID = 1L;
	/** Indicates whether or not the health regenerated is based on maximum health percent, or a flat value (true indicates a percent). */
	private boolean percentile;
	/** The amount healed by this aura. */
	private double healAmount;
	/** The activation threshold, a percentage based from 0.0D to 1.0D. */
	private double activationThreshold;
	
	/**
	 * Constructs a new smart heal aura.
	 * @param cooldownSeconds the cooldown of the aura in seconds
	 * @param healAmount a flat amount; or value from 0.0-1.0 if healing a percentage
	 * @param activationThreshold a value from 0.0-1.0 indicating the percentage of health to activate at
	 * @param percentile true if healing a percent of maximum health; otherwise false
	 */
	public AuraSmartHeal(int cooldownSeconds, double healAmount, double activationThreshold, boolean percentile)
	{
		super();
		maxCooldown = cooldownSeconds * MPGameLoop.TICKS_PER_SECOND;
		this.percentile = percentile;
		this.activationThreshold = activationThreshold;
		this.healAmount = healAmount;		
	}
	
	public void onPercentageHealth(World world, EntityPlayer player)
	{
		if(remainingCooldown <= 0 && player.getHealth() > 0 && (player.getHealthPercent() <= activationThreshold))
		{
			double heal = (percentile) ? (this.healAmount * player.maxHealth) : this.healAmount;
			player.heal(world, (int)heal, true);
			remainingCooldown = maxCooldown;
		}
	}
	
	public String toString()
	{
		return "Heals for " + (int)healAmount + " below " + (int)(100 * activationThreshold) + "%"; 
	}
}
