package com.github.ping.liminal.world.level1;

import com.github.ping.liminal.item.CustomItem;
import com.github.ping.liminal.item.CustomItems;
import com.github.ping.liminal.item.items.AlmondWaterItem;
import com.github.ping.liminal.item.items.BottledWaterItem;
import com.github.ping.liminal.item.items.CompassItem;
import com.github.ping.liminal.item.items.JacketItem;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Populates Level 1 chunks with random barrels and chests carrying custom-item loot.
 * Runs after the main chunk pass (BlockPopulator). All decisions are deterministic
 * from the world seed + cell coords + floor index, so a regenerated chunk gets the
 * same containers and contents.
 *
 * Density is per-cell-per-floor: at ~8% each cell-floor pair gets a container, ~4
 * containers per chunk on average across all three floors. Loot rolls 1–3 items per
 * container; ~20% of rolls are empty for sparseness.
 */
public final class Level1ContainerPopulator extends BlockPopulator {

    private static final int CONTAINER_PCT = 8;

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion lr) {
        long seed = worldInfo.getSeed();
        int cellsPerChunk = 16 / Level1Layout.CELL_SIZE;
        int cellOriginX = chunkX * cellsPerChunk;
        int cellOriginZ = chunkZ * cellsPerChunk;
        for (int dcx = 0; dcx < cellsPerChunk; dcx++) {
            for (int dcz = 0; dcz < cellsPerChunk; dcz++) {
                int cx = cellOriginX + dcx;
                int cz = cellOriginZ + dcz;
                for (int floor = 0; floor < Level1Layout.FLOORS; floor++) {
                    placeContainer(seed, cx, cz, floor, lr);
                }
            }
        }
    }

    private void placeContainer(long seed, int cx, int cz, int floor, LimitedRegion lr) {
        long roll = Level1Layout.rand(seed, cx, cz, 'C', floor);
        if (roll % 100 >= CONTAINER_PCT) return;

        int lx = 1 + (int) ((roll >> 8) % 3);   // 1..3 (cell interior)
        int lz = 1 + (int) ((roll >> 16) % 3);
        int wx = cx * Level1Layout.CELL_SIZE + lx;
        int wz = cz * Level1Layout.CELL_SIZE + lz;
        int y  = Level1Layout.FLOOR_SURFACE[floor] + 1;

        // Skip if a wall, ladder, or door already occupies the slot.
        if (lr.getBlockData(wx, y, wz).getMaterial() != Material.AIR) return;

        Material type = pickContainer(roll);
        lr.setType(wx, y, wz, type);
        BlockState state = lr.getBlockState(wx, y, wz);
        if (state instanceof Container container) {
            populateLoot(container.getInventory(), seed, cx, cz, floor);
            state.update(true, false);
        }
    }

    private Material pickContainer(long roll) {
        // 60% barrel, 40% chest.
        return ((roll >> 24) % 100) < 60 ? Material.BARREL : Material.CHEST;
    }

    private void populateLoot(Inventory inv, long seed, int cx, int cz, int floor) {
        long lootSeed = Level1Layout.rand(seed, cx, cz, 'L', floor);
        int rolls = 1 + (int) (lootSeed % 3); // 1..3 items
        for (int i = 0; i < rolls; i++) {
            int weight = (int) ((lootSeed >> (8 * i + 1)) % 100);
            int slot   = (int) ((lootSeed >> (8 * i + 5)) % inv.getSize());
            if (slot < 0) slot += inv.getSize();
            ItemStack item = rollLoot(weight);
            if (item != null && inv.getItem(slot) == null) {
                inv.setItem(slot, item);
            }
        }
    }

    /** Weighted loot table. Total = 100; ~20% of rolls produce nothing. */
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
