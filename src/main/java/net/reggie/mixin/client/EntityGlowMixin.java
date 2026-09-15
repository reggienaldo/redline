package net.reggie.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.reggie.Redline;
import net.reggie.game.haki.IHakiComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Environment(EnvType.CLIENT)
@Mixin(Entity.class)
public abstract class EntityGlowMixin {

    @Unique
    private static final Set<UUID> clientGlowTargetIds = new HashSet<>();

    @Unique
    private static boolean lastObservationState = false;

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    private void redline$makeEntitiesGlowOnlyForHakiUser(CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        Entity targetEntity = (Entity) (Object) this;

        // Der lokale Spieler darf niemals selbst leuchten
        if (targetEntity.getUuid().equals(client.player.getUuid()) || !(targetEntity instanceof LivingEntity livingTarget)) {
            return;
        }

        // Holt deine flache Haki-Komponente über den zentralen Redline-Key
        IHakiComponent hakiComp = Redline.HAKI.get(client.player);

        // Fragt direkt den flachen Aktivitätsstatus des Kenbunshoku ab
        boolean currentObservationActive = hakiComp.isKenActive();

        // --- 1. DETEKTOR: HAKI WURDE GERADE AUSGESCHALTET ---
        if (lastObservationState && !currentObservationActive) {
            lastObservationState = false;

            // Wir räumen alle manipulierten Entities im Client auf
            for (Entity entity : client.world.getEntities()) {
                if (entity instanceof LivingEntity && clientGlowTargetIds.contains(entity.getUuid())) {

                    BlockPos pos = entity.getBlockPos();
                    BlockState state = client.world.getBlockState(pos);

                    // FIX FÜR 1.21.1: Übergibt exakt (BlockPos, BlockState old, BlockState updated)
                    client.worldRenderer.scheduleBlockRerenderIfNeeded(pos, state, state);

                    entity.setCustomNameVisible(entity.isCustomNameVisible());
                }
            }
            clientGlowTargetIds.clear();
            return;
        }

        lastObservationState = currentObservationActive;

        // --- 2. AKTIVER GLOW-SCANNER ---
        if (currentObservationActive) {
            // Skaliert den Radius sauber: 16 Blöcke Basis + 4 zusätzliche Blöcke pro Kenbunshoku-Level
            double maxRadius = 16.0 + (hakiComp.getKenLevel() * 4.0);
            double distanceSq = client.player.squaredDistanceTo(targetEntity);

            if (distanceSq <= (maxRadius * maxRadius)) {
                clientGlowTargetIds.add(livingTarget.getUuid());
                cir.setReturnValue(true);
                return;
            }
        }

        // --- 3. ENTITÄT LÄUFT AUS DER REICHWEITE ---
        if (clientGlowTargetIds.contains(livingTarget.getUuid())) {
            clientGlowTargetIds.remove(livingTarget.getUuid());

            BlockPos targetPos = livingTarget.getBlockPos();
            BlockState targetState = client.world.getBlockState(targetPos);

            // FIX FÜR 1.21.1: Aktualisiert den Render-Zustand des Blocks, in dem der Mob steht
            client.worldRenderer.scheduleBlockRerenderIfNeeded(targetPos, targetState, targetState);

            livingTarget.setCustomNameVisible(livingTarget.isCustomNameVisible());
        }
    }
}