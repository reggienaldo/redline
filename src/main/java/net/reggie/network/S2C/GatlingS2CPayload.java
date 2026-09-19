package net.reggie.network.S2C;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.reggie.Redline;

import java.util.UUID;

public record GatlingS2CPayload(UUID playerUuid) implements CustomPayload {
    public static final Id<GatlingS2CPayload> ID =
            new Id<>(Identifier.of(Redline.MOD_ID, "gomu_gatling_s2c"));

    public static final PacketCodec<RegistryByteBuf, GatlingS2CPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.STRING.xmap(UUID::fromString, UUID::toString), GatlingS2CPayload::playerUuid,
                    GatlingS2CPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
