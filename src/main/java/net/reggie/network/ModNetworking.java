package net.reggie.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.reggie.Redline;
import net.reggie.RedlineClient;
import net.reggie.game.abilities.IAbility;
import net.reggie.game.abilities.ModAbilities;
import net.reggie.game.abilities.fruit.gomu.GomuGatlingAbility;
import net.reggie.network.C2S.*;
import net.reggie.network.S2C.*;
import net.reggie.sound.ModSounds;

public class ModNetworking {

    public static void registerC2SPackets() {

        PayloadTypeRegistry.playC2S().register(UseAbilityC2SPayload.ID, UseAbilityC2SPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(UseAbilityC2SPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();

                if (!Redline.COMBAT_COMPONENT.get(player).isCombatModeEnabled()) return;

                var abilityComp = Redline.ABILITY_COMPONENT.get(player);
                int slot = payload.slotIndex();
                boolean isKeybind = payload.isKeybind();

                String[] targetArray = isKeybind ? abilityComp.getInventory().getEquippedSlots() : abilityComp.getInventory().getScrollSlots();

                if (slot < 0 || slot >= targetArray.length) return;

                String equippedId = targetArray[slot];

                if (equippedId != null) {

                    // =========================================================
                    // GLOBALER CHANNELLING-BLOCK: SPERRT ALLE ANDEREN SKILLS
                    // =========================================================
                    if (GomuGatlingAbility.isChannelling()) {
                        // AUSNAHME: Wenn er die Gatling selbst drückt, erlauben wir es (für den Abbruch!)
                        if (!"gomu_gatling".equals(equippedId)) {
                            player.sendMessage(Text.literal("§cDu kannst während der Gatling keine anderen Fähigkeiten nutzen!"), true);
                            return; // Blockiert den Cast komplett auf dem Server
                        }
                    }

                    IAbility ability = ModAbilities.get(equippedId);
                    if (ability != null && !ability.isPassive()) {

                        // COOLDOWN-CHECK: Prüft über deine isReady() Methode aus der Composition!
                        if (!ability.isToggleable() && !abilityComp.getCooldowns().isReady(equippedId)) {
                            int remainingSecs = (abilityComp.getCooldowns().getRemainingTicks(equippedId) / 20) + 1;
                            player.sendMessage(Text.literal("§cFähigkeit hat noch Cooldown! (" + remainingSecs + "s)"), true);
                            return;
                        }

                        // Führt die Attacke aus (An- oder Ausschalten)
                        ability.execute(player);

                        // --- FIX: Cooldown NUR anwenden, wenn es KEINE Toggle-Fähigkeit ist ---
                        if (!ability.isToggleable()) {
                            abilityComp.getCooldowns().setCooldown(equippedId, ability.getCooldownTicks());
                        }

                        // Synchronisiert das komplette Ability-System live mit dem Client-HUD
                        Redline.ABILITY_COMPONENT.sync(player);
                    }
                }
            });
        });

        // =========================================================
        // 2. EQUIP ABILITY PACKET (Ausrüsten / Ablegen im Gitter)
        // =========================================================
        PayloadTypeRegistry.playC2S().register(EquipAbilityC2SPayload.ID, EquipAbilityC2SPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(EquipAbilityC2SPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                var abilityComp = Redline.ABILITY_COMPONENT.get(player);

                int slot = payload.slotIndex();
                String abilityId = payload.abilityId();
                boolean isKeybind = payload.isKeybind();

                // --- 1. LOGIK FÜR DAS DEEQUIPPEN (Rotes X-Button Klick) ---
                // Hier überspringen wir den Freischaltungscheck, da abilityId oft "none" ist
                if (slot == -1) {
                    String[] targetArray = isKeybind ? abilityComp.getInventory().getEquippedSlots() : abilityComp.getInventory().getScrollSlots();
                    int maxSlots = isKeybind ? 4 : 8;

                    // Sucht nach der Fähigkeit, die abgelegt werden soll, und leert den Slot
                    for (int i = 0; i < maxSlots; i++) {
                        // Wenn der Client eine spezifische ID schickt, löschen wir diese, andernfalls leeren wir den gewählten Slot
                        if (targetArray[i] != null && (targetArray[i].equals(abilityId) || "none".equals(abilityId))) {

                            // --- ANTI-EXPLOIT CHECK: Wurde die Königshaki Aura abgelegt? ---
                            if ("conq_aura".equals(targetArray[i])) {
                                var hakiComp = Redline.HAKI.get(player);
                                if (hakiComp.isHaoActive()) {
                                    hakiComp.setHaoActive(false); // Schaltet die Aura sofort ab
                                    Redline.HAKI.sync(player);    // Synchronisiert den Zustand sofort mit dem Client
                                }
                            }

                            targetArray[i] = null;
                        }
                    }

                    // Zwingt das HUD und den Screen zum sofortigen Neu-Render-Update
                    Redline.ABILITY_COMPONENT.sync(player);
                }
                // --- 2. LOGIK FÜR DAS AUSRÜSTEN (Gitter-Klick auf ein Icon) ---
                else {
                    // SICHERHEITS-CHECK (ANTI-CHEAT): Besitzt der Spieler diese Fähigkeit wirklich im Raster?
                    if (abilityComp.getInventory().hasUnlocked(abilityId)) {

                        // Nutzt deine Composition-Methode, die beide Seiten (Links/Rechts) fehlerfrei beschreibt
                        abilityComp.getInventory().equipToSlot(slot, abilityId, isKeybind);

                        // Zwingt das HUD und den Screen zum sofortigen Neu-Render-Update
                        Redline.ABILITY_COMPONENT.sync(player);
                    }
                }
            });
        });
        PayloadTypeRegistry.playC2S().register(
                CombatModeC2SPayload.ID,
                CombatModeC2SPayload.CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(
                CombatModeC2SPayload.ID,
                (payload, context) -> {
                    ServerPlayerEntity player = context.player();

                    Redline.COMBAT_COMPONENT.get(player).toggleCombatMode();
                }
        );
        PayloadTypeRegistry.playC2S().register(BusoTogglePayload.ID, BusoTogglePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(KenTogglePayload.ID, KenTogglePayload.CODEC);

        // 2. Server-Receiver für Busoshoku Haki (Taste J)
        ServerPlayNetworking.registerGlobalReceiver(BusoTogglePayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                var hakiComp = Redline.HAKI.get(player);
                var combatComp = Redline.COMBAT_COMPONENT.get(player);

                // Holt deine Teufelsfrucht-Komponente, um den Fluch abzufragen
                var dfComp = Redline.DEVIL_FRUIT.get(player);

                // --- 1. CORE CHECK: Prüft, ob Busoshoku gelernt wurde und Kampfmodus an ist ---
                if (!hakiComp.isBusoUnlocked() || !combatComp.isCombatModeEnabled()) {
                    player.sendMessage(Text.literal("§cʙᴜꜱᴏꜱʜᴏᴋᴜ ʜᴀᴋɪ ɪꜱ ɴᴏᴛ ᴜɴʟᴏᴄᴋᴇᴅ!"), true);
                    return;
                }

                // --- 2. SENSITIVER MEERWASSER-CHECK FOR DEVIL FRUITS ---
                // Blockiert das Einschalten komplett ohne Energie-Raschur, solange der Spieler nass ist!
                if (dfComp.hasFruit() && player.isTouchingWater()) {
                    player.sendMessage(Text.literal("§cDas Wasser lähmt deinen Körper, du kannst kein Haki aktivieren!"), true);
                    return;
                }

                boolean newState = !hakiComp.isBusoActive();

                if (newState && !hakiComp.canUseHaki(5f)) {
                    player.sendMessage(Text.literal("§cNot enough Haki energy!"), true);
                    return;
                }

                hakiComp.setBusoActive(newState);

                if (newState) {
                    player.getServerWorld().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.HAKI_ACTIVATE, SoundCategory.PLAYERS, 1.0f, 1.0f);

                    // =========================================================
                    // MULTIPLAYER POSEN-SYNC FÜR MITspieler (CLIENT-FIRST ERGÄNZUNG)
                    // =========================================================
                    net.minecraft.item.ItemStack hand = player.getMainHandStack();
                    boolean holdingWeapon = !hand.isEmpty() &&
                            (hand.getItem() instanceof net.minecraft.item.SwordItem ||
                                    hand.getItem() instanceof net.minecraft.item.MiningToolItem ||
                                    hand.getItem().toString().contains("sword") ||
                                    hand.getItem().toString().contains("weapon"));

                    // Sendet das Paket NUR an die Beobachter (player selbst hat es schon lokal via Keybind gestartet!)
                    for (ServerPlayerEntity trackingPlayer : PlayerLookup.tracking(player)) {
                        ServerPlayNetworking.send(trackingPlayer, new HakiAnimS2CPayload(player.getUuid(), holdingWeapon));
                    }
                }
            });
        });

        // 3. Server-Receiver für Kenbunshoku Haki (Taste G)
        ServerPlayNetworking.registerGlobalReceiver(KenTogglePayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                var hakiComp = Redline.HAKI.get(player);
                var combatComp = Redline.COMBAT_COMPONENT.get(player);

                // Holt deine Teufelsfrucht-Komponente für den Wasser-Check
                var dfComp = Redline.DEVIL_FRUIT.get(player);

                // --- 1. CORE CHECK: Prüft, ob Kenbunshoku gelernt wurde und Kampfmodus an ist ---
                if (!hakiComp.isKenUnlocked() || !combatComp.isCombatModeEnabled()) {
                    player.sendMessage(Text.literal("§cᴋᴇɴʙᴜɴꜱʜᴏᴋᴜ ʜᴀᴋɪ ɪꜱ ɴᴏᴛ ᴜɴʟᴏᴄᴋᴇᴅ!"), true);
                    return;
                }

                boolean newState = !hakiComp.isKenActive();

                // --- 2. SENSITIVER MEERWASSER-CHECK FOR DEVIL FRUITS ---
                if (newState && dfComp.hasFruit() && player.isTouchingWater()) {
                    player.sendMessage(Text.literal("§cDas Wasser lähmt deinen Körper, du kannst kein Haki aktivieren!"), true);
                    return;
                }

                if (newState && !hakiComp.canUseHaki(5f)) {
                    player.sendMessage(Text.literal("§cNot enough Haki energy!"), true);
                    return;
                }

                hakiComp.setKenActive(newState);

                // Sound-Effekte bei Aktivierung und Deaktivierung
                if (newState) {
                    player.getServerWorld().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.KEN_HAKI_ACTIVATE, SoundCategory.PLAYERS, 1.0f, 1.0f);
                } else {
                    player.getServerWorld().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.KEN_HAKI_DEACTIVATE, SoundCategory.PLAYERS, 1.0f, 1.0f);
                }
            });
        });
    }

    public static void registerS2CPackets() {

        PayloadTypeRegistry.playS2C().register(PistolS2CPayload.ID, PistolS2CPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(
                PistolS2CPayload.ID,
                (payload, context) -> {

                    context.client().execute(() -> {

                        MinecraftClient client = MinecraftClient.getInstance();
                        if (client.player == null) return;

                        RedlineClient.playPistol();
                    });
                }
        );

        // --- NEU: HAKI ANIMATION S2C ANMELDEN ---
        PayloadTypeRegistry.playS2C().register(HakiAnimS2CPayload.ID, HakiAnimS2CPayload.CODEC);

        // Der Empfänger für das Haki-Signal
        ClientPlayNetworking.registerGlobalReceiver(HakiAnimS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.world == null) return;

                // Sucht den anderen Spieler in der Welt und spielt die Pose ab
                var targetPlayer = client.world.getPlayerByUuid(payload.playerUuid());
                if (targetPlayer instanceof AbstractClientPlayerEntity clientPlayer) {
                    RedlineClient.playHakiAnim(clientPlayer, payload.hasWeapon());
                }
            });
        });

        PayloadTypeRegistry.playS2C().register(BazookaS2CPayload.ID, BazookaS2CPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(BazookaS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                // Ruft die Methode auf, die du mir vorhin geschickt hast!
                RedlineClient.playBazooka();
            });
        });

        PayloadTypeRegistry.playS2C().register(GatlingS2CPayload.ID, GatlingS2CPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(GatlingS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                RedlineClient.playGatling();
            });
        });

        PayloadTypeRegistry.playS2C().register(GatlingStopS2CPayload.ID, GatlingStopS2CPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(GatlingStopS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                RedlineClient.stopGatlingAnimation();
            });
        });
    }
}
