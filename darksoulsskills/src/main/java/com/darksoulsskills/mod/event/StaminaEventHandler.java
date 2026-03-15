package com.darksoulsskills.mod.event;

import com.darksoulsskills.mod.DarkSoulsSkillsMod;
import com.darksoulsskills.mod.capability.SoulsCapability;
import com.darksoulsskills.mod.network.ModNetwork;
import com.darksoulsskills.mod.network.SyncSoulsPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DarkSoulsSkillsMod.MOD_ID)
public class StaminaEventHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer sp)) return;

        SoulsCapability.get(sp).ifPresent(data -> {
            boolean isSprinting = sp.isSprinting();

            if (isSprinting) {
                float newStamina = data.getCurrentStamina() - 1.5f;
                if (newStamina <= 0) {
                    sp.setSprinting(false);
                    newStamina = 0;
                }
                data.setCurrentStamina(newStamina);
                // Sync stamina changes every 2 ticks to avoid spam
                if (sp.tickCount % 2 == 0) {
                    ModNetwork.sendToPlayer(new SyncSoulsPacket(data.serializeNBT()), sp);
                }
            } else {
                // Regenerate stamina when not sprinting
                float max = data.getMaxStamina();
                if (data.getCurrentStamina() < max) {
                    float regenRate = 0.8f + (data.getEndurance() - 10) * 0.05f;
                    data.setCurrentStamina(Math.min(max, data.getCurrentStamina() + regenRate));
                    if (sp.tickCount % 3 == 0) {
                        ModNetwork.sendToPlayer(new SyncSoulsPacket(data.serializeNBT()), sp);
                    }
                }
            }
        });
    }
}
