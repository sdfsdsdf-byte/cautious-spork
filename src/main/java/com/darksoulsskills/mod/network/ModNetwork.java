package com.darksoulsskills.mod.network;

import com.darksoulsskills.mod.DarkSoulsSkillsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(DarkSoulsSkillsMod.MOD_ID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int id = 0;

    public static void register() {
        // Server → Client: sync all souls data
        CHANNEL.messageBuilder(SyncSoulsPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
            .decoder(SyncSoulsPacket::new)
            .encoder(SyncSoulsPacket::encode)
            .consumerMainThread(SyncSoulsPacket::handle)
            .add();

        // Server → Client: open the level-up screen
        CHANNEL.messageBuilder(OpenLevelUpScreenPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
            .decoder(OpenLevelUpScreenPacket::new)
            .encoder(OpenLevelUpScreenPacket::encode)
            .consumerMainThread(OpenLevelUpScreenPacket::handle)
            .add();

        // Client → Server: spend a point on an attribute
        CHANNEL.messageBuilder(SpendPointPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
            .decoder(SpendPointPacket::new)
            .encoder(SpendPointPacket::encode)
            .consumerMainThread(SpendPointPacket::handle)
            .add();

        // Client → Server: level up (spend souls)
        CHANNEL.messageBuilder(LevelUpPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
            .decoder(LevelUpPacket::new)
            .encoder(LevelUpPacket::encode)
            .consumerMainThread(LevelUpPacket::handle)
            .add();
    }

    public static void sendToPlayer(Object packet, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }
}
