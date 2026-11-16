package com.example.crimetown.world;

import com.example.crimetown.economy.TownData;
import com.example.crimetown.npc.Npcs;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the town procedurally from code (no .nbt templates), 61 x 61 blocks.
 * Layout (x east, z south, origin in the north-west corner):
 * bank NW, casino NE, armory SW, town hall SE, stage and plaza in the middle, alleys between buildings.
 */
public final class TownBuilder {
    public static final int SIZE = 61;

    private final ServerLevel lv;
    private final int ox, oy, oz;
    private final List<int[]> slots = new ArrayList<>();

    private TownBuilder(ServerLevel lv, int ox, int oy, int oz) {
        this.lv = lv;
        this.ox = ox;
        this.oy = oy;
        this.oz = oz;
    }

    /** Builds the town with its north-west corner at (ox, oz); the ground level is taken from the terrain. */
    public static void build(ServerLevel lv, int ox, int oz) {
        int oy = lv.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ox + 30, oz + 30);
        new TownBuilder(lv, ox, oy, oz).run();
    }

    private void f(int x1, int y1, int z1, int x2, int y2, int z2, Block b) {
        BlockState s = b.defaultBlockState();
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++)
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++)
                for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++)
                    lv.setBlock(new BlockPos(ox + x, oy + y, oz + z), s, 2);
    }

    private void p(int x, int y, int z, Block b) {
        f(x, y, z, x, y, z, b);
    }

    private void run() {
        f(0, 0, 0, SIZE - 1, 16, SIZE - 1, Blocks.AIR);                 // clear
        f(0, -9, 0, SIZE - 1, -2, SIZE - 1, Blocks.STONE_BRICKS);       // foundation
        f(0, -1, 0, SIZE - 1, -1, SIZE - 1, Blocks.POLISHED_ANDESITE);  // street level
        f(28, -1, 0, 32, -1, 60, Blocks.DEEPSLATE_TILES);               // roads
        f(0, -1, 28, 60, -1, 32, Blocks.DEEPSLATE_TILES);
        for (int x = 18; x <= 42; x++)                                  // checkered plaza
            for (int z = 18; z <= 42; z++)
                p(x, -1, z, (x + z) % 2 == 0 ? Blocks.SMOOTH_STONE : Blocks.STONE_BRICKS);

        // street lamps
        int[][] lamps = {{22, 22}, {38, 22}, {22, 38}, {38, 38}, {30, 10}, {30, 50}, {10, 30}, {50, 30},
                {19, 9}, {41, 9}, {19, 51}, {41, 51}};
        for (int[] l : lamps) {
            f(l[0], 0, l[1], l[0], 3, l[1], Blocks.COBBLESTONE_WALL);
            p(l[0], 4, l[1], Blocks.LANTERN);
        }

        bank();
        casino();
        armory();
        townHall();
        stage();
        alleys();
        spawnNpcs();

        TownData d = TownData.get();
        d.built = true;
        d.cx = ox + 30;
        d.cy = oy;
        d.cz = oz + 30;
        d.slots = new ArrayList<>(slots);
        d.save();
    }

    /**
     * Hollow building. doorOnSouth: door in the z2 wall (true) or z1 wall (false), centred on dx.
     */
    private void house(int x1, int z1, int x2, int z2, int h, Block wall, Block trim, Block roof,
                       Block floor, boolean doorOnSouth, int dx) {
        f(x1, 0, z1, x2, h, z2, wall);
        f(x1 + 1, 0, z1 + 1, x2 - 1, h - 1, z2 - 1, Blocks.AIR);
        f(x1 - 1, h, z1 - 1, x2 + 1, h, z2 + 1, roof);
        f(x1 + 1, -1, z1 + 1, x2 - 1, -1, z2 - 1, floor);
        for (int[] c : new int[][]{{x1, z1}, {x1, z2}, {x2, z1}, {x2, z2}}) {
            f(c[0], 0, c[1], c[0], h, c[1], trim);
        }
        int doorZ = doorOnSouth ? z2 : z1;
        int blankZ = doorOnSouth ? z1 : z2;
        for (int i = x1 + 2; i <= x2 - 2; i += 3) {
            p(i, 2, blankZ, Blocks.GLASS);
            if (Math.abs(i - dx) > 1) p(i, 2, doorZ, Blocks.GLASS);
        }
        for (int z = z1 + 2; z <= z2 - 2; z += 3) {
            p(x1, 2, z, Blocks.GLASS);
            p(x2, 2, z, Blocks.GLASS);
        }
        f(dx - 1, 0, doorZ, dx + 1, 2, doorZ, Blocks.AIR);               // doorway
        f(dx - 1, 3, doorZ, dx + 1, 3, doorZ, trim);                     // lintel
        for (int x = x1 + 2; x <= x2 - 2; x += 4)                        // ceiling lights
            for (int z = z1 + 2; z <= z2 - 2; z += 4)
                p(x, h - 1, z, Blocks.GLOWSTONE);
    }

    private void bank() {
        house(4, 4, 16, 14, 6, Blocks.QUARTZ_BRICKS, Blocks.GOLD_BLOCK, Blocks.SMOOTH_QUARTZ, Blocks.POLISHED_DIORITE, true, 10);
        f(6, 0, 11, 14, 0, 11, Blocks.QUARTZ_BLOCK);          // counter
        f(6, 1, 11, 14, 1, 11, Blocks.GLASS);                  // teller window
        p(7, 0, 6, Blocks.GOLD_BLOCK);                         // vault decoration
        p(13, 0, 6, Blocks.GOLD_BLOCK);
    }

    private void casino() {
        house(44, 4, 56, 14, 6, Blocks.RED_NETHER_BRICKS, Blocks.GOLD_BLOCK, Blocks.BLACKSTONE, Blocks.POLISHED_BLACKSTONE, true, 50);
        f(45, -1, 5, 55, -1, 13, Blocks.RED_TERRACOTTA);
        for (int x : new int[]{47, 50, 53}) {
            p(x, 0, 7, Blocks.SEA_LANTERN);                    // slot machine (interact with it)
            p(x, 0, 6, Blocks.BLACKSTONE);
            slots.add(new int[]{ox + x, oy, oz + 7});
        }
    }

    private void armory() {
        house(4, 46, 16, 56, 6, Blocks.DEEPSLATE_BRICKS, Blocks.IRON_BLOCK, Blocks.POLISHED_DEEPSLATE, Blocks.SMOOTH_STONE, false, 10);
        f(6, 0, 51, 14, 0, 51, Blocks.POLISHED_BLACKSTONE);    // counter
        p(6, 0, 54, Blocks.ANVIL);
        p(14, 0, 54, Blocks.GRINDSTONE);
    }

    private void townHall() {
        house(44, 46, 56, 56, 9, Blocks.STONE_BRICKS, Blocks.WHITE_CONCRETE, Blocks.DARK_OAK_PLANKS, Blocks.POLISHED_ANDESITE, false, 50);
        f(47, 0, 51, 53, 0, 51, Blocks.DARK_OAK_PLANKS);       // mayor's desk
        f(46, -1, 48, 54, -1, 50, Blocks.RED_TERRACOTTA);      // red "carpet" path
    }

    private void stage() {
        f(24, 0, 24, 36, 1, 34, Blocks.DARK_OAK_PLANKS);
        f(28, 0, 35, 32, 0, 35, Blocks.DARK_OAK_SLAB);         // steps
        f(24, 2, 24, 36, 6, 24, Blocks.BLACK_CONCRETE);        // backdrop
        for (int[] c : new int[][]{{24, 24}, {36, 24}, {24, 34}, {36, 34}})
            f(c[0], 2, c[1], c[0], 6, c[1], Blocks.STRIPPED_DARK_OAK_LOG);
        f(24, 7, 24, 36, 7, 24, Blocks.DARK_OAK_PLANKS);
        f(24, 7, 34, 36, 7, 34, Blocks.DARK_OAK_PLANKS);
        f(24, 7, 24, 24, 7, 34, Blocks.DARK_OAK_PLANKS);
        f(36, 7, 24, 36, 7, 34, Blocks.DARK_OAK_PLANKS);
        for (int x : new int[]{26, 30, 34}) p(x, 6, 29, Blocks.SEA_LANTERN);
        p(25, 2, 26, Blocks.NOTE_BLOCK);                       // speakers
        p(35, 2, 26, Blocks.NOTE_BLOCK);
    }

    private void alleys() {
        int[][] a = {{19, 9}, {41, 9}, {19, 51}, {41, 51}};
        for (int[] c : a) {
            p(c[0] - 2, 0, c[1] - 1, Blocks.BARREL);
            p(c[0] - 2, 0, c[1] + 1, Blocks.BARREL);
            p(c[0] + 2, 0, c[1], Blocks.CAULDRON);
            p(c[0] + 2, 1, c[1], Blocks.SOUL_LANTERN);
        }
    }

    private void spawnNpcs() {
        Npcs.spawn(lv, EntityType.VILLAGER, ox + 10.5, oy, oz + 8.5, Npcs.CASHIER, "Кассир", true);
        Npcs.spawn(lv, EntityType.VILLAGER, ox + 10.5, oy, oz + 53.5, Npcs.GUNSMITH, "Оружейник", true);
        Npcs.spawn(lv, EntityType.VILLAGER, ox + 50.5, oy, oz + 53.5, Npcs.MAYOR, "Мэр", true);
        double[][] g = {{22.5, 22.5}, {38.5, 22.5}, {22.5, 38.5}, {38.5, 38.5}};
        for (double[] c : g) {
            Npcs.spawn(lv, EntityType.IRON_GOLEM, ox + c[0], oy, oz + c[1], Npcs.GUARD, "Вышибала", false);
        }
    }
}
