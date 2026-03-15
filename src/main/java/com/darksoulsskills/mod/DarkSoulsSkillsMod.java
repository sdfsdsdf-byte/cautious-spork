package com.darksoulsskills.mod;

import com.darksoulsskills.mod.capability.SoulsCapability;
import com.darksoulsskills.mod.event.PlayerEventHandler;
import com.darksoulsskills.mod.event.SoulsDropHandler;
import com.darksoulsskills.mod.item.ModCreativeTabs;
import com.darksoulsskills.mod.item.ModItems;
import com.darksoulsskills.mod.network.ModNetwork;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(DarkSoulsSkillsMod.MOD_ID)
public class DarkSoulsSkillsMod {

    public static final String MOD_ID = "darksoulsskills";
    public static final Logger LOGGER = LogUtils.getLogger();

    public DarkSoulsSkillsMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(new PlayerEventHandler());
        MinecraftForge.EVENT_BUS.register(new SoulsDropHandler());
        MinecraftForge.EVENT_BUS.register(SoulsCapability.class);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ModNetwork.register();
        LOGGER.info("Dark Souls Skills Mod initialized!");
    }
}
