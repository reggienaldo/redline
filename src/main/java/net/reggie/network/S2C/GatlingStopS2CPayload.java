package net.reggie.network.S2C;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.reggie.Redline;

import java.util.UUID;

public record GatlingStopS2CPayload(UUID playerUuid) implements CustomPayload {
    public static final Id<GatlingStopS2CPayload> ID =
            new Id<>(Identifier.of(Redline.MOD_ID, "gomu_gatling_stop_s2c"));

    public static final PacketCodec<RegistryByteBuf, GatlingStopS2CPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.STRING.xmap(UUID::fromString, UUID::toString), GatlingStopS2CPayload::playerUuid,
                    GatlingStopS2CPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
