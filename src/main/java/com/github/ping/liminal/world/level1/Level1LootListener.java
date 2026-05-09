package com.github.ping.liminal.world.level1;

import com.github.ping.liminal.item.CustomItem;
import com.github.ping.liminal.item.CustomItems;
import com.github.ping.liminal.item.items.AlmondWaterItem;
import com.github.ping.liminal.item.items.BottledWaterItem;
import com.github.ping.liminal.item.items.CompassItem;
import com.github.ping.liminal.item.items.JacketItem;
import com.github.ping.liminal.world.LiminalChunkGenerator;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Populates Level 1 container inventories. Containers themselves are placed as blocks
 * during chunk generation (Level1Layout#placeContainers); inventories can't be written
 * reliably from a BlockPopulator because the LimitedRegion BlockState snapshot doesn't
 * persist inventory back to the chunk. So we wait for ChunkLoadEvent#isNewChunk — by
 * which point the chunk has live block entities — and walk the same deterministic
 * cell roll the layout used to find each container's exact world position.
 */
public final class Level1LootListener implements Listener {

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        if (!e.isNewChunk()) return;
        World world = e.getWorld();
        if (!(world.getGenerator() instanceof LiminalChunkGenerator)) return;

        Chunk chunk = e.getChunk();
        long seed = world.getSeed();
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();
        int cellsPerChunk = 16 / Level1Layout.CELL_SIZE;
        int cellOriginX = chunkX * cellsPerChunk;
        int cellOriginZ = chunkZ * cellsPerChunk;
        for (int dcx = 0; dcx < cellsPerChunk; dcx++) {
            for (int dcz = 0; dcz < cellsPerChunk; dcz++) {
                int cx = cellOriginX + dcx;
                int cz = cellOriginZ + dcz;
                for (int floor = 0; floor < Level1Layout.FLOORS; floor++) {
                    populate(seed, world, cx, cz, floor);
                }
            }
        }
    }

    private void populate(long seed, World world, int cx, int cz, int floor) {
        Level1Layout.ContainerSpot spot = Level1Layout.rollContainer(seed, cx, cz, floor);
        if (spot == null) return;
        Block block = world.getBlockAt(spot.x(), spot.y(), spot.z());
        if (!(block.getState() instanceof Container container)) return;
        Inventory inv = container.getInventory();
        if (!isEmpty(inv)) return; // already populated (or modified by a player) — leave alone

        long lootSeed = Level1Layout.rand(seed, cx, cz, 'L', floor);
        int rolls = 1 + (int) (lootSeed % 3);
        for (int i = 0; i < rolls; i++) {
            int weight = (int) ((lootSeed >> (8 * i + 1)) % 100);
            int slot = (int) Math.floorMod(lootSeed >> (8 * i + 5), inv.getSize());
            ItemStack item = rollLoot(weight);
            if (item != null && inv.getItem(slot) == null) {
                inv.setItem(slot, item);
            }
        }
    }

    private boolean isEmpty(Inventory inv) {
        for (ItemStack i : inv.getContents()) {
            if (i != null && i.getType() != Material.AIR) return false;
        }
        return true;
    }

    /** Weighted loot table. Total = 100; ~20% empty for sparseness. */
    private ItemStack rollLoot(int weight) {
        if (weight < 30) return tryCreate(AlmondWaterItem.ID);
        if (weight < 65) return tryCreate(BottledWaterItem.ID);
        if (weight < 75) return tryCreate(JacketItem.ID);
        if (weight < 80) return tryCreate(CompassItem.ID);
        return null;
    }

    private ItemStack tryCreate(String id) {
        return CustomItems.registry().get(id).map(CustomItem::create).orElse(null);
    }
}
