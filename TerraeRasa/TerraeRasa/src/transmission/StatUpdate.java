package transmission;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;

import statuseffects.DisplayableStatusEffect;
import utils.Cooldown;

/**
 * a data structure that sends everything relating to stats back to the player.
 * Well, anything they need to see anyway. Turns out the client doesnt actually need 
 * to know that much.
 *
 */
public class StatUpdate
		implements Serializable
{
	private static final long serialVersionUID = 1L;
	public int entityID;
	public double defense;
	public double mana;
	public double maxMana;
	public double health;
	public double maxHealth;
	public double specialEnergy;
	public double maxSpecialEnergy;
	public boolean isSwingingRight;
	public boolean hasSwungTool;
	public double rotateAngle;
	public List<DisplayableStatusEffect> statusEffects;
	public Hashtable<String, Cooldown> cooldowns;
	public boolean defeated;
}
