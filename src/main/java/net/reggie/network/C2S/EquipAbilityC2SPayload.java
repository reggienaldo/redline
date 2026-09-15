package net.reggie.network.C2S;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record EquipAbilityC2SPayload(int slotIndex, String abilityId, boolean isKeybind) implements CustomPayload {
    public static final Id<EquipAbilityC2SPayload> ID = new Id<>(Identifier.of("redline", "equip_ability"));

    public static final PacketCodec<PacketByteBuf, EquipAbilityC2SPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, EquipAbilityC2SPayload::slotIndex,
            PacketCodecs.STRING, EquipAbilityC2SPayload::abilityId,
            PacketCodecs.BOOL, EquipAbilityC2SPayload::isKeybind,
            EquipAbilityC2SPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
