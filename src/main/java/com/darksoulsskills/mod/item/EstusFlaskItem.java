package com.darksoulsskills.mod.item;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EstusFlaskItem extends Item {

    public EstusFlaskItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.getHealth() < player.getMaxHealth()) {
            if (!level.isClientSide) {
                player.heal(player.getMaxHealth() * 0.4f); // heal 40% of max HP
                level.playSound(null, player.blockPosition(), SoundEvents.BOTTLE_FILL,
                    SoundSource.PLAYERS, 1.0f, 1.2f);
            }
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
        }
        return InteractionResultHolder.fail(player.getItemInHand(hand));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§cRestores 40% of your maximum health"));
        tooltip.add(Component.literal("§8Kindled by the bonfire's flame"));
    }
}
