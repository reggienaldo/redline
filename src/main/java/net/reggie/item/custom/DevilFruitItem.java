package net.reggie.item.custom;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.reggie.Redline;

public class DevilFruitItem extends Item {
    private final String fruitId;

    public DevilFruitItem(String fruitId, Settings settings) {
        // Macht das Item essbar (nutzt die neuen 1.21.1 FoodComponent-Settings)
        super(settings.food(new FoodComponent.Builder().nutrition(4).saturationModifier(0.3f).alwaysEdible().build()));
        this.fruitId = fruitId;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof PlayerEntity player && !world.isClient()) {
            var dfComp = Redline.DEVIL_FRUIT.get(player);
            var abilityComp = Redline.ABILITY_COMPONENT.get(player);

            // --- 1. AMIME EXPULSION FIX: Wenn bereits eine Frucht gegessen wurde -> TOD ---
            if (dfComp.hasFruit()) {
                world.createExplosion(player, player.getX(), player.getY(), player.getZ(), 3.0f, World.ExplosionSourceType.NONE);
                player.damage(world.getDamageSources().magic(), 1000.0f); // Sofortiger, unblockbarer Tod
                player.sendMessage(Text.literal("§cDein Körper ist explodiert, weil du zwei Teufelsfrüchte gegessen hast!"), true);
                return super.finishUsing(stack, world, user);
            }

            // --- 2. ERSTE FRUCHT ERFOLGREICH GEGESSEN ---
            dfComp.setFruit(this.fruitId);
            player.sendMessage(Text.literal("§6Du hast die Kräfte der " + this.fruitId.toUpperCase().replace("_", " ") + " erhalten!"), false);

            // --- 3. DYNAMISCHE FÄHIGKEITEN-FREISCHALTUNG ---
            // Schaltet basierend auf der Frucht-ID vollautomatisch die Attacken im JJK-Raster-Inventar frei!
            if ("gomu_gomu".equals(this.fruitId)) {
                abilityComp.getInventory().unlockAbilityDynamically("gomu_pistol");
                abilityComp.getInventory().unlockAbilityDynamically("gomu_bazooka");
                abilityComp.getInventory().unlockAbilityDynamically("gomu_gatling");
                Redline.ABILITY_COMPONENT.sync(player);
            } else if ("mera_mera".equals(this.fruitId)) {
                abilityComp.getInventory().unlockAbilityDynamically("hiken");
                abilityComp.getInventory().unlockAbilityDynamically("hikai");
                Redline.ABILITY_COMPONENT.sync(player);
            }
        }
        return super.finishUsing(stack, world, user);
    }
}
