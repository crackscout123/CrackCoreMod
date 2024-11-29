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

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ChestLogger {

    public ChestLogger() {
        // Event-Handler registrieren
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Event, wenn ein Spieler mit einer Kiste interagiert.
     */
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        // Sicherstellen, dass die Logik nur auf der Serverseite läuft
        if (!event.getLevel().isClientSide) {
            BlockPos pos = event.getPos();
            var world = event.getLevel();
            var blockState = world.getBlockState(pos);

            // Prüfen, ob der Block eine normale oder gefangene Kiste ist
            if (blockState.is(Blocks.CHEST) || blockState.is(Blocks.TRAPPED_CHEST)) {
                var player = event.getEntity();

                // Nur Spieler auf dem Server protokollieren (Multiplayer)
                if (player instanceof ServerPlayer serverPlayer) {
                    logToFile(serverPlayer.getName().getString(), "Interacted with chest", pos.getX(), pos.getY(), pos.getZ());
                }
            }
        }
    }

    /**
     * Protokollieren der Änderungen am Container (z.B. Hinzufügen oder Entfernen von Items).
     */
    @SubscribeEvent
    public void onBlockInventoryChange(BlockEvent.EntityPlaceEvent event) {
        // Stelle sicher, dass dies serverseitig ausgeführt wird
        if (!event.getLevel().isClientSide()) {
            var entity = event.getEntity();
            var blockState = event.getPlacedBlock();
            var pos = event.getPos();

            // Prüfen, ob der Spieler einen Block wie eine Kiste platziert hat
            if (blockState.is(Blocks.CHEST) || blockState.is(Blocks.TRAPPED_CHEST)) {
                if (entity instanceof ServerPlayer serverPlayer) {
                    logToFile(serverPlayer.getName().getString(), "Placed a chest", pos.getX(), pos.getY(), pos.getZ());
                }
            }
        }
    }

    /**
     * Hilfsmethode zum Schreiben von Logeinträgen in eine Datei.
     */
    private void logToFile(String playerName, String action, int x, int y, int z) {
        String logEntry = String.format("[%s] Player: %s Action: %s Position: (%d, %d, %d)%n",
                LocalDateTime.now(), playerName, action, x, y, z);
        System.out.println(logEntry); // DEBUG
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("chest_log.txt", true))) {
            writer.write(logEntry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
