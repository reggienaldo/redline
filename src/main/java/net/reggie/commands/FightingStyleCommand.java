package net.reggie.commands;

import com.mojang.brigadier.arguments.LongArgumentType;
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
import net.reggie.game.styles.IFightingStyleComponent;

import java.util.List;

public class FightingStyleCommand {

    // Liste der verfügbaren Kampfstile für die Tab-Vervollständigung im Chat
    private static final List<String> STYLES = List.of("none", "black_leg", "fishman_karate", "rokushiki");

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            // Haupt-Argument für Admin-Befehle (Erfordert OP-Level 2)
            LiteralArgumentBuilder<ServerCommandSource> styleAdminBuilder = CommandManager.literal("style")
                    .requires(source -> source.hasPermissionLevel(2));

            // Haupt-Argument für Spieler-Befehle (Für JEDEN ohne OP verfügbar)
            LiteralArgumentBuilder<ServerCommandSource> stylePlayerBuilder = CommandManager.literal("style");

            // =========================================================
            // INFO SUBCOMMAND (Für normale Spieler, kein OP nötig!)
            // =========================================================
            stylePlayerBuilder.then(CommandManager.literal("info")
                    .executes(context -> sendStyleInfo(context.getSource())));

            // =========================================================
            // SET STYLE SUBCOMMAND (Admin Only)
            // Syntax: /redline style set <Spieler> <Stil>
            // =========================================================
            styleAdminBuilder.then(CommandManager.literal("set")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                            .then(CommandManager.argument("style_id", StringArgumentType.word())
                                    .suggests((context, builder) -> CommandSource.suggestMatching(STYLES, builder)) // Tab-Suggestions
                                    .executes(context -> setFightingStyle(
                                            context.getSource(),
                                            EntityArgumentType.getPlayer(context, "player"),
                                            StringArgumentType.getString(context, "style_id")
                                    )))));

            // =========================================================
            // ADD XP SUBCOMMAND (Admin Only)
            // Syntax: /redline style addXp <Spieler> <Anzahl>
            // =========================================================
            styleAdminBuilder.then(CommandManager.literal("addXp")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                            .then(CommandManager.argument("xp_value", LongArgumentType.longArg(1L)) // Mindestens 1 XP
                                    .executes(context -> addStyleXp(
                                            context.getSource(),
                                            EntityArgumentType.getPlayer(context, "player"),
                                            LongArgumentType.getLong(context, "xp_value")
                                    )))));

            // Registrierung unter /redline (Kombiniert Spieler- und Admin-Zweige)
            dispatcher.register(CommandManager.literal("redline")
                    .then(stylePlayerBuilder)
                    .then(styleAdminBuilder)
            );
        });
    }

    // --- LOGIK-METHODEN ---

    private static int sendStyleInfo(ServerCommandSource source) {
        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            IFightingStyleComponent styleComp = Redline.FIGHTING_STYLE.get(player);

            String currentStyle = styleComp.getStyleId().toUpperCase().replace("_", " ");

            source.sendFeedback(() -> Text.literal(" \n§6=== KAMPFSTIL STATUS ===" +
                    "\n§fAktueller Stil: §b" + currentStyle +
                    "\n§fStil XP: §e" + styleComp.getStyleXp() +
                    "\n§fNahkampf-Bonus: §a+" + String.format("%.2f", styleComp.getStyleDamageBonus()) + " DMG" +
                    "\n§6========================"), false);
            return 1;
        } catch (Exception e) {
            source.sendError(Text.literal("Dieser Befehl kann nur von einem Spieler ausgeführt werden!"));
            return 0;
        }
    }

    private static int setFightingStyle(ServerCommandSource source, ServerPlayerEntity target, String styleId) {
        if (!STYLES.contains(styleId)) {
            source.sendError(Text.literal("Ungültiger Kampfstil! Verfügbar: " + STYLES));
            return 0;
        }

        IFightingStyleComponent styleComp = Redline.FIGHTING_STYLE.get(target);
        styleComp.setStyle(styleId);

        String formatName = styleId.toUpperCase().replace("_", " ");
        source.sendFeedback(() -> Text.literal("§aKampfstil von " + target.getName().getString() + " auf §b" + formatName + "§a gesetzt!"), true);
        return 1;
    }

    private static int addStyleXp(ServerCommandSource source, ServerPlayerEntity target, long value) {
        IFightingStyleComponent styleComp = Redline.FIGHTING_STYLE.get(target);

        if (!styleComp.hasStyle()) {
            source.sendError(Text.literal(target.getName().getString() + " hat aktuell keinen Kampfstil ausgerüstet!"));
            return 0;
        }

        styleComp.addStyleXp(value);

        source.sendFeedback(() -> Text.literal("§e" + value + "§a Kampfstil-XP zu " + target.getName().getString() + " hinzugefügt!"), true);
        return 1;
    }
}
