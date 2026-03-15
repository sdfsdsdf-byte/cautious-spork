package com.darksoulsskills.mod.item;

import com.darksoulsskills.mod.DarkSoulsSkillsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DarkSoulsSkillsMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> DARK_SOULS_TAB = CREATIVE_MODE_TABS.register(
        "dark_souls_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.darksoulsskills"))
            .icon(() -> new ItemStack(ModItems.BONFIRE_ITEM.get()))
            .displayItems((params, output) -> {
                output.accept(ModItems.BONFIRE_ITEM.get());
                output.accept(ModItems.ESTUS_FLASK.get());
            })
            .build()
    );
}
