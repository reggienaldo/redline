package net.reggie.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.reggie.network.S2C.*;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnimationPayloadTest {
    @Test
    void animationPacketsPreserveTheAttackerInsteadOfUsingTheReceivingPlayer() {
        UUID attacker = UUID.randomUUID();
        assertRoundTrip(PistolS2CPayload.CODEC, new PistolS2CPayload(attacker));
        assertRoundTrip(BazookaS2CPayload.CODEC, new BazookaS2CPayload(attacker));
        assertRoundTrip(GatlingS2CPayload.CODEC, new GatlingS2CPayload(attacker));
        assertRoundTrip(GatlingStopS2CPayload.CODEC, new GatlingStopS2CPayload(attacker));
    }

    private <T> void assertRoundTrip(PacketCodec<RegistryByteBuf, T> codec, T payload) {
        RegistryByteBuf buffer = new RegistryByteBuf(Unpooled.buffer(), null);
        try {
            codec.encode(buffer, payload);
            assertEquals(payload, codec.decode(buffer));
        } finally {
            buffer.release();
        }
    }
}
