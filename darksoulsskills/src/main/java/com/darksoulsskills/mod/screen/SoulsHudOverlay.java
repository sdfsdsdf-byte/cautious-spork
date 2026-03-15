package com.darksoulsskills.mod.screen;

import com.darksoulsskills.mod.capability.SoulsCapability;
import com.darksoulsskills.mod.capability.SoulsData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.darksoulsskills.mod.DarkSoulsSkillsMod;

@Mod.EventBusSubscriber(modid = DarkSoulsSkillsMod.MOD_ID, value = Dist.CLIENT)
public class SoulsHudOverlay {

    private static float smoothStamina = 100f;

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.EXPERIENCE_BAR.type()) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui) return;

        SoulsCapability.get(player).ifPresent(data -> {
            GuiGraphics gfx = event.getGuiGraphics();
            int screenW = mc.getWindow().getGuiScaledWidth();
            int screenH = mc.getWindow().getGuiScaledHeight();

            // ── Stamina bar (above hotbar, Green → Yellow → Red) ─────────────
            float maxStamina = data.getMaxStamina();
            float curStamina = data.getCurrentStamina();
            smoothStamina += (curStamina - smoothStamina) * 0.2f;

            int barW  = 182;
            int barH  = 5;
            int barX  = (screenW - barW) / 2;
            int barY  = screenH - 49; // just above XP bar

            // Background
            gfx.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF000000);
            gfx.fill(barX,     barY,     barX + barW,     barY + barH,     0xFF222222);

            float ratio = Math.min(smoothStamina / maxStamina, 1f);
            int fillW   = (int)(barW * ratio);
            int color   = staminaColor(ratio);
            if (fillW > 0) {
                gfx.fill(barX, barY, barX + fillW, barY + barH, color);
            }

            // "STA" label
            gfx.drawString(mc.font, "STA", barX - 22, barY - 1, 0xFFAAFFAA, false);

            // ── Souls counter (bottom-left corner) ────────────────────────
            long souls = data.getCurrentSouls();
            long need  = data.getSoulsRequiredForNextLevel();
            int lvl    = data.getPlayerLevel();
            int pts    = data.getAvailablePoints();

            String soulsText = "§6" + souls + " §8souls";
            String levelText = "§7SL §e" + lvl;
            String ptsText   = pts > 0 ? " §a(+" + pts + ")" : "";

            gfx.drawString(mc.font, soulsText, 5, screenH - 55, 0xFFFFFF, true);
            gfx.drawString(mc.font, levelText + ptsText, 5, screenH - 45, 0xFFFFFF, true);

            // ── Soul XP bar (left side, small) ───────────────────────────
            int xpBarW = 60;
            int xpBarH = 3;
            int xpBarX = 5;
            int xpBarY = screenH - 38;
            float xpRatio = (float) souls / need;

            gfx.fill(xpBarX,     xpBarY,     xpBarX + xpBarW,     xpBarY + xpBarH,     0xFF111111);
            gfx.fill(xpBarX,     xpBarY,     xpBarX + (int)(xpBarW * Math.min(xpRatio,1f)), xpBarY + xpBarH, 0xFF88CC00);

            // ── Lost souls indicator ──────────────────────────────────────
            if (data.hasPendingRetrieval()) {
                String lostText = "§c⚠ " + data.getLostSouls() + " lost souls";
                int tx = (screenW - mc.font.width(lostText)) / 2;
                gfx.drawString(mc.font, lostText, tx, 40, 0xFFFFFF, true);
            }

            // ── Available points flash ────────────────────────────────────
            if (pts > 0) {
                long time = System.currentTimeMillis();
                boolean blink = (time / 600) % 2 == 0;
                if (blink) {
                    String ptsTxt = "§a§l▲ Level Up Available ▲";
                    int tx = (screenW - mc.font.width(ptsTxt)) / 2;
                    gfx.drawString(mc.font, ptsTxt, tx, 52, 0xFFFFFF, true);
                }
            }
        });
    }

    private static int staminaColor(float ratio) {
        if (ratio > 0.5f) {
            // Green → Yellow
            int r = (int)(255 * (1f - ratio) * 2);
            return 0xFF000000 | (r << 16) | (0xCC << 8);
        } else {
            // Yellow → Red
            int g = (int)(0xCC * ratio * 2);
            return 0xFF000000 | (0xFF << 16) | (g << 8);
        }
    }
}
