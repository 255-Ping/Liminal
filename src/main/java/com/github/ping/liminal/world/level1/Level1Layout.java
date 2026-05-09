package com.github.ping.liminal.world.level1;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Ladder;
import org.bukkit.generator.ChunkGenerator;

/**
 * Level 1 — the canonical backrooms floor. Three vertically stacked floors with an
 * infinite cell-grid maze, occasional doors, ladder stairwells, and a (mostly) lit
 * ceiling. Bedrock cap top and bottom: players who somehow land above the top can
 * stand on it; anything below the bottom falls into the void.
 *
 * Everything is deterministic from the world seed; no per-chunk state is held outside
 * ChunkData. Cells (CELL_SIZE x CELL_SIZE) align with chunk boundaries (16 % 4 == 0)
 * so wall/door/stairwell math never spans two chunks.
 */
final class Level1Layout {
    private Level1Layout() {}

    // --- Vertical layout (Y) ---
    // Total height = 1 (bedrock) + 1 (floor) + 3 (air) + 1 (ceil) + 3 (air) + 1 (ceil) + 3 (air) + 1 (ceil) + 1 (bedrock) = 15.
    static final int BEDROCK_BOTTOM = -64;
    static final int FLOOR_BASE     = -63; // wool floor of floor 1, sits on top of bottom bedrock
    static final int CEILING_1      = -59; // ceiling of floor 1 / floor of floor 2
    static final int CEILING_2      = -55; // ceiling of floor 2 / floor of floor 3
    static final int CEILING_3      = -51; // ceiling of floor 3
    static final int BEDROCK_TOP    = -50;

    /** Air interior Y ranges (inclusive) for each of the 3 floors. */
    private static final int[][] FLOOR_AIR = {
            { -62, -60 }, // floor 1
            { -58, -56 }, // floor 2
            { -54, -52 }, // floor 3
    };
    /** Ceiling Y per floor (the block above the air range). */
    private static final int[] FLOOR_CEILING = { CEILING_1, CEILING_2, CEILING_3 };

    /** Maze cell footprint. Must divide 16 evenly so cells align with chunk borders. */
    static final int CELL_SIZE = 4;

    // --- Materials ---
    private static final Material WALL_MAT          = Material.YELLOW_TERRACOTTA;
    private static final Material FLOOR_MAT         = Material.BROWN_WOOL;
    private static final Material CEILING_LIT_MAT   = Material.OCHRE_FROGLIGHT;
    private static final Material CEILING_DARK_MAT  = Material.WHITE_WOOL;
    private static final Material BEDROCK_MAT       = Material.BEDROCK;
    private static final Material DOOR_MAT          = Material.OAK_DOOR;

    // --- Probabilities (0..99) ---
    private static final int WALL_PCT       = 65;
    private static final int LIGHT_PCT      = 80;
    private static final int DOOR_PCT       = 12;
    private static final int STAIRWELL_PCT  = 5;

    static void generate(long seed, int chunkX, int chunkZ, ChunkGenerator.ChunkData cd) {
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;

        // 1. Per-column blocks: bedrock cap, floor surface, walls per floor, ceiling lit/dark.
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                generateColumn(seed, baseX + dx, baseZ + dz, dx, dz, cd);
            }
        }

        // 2. Per-cell features: doors and stairwells. Cells are aligned with chunks so each
        // chunk owns exactly (16 / CELL_SIZE)^2 cells.
        int cellsPerChunk = 16 / CELL_SIZE;
        int cellOriginX = chunkX * cellsPerChunk;
        int cellOriginZ = chunkZ * cellsPerChunk;
        for (int dcx = 0; dcx < cellsPerChunk; dcx++) {
            for (int dcz = 0; dcz < cellsPerChunk; dcz++) {
                int cx = cellOriginX + dcx;
                int cz = cellOriginZ + dcz;
                placeDoors(seed, cx, cz, baseX, baseZ, cd);
                placeStairwells(seed, cx, cz, baseX, baseZ, cd);
            }
        }
    }

    private static void generateColumn(long seed, int wx, int wz, int dx, int dz,
                                       ChunkGenerator.ChunkData cd) {
        cd.setBlock(dx, BEDROCK_BOTTOM, dz, BEDROCK_MAT);
        cd.setBlock(dx, BEDROCK_TOP,    dz, BEDROCK_MAT);
        cd.setBlock(dx, FLOOR_BASE,     dz, FLOOR_MAT);

        for (int floor = 0; floor < 3; floor++) {
            if (isWallAt(seed, wx, wz, floor)) {
                int yLo = FLOOR_AIR[floor][0];
                int yHi = FLOOR_AIR[floor][1];
                for (int y = yLo; y <= yHi; y++) {
                    cd.setBlock(dx, y, dz, WALL_MAT);
                }
            }
        }

        for (int floor = 0; floor < 3; floor++) {
            int ceilY = FLOOR_CEILING[floor];
            boolean lit = isCeilingLit(seed, wx, wz, floor);
            cd.setBlock(dx, ceilY, dz, lit ? CEILING_LIT_MAT : CEILING_DARK_MAT);
        }
    }

    /**
     * Maze rule: each 4x4 cell has a corner pillar at (lx==0 && lz==0), interior at
     * (lx&gt;0 && lz&gt;0), and edges at (lx==0, lz&gt;0) [west wall] / (lx&gt;0, lz==0) [north wall].
     * Each edge wall span exists with WALL_PCT probability deterministic per (seed, cell, side, floor).
     * Corner pillars are always solid — gives the maze its tiled-pillar look.
     */
    private static boolean isWallAt(long seed, int wx, int wz, int floor) {
        int lx = Math.floorMod(wx, CELL_SIZE);
        int lz = Math.floorMod(wz, CELL_SIZE);
        if (lx != 0 && lz != 0) return false;             // interior
        if (lx == 0 && lz == 0) return true;               // corner pillar
        int cx = Math.floorDiv(wx, CELL_SIZE);
        int cz = Math.floorDiv(wz, CELL_SIZE);
        char side = (lx == 0) ? 'W' : 'N';
        return rand(seed, cx, cz, side, floor) % 100 < WALL_PCT;
    }

    private static boolean isCeilingLit(long seed, int wx, int wz, int floor) {
        return rand(seed, wx, wz, 'L', floor) % 100 < LIGHT_PCT;
    }

    private static void placeDoors(long seed, int cx, int cz, int baseX, int baseZ,
                                   ChunkGenerator.ChunkData cd) {
        for (int floor = 0; floor < 3; floor++) {
            // West wall midpoint at world (cx*4, cz*4 + 2): replace if both door-roll and wall-roll hit.
            if (rand(seed, cx, cz, 'd', floor) % 100 < DOOR_PCT
                    && rand(seed, cx, cz, 'W', floor) % 100 < WALL_PCT) {
                int wx = cx * CELL_SIZE;
                int wz = cz * CELL_SIZE + 2;
                placeDoor(wx, wz, baseX, baseZ, FLOOR_AIR[floor][0], BlockFace.EAST, cd);
            }
            // North wall midpoint at world (cx*4 + 2, cz*4): door faces south into cell.
            if (rand(seed, cx, cz, 'D', floor) % 100 < DOOR_PCT
                    && rand(seed, cx, cz, 'N', floor) % 100 < WALL_PCT) {
                int wx = cx * CELL_SIZE + 2;
                int wz = cz * CELL_SIZE;
                placeDoor(wx, wz, baseX, baseZ, FLOOR_AIR[floor][0], BlockFace.SOUTH, cd);
            }
        }
    }

    private static void placeDoor(int wx, int wz, int baseX, int baseZ, int yBottom,
                                  BlockFace facing, ChunkGenerator.ChunkData cd) {
        int dx = wx - baseX, dz = wz - baseZ;
        if (dx < 0 || dx >= 16 || dz < 0 || dz >= 16) return;

        Door bottom = (Door) DOOR_MAT.createBlockData();
        bottom.setFacing(facing);
        bottom.setHinge(Door.Hinge.LEFT);
        bottom.setHalf(Bisected.Half.BOTTOM);
        bottom.setOpen(false);
        cd.setBlock(dx, yBottom, dz, bottom);

        Door top = (Door) DOOR_MAT.createBlockData();
        top.setFacing(facing);
        top.setHinge(Door.Hinge.LEFT);
        top.setHalf(Bisected.Half.TOP);
        top.setOpen(false);
        cd.setBlock(dx, yBottom + 1, dz, top);
        // Y = yBottom + 2 stays as wall block from the column pass — door fits in 2 of the 3 wall blocks.
    }

    private static void placeStairwells(long seed, int cx, int cz, int baseX, int baseZ,
                                        ChunkGenerator.ChunkData cd) {
        // K = 0 connects floor 1 → 2; K = 1 connects floor 2 → 3.
        for (int k = 0; k < 2; k++) {
            if (rand(seed, cx, cz, 's', k) % 100 < STAIRWELL_PCT) {
                placeStairwell(cx, cz, k, baseX, baseZ, cd);
            }
        }
    }

    private static void placeStairwell(int cx, int cz, int floorK, int baseX, int baseZ,
                                       ChunkGenerator.ChunkData cd) {
        // Pillar at cell-local (1, 2); ladder at (2, 2) facing east (attached to the pillar).
        // Vertical span: from floor K's air bottom up through floor K+1's air bottom, replacing
        // the ceiling block between them with a ladder block (which opens the 1x1 hole).
        int pillarLx = 1, pillarLz = 2;
        int ladderLx = 2, ladderLz = 2;
        int yBot = FLOOR_AIR[floorK][0];
        int yTop = FLOOR_AIR[floorK + 1][0];

        int pDx = (cx * CELL_SIZE + pillarLx) - baseX;
        int pDz = (cz * CELL_SIZE + pillarLz) - baseZ;
        int lDx = (cx * CELL_SIZE + ladderLx) - baseX;
        int lDz = (cz * CELL_SIZE + ladderLz) - baseZ;

        if (pDx >= 0 && pDx < 16 && pDz >= 0 && pDz < 16) {
            for (int y = yBot; y <= yTop; y++) {
                cd.setBlock(pDx, y, pDz, WALL_MAT);
            }
        }
        if (lDx >= 0 && lDx < 16 && lDz >= 0 && lDz < 16) {
            Ladder ladder = (Ladder) Material.LADDER.createBlockData();
            ladder.setFacing(BlockFace.EAST);
            for (int y = yBot; y <= yTop; y++) {
                cd.setBlock(lDx, y, lDz, ladder);
            }
        }
    }

    /** SplitMix-style avalanche. Returns a non-negative long so callers can `% N` safely. */
    private static long rand(long seed, int a, int b, int c, int d) {
        long h = seed ^ 0x9E3779B97F4A7C15L;
        h = h * 0xBF58476D1CE4E5B9L + a;
        h = h * 0xBF58476D1CE4E5B9L + b;
        h = h * 0xBF58476D1CE4E5B9L + c;
        h = h * 0xBF58476D1CE4E5B9L + d;
        h ^= h >>> 30;
        h *= 0x94D049BB133111EBL;
        h ^= h >>> 31;
        return h >>> 1;
    }
}
