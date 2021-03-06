package passivebonuses;

import java.io.Serializable;

import server.entities.EntityPlayer;


/**
 * PassiveBonus is a fairly lightweight abstract base class for all PassiveBonus needs. A PassiveBonus is a static benefit to a stat
 * or something else in the player class that can be reversed. It does not respond to events. All subclasses of PassiveBonus
 * must implement {@link #apply(EntityPlayer)} and {@link #remove(EntityPlayer)} which will apply the set
 * bonus and then later remove it. These should directly counter each other and comply with stat-stacking rules.
 * @author      Alec Sobeck
 * @author      Matthew Robertson
 * @version     1.0
 * @since       1.0
 */
public abstract class PassiveBonus 
		 implements IPassiveBonus, Serializable
{
	private static final long serialVersionUID = 1L;
	/** A value that determines the strength of a set bonus. Effects vary by actual bonus. */
	protected double power;
	/** The number of pieces required to activate this set bonus. */
	protected int piecesRequiredToActivate;
	
	/**
	 * Only a subclass of PassiveBonus can use this constructor because this class requires subclassing.
	 * @param power the strength of the PassiveBonus, varying by effect
	 */
	protected PassiveBonus(double power)
	{
		this.power = power;
		this.piecesRequiredToActivate = 1;
	}

	/**
	 * Only a subclass of PassiveBonus can use this constructor because this class requires subclassing.
	 * @param power the strength of the PassiveBonus, varying by effect
	 * @param piecesRequiredToActivate the number of pieces required to activate this set bonus
	 */
	protected PassiveBonus(double power, int piecesRequiredToActivate)
	{
		this.power = power;
		this.piecesRequiredToActivate = piecesRequiredToActivate;
	}
	
	/**
	 * Gets the pieces of armour required to activate this PassiveBonus. This is generally one and has little effect unless this is a set bonus.
	 * @return the number of armour pieces required to activate this PassiveBonus
	 */
	public int getPiecesRequiredToActivate()
	{
		return piecesRequiredToActivate;
	}
	
	/**
	 * Sets the number of pieces required to activate this PassiveBonus. This may or may not have a substancial effect.
	 * @param pieces the number of pieces required to activate this PassiveBonus
	 * @return a reference to this PassiveBonus
	 */
	public PassiveBonus setPiecesRequiredToActivate(int pieces)
	{
		this.piecesRequiredToActivate = pieces;
		return this;
	}

	/**
	 * Converts the PassiveBonus or extension thereof into a readable plaintext form.
	 */
	public String toString()
	{
		return "PassiveBonus: Effect=None";
	}
	
	/**
	 * Gets the power of this PassiveBonus.
	 * @return the power of this PassiveBonus
	 */
	public double getPower()
	{
		return power;
	}
	
	/**
	 * Gets this PassiveBonus as a DisplayablePassiveBonus.
	 * @return a DisplayablePassiveBonus corresponding to this PassiveBonus instance
	 */
	public DisplayablePassiveBonus getAsDisplayable()
	{
		return new DisplayablePassiveBonus(this);
	}
}
