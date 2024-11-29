/**
 * @author crackscout
 *
 *  Nov 29, 2024 3:52:40 AM
 */
package eu.crackscout.crackcore.utils;

import net.minecraft.world.entity.player.Player;

public class Resolver {

	public static boolean isAdminPlayer(Player p) {
		if(p.isCrouching()) {
			if(p.isCreative() || p.isSpectator()) {
				return true;
			}
		}
		return false;
	}
	
}
