package net.reggie.network.S2C;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.reggie.Redline;

public record HakiAnimS2CPayload(java.util.UUID playerUuid, boolean hasWeapon) implements CustomPayload {
    public static final Id<HakiAnimS2CPayload> ID =
            new Id<>(Identifier.of(Redline.MOD_ID, "haki_anim_s2c"));

    // Sicherer 1.21.1 Codec: UUID als String und Wahrheitswert als BOOL
    public static final PacketCodec<RegistryByteBuf, HakiAnimS2CPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING.xmap(java.util.UUID::fromString, java.util.UUID::toString), HakiAnimS2CPayload::playerUuid,
            PacketCodecs.BOOL, HakiAnimS2CPayload::hasWeapon,
            HakiAnimS2CPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}