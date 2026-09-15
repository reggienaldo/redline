package net.reggie.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.reggie.Redline;
import net.reggie.RedlineClient;
import net.reggie.gui.TechniqueInventoryScreen;
import net.reggie.network.C2S.*;
import org.lwjgl.glfw.GLFW;

public class KeyInputHandler {
    public static final String KEY_CATEGORY = "key.category.redline.redline";
    public static final String KEY_BUSOSHOKU_ACTIVATION = "key.redline.busoshoku_activation";
    public static final String KEY_KENBUNSHOKU_ACTIVATION = "key.redline.kenbunshoku_activation";
    public static final String KEY_COMBAT_MODE = "key.redline.combat_mode";
    public static final String KEY_TECHNIQUE_INVENTORY = "key.redline.technique_inventory";

    public static KeyBinding busoshokuKey;
    public static KeyBinding kenbunshokuKey;
    public static KeyBinding combatKey;
    public static KeyBinding techniqueInventoryKey;

    public static KeyBinding abilitySlot1Key;
    public static KeyBinding abilitySlot2Key;
    public static KeyBinding abilitySlot3Key;
    public static KeyBinding abilitySlot4Key;

    public static void registerKeyInputs() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // --- COMBAT MODE TOGGLE ---
            if (combatKey.wasPressed()) {
                ClientPlayNetworking.send(new CombatModeC2SPayload());
            }

            // --- BUSOSHOKU HAKI TOGGLE (Rüstungshaki) ---
            if (busoshokuKey.wasPressed()) {
                var hakiComp = Redline.HAKI.get(client.player);
                var dfComp = Redline.DEVIL_FRUIT.get(client.player);

                if (!hakiComp.isBusoUnlocked()) {
                    client.player.sendMessage(Text.literal("§cʙᴜꜱᴏꜱʜᴏᴋᴜ ʜᴀᴋɪ ɪꜱ ɴᴏᴛ ᴜɴʟᴏᴄᴋᴇᴅ!"), true);
                    return;
                }

                if (!Redline.COMBAT_COMPONENT.get(client.player).isCombatModeEnabled()) {
                    client.player.sendMessage(Text.literal("§cᴀᴄᴛɪᴠᴀᴛᴇ ᴄᴏᴍʙᴀᴛ ᴍᴏᴅᴇ ꜰɪʀꜱᴛ!"), true);
                    return;
                }

                // --- KEYBIND-BLOCK: Verhindert die Animation, wenn man im Wasser ist ---
                if (dfComp.hasFruit() && client.player.isTouchingWater()) {
                    client.player.sendMessage(Text.literal("§cDas Wasser lähmt deinen Körper!"), true);
                    return; // Bricht ab, BEVOR die Animation oder das Paket gesendet wird!
                }

                // --- SCHLÜSSEL-FIX: Animation startet SOFORT lokal auf deinem Bildschirm ---
                ItemStack hand = client.player.getMainHandStack();
                boolean holdingWeapon = !hand.isEmpty() &&
                        (hand.getItem() instanceof net.minecraft.item.SwordItem ||
                                hand.getItem() instanceof net.minecraft.item.MiningToolItem ||
                                hand.getItem().toString().contains("sword") ||
                                hand.getItem().toString().contains("weapon"));

                // Spielt die Animation direkt auf deinem eigenen Client ab (Multiplayer-Safe)
                RedlineClient.playHakiAnim(client.player, holdingWeapon);

                ClientPlayNetworking.send(new BusoTogglePayload()); // Server ruft hakiComp.toggleBusoshoku() auf
            }

            // --- KENBUNSHOKU HAKI TOGGLE (Beobachtungshaki) ---
            if (kenbunshokuKey.wasPressed()) {
                var hakiComp = Redline.HAKI.get(client.player);

                if (!hakiComp.isKenUnlocked()) {
                    client.player.sendMessage(Text.literal("§cᴋᴇɴʙᴜɴꜱʜᴏᴋᴜ ʜᴀᴋɪ ɪꜱ ɴᴏᴛ ᴜɴʟᴏᴄᴋᴇᴅ!"), true);
                    return;
                }

                if (!Redline.COMBAT_COMPONENT.get(client.player).isCombatModeEnabled()) {
                    client.player.sendMessage(Text.literal("§cᴀᴄᴛɪᴠᴀᴛᴇ ᴄᴏᴍʙᴀᴛ ᴍᴏᴅᴇ ꜰɪʀꜱᴛ!"), true);
                    return;
                }

                ClientPlayNetworking.send(new KenTogglePayload()); // Server ruft hakiComp.toggleKenbunshoku() auf
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // Die Tasten reagieren nur, wenn der Combat Mode aktiv ist!
            boolean combatActive = Redline.COMBAT_COMPONENT.get(client.player).isCombatModeEnabled();
            if (!combatActive) return;

            if (abilitySlot1Key.wasPressed()) ClientPlayNetworking.send(new UseAbilityC2SPayload(0, true));
            if (abilitySlot2Key.wasPressed()) ClientPlayNetworking.send(new UseAbilityC2SPayload(1, true));
            if (abilitySlot3Key.wasPressed()) ClientPlayNetworking.send(new UseAbilityC2SPayload(2, true));
            if (abilitySlot4Key.wasPressed()) ClientPlayNetworking.send(new UseAbilityC2SPayload(3, true));
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (techniqueInventoryKey.wasPressed()) {
                var player = MinecraftClient.getInstance().player;
                if (player != null) {
                    var abilityComp = Redline.ABILITY_COMPONENT.get(player);

                    MinecraftClient.getInstance().setScreen(new TechniqueInventoryScreen(abilityComp));
                }
            }
        });
    }

    public static void register() {
        combatKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_COMBAT_MODE,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R, // Beispielbelegung für den Combat Mode
                KEY_CATEGORY
        ));

        busoshokuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_BUSOSHOKU_ACTIVATION,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J, // Standardbelegung für Rüstungshaki
                KEY_CATEGORY
        ));

        kenbunshokuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_KENBUNSHOKU_ACTIVATION,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G, // Standardbelegung für Beobachtungshaki
                KEY_CATEGORY
        ));
        techniqueInventoryKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_TECHNIQUE_INVENTORY,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H, // Standardbelegung 'H' für das JJK Technique Inventory
                KEY_CATEGORY
        ));

        abilitySlot1Key = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.redline.ability_1", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Z, KEY_CATEGORY));
        abilitySlot2Key = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.redline.ability_2", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X, KEY_CATEGORY));
        abilitySlot3Key = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.redline.ability_3", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, KEY_CATEGORY));
        abilitySlot4Key = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.redline.ability_4", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, KEY_CATEGORY));

        registerKeyInputs();
    }
}
