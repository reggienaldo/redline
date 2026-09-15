package net.reggie.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.reggie.Redline;

public class ModItemGroups {
    public static final ItemGroup INGRIDINETS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(Redline.MOD_ID, "ingridients"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.ingridients"))
                    .icon(() -> new ItemStack(ModItems.STEEL_INGOT)).entries((displayContext, entries) -> {

                        entries.add(ModItems.STEEL_INGOT);

                    }).build());

    public static final ItemGroup COMBAT_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(Redline.MOD_ID, "combat"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.combat"))
                    .icon(() -> new ItemStack(ModItems.ENMA)).entries((displayContext, entries) -> {

                        entries.add(ModItems.ENMA);
                        entries.add(ModItems.SHUSUI);
                        entries.add(ModItems.GRYPHON);
                        entries.add(ModItems.YORU);
                        entries.add(ModItems.Axe);


                    }).build());

    public static final ItemGroup FRUITS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(Redline.MOD_ID, "fruits"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.fruits"))
                    .icon(() -> new ItemStack(ModItems.GOMU_GOMU)).entries((displayContext, entries) -> {

                        entries.add(ModItems.GOMU_GOMU);

                    }).build());

    public static void registerItemGroups() {
        Redline.LOGGER.info("Registering Item Groups for " + Redline.MOD_ID);
    }
}
