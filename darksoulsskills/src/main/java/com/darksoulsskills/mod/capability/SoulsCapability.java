package com.darksoulsskills.mod.capability;

import com.darksoulsskills.mod.DarkSoulsSkillsMod;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = DarkSoulsSkillsMod.MOD_ID)
public class SoulsCapability {

    public static final Capability<SoulsData> SOULS_CAP = CapabilityManager.get(new CapabilityToken<>(){});
    public static final ResourceLocation KEY = new ResourceLocation(DarkSoulsSkillsMod.MOD_ID, "souls_data");

    // ════════════════════════════════════════════════════════════════════
    //  Attach capability to every player entity
    // ════════════════════════════════════════════════════════════════════
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(SOULS_CAP).isPresent()) {
                event.addCapability(KEY, new Provider());
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  Copy data on player respawn / dimension change
    // ════════════════════════════════════════════════════════════════════
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(SOULS_CAP).ifPresent(oldData -> {
            event.getEntity().getCapability(SOULS_CAP).ifPresent(newData -> {
                newData.deserializeNBT(oldData.serializeNBT());
            });
        });
        event.getOriginal().invalidateCaps();
    }

    // ════════════════════════════════════════════════════════════════════
    //  Convenience helper
    // ════════════════════════════════════════════════════════════════════
    public static Optional<SoulsData> get(Player player) {
        return player.getCapability(SOULS_CAP).resolve();
    }

    // ════════════════════════════════════════════════════════════════════
    //  Inner Provider
    // ════════════════════════════════════════════════════════════════════
    public static class Provider implements ICapabilitySerializable<CompoundTag> {

        private final SoulsData data = new SoulsData();
        private final LazyOptional<SoulsData> optional = LazyOptional.of(() -> data);

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return cap == SOULS_CAP ? optional.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return data.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            data.deserializeNBT(nbt);
        }
    }
}
