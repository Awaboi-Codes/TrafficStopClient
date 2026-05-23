package com.tsc.client.util.packetspoof;

import net.minecraft.world.entity.Entity;

public class SpoofData {
    public static Entity target;
    public static double heightOffset;
    public static boolean forceCrit;

    // Flexible constructor to pass any custom attack profile values you want parsed
    public SpoofData(Entity target, double heightOffset, boolean forceCrit) {
        this.target = target;
        this.heightOffset = heightOffset;
        this.forceCrit = forceCrit;
    }
}
