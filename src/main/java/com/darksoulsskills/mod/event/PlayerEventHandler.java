package com.darksoulsskills.mod.event;

import com.darksoulsskills.mod.capability.SoulsCapability;
import com.darksoulsskills.mod.capability.SoulsData;
import com.darksoulsskills.mod.network.ModNetwork;
import com.darksoulsskills.mod.network.SyncSoulsPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;

import java.util.UUID;

public class PlayerEventHandler {

    // UUIDs for persistent attribute modifiers (must be stable)
    private static final UUID VIGOR_UUID    = UUID.fromString("a1b2c3d4-0001-0001-0001-000000000001");
    private static final UUID STR_UUID      = UUID.fromString("a1b2c3d4-0002-0002-0002-000000000002");
    private static final UUID DEX_UUID      = UUID.fromString("a1b2c3d4-0003-0003-0003-000000000003");

    // ════════════════════════════════════════════════════════════════════
    //  Death: save souls, mark retrieval point
    // ════════════════════════════════════════════════════════════════════
    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        SoulsCapability.get(player).ifPresent(data -> {
            if (data.getCurrentSouls() > 0) {
                data.onDeath(player);
                player.sendSystemMessage(Component.literal(
                    "§c§lYOU DIED§r §7- You lost §e" + data.getLostSouls() + " souls§7. Retrieve them before dying again!"
                ));
            }
        });
    }

    // ════════════════════════════════════════════════════════════════════
    //  On respawn: sync data to client, apply attributes
    // ════════════════════════════════════════════════════════════════════
    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            SoulsCapability.get(sp).ifPresent(data -> {
                applyAttributes(sp, data);
                ModNetwork.sendToPlayer(new SyncSoulsPacket(data.serializeNBT()), sp);
                if (data.hasPendingRetrieval()) {
                    sp.sendSystemMessage(Component.literal(
                        "§6Retrieve your §e" + data.getLostSouls() + " souls §6at §b(" +
                        (int)data.getLostSoulsX() + ", " + (int)data.getLostSoulsY() + ", " + (int)data.getLostSoulsZ() + ")"
                    ));
                }
            });
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  Player login: sync and apply
    // ════════════════════════════════════════════════════════════════════
    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            SoulsCapability.get(sp).ifPresent(data -> {
                applyAttributes(sp, data);
                ModNetwork.sendToPlayer(new SyncSoulsPacket(data.serializeNBT()), sp);
            });
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  Server tick: stamina regen, hp regen, soul retrieval proximity
    // ════════════════════════════════════════════════════════════════════
    @SubscribeEvent
    public void onServerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer sp)) return;

        SoulsCapability.get(sp).ifPresent(data -> {
            boolean dirty = false;

            // ── Stamina regeneration ──────────────────────────────────
            float maxStamina = data.getMaxStamina();
            if (data.getCurrentStamina() < maxStamina) {
                data.setCurrentStamina(Math.min(maxStamina, data.getCurrentStamina() + 0.5f));
                dirty = true;
            }

            // ── HP regeneration from Faith ────────────────────────────
            if (data.getFaith() > 10 && sp.getHealth() < sp.getMaxHealth() && !sp.isDeadOrDying()) {
                data.incrementRegenTick();
                if (data.getRegenTickCounter() >= data.getRegenIntervalTicks()) {
                    sp.heal(0.5f);
                    data.resetRegenTick();
                    dirty = true;
                }
            }

            // ── Soul retrieval proximity (8 blocks) ──────────────────
            if (data.hasPendingRetrieval()) {
                double dx = sp.getX() - data.getLostSoulsX();
                double dy = sp.getY() - data.getLostSoulsY();
                double dz = sp.getZ() - data.getLostSoulsZ();
                if (dx*dx + dy*dy + dz*dz <= 64.0) { // 8 blocks radius
                    long souls = data.getLostSouls();
                    data.retrieveLostSouls();
                    sp.sendSystemMessage(Component.literal(
                        "§a§lSOULS RETRIEVED! §e+" + souls + " souls"
                    ));
                    dirty = true;
                }
            }

            if (dirty) {
                ModNetwork.sendToPlayer(new SyncSoulsPacket(data.serializeNBT()), sp);
            }
        });
    }

    // ════════════════════════════════════════════════════════════════════
    //  Apply attribute modifiers based on current stats
    // ════════════════════════════════════════════════════════════════════
    public static void applyAttributes(Player player, SoulsData data) {
        // ── Max Health from Vigor ─────────────────────────────────────
        var maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.removeModifier(VIGOR_UUID);
            int bonusHearts = data.getBonusMaxHearts();
            if (bonusHearts > 0) {
                maxHealthAttr.addPermanentModifier(new AttributeModifier(
                    VIGOR_UUID, "vigor_hp_bonus",
                    bonusHearts * 2.0, AttributeModifier.Operation.ADDITION
                ));
            }
        }

        // ── Attack Damage from Strength ───────────────────────────────
        var dmgAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dmgAttr != null) {
            dmgAttr.removeModifier(STR_UUID);
            float bonus = data.getStrengthDamageMultiplier() - 1f;
            if (bonus > 0) {
                dmgAttr.addPermanentModifier(new AttributeModifier(
                    STR_UUID, "strength_dmg_bonus",
                    bonus, AttributeModifier.Operation.MULTIPLY_BASE
                ));
            }
        }

        // ── Attack Speed from Dexterity ───────────────────────────────
        var speedAttr = player.getAttribute(Attributes.ATTACK_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(DEX_UUID);
            float speedBonus = data.getDexteritySpeedBonus();
            if (speedBonus > 0) {
                speedAttr.addPermanentModifier(new AttributeModifier(
                    DEX_UUID, "dex_speed_bonus",
                    speedBonus, AttributeModifier.Operation.MULTIPLY_BASE
                ));
            }
        }
    }
}
