package com.darksoulsskills.mod.network;

import com.darksoulsskills.mod.capability.SoulsCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class LevelUpPacket {

    public LevelUpPacket() {}

    public LevelUpPacket(FriendlyByteBuf buf) {}

    public void encode(FriendlyByteBuf buf) {}

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            SoulsCapability.get(player).ifPresent(data -> {
                if (data.canLevelUp()) {
                    int oldLevel = data.getPlayerLevel();
                    data.spendSoulsForLevel();
                    player.sendSystemMessage(Component.literal(
                        "§6§lLEVEL UP! §r§eSoul Level " + data.getPlayerLevel() +
                        " §7(+" + (data.getPlayerLevel() - oldLevel) + " attribute point)"
                    ));
                    ModNetwork.sendToPlayer(new SyncSoulsPacket(data.serializeNBT()), player);
                } else {
                    player.sendSystemMessage(Component.literal(
                        "§cNot enough souls. Need §e" + data.getSoulsRequiredForNextLevel() +
                        "§c, have §e" + data.getCurrentSouls()
                    ));
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
