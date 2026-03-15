package com.darksoulsskills.mod.item;

import com.darksoulsskills.mod.network.ModNetwork;
import com.darksoulsskills.mod.network.OpenLevelUpScreenPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BonfireItem extends Item {

    public BonfireItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            // Send packet to client to open the leveling screen
            ModNetwork.sendToPlayer(new OpenLevelUpScreenPacket(), sp);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§8Right-click to rest at the bonfire"));
        tooltip.add(Component.literal("§6Spend souls to level up your attributes"));
    }
}
