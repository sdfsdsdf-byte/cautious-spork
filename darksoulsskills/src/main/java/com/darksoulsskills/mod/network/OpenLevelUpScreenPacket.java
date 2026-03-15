package com.darksoulsskills.mod.network;

import com.darksoulsskills.mod.screen.LevelUpScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenLevelUpScreenPacket {

    public OpenLevelUpScreenPacket() {}

    public OpenLevelUpScreenPacket(FriendlyByteBuf buf) {}

    public void encode(FriendlyByteBuf buf) {}

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft.getInstance().setScreen(new LevelUpScreen());
        });
        ctx.get().setPacketHandled(true);
    }
}
