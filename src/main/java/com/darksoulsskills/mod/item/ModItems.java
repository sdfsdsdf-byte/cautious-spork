package com.darksoulsskills.mod.item;

import com.darksoulsskills.mod.DarkSoulsSkillsMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, DarkSoulsSkillsMod.MOD_ID);

    public static final RegistryObject<Item> BONFIRE_ITEM =
        ITEMS.register("bonfire", () -> new BonfireItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> ESTUS_FLASK =
        ITEMS.register("estus_flask", () -> new EstusFlaskItem(new Item.Properties().stacksTo(1)));
}
