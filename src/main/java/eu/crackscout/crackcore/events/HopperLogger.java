/**
 * @author crackscout
 *
 *  Nov 30, 2024 9:40:52 AM
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
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@SuppressWarnings("preview")
public class HopperLogger {

    private final Map<ServerPlayer, ItemStack[]> trackedInventories = new HashMap<>();

    public HopperLogger() {
        // Event-Handler registrieren
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Event, wenn ein Spieler mit einem Trichter interagiert.
     */
    @SubscribeEvent
    public void onBlockInventoryChange(BlockEvent.EntityPlaceEvent event) {
        // Sicherstellen, dass dies serverseitig ausgeführt wird
        if (!event.getLevel().isClientSide()) {
            var entity = event.getEntity();
            var blockState = event.getPlacedBlock();
            var pos = event.getPos();

            if (entity instanceof ServerPlayer serverPlayer) {
                // Prüfen, ob der Block ein Trichter ist
                if (blockState.is(Blocks.HOPPER)) {
                    logToFile(serverPlayer.getName().getString(), "Placed a hopper", pos.getX(), pos.getY(), pos.getZ());
                }
            }
        }
    }

    /**
     * Event, wenn ein Spieler einen Trichter öffnet.
     */
    @SubscribeEvent
    public void onContainerOpen(PlayerContainerEvent.Open event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            AbstractContainerMenu menu = event.getContainer();
            BlockPos hopperPos = getBlockPos(player);
            // Prüfen, ob es sich um einen Trichter handelt
            if (menu instanceof HopperMenu hopperMenu) {
                // Inventar des Trichters initial speichern
                trackedInventories.put(player, copyInventory(hopperMenu));
                logToFile(player.getName().getString(), "Opened hopper", hopperPos.getX(), hopperPos.getY(), hopperPos.getZ());
            }
        }
    }

    /**
     * Event, wenn ein Spieler einen Trichter schließt.
     */
    @SubscribeEvent
    public void onContainerClose(PlayerContainerEvent.Close event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            AbstractContainerMenu menu = event.getContainer();

            // Nur Hopper überprüfen
            if (menu instanceof HopperMenu hopperMenu) {
                BlockPos hopperPos = getBlockPos(player);
                // Vergleiche Änderungen
                ItemStack[] previousInventory = trackedInventories.remove(player);
                if (previousInventory != null) {
                    logInventoryChanges(player, previousInventory, hopperMenu, hopperPos);
                }
            }
        }
    }

    /**
     * Protokolliere Änderungen im Inventar des Hoppers.
     */
    private void logInventoryChanges(ServerPlayer player, ItemStack[] oldInventory, HopperMenu hopperMenu, BlockPos hopperPos) {
        ItemStack[] currentInventory = copyInventory(hopperMenu);

        for (int i = 0; i < oldInventory.length; i++) {
            ItemStack oldStack = oldInventory[i];
            ItemStack currentStack = currentInventory[i];

            // Prüfe nur Änderungen im Hopper-Inventar
            if (!ItemStack.isSameItemSameTags(oldStack, currentStack)) {
                int difference = currentStack.getCount() - oldStack.getCount();
                String action = difference > 0 ? "Added" : "Removed";
                int count = Math.abs(difference);
                String itemName = (difference > 0 ? currentStack : oldStack).getDisplayName().getString();

                if (count > 0) {
                    logToFile(
                            player.getName().getString(),
                            String.format("%s %d x %s", action, count, itemName),
                            (int)getBlockPos(player).getX(), (int)getBlockPos(player).getY(), (int)getBlockPos(player).getZ()
                    );
                }
            }
        }
    }


    /**
     * Kopiert das Inventar eines HopperMenus in ein Array von ItemStacks.
     */
    private ItemStack[] copyInventory(HopperMenu hopperMenu) {
        int size = hopperMenu.slots.size(); // Anzahl der Slots im Menü
        ItemStack[] copy = new ItemStack[size];
        for (int i = 0; i < size; i++) {
            // Die Slots überprüfen und die Items kopieren
            copy[i] = hopperMenu.slots.get(i).getItem().copy();
        }
        return copy;
    }



    /**
     * Holt die Position des Blocks, mit dem der Spieler interagiert.
     */
    private BlockPos getBlockPos(ServerPlayer player) {
        return player.blockPosition();
    }
    
    @SuppressWarnings("unused")
	private Vec3 blockPosToVec3(BlockPos pos) {
        return new Vec3(pos.getX(), pos.getY(), pos.getZ());
    }


    /**
     * Schreibt die Log-Daten in eine Datei.
     */
    private void logToFile(String playerName, String action, int x, int y, int z) {
        // logToDatabase(playerName, action, x, y, z); // Unkommentieren für Datenbank-Integration
        String logEntry = String.format("[%s] Player: %s Action: %s Position: (%d, %d, %d)%n",
                LocalDateTime.now(), playerName, action, x, y, z);
        		System.out.println(logEntry);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("hopper_log.txt", true))) {
            writer.write(logEntry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    private void logToDatabase(String playerName, String action, int x, int y, int z) {
        String query = "INSERT INTO hopper_logs (player_name, action, x, y, z, timestamp) VALUES (?, ?, ?, ?, ?, NOW())";
        try {
            CrackCoreMod.getDatabaseManager().executeUpdate(query, playerName, action, x, y, z);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
