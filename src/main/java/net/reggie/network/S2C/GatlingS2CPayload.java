package net.reggie.network.S2C;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.reggie.Redline;

public record GatlingS2CPayload() implements CustomPayload {
    public static final Id<GatlingS2CPayload> ID =
            new Id<>(Identifier.of(Redline.MOD_ID, "gomu_gatling_s2c"));

    public static final PacketCodec<RegistryByteBuf, GatlingS2CPayload> CODEC =
            PacketCodec.unit(new GatlingS2CPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
