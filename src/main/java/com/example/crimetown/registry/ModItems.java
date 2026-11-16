package com.example.crimetown.registry;

import com.example.crimetown.CrimeTown;
import com.example.crimetown.drug.DrugItem;
import com.example.crimetown.drug.DrugType;
import com.example.crimetown.weapon.GunItem;
import com.example.crimetown.weapon.GunType;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public final class ModItems {
    public static final Item PISTOL = reg("pistol", p -> new GunItem(p, GunType.PISTOL), new Item.Properties().stacksTo(1).component(ModComponents.AMMO, 0));
    public static final Item SHOTGUN = reg("shotgun", p -> new GunItem(p, GunType.SHOTGUN), new Item.Properties().stacksTo(1).component(ModComponents.AMMO, 0));
    public static final Item RIFLE = reg("rifle", p -> new GunItem(p, GunType.RIFLE), new Item.Properties().stacksTo(1).component(ModComponents.AMMO, 0));

    public static final Item PISTOL_AMMO = reg("pistol_ammo", Item::new, new Item.Properties());
    public static final Item SHOTGUN_SHELLS = reg("shotgun_shells", Item::new, new Item.Properties());
    public static final Item RIFLE_AMMO = reg("rifle_ammo", Item::new, new Item.Properties());

    public static final Item SUGAR = reg("sugar", p -> new DrugItem(p, DrugType.SUGAR), new Item.Properties().stacksTo(16));
    public static final Item ENERGY_DRINK = reg("energy_drink", p -> new DrugItem(p, DrugType.ENERGY), new Item.Properties().stacksTo(16));
    public static final Item CRYSTALS = reg("crystals", p -> new DrugItem(p, DrugType.CRYSTALS), new Item.Properties().stacksTo(16));

    public static final Item OLIGARCH_CROWN = reg("oligarch_crown", Item::new, new Item.Properties().stacksTo(1).fireResistant());

    private ModItems() {}

    private static <T extends Item> T reg(String name, Function<Item.Properties, T> factory, Item.Properties props) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(CrimeTown.MOD_ID, name));
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(props.setId(key)));
    }

    public static void init() {
        // Items go into vanilla tabs, which avoids depending on the tab builder API.
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(out -> {
            out.accept(PISTOL);
            out.accept(SHOTGUN);
            out.accept(RIFLE);
            out.accept(PISTOL_AMMO);
            out.accept(SHOTGUN_SHELLS);
            out.accept(RIFLE_AMMO);
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(out -> {
            out.accept(SUGAR);
            out.accept(ENERGY_DRINK);
            out.accept(CRYSTALS);
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(out -> out.accept(OLIGARCH_CROWN));
    }
}
