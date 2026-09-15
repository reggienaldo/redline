package net.reggie.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.reggie.Redline;
import net.reggie.game.haki.IHakiComponent;

public class HakiCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            // Haupt-Argument für Admin-Befehle (Erfordert OP-Level 2)
            LiteralArgumentBuilder<ServerCommandSource> hakiAdminBuilder = CommandManager.literal("haki")
                    .requires(source -> source.hasPermissionLevel(2));

            // Haupt-Argument für Spieler-Befehle (Für JEDEN ohne OP verfügbar)
            LiteralArgumentBuilder<ServerCommandSource> hakiPlayerBuilder = CommandManager.literal("haki");

            // =========================================================
            // INFO SUBCOMMAND (Für normale Spieler, kein OP nötig!)
            // =========================================================
            hakiPlayerBuilder.then(CommandManager.literal("info")
                    .executes(context -> sendHakiInfo(context.getSource())));

            // =========================================================
            // SET SUBCOMMAND (Admin Only)
            // =========================================================
            hakiAdminBuilder.then(CommandManager.literal("set")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                            .then(CommandManager.literal("busoshoku")
                                    .then(CommandManager.argument("value", LongArgumentType.longArg(0L, 1000000000L))
                                            .executes(context -> setHakiValue(context.getSource(), EntityArgumentType.getPlayer(context, "player"), "busoshoku", LongArgumentType.getLong(context, "value")))))
                            .then(CommandManager.literal("kenbunshoku")
                                    .then(CommandManager.argument("value", LongArgumentType.longArg(0L, 1000000000L))
                                            .executes(context -> setHakiValue(context.getSource(), EntityArgumentType.getPlayer(context, "player"), "kenbunshoku", LongArgumentType.getLong(context, "value")))))
                            .then(CommandManager.literal("haoshoku")
                                    .then(CommandManager.argument("value", LongArgumentType.longArg(0L, 1000000000L))
                                            .executes(context -> setHakiValue(context.getSource(), EntityArgumentType.getPlayer(context, "player"), "haoshoku", LongArgumentType.getLong(context, "value")))))));

            // =========================================================
            // ADD SUBCOMMAND (Admin Only)
            // =========================================================
            hakiAdminBuilder.then(CommandManager.literal("add")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                            .then(CommandManager.literal("busoshoku")
                                    .then(CommandManager.argument("value", LongArgumentType.longArg(1L, 100000000L))
                                            .executes(context -> addHakiValue(context.getSource(), EntityArgumentType.getPlayer(context, "player"), "busoshoku", LongArgumentType.getLong(context, "value")))))
                            .then(CommandManager.literal("kenbunshoku")
                                    .then(CommandManager.argument("value", LongArgumentType.longArg(1L, 100000000L))
                                            .executes(context -> addHakiValue(context.getSource(), EntityArgumentType.getPlayer(context, "player"), "kenbunshoku", LongArgumentType.getLong(context, "value")))))
                            .then(CommandManager.literal("haoshoku")
                                    .then(CommandManager.argument("value", LongArgumentType.longArg(1L, 100000000L))
                                            .executes(context -> addHakiValue(context.getSource(), EntityArgumentType.getPlayer(context, "player"), "haoshoku", LongArgumentType.getLong(context, "value")))))));

            // =========================================================
            // GRANT BUSOSHOKU SUBCOMMAND (Admin Only)
            // =========================================================
            hakiAdminBuilder.then(CommandManager.literal("grantBusoshoku")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                            .then(CommandManager.argument("allowed", BoolArgumentType.bool())
                                    .executes(context -> grantHakiLogic(context.getSource(), EntityArgumentType.getPlayer(context, "player"), "busoshoku", BoolArgumentType.getBool(context, "allowed"))))));

            // =========================================================
            // GRANT KENBUNSHOKU SUBCOMMAND (Admin Only)
            // =========================================================
            hakiAdminBuilder.then(CommandManager.literal("grantKenbunshoku")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                            .then(CommandManager.argument("allowed", BoolArgumentType.bool())
                                    .executes(context -> grantHakiLogic(context.getSource(), EntityArgumentType.getPlayer(context, "player"), "kenbunshoku", BoolArgumentType.getBool(context, "allowed"))))));

            // =========================================================
            // GRANT HAOSHOKU SUBCOMMAND (Admin Only)
            // =========================================================
            hakiAdminBuilder.then(CommandManager.literal("grantHaoshoku")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                            .then(CommandManager.argument("allowed", BoolArgumentType.bool())
                                    .executes(context -> grantHakiLogic(context.getSource(), EntityArgumentType.getPlayer(context, "player"), "haoshoku", BoolArgumentType.getBool(context, "allowed"))))));

            // =========================================================
            // COLOR SUBCOMMAND (Admin Only)
            // =========================================================
            hakiAdminBuilder.then(CommandManager.literal("color")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                            .then(CommandManager.literal("red").executes(context -> setRyouColorLogic(context.getSource(), EntityArgumentType.getPlayer(context, "player"), "red", 0)))
                            .then(CommandManager.literal("blue").executes(context -> setRyouColorLogic(context.getSource(), EntityArgumentType.getPlayer(context, "player"), "blue", 1)))
                            .then(CommandManager.literal("purple").executes(context -> setRyouColorLogic(context.getSource(), EntityArgumentType.getPlayer(context, "player"), "purple", 2)))
                            .then(CommandManager.literal("gold").executes(context -> setRyouColorLogic(context.getSource(), EntityArgumentType.getPlayer(context, "player"), "gold", 3)))
                            .then(CommandManager.literal("cyan").executes(context -> setRyouColorLogic(context.getSource(), EntityArgumentType.getPlayer(context, "player"), "cyan", 4)))
                            .then(CommandManager.literal("green").executes(context -> setRyouColorLogic(context.getSource(), EntityArgumentType.getPlayer(context, "player"), "green", 5)))
                            .then(CommandManager.literal("orange").executes(context -> setRyouColorLogic(context.getSource(), EntityArgumentType.getPlayer(context, "player"), "orange", 6)))));

            // Registrierung unter /redline
            dispatcher.register(CommandManager.literal("redline")
                    .then(hakiPlayerBuilder)
                    .then(hakiAdminBuilder)
            );
        });
    }

    private static int sendHakiInfo(ServerCommandSource source) {
        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            IHakiComponent haki = Redline.HAKI.get(player);

            source.sendFeedback(() -> Text.literal(" \n§d============= §5§lDeine Haki Info §d============="), false);
            source.sendFeedback(() -> Text.literal("§7Gesamt-Haki XP: §f" + haki.getTotalXp()), false);
            source.sendFeedback(() -> Text.literal("§7Haki-Pool: §e" + (int)haki.getHaki() + " §7/ §6" + (int)haki.getMaxHaki()), false);
            source.sendFeedback(() -> Text.literal("---------------------------------------------"), false);

            final String busoStages;
            if (haki.isInternalDestructionUnlocked()) busoStages = "Basic -> Ryou -> Emission -> Internal Destr.";
            else if (haki.isEmissionUnlocked()) busoStages = "Basic -> Ryou -> Emission";
            else if (haki.isRyouUnlocked()) busoStages = "Basic -> Ryou";
            else busoStages = "Basic";

            source.sendFeedback(() -> Text.literal("§8[§7J§8] §b§lBusoshoku (Rüstung): §fLevel " + haki.getBusoLevel() + " §7(" + haki.getBusoXp() + " XP)\n§7    ↳ Unlocks: " + busoStages), false);

            final String kenStages;
            if (haki.isFutureSightUnlocked()) kenStages = "Basic -> Advanced -> Future Sight";
            else if (haki.isAdvancedObservationUnlocked()) kenStages = "Basic -> Advanced";
            else kenStages = "Basic";

            source.sendFeedback(() -> Text.literal("§8[§7G§8] §3§lKenbunshoku (Beobachtung): §fLevel " + haki.getKenLevel() + " §7(" + haki.getKenXp() + " XP)\n§7    ↳ Unlocks: " + kenStages), false);

            if (haki.isHaoUnlocked()) {
                final String haoStages;
                if (haki.hasConquerorCoating()) haoStages = "Erwacht -> Aura -> Coating";
                else if (haki.hasConquerorAura()) haoStages = "Erwacht -> Aura";
                else haoStages = "Erwacht";

                source.sendFeedback(() -> Text.literal("§5§lHaoshoku (Königshaki): §fLevel " + haki.getHaoLevel() + " §7(" + haki.getHaoXp() + " XP)\n§7    ↳ Unlocks: " + haoStages), false);
            } else {
                source.sendFeedback(() -> Text.literal("§5§lHaoshoku (Königshaki): §c§oNicht erwacht / Keine Veranlagung"), false);
            }

            source.sendFeedback(() -> Text.literal("§d==========================================="), false);

            return 1;
        } catch (Exception e) {
            source.sendError(Text.literal("Dieser Befehl kann nur von einem Spieler im Spiel ausgeführt werden!"));
            return 0;
        }
    }

    private static int setHakiValue(ServerCommandSource source, ServerPlayerEntity player, String type, long value) {
        IHakiComponent haki = Redline.HAKI.get(player);

        switch (type) {
            case "busoshoku" -> {
                if (!haki.isBusoUnlocked()) {
                    source.sendError(Text.literal("Dieser Spieler hat Busoshoku Haki noch nicht gelernt! Schalte es zuerst frei.").formatted(Formatting.RED));
                    return 0;
                }
                long currentXp = haki.getBusoXp();
                haki.addBusoXp(value - currentXp);
                final int finalLevel = haki.getBusoLevel();
                source.sendFeedback(() -> Text.literal("Busoshoku XP für " + player.getName().getString() + " auf " + value + " gesetzt. (Level: " + finalLevel + ")").formatted(Formatting.GOLD), true);
            }
            case "kenbunshoku" -> {
                if (!haki.isKenUnlocked()) {
                    source.sendError(Text.literal("Dieser Spieler hat Kenbunshoku Haki noch nicht gelernt! Schalte es zuerst frei.").formatted(Formatting.RED));
                    return 0;
                }
                long currentXp = haki.getKenXp();
                haki.addKenXp(value - currentXp);
                final int finalLevel = haki.getKenLevel();
                source.sendFeedback(() -> Text.literal("Kenbunshoku XP für " + player.getName().getString() + " auf " + value + " gesetzt. (Level: " + finalLevel + ")").formatted(Formatting.GOLD), true);
            }
            case "haoshoku" -> {
                if (!haki.isHaoUnlocked()) {
                    source.sendError(Text.literal("Dieser Spieler besitzt kein Königshaki! Schalte es zuerst mit 'grantHaoshoku' frei.").formatted(Formatting.RED));
                    return 0;
                }
                long currentXp = haki.getHaoXp();
                haki.addHaoXp(value - currentXp);
                final int finalLevel = haki.getHaoLevel();
                source.sendFeedback(() -> Text.literal("Haoshoku XP für " + player.getName().getString() + " auf " + value + " gesetzt. (Level: " + finalLevel + ")").formatted(Formatting.GOLD), true);
            }
        }
        return 1;
    }

    private static int addHakiValue(ServerCommandSource source, ServerPlayerEntity player, String type, long value) {
        IHakiComponent haki = Redline.HAKI.get(player);

        switch (type) {
            case "busoshoku" -> {
                if (!haki.isBusoUnlocked()) {
                    source.sendError(Text.literal("Dieser Spieler hat Busoshoku Haki noch nicht gelernt! XP können nicht hinzugefügt werden.").formatted(Formatting.RED));
                    return 0;
                }
                haki.addBusoXp(value);
                final long finalXp = haki.getBusoXp();
                source.sendFeedback(() -> Text.literal(value + " XP zu Busoshoku von " + player.getName().getString() + " hinzugefügt. (Gesamt: " + finalXp + ")").formatted(Formatting.GOLD), true);
            }
            case "kenbunshoku" -> {
                if (!haki.isKenUnlocked()) {
                    source.sendError(Text.literal("Dieser Spieler hat Kenbunshoku Haki noch nicht gelernt! XP können nicht hinzugefügt werden.").formatted(Formatting.RED));
                    return 0;
                }
                haki.addKenXp(value);
                final long finalXp = haki.getKenXp();
                source.sendFeedback(() -> Text.literal(value + " XP zu Kenbunshoku von " + player.getName().getString() + " hinzugefügt. (Gesamt: " + finalXp + ")").formatted(Formatting.GOLD), true);
            }
            case "haoshoku" -> {
                if (!haki.isHaoUnlocked()) {
                    source.sendError(Text.literal("Dieser Spieler besitzt kein Königshaki! Schalte es zuerst mit 'grantHaoshoku' frei.").formatted(Formatting.RED));
                    return 0;
                }
                haki.addHaoXp(value);
                final long finalXp = haki.getHaoXp();
                source.sendFeedback(() -> Text.literal(value + " XP zu Haoshoku von " + player.getName().getString() + " hinzugefügt. (Gesamt: " + finalXp + ")").formatted(Formatting.GOLD), true);
            }
        }
        return 1;
    }

    private static int grantHakiLogic(ServerCommandSource source, ServerPlayerEntity player, String type, boolean allowed) {
        IHakiComponent haki = Redline.HAKI.get(player);

        switch (type) {
            case "busoshoku" -> {
                haki.setBusoUnlocked(allowed);
                if (!allowed) haki.addBusoXp(-haki.getBusoXp());
                source.sendFeedback(() -> Text.literal(player.getName().getString() + (allowed ? " §ahat Busoshoku Haki gelernt!" : " §chat Busoshoku Haki verloren.")).formatted(Formatting.GOLD), true);
            }
            case "kenbunshoku" -> {
                haki.setKenUnlocked(allowed);
                if (!allowed) haki.addKenXp(-haki.getKenXp());
                source.sendFeedback(() -> Text.literal(player.getName().getString() + (allowed ? " §ahat Kenbunshoku Haki gelernt!" : " §chat Kenbunshoku Haki verloren.")).formatted(Formatting.GOLD), true);
            }
            case "haoshoku" -> {
                haki.setHaoUnlocked(allowed);
                if (!allowed) haki.addHaoXp(-haki.getHaoXp());
                source.sendFeedback(() -> Text.literal(player.getName().getString() + (allowed ? " §awurde die Veranlagung für Haoshoku Haki gewährt!" : " §chat das Haoshoku Haki verloren.")).formatted(Formatting.GOLD), true);
            }
        }
        return 1;
    }

    private static int setRyouColorLogic(ServerCommandSource source, ServerPlayerEntity player, String colorName, int colorId) {
        IHakiComponent haki = Redline.HAKI.get(player);
        haki.setRyouColorIndex(colorId);

        source.sendFeedback(() -> Text.literal("Ryou-Farbe für " + player.getName().getString() + " auf " + colorName.toUpperCase() + " geändert.").formatted(Formatting.AQUA), true);
        return 1;
    }
}