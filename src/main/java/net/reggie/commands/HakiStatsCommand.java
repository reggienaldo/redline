package net.reggie.commands;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.reggie.Redline;
import net.reggie.game.haki.IHakiComponent;

public class HakiStatsCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            // Registriert den Befehl "/haki" für ALLE Spieler (kein OP benötigt)
            dispatcher.register(CommandManager.literal("haki")
                    .executes(context -> showHakiStats(context.getSource()))
            );

            // Optionaler Alias "/hakistats", falls "/haki" mit anderen Mods kollidiert
            dispatcher.register(CommandManager.literal("hakistats")
                    .executes(context -> showHakiStats(context.getSource()))
            );
        });
    }

    private static int showHakiStats(ServerCommandSource source) {
        try {
            // Prüft, ob der Befehl von einem echten Spieler gesendet wurde
            ServerPlayerEntity player = source.getPlayerOrThrow();
            IHakiComponent haki = Redline.HAKI.get(player);

            // Alle veränderlichen Werte VOR den Lambdas in finale Variablen schreiben
            final long totalXp = haki.getTotalXp();
            final int currentHaki = (int) haki.getHaki();
            final int maxHaki = (int) haki.getMaxHaki();

            final int busoLevel = haki.getBusoLevel();
            final long busoXp = haki.getBusoXp();

            final int kenLevel = haki.getKenLevel();
            final long kenXp = haki.getKenXp();

            final boolean haoUnlocked = haki.isHaoUnlocked();
            final int haoLevel = haki.getHaoLevel();
            final long haoXp = haki.getHaoXp();

            // Meilenstein-Pfade berechnen
            String tempBusoMilestones = "§8[§7Standard Hardening§8]";
            if (haki.isInternalDestructionUnlocked()) tempBusoMilestones = "§aHardening §7➔ §aRyou §7➔ §aEmission §7➔ §6Internal Destr.";
            else if (haki.isEmissionUnlocked()) tempBusoMilestones = "§aHardening §7➔ §aRyou §7➔ §aEmission";
            else if (haki.isRyouUnlocked()) tempBusoMilestones = "§aHardening §7➔ §aRyou";
            final String busoMilestones = tempBusoMilestones;

            String tempKenMilestones = "§8[§7Standard Wahrnehmung§8]";
            if (haki.isFutureSightUnlocked()) tempKenMilestones = "§aWahrnehmung §7➔ §aAdvanced §7➔ §9Future Sight";
            else if (haki.isAdvancedObservationUnlocked()) tempKenMilestones = "§aWahrnehmung §7➔ §aAdvanced";
            final String kenMilestones = tempKenMilestones;

            String tempHaoMilestones = "§aErwacht";
            if (haki.hasConquerorCoating()) tempHaoMilestones = "§aErwacht §7➔ §aAura §7➔ §5Conqueror's Coating";
            else if (haki.hasConquerorAura()) tempHaoMilestones = "§aErwacht §7➔ §aAura";
            final String haoMilestones = tempHaoMilestones;

            // =========================================================
            // HUD / CHAT OUTPUT VIA FEEDBACK-SUPPLIER
            // =========================================================
            source.sendFeedback(() -> Text.literal(" \n§d⚡ =============== §5§lHAKI STATUS §d=============== ⚡"), false);
            source.sendFeedback(() -> Text.literal(" §7Gesamtfortschritt: §f" + totalXp + " Gesamt-XP"), false);
            source.sendFeedback(() -> Text.literal(" §7Energie-Reserven:  §e" + currentHaki + " §7/ §6" + maxHaki + " Haki"), false);
            source.sendFeedback(() -> Text.literal("§d--------------------------------------------------"), false);

            // 1. Busoshoku Haki (Rüstung)
            source.sendFeedback(() -> Text.literal(" §b§lBusoshoku Haki §8(Taste: §7J§8)"), false);
            source.sendFeedback(() -> Text.literal("   §7Stufe: §fLevel " + busoLevel + " §8| §7Erfahrung: §f" + busoXp + " XP"), false);
            source.sendFeedback(() -> Text.literal("   §7Pfad:  " + busoMilestones), false);
            source.sendFeedback(() -> Text.literal(""), false);

            // 2. Kenbunshoku Haki (Beobachtung)
            source.sendFeedback(() -> Text.literal(" §3§lKenbunshoku Haki §8(Taste: §7G§8)"), false);
            source.sendFeedback(() -> Text.literal("   §7Stufe: §fLevel " + kenLevel + " §8| §7Erfahrung: §f" + kenXp + " XP"), false);
            source.sendFeedback(() -> Text.literal("   §7Pfad:  " + kenMilestones), false);
            source.sendFeedback(() -> Text.literal(""), false);

            // 3. Haoshoku Haki (Königshaki)
            if (haoUnlocked) {
                source.sendFeedback(() -> Text.literal(" §5§lHaoshoku Haki §8(§dKönigliche Veranlagung§8)"), false);
                source.sendFeedback(() -> Text.literal("   §7Stufe: §fLevel " + haoLevel + " §8| §7Erfahrung: §f" + haoXp + " XP"), false);
                source.sendFeedback(() -> Text.literal("   §7Pfad:  " + haoMilestones), false);
            } else {
                source.sendFeedback(() -> Text.literal(" §5§lHaoshoku Haki"), false);
                source.sendFeedback(() -> Text.literal("   §c§oDu besitzt keine königliche Veranlagung..."), false);
            }

            source.sendFeedback(() -> Text.literal("§d=================================================="), false);
            return 1;

        } catch (Exception e) {
            source.sendError(Text.literal("Dieser Befehl kann nur von Spielern im Spiel genutzt werden!"));
            return 0;
        }
    }
}
