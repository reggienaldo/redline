package net.reggie.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommandPermissionTest {
    @Test
    void mergedCommandsKeepAdminActionsRestrictedAndInfoPublic() {
        HakiCommand.register();
        DorikiCommand.register();
        RaceCommand.register();
        FightingStyleCommand.register();

        CommandDispatcher<ServerCommandSource> dispatcher = new CommandDispatcher<>();
        CommandRegistrationCallback.EVENT.invoker().register(dispatcher, null, CommandManager.RegistrationEnvironment.ALL);

        ServerCommandSource player = mock(ServerCommandSource.class);
        ServerCommandSource admin = mock(ServerCommandSource.class);
        when(admin.hasPermissionLevel(2)).thenReturn(true);

        var redline = dispatcher.getRoot().getChild("redline");
        for (String branch : new String[] { "haki", "doriki", "race", "style" }) {
            var command = redline.getChild(branch);
            assertTrue(command.canUse(player), branch);
            assertTrue(command.getChild("info").canUse(player), branch + " info");
            for (var child : command.getChildren()) {
                if (child.getName().equals("info")) continue;
                assertFalse(child.canUse(player), branch + " " + child.getName());
                assertTrue(child.canUse(admin), branch + " " + child.getName());
            }
        }
    }
}
