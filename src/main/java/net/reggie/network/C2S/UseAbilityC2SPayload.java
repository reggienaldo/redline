package net.reggie.network.C2S;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.reggie.Redline;

public record UseAbilityC2SPayload(int slotIndex, boolean isKeybind) implements CustomPayload {
    public static final Id<UseAbilityC2SPayload> ID = new Id<>(Identifier.of(Redline.MOD_ID, "use_ability"));

    public static final PacketCodec<PacketByteBuf, UseAbilityC2SPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, UseAbilityC2SPayload::slotIndex,
            PacketCodecs.BOOL, UseAbilityC2SPayload::isKeybind,
            UseAbilityC2SPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
