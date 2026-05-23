package com.tsc.client.util.render;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Set;

public class EspUtils {
    public static final Set<Block> CHEST_ESP_BLOCKS = Set.of(
            Blocks.CHEST,
            Blocks.TRAPPED_CHEST,
            Blocks.ENDER_CHEST,
            Blocks.BARREL,
            Blocks.SHULKER_BOX
            // add more blocks here
    );

    public static final Set<Block> BASE_ESP_BLOCKS = Set.of(
            Blocks.CHEST,
            Blocks.TRAPPED_CHEST,
            Blocks.ENDER_CHEST,
            Blocks.BARREL,
            Blocks.SHULKER_BOX,
            Blocks.ENCHANTING_TABLE,
            Blocks.CAKE,
            Blocks.CRAFTING_TABLE,
            Blocks.FURNACE,
            Blocks.SMOKER,
            Blocks.BLAST_FURNACE,
            Blocks.BEACON
            // add more blocks here
    );
}
