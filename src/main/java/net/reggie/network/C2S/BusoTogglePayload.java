package net.reggie.network.C2S;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.reggie.Redline;

public record BusoTogglePayload() implements CustomPayload {
    // Ersetze "redline" durch deine tatsächliche MOD_ID, falls nötig
    public static final CustomPayload.Id<BusoTogglePayload> ID =
            new CustomPayload.Id<>(Identifier.of(Redline.MOD_ID, "buso_toggle"));

    // Da wir nur den Tastendruck signalisieren, reicht ein leerer Codec (unit) ohne zusätzliche Daten
    public static final PacketCodec<RegistryByteBuf, BusoTogglePayload> CODEC =
            PacketCodec.unit(new BusoTogglePayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
