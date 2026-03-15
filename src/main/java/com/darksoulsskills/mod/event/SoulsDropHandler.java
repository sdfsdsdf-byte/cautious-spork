package com.darksoulsskills.mod.event;

import com.darksoulsskills.mod.capability.SoulsCapability;
import com.darksoulsskills.mod.network.ModNetwork;
import com.darksoulsskills.mod.network.SyncSoulsPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SoulsDropHandler {

    @SubscribeEvent
    public void onMobDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();

        // Only care about deaths caused by a player
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (!(player instanceof ServerPlayer sp)) return;

        long souls = getSoulsForEntity(entity);
        if (souls <= 0) return;

        SoulsCapability.get(player).ifPresent(data -> {
            data.addSouls(souls);
            sp.sendSystemMessage(Component.literal(
                "§6+" + souls + " souls §8[" + entity.getName().getString() + "]"
            ));
            ModNetwork.sendToPlayer(new SyncSoulsPacket(data.serializeNBT()), sp);
        });
    }

    /**
     * Soul values inspired by Dark Souls enemy scaling.
     * Bosses give significantly more souls.
     */
    private long getSoulsForEntity(LivingEntity entity) {
        // ── Bosses ────────────────────────────────────────────────────
        if (entity instanceof EnderDragon) return 80000L;
        if (entity instanceof WitherBoss)  return 50000L;
        if (entity instanceof ElderGuardian) return 5000L;

        // ── Strong Monsters ───────────────────────────────────────────
        if (entity instanceof Warden)   return 10000L;
        if (entity instanceof Ravager)  return 3000L;
        if (entity instanceof Evoker)   return 2500L;
        if (entity instanceof Witch)    return 1000L;
        if (entity instanceof Vindicator) return 800L;
        if (entity instanceof Raider)   return 600L;
        if (entity instanceof Pillager) return 500L;

        // ── Mid-tier ─────────────────────────────────────────────────
        if (entity instanceof Blaze)    return 400L;
        if (entity instanceof Ghast)    return 600L;
        if (entity instanceof Guardian) return 300L;
        if (entity instanceof Drowned)  return 200L;
        if (entity instanceof Husk)     return 150L;
        if (entity instanceof Stray)    return 150L;
        if (entity instanceof Phantom)  return 250L;

        // ── Common Mobs ───────────────────────────────────────────────
        if (entity instanceof Zombie)   return 100L;
        if (entity instanceof Skeleton) return 100L;
        if (entity instanceof Creeper)  return 120L;
        if (entity instanceof Spider)   return 80L;
        if (entity instanceof Enderman) return 350L;
        if (entity instanceof Silverfish) return 30L;
        if (entity instanceof Slime s) {
            // Size-based souls for slimes
            return (long)(s.getSize() * 50);
        }

        // ── Generic fallback ─────────────────────────────────────────
        if (entity instanceof Monster) return 60L;

        return 0L;
    }
}
