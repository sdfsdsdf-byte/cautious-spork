package com.darksoulsskills.mod.network;

import com.darksoulsskills.mod.capability.SoulsCapability;
import com.darksoulsskills.mod.capability.SoulsData;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncSoulsPacket {

    private final CompoundTag data;

    public SyncSoulsPacket(CompoundTag data) {
        this.data = data;
    }

    public SyncSoulsPacket(FriendlyByteBuf buf) {
        this.data = buf.readNbt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeNbt(data);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // CLIENT SIDE
            Player player = Minecraft.getInstance().player;
            if (player == null) return;
            SoulsCapability.get(player).ifPresent(soulsData -> {
                soulsData.deserializeNBT(data);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
