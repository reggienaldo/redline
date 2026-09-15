package net.reggie.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.reggie.Redline;

import java.util.List;

public class RaceCommand {

    // Liste der verfügbaren Rassen für die Tab-Vervollständigung im Chat
    private static final List<String> RACES = List.of("human", "fishman", "mink", "skypiean", "oni", "lunarian");

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            // Haupt-Argument für Admin-Befehle (Erfordert OP-Level 2)
            LiteralArgumentBuilder<ServerCommandSource> raceAdminBuilder = CommandManager.literal("race")
                    .requires(source -> source.hasPermissionLevel(2));

            // Haupt-Argument für Spieler-Befehle (Für JEDEN ohne OP verfügbar)
            LiteralArgumentBuilder<ServerCommandSource> racePlayerBuilder = CommandManager.literal("race");

            // =========================================================
            // INFO SUBCOMMAND (Für normale Spieler, kein OP nötig!)
            // =========================================================
            racePlayerBuilder.then(CommandManager.literal("info")
                    .executes(context -> sendRaceInfo(context.getSource())));

            // =========================================================
            // SET RACE SUBCOMMAND (Admin Only)
            // Syntax: /redline race set <Spieler> <Rasse>
            // =========================================================
            raceAdminBuilder.then(CommandManager.literal("set")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                            .then(CommandManager.argument("race_id", StringArgumentType.word())
                                    .suggests((context, builder) -> CommandSource.suggestMatching(RACES, builder)) // Tab-Suggestions
                                    .executes(context -> setPlayerRace(
                                            context.getSource(),
                                            EntityArgumentType.getPlayer(context, "player"),
                                            StringArgumentType.getString(context, "race_id")
                                    )))));

            // Registrierung unter /redline (Kombiniert Spieler- und Admin-Zweige)
            dispatcher.register(CommandManager.literal("redline")
                    .then(racePlayerBuilder)
                    .then(raceAdminBuilder)
            );
        });
    }

    // --- LOGIK-METHODEN ---

    private static int sendRaceInfo(ServerCommandSource source) {
        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            var raceComp = Redline.RACE.get(player);

            String currentRace = raceComp.getRaceId().toUpperCase();
            String passives = "";

            if (raceComp.isHuman()) {
                passives = "§7- Keine besonderen Vor- oder Nachteile (Ausgeglichen)";
            } else if (raceComp.isFishman()) {
                passives = "§b- Unendliche Unterwasseratmung\n§b- Extrem schnelles Schwimmen\n§b- Immun gegen den Teufelsfrucht-Wasserfluch!";
            } else if (raceComp.isMink()) {
                passives = "§e- Permanenter Schnelligkeits-Buff (Speed I)";
            } else if (raceComp.isSkypiean()) {
                passives = "§f- Permanenter Sprungkraft-Buff (Jump Boost II)\n§f- Absolut immun gegen Fallschaden!";
            } else if (raceComp.isOni()) { // --- NEU ---
                passives = "§c- Unbändige Urgewalt (+2 flacher DMG)\n§c- Erhöhte Zähigkeit (+4 maximale HP)";
            } else if (raceComp.isLunarian()) { // --- NEU ---
                passives = "§6- Absolut immun gegen jeglichen Feuerschaden & Lava!\n§6- Permanente körpereigene Panzerung (Resistance I)";
            }

            String finalPassives = passives;
            source.sendFeedback(() -> Text.literal(" \n§b=== DEINE RASSE ===" +
                    "\n§fRasse: §d§l" + currentRace +
                    "\n§fPassive Fähigkeiten:\n" + finalPassives +
                    "\n§b====================="), false);
            return 1;
        } catch (Exception e) {
            source.sendError(Text.literal("Dieser Befehl kann nur von einem Spieler ausgeführt werden!"));
            return 0;
        }
    }

    private static int setPlayerRace(ServerCommandSource source, ServerPlayerEntity target, String raceId) {
        if (!RACES.contains(raceId)) {
            source.sendError(Text.literal("Ungültige Rasse! Verfügbar: " + RACES));
            return 0;
        }

        var raceComp = Redline.RACE.get(target);
        raceComp.setRace(raceId);

        String formatName = raceId.toUpperCase();
        source.sendFeedback(() -> Text.literal("§aRasse von " + target.getName().getString() + " auf §d§l" + formatName + "§a gesetzt!"), true);
        return 1;
    }
}
