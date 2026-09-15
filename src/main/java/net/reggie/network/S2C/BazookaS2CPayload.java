package net.reggie.network.S2C;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.reggie.Redline;

public record BazookaS2CPayload() implements CustomPayload {

    public static final Id<BazookaS2CPayload> ID =
            new Id<>(Identifier.of(Redline.MOD_ID, "gomu_bazooka_s2c"));

    public static final PacketCodec<RegistryByteBuf, BazookaS2CPayload> CODEC =
            PacketCodec.unit(new BazookaS2CPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
