package net.reggie.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.reggie.Redline;
import net.reggie.item.custom.DevilFruitItem;
import net.reggie.item.custom.EnmaSwordItem;
import net.reggie.item.custom.GryphonSwordItem;
import net.reggie.item.custom.YoruSwordItem;

public class ModItems {
    //Items
    public static final Item STEEL_INGOT = registerItem("steel_ingot", new Item(new Item.Settings()));

    //Fruits
    public static final Item GOMU_GOMU = Registry.register(
            Registries.ITEM,
            Identifier.of(Redline.MOD_ID, "gomu_gomu_no_mi"),
            new DevilFruitItem("gomu_gomu", new Item.Settings().maxCount(1)) // Registriert sich als "gomu_gomu"
    );

    //Tools
    public static final Item ENMA = registerItem("king_of_purgatory_katana",
            new EnmaSwordItem(ModToolMaterials.GREAT_GRADE, new Item.Settings()
                    .attributeModifiers(EnmaSwordItem.createAttributeModifiers(ModToolMaterials.GREAT_GRADE, 6, -2.4f))));

    public static final Item SHUSUI = registerItem("shusui",
            new EnmaSwordItem(ModToolMaterials.GREAT_GRADE, new Item.Settings()
                    .attributeModifiers(EnmaSwordItem.createAttributeModifiers(ModToolMaterials.GREAT_GRADE, 6, -2.4f))));

    public static final Item GRYPHON = registerItem("emperors_skyrend_saber",
            new GryphonSwordItem(ModToolMaterials.GREAT_GRADE, new Item.Settings()
                    .attributeModifiers(GryphonSwordItem.createAttributeModifiers(ModToolMaterials.GREAT_GRADE, 7, -2.4f))));

    public static final Item YORU = registerItem("dark_sovereign_long_great_sword",
            new YoruSwordItem(ModToolMaterials.GREAT_GRADE, new Item.Settings().component(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(true))
                    .attributeModifiers(YoruSwordItem.createAttributeModifiers(ModToolMaterials.GREAT_GRADE, 8, -2.5f))));

    public static final Item Axe = registerItem("axe",
            new AxeItem(ModToolMaterials.GREAT_GRADE, new Item.Settings()
                    .attributeModifiers(AxeItem.createAttributeModifiers(ModToolMaterials.GREAT_GRADE, 7, -2.4f))));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(Redline.MOD_ID, name), item);
    }

    public static void registerModItems() {
        Redline.LOGGER.info("Registering Mod Items for " + Redline.MOD_ID);

    }
}
