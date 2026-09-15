package net.reggie.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.reggie.Redline;
import net.reggie.game.doriki.IDorikiComponent;

public class DorikiCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            // Haupt-Argument für Admin-Befehle (Erfordert OP-Level 2)
            LiteralArgumentBuilder<ServerCommandSource> dorikiAdminBuilder = CommandManager.literal("doriki")
                    .requires(source -> source.hasPermissionLevel(2));

            // Haupt-Argument für Spieler-Befehle (Für JEDEN ohne OP verfügbar)
            LiteralArgumentBuilder<ServerCommandSource> dorikiPlayerBuilder = CommandManager.literal("doriki");

            // =========================================================
            // INFO SUBCOMMAND (Für normale Spieler, kein OP nötig!)
            // =========================================================
            dorikiPlayerBuilder.then(CommandManager.literal("info")
                    .executes(context -> sendDorikiInfo(context.getSource())));

            // =========================================================
            // SET SUBCOMMAND (Admin Only)
            // =========================================================
            dorikiAdminBuilder.then(CommandManager.literal("set")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                            // LongArgumentType.longArg(0L) erlaubt nun alle Zahlen ab 0 aufwärts ohne Deckelung!
                            .then(CommandManager.argument("value", LongArgumentType.longArg(0L))
                                    .executes(context -> setDorikiValue(
                                            context.getSource(),
                                            EntityArgumentType.getPlayer(context, "player"),
                                            LongArgumentType.getLong(context, "value")
                                    )))));

            // =========================================================
            // ADD SUBCOMMAND (Admin Only)
            // =========================================================
            dorikiAdminBuilder.then(CommandManager.literal("add")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                            // KORREKTUR: LongArgumentType.longArg(1L) erlaubt nun das Hinzufügen
                            // jeder beliebigen Zahl ab 1 aufwärts, ohne Limit!
                            .then(CommandManager.argument("value", LongArgumentType.longArg(1L))
                                    .executes(context -> addDorikiValue(
                                            context.getSource(),
                                            EntityArgumentType.getPlayer(context, "player"),
                                            LongArgumentType.getLong(context, "value")
                                    )))));

            // Registrierung unter /redline (Kombiniert Spieler- und Admin-Zweige)
            dispatcher.register(CommandManager.literal("redline")
                    .then(dorikiPlayerBuilder)
                    .then(dorikiAdminBuilder)
            );
        });
    }

    // --- LOGIK-METHODEN ---

    private static int sendDorikiInfo(ServerCommandSource source) {
        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            IDorikiComponent dorikiComp = Redline.DORIKI.get(player);

            source.sendFeedback(() -> Text.literal(" \n§a=== DEIN RPG STATUS ===" +
                    "\n§fAktuelles Doriki: §e" + dorikiComp.getDoriki() + " §7/ 10000" +
                    "\n§fHP-Bonus: §c+" + (int) dorikiComp.getHealthBonus() + " HP §7(+" + ((int) dorikiComp.getHealthBonus() / 2) + " Herzen)" +
                    "\n§fAngriffs-Bonus: §2+" + (int) dorikiComp.getDamageBonus() + " DMG" +
                    "\n§a========================"), false);
            return 1;
        } catch (Exception e) {
            source.sendError(Text.literal("Dieser Befehl kann nur von einem Spieler ausgeführt werden!"));
            return 0;
        }
    }

    private static int setDorikiValue(ServerCommandSource source, ServerPlayerEntity target, long value) {
        IDorikiComponent dorikiComp = Redline.DORIKI.get(target);
        dorikiComp.setDoriki(value);

        source.sendFeedback(() -> Text.literal("§aDoriki von " + target.getName().getString() + " auf §e" + value + "§a gesetzt!"), true);
        return 1;
    }

    private static int addDorikiValue(ServerCommandSource source, ServerPlayerEntity target, long value) {
        IDorikiComponent dorikiComp = Redline.DORIKI.get(target);
        dorikiComp.addDoriki(value);

        source.sendFeedback(() -> Text.literal("§e" + value + "§a Doriki zu " + target.getName().getString() + " hinzugefügt!"), true);
        return 1;
    }
}