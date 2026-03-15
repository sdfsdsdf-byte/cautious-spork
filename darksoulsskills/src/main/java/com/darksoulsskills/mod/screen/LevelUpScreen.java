package com.darksoulsskills.mod.screen;

import com.darksoulsskills.mod.capability.SoulsCapability;
import com.darksoulsskills.mod.capability.SoulsData;
import com.darksoulsskills.mod.network.LevelUpPacket;
import com.darksoulsskills.mod.network.ModNetwork;
import com.darksoulsskills.mod.network.SpendPointPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LevelUpScreen extends Screen {

    // ── Layout constants ────────────────────────────────────────────
    private static final int PANEL_W   = 320;
    private static final int PANEL_H   = 280;
    private static final int COL_LEFT  = 20;
    private static final int COL_RIGHT = 175;
    private static final int ROW_H     = 26;

    // Attribute list in display order
    private static final String[] ATTRS = { "vigor", "endurance", "strength", "dexterity", "intelligence", "faith" };
    private static final String[] ATTR_DISPLAY = { "Vigor", "Endurance", "Strength", "Dexterity", "Intelligence", "Faith" };
    private static final String[] ATTR_DESC = {
        "Increases maximum HP",
        "Increases max stamina",
        "Increases melee damage",
        "Increases attack speed",
        "Increases magic power",
        "Increases HP regeneration"
    };

    private int panelX, panelY;
    private final List<Button> plusButtons = new ArrayList<>();

    // ── Soul EXP bar animation ───────────────────────────────────────
    private float animSoulBar = 0f;

    public LevelUpScreen() {
        super(Component.literal("Bonfire"));
    }

    @Override
    protected void init() {
        super.init();
        panelX = (this.width  - PANEL_W) / 2;
        panelY = (this.height - PANEL_H) / 2;

        plusButtons.clear();

        // ── Level-up button (top right corner of panel) ───────────────
        this.addRenderableWidget(Button.builder(
            Component.literal("Level Up"),
            btn -> ModNetwork.sendToServer(new LevelUpPacket())
        ).pos(panelX + COL_RIGHT, panelY + 50).size(120, 18).build());

        // ── Per-attribute + buttons ────────────────────────────────────
        for (int i = 0; i < ATTRS.length; i++) {
            final String attr = ATTRS[i];
            int bx = panelX + COL_RIGHT + 90;
            int by = panelY + 110 + i * ROW_H;
            Button btn = Button.builder(
                Component.literal("+"),
                b -> ModNetwork.sendToServer(new SpendPointPacket(attr))
            ).pos(bx, by).size(18, 14).build();
            plusButtons.add(btn);
            this.addRenderableWidget(btn);
        }

        // ── Close button ──────────────────────────────────────────────
        this.addRenderableWidget(Button.builder(
            Component.literal("Leave Bonfire"),
            btn -> onClose()
        ).pos(panelX + (PANEL_W - 120) / 2, panelY + PANEL_H - 25).size(120, 16).build());
    }

    @Override
    public void render(GuiGraphics gfx, int mx, int my, float partialTick) {
        // Dim background
        renderBackground(gfx);

        Optional<SoulsData> dataOpt = getPlayerData();
        SoulsData data = dataOpt.orElse(null);

        // ── Panel background ──────────────────────────────────────────
        gfx.fill(panelX - 2, panelY - 2, panelX + PANEL_W + 2, panelY + PANEL_H + 2, 0xFF1A0A00);
        gfx.fill(panelX, panelY, panelX + PANEL_W, panelY + PANEL_H, 0xFF2D1600);

        // ── Decorative border ─────────────────────────────────────────
        drawBorder(gfx, panelX, panelY, PANEL_W, PANEL_H, 0xFFA06010);

        // ── Title: "LEVEL UP" ─────────────────────────────────────────
        gfx.drawCenteredString(font, "§6§l⚔  LEVEL UP  ⚔", panelX + PANEL_W / 2, panelY + 10, 0xFFFFFF);
        drawHRule(gfx, panelX + 10, panelY + 22, PANEL_W - 20, 0xFFA06010);

        if (data != null) {
            // ── Soul Level ───────────────────────────────────────────
            gfx.drawString(font, "§eSoul Level  §f" + data.getPlayerLevel(),
                panelX + COL_LEFT, panelY + 30, 0xFFFFFF);

            // ── Souls counter ────────────────────────────────────────
            long cur  = data.getCurrentSouls();
            long need = data.getSoulsRequiredForNextLevel();
            gfx.drawString(font, "§6Souls: §e" + cur + " §8/ §7" + need,
                panelX + COL_LEFT, panelY + 50, 0xFFFFFF);

            // ── XP bar ───────────────────────────────────────────────
            animSoulBar = Mth.lerp(0.15f, animSoulBar, (float) cur / need);
            int barW = PANEL_W - 20;
            int bx   = panelX + 10;
            int by   = panelY + 64;
            gfx.fill(bx, by, bx + barW, by + 6, 0xFF111111);
            gfx.fill(bx, by, bx + (int)(barW * Math.min(animSoulBar, 1f)), by + 6, 0xFF88CC00);
            drawBorder(gfx, bx - 1, by - 1, barW + 2, 8, 0xFF605020);

            // ── Available points ──────────────────────────────────────
            int pts = data.getAvailablePoints();
            String ptsTxt = pts > 0 ? "§a" + pts + " attribute point(s) available" : "§8No points available";
            gfx.drawCenteredString(font, ptsTxt, panelX + PANEL_W / 2, panelY + 76, 0xFFFFFF);

            drawHRule(gfx, panelX + 10, panelY + 88, PANEL_W - 20, 0xFF604010);

            // ── Attribute table header ────────────────────────────────
            gfx.drawString(font, "§7Attribute", panelX + COL_LEFT, panelY + 96, 0xFFFFFF);
            gfx.drawString(font, "§7Value",     panelX + COL_RIGHT, panelY + 96, 0xFFFFFF);
            gfx.drawString(font, "§7Effect",    panelX + COL_RIGHT + 30, panelY + 96, 0xFFFFFF);

            // ── Attribute rows ────────────────────────────────────────
            int[] values = {
                data.getVigor(), data.getEndurance(), data.getStrength(),
                data.getDexterity(), data.getIntelligence(), data.getFaith()
            };
            for (int i = 0; i < ATTRS.length; i++) {
                int ry = panelY + 110 + i * ROW_H;
                boolean hovered = mx >= panelX + COL_LEFT && mx < panelX + COL_RIGHT - 5
                    && my >= ry && my < ry + ROW_H;

                if (hovered) {
                    gfx.fill(panelX + COL_LEFT - 2, ry - 1, panelX + PANEL_W - 10, ry + ROW_H - 2, 0x33AA8800);
                }

                // Name
                gfx.drawString(font, "§f" + ATTR_DISPLAY[i], panelX + COL_LEFT, ry + 3, 0xFFFFFF);
                // Value
                gfx.drawString(font, "§e" + values[i], panelX + COL_RIGHT, ry + 3, 0xFFFFFF);
                // Description
                gfx.drawString(font, "§8" + ATTR_DESC[i], panelX + COL_RIGHT + 30, ry + 3, 0xFFFFFF);
            }

            // ── Lost souls warning ────────────────────────────────────
            if (data.hasPendingRetrieval()) {
                drawHRule(gfx, panelX + 10, panelY + PANEL_H - 38, PANEL_W - 20, 0xFF604010);
                gfx.drawCenteredString(font,
                    "§c§lLost souls: §e" + data.getLostSouls() + " §c(go retrieve them!)",
                    panelX + PANEL_W / 2, panelY + PANEL_H - 32, 0xFFFFFF);
            }
        } else {
            gfx.drawCenteredString(font, "§cNo data found", panelX + PANEL_W / 2, panelY + PANEL_H / 2, 0xFFFFFF);
        }

        // Update + button visibility
        boolean hasPoints = data != null && data.getAvailablePoints() > 0;
        plusButtons.forEach(b -> b.active = hasPoints);

        super.render(gfx, mx, my, partialTick);
    }

    // ════════════════════════════════════════════════════════════════
    //  Helpers
    // ════════════════════════════════════════════════════════════════
    private Optional<SoulsData> getPlayerData() {
        var player = Minecraft.getInstance().player;
        if (player == null) return Optional.empty();
        return SoulsCapability.get(player);
    }

    private void drawBorder(GuiGraphics gfx, int x, int y, int w, int h, int color) {
        gfx.fill(x,         y,         x + w,     y + 1,     color); // top
        gfx.fill(x,         y + h - 1, x + w,     y + h,     color); // bottom
        gfx.fill(x,         y,         x + 1,     y + h,     color); // left
        gfx.fill(x + w - 1, y,         x + w,     y + h,     color); // right
    }

    private void drawHRule(GuiGraphics gfx, int x, int y, int w, int color) {
        gfx.fill(x, y, x + w, y + 1, color);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
