package net.reggie.render;

import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.reggie.Redline;
import net.reggie.game.haki.IHakiComponent;
import net.reggie.item.ModItems;

public class ModModelPredicates {

    public static void registerHakiWeapons() {
        // Liste all deiner Schwerter auf, die im JSON-Modell auf "haki_active" reagieren sollen
        registerHakiProperty(ModItems.Axe);
        registerHakiProperty(ModItems.ENMA);
        registerHakiProperty(ModItems.GRYPHON);
        registerHakiProperty(ModItems.YORU);
    }

    private static void registerHakiProperty(Item item) {
        ModelPredicateProviderRegistry.register(item, Identifier.of(Redline.MOD_ID, "haki_active"),
                (stack, world, entity, seed) -> {
                    if (entity instanceof PlayerEntity player) {

                        // STRIKTE PRÜFUNG: Ist das Item aktuell aktiv in der Main- oder Off-Hand ausgerüstet?
                        boolean isHeldInHand = player.getMainHandStack() == stack || player.getOffHandStack() == stack;
                        if (!isHeldInHand) {
                            return 0.0F;
                        }

                        // Holt deine flache Haki-Komponente über den zentralen Redline-Key
                        IHakiComponent hakiComp = Redline.HAKI.get(player);

                        // Gibt 1.0F zurück, wenn das Rüstungshaki (Busoshoku) aktiv geschaltet ist
                        return hakiComp.isBusoActive() ? 1.0F : 0.0F;
                    }
                    return 0.0F;
                }
        );
    }
}
