package com.tcc.client.util.packetspoof;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PacketPayload(int targetEntityId, double heightOffset, boolean forceCrit) implements CustomPacketPayload {

    // Define a completely custom unique network channel ID for your mod
    public static final CustomPacketPayload.Type<PacketPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("tcc", "combat_spoof"));

    // The Stream Codec automates serialization (writing data) and deserialization (reading data) across the network pipe
    public static final StreamCodec<FriendlyByteBuf, PacketPayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeInt(payload.targetEntityId());
                buf.writeDouble(payload.heightOffset());
                buf.writeBoolean(payload.forceCrit());
            },
            buf -> new PacketPayload(buf.readInt(), buf.readDouble(), buf.readBoolean())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
