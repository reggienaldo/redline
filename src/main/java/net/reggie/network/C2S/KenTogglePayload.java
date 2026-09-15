package net.reggie.network.C2S;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.reggie.Redline;

public record KenTogglePayload() implements CustomPayload {
    public static final CustomPayload.Id<KenTogglePayload> ID =
            new CustomPayload.Id<>(Identifier.of(Redline.MOD_ID, "ken_toggle"));

    public static final PacketCodec<RegistryByteBuf, KenTogglePayload> CODEC =
            PacketCodec.unit(new KenTogglePayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
