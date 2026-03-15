package com.darksoulsskills.mod.network;

import com.darksoulsskills.mod.capability.SoulsCapability;
import com.darksoulsskills.mod.event.PlayerEventHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SpendPointPacket {

    private final String attribute;

    public SpendPointPacket(String attribute) {
        this.attribute = attribute;
    }

    public SpendPointPacket(FriendlyByteBuf buf) {
        this.attribute = buf.readUtf(32);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(attribute);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            SoulsCapability.get(player).ifPresent(data -> {
                boolean spent = data.spendPoint(attribute);
                if (spent) {
                    PlayerEventHandler.applyAttributes(player, data);
                    player.sendSystemMessage(Component.literal(
                        "§a+" + attribute.substring(0,1).toUpperCase() + attribute.substring(1) +
                        " §7upgraded to §e" + getAttrValue(data, attribute)
                    ));
                    ModNetwork.sendToPlayer(new SyncSoulsPacket(data.serializeNBT()), player);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }

    private int getAttrValue(com.darksoulsskills.mod.capability.SoulsData data, String attr) {
        return switch (attr.toLowerCase()) {
            case "vigor"        -> data.getVigor();
            case "endurance"    -> data.getEndurance();
            case "strength"     -> data.getStrength();
            case "dexterity"    -> data.getDexterity();
            case "intelligence" -> data.getIntelligence();
            case "faith"        -> data.getFaith();
            default -> 0;
        };
    }
}
