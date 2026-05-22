package com.tcc.client.util.render;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Set;

public class EspUtils {
    public static final Set<Block> ESP_BLOCKS = Set.of(
            Blocks.CHEST,
            Blocks.TRAPPED_CHEST,
            Blocks.ENDER_CHEST,
            Blocks.BARREL,
            Blocks.SHULKER_BOX
            // add more blocks here
    );
}
