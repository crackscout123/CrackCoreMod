/**
 * @author rzepk
 *
 *  Nov 29, 2024 5:03:10 AM
 */

package eu.crackscout.crackcore.events;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import eu.crackscout.crackcore.CrackCoreMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
//import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@SuppressWarnings("preview")
public class ChestLogger {


    private final Map<ServerPlayer, ItemStack[]> trackedInventories = new HashMap<>();

    public ChestLogger() {
        // Event-Handler registrieren
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Event, wenn ein Spieler mit einer Kiste interagiert.
     */
//	@SubscribeEvent
//    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
//        // Sicherstellen, dass die Logik nur auf der Serverseite läuft
//        if (!event.getLevel().isClientSide) {
//            BlockPos pos = event.getPos();
//            var world = event.getLevel();
//            var blockState = world.getBlockState(pos);
//
//            // Prüfen, ob der Block eine normale oder gefangene Kiste ist
//            if (blockState.is(Blocks.CHEST) || blockState.is(Blocks.TRAPPED_CHEST)) {
//                var player = event.getEntity();
//
//                // Nur Spieler auf dem Server protokollieren (Multiplayer)
//                if (player instanceof ServerPlayer serverPlayer) {
//                    logToFile(serverPlayer.getName().getString(), "Interacted with chest", pos.getX(), pos.getY(), pos.getZ());
//                }
//            }
//        }
//    }

    // Check placement of: CHEST / TRAPPED CHEST, HOPPER, BARREL, SHULKER, FRUNANCE
	@SubscribeEvent
    public void onBlockInventoryChange(BlockEvent.EntityPlaceEvent event) {
        // Stelle sicher, dass dies serverseitig ausgeführt wird
        if (!event.getLevel().isClientSide()) {
            var entity = event.getEntity();
            var blockState = event.getPlacedBlock();
            var pos = event.getPos();

            if (entity instanceof ServerPlayer serverPlayer) {
            	// CHEST / TRAPPED CHEST
	            if (blockState.is(Blocks.CHEST) || blockState.is(Blocks.TRAPPED_CHEST)) {
	            	logToFile(serverPlayer.getName().getString(), "Placed a chest", pos.getX(), pos.getY(), pos.getZ());
	            }
	            // TODO:
	            // HOPPER       
	            // BAREL  
	            // SHULKER
	            // FURNACE 

            }
        }
    }	
	
	@SubscribeEvent
	public void onChestBreak(BlockEvent.BreakEvent event) {
	    // Sicherstellen, dass das Event auf der Serverseite läuft
	    if (!event.getLevel().isClientSide()) {
	        var blockState = event.getState();
	        var pos = event.getPos();

	        // Überprüfen, ob der zerstörte Block eine normale oder gefangene Kiste ist
	        if (blockState.is(Blocks.CHEST) || blockState.is(Blocks.TRAPPED_CHEST)) {
	            if (event.getPlayer() instanceof ServerPlayer serverPlayer) {
	                logToFile(
	                    serverPlayer.getName().getString(),
	                    "Destroyed chest", pos.getX(), pos.getY(), pos.getZ());
	            }
	        }
	    }
	}

    
    /**
     * Event wenn Spieler Ksite öffnet 
     */
	@SubscribeEvent
    public void onContainerOpen(PlayerContainerEvent.Open event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            AbstractContainerMenu menu = event.getContainer();

            // CHECK CHEST
            if (menu instanceof ChestMenu chestMenu) {
                // Kisteninventar initial speichern
                trackedInventories.put(player, copyInventory(chestMenu));
                logToFile(player.getName().getString(), "Opened chest", (int)getBlockPos(player).x, (int)getBlockPos(player).y, (int)getBlockPos(player).z);
            }
        }
    }

    /**
     * Event, wenn ein Spieler eine Kiste schließt.
     */
	@SubscribeEvent
    public void onContainerClose(PlayerContainerEvent.Close event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            AbstractContainerMenu menu = event.getContainer();

            // Nur Kisten überprüfen
            if (menu instanceof ChestMenu chestMenu) {
                // Vergleiche Änderungen
                ItemStack[] previousInventory = trackedInventories.remove(player);
                if (previousInventory != null) {
                    logInventoryChanges(player, previousInventory, chestMenu);
                }
            }
        }
    }

    /**
     * Protokolliere Änderungen im Inventar der Kiste.
     */
    private void logInventoryChanges(ServerPlayer player, ItemStack[] oldInventory, ChestMenu chestMenu) {
        ItemStack[] currentInventory = copyInventory(chestMenu);

        for (int i = 0; i < oldInventory.length; i++) {
            ItemStack oldStack = oldInventory[i];
            ItemStack currentStack = currentInventory[i];

            if (!ItemStack.isSameItemSameTags(oldStack, currentStack)) {
                int difference = currentStack.getCount() - oldStack.getCount();
                String action = difference > 0 ? "Added" : "Removed";
                int count = Math.abs(difference);
                String itemName = (difference > 0 ? currentStack : oldStack).getDisplayName().getString();

                if (count > 0) {
                    logToFile(
                            player.getName().getString(),
                            String.format("%s %d x %s", action, count, itemName),
                            (int)getBlockPos(player).x, (int)getBlockPos(player).y, (int)getBlockPos(player).z
                    );
                }
            }
        }
    }

    /**
     * Kopiert das Inventar eines ChestMenus in ein Array von ItemStacks.
     */
        
    private ItemStack[] copyInventory(ChestMenu chestMenu) {
        int size = chestMenu.getContainer().getContainerSize();
        ItemStack[] copy = new ItemStack[size];
        for (int i = 0; i < size; i++) {
            copy[i] = chestMenu.getContainer().getItem(i).copy();
        }
        return copy;
    }
    


    /**
     * Holt die Position des Blocks, mit dem der Spieler interagiert.
     */
    private Vec3 getBlockPos(ServerPlayer player) {
        BlockPos pos = player.blockPosition();
        return new Vec3(pos.getX(), pos.getY(), pos.getZ());
    }
    
    
    private void logToFile(String playerName, String action, int x, int y, int z) {
    	//logToDatabase(playerName, action, x, y, z); // passing the information to the database - coming soon
        String logEntry = String.format("[%s] Player: %s Action: %s Position: (%d, %d, %d)%n",
                LocalDateTime.now(), playerName, action, x, y, z);
        		System.out.println(logEntry);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("chest_log.txt", true))) {
            writer.write(logEntry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unused")
	private void logToDatabase(String playerName, String action, int x, int y, int z) {
        String query = "INSERT INTO chest_logs (player_name, action, x, y, z, timestamp) VALUES (?, ?, ?, ?, ?, NOW())";
        try {
            CrackCoreMod.getDatabaseManager().executeUpdate(query, playerName, action, x, y, z);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
