package com.darksoulsskills.mod.event;

import com.darksoulsskills.mod.DarkSoulsSkillsMod;
import com.darksoulsskills.mod.capability.SoulsCapability;
import com.darksoulsskills.mod.network.ModNetwork;
import com.darksoulsskills.mod.network.SyncSoulsPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DarkSoulsSkillsMod.MOD_ID, value = Dist.CLIENT)
public class ClientEventHandler {

    private static boolean wasSprinting = false;

    /**
     * Client-side stamina drain when sprinting.
     * Real enforcement happens server-side; this is purely predictive.
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.isPaused()) return;

        SoulsCapability.get(player).ifPresent(data -> {
            boolean isSprinting = player.isSprinting();

            if (isSprinting) {
                float newStamina = data.getCurrentStamina() - 1.5f;
                if (newStamina <= 0) {
                    // Force stop sprinting when out of stamina
                    player.setSprinting(false);
                    newStamina = 0;
                }
                data.setCurrentStamina(newStamina);
            }

            wasSprinting = isSprinting;
        });
    }
}
