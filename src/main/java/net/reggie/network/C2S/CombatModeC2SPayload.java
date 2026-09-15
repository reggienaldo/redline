package net.reggie.network.C2S;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.reggie.Redline;

public record CombatModeC2SPayload() implements CustomPayload {

    public static final Id<CombatModeC2SPayload> ID =
            new Id<>(Identifier.of(Redline.MOD_ID, "combat_mode"));

    public static final PacketCodec<RegistryByteBuf, CombatModeC2SPayload> CODEC =
            PacketCodec.unit(new CombatModeC2SPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
