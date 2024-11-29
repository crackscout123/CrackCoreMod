/**
 * @author crackscout
 *
 *  Nov 29, 2024 3:52:40 AM
 */
package eu.crackscout.crackcore.events;



import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GeneralEventHandler {
	
	 @SubscribeEvent
	    public void pickupItem(EntityItemPickupEvent event) {
	        System.out.println("Item picked up!");
	    }
	 
	 
}
	
	

