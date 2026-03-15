package com.darksoulsskills.mod.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Core data container for the Dark Souls leveling system.
 *
 * Attributes:
 *  - VIGOR       → increases max health
 *  - ENDURANCE   → increases stamina (sprint duration)
 *  - STRENGTH    → increases melee damage
 *  - DEXTERITY   → increases attack speed
 *  - INTELLIGENCE→ increases magic damage (unused slot, extensible)
 *  - FAITH       → increases passive HP regeneration
 */
public class SoulsData {

    // ── Souls (currency) ────────────────────────────────────────────────
    private long currentSouls = 0;
    private long lostSouls    = 0;       // souls dropped on death
    private double lostSoulsX = 0;       // coordinates of death
    private double lostSoulsY = 0;
    private double lostSoulsZ = 0;
    private boolean hasPendingRetrieval = false;

    // ── Player Level ─────────────────────────────────────────────────────
    private int playerLevel = 1;

    // ── Attributes (base = 10 each) ──────────────────────────────────────
    private int vigor        = 10;
    private int endurance    = 10;
    private int strength     = 10;
    private int dexterity    = 10;
    private int intelligence = 10;
    private int faith        = 10;

    // ── Pending attribute points (from leveling up) ─────────────────────
    private int availablePoints = 0;

    // ── Stamina system ───────────────────────────────────────────────────
    private float currentStamina    = 100f;
    private long  lastStaminaRegen  = 0L;

    // ── Regen tick counter ───────────────────────────────────────────────
    private int regenTickCounter = 0;

    // ════════════════════════════════════════════════════════════════════
    //  Souls cost formula: souls_for_level = 100 * level^1.5
    // ════════════════════════════════════════════════════════════════════
    public long getSoulsRequiredForNextLevel() {
        return (long)(100L * Math.pow(playerLevel, 1.5));
    }

    public boolean canLevelUp() {
        return currentSouls >= getSoulsRequiredForNextLevel();
    }

    public void spendSoulsForLevel() {
        if (canLevelUp()) {
            currentSouls -= getSoulsRequiredForNextLevel();
            playerLevel++;
            availablePoints++;
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  Attribute upgrading
    // ════════════════════════════════════════════════════════════════════
    public boolean spendPoint(String attribute) {
        if (availablePoints <= 0) return false;
        switch (attribute.toLowerCase()) {
            case "vigor"        -> { vigor++;        availablePoints--; return true; }
            case "endurance"    -> { endurance++;    availablePoints--; return true; }
            case "strength"     -> { strength++;     availablePoints--; return true; }
            case "dexterity"    -> { dexterity++;    availablePoints--; return true; }
            case "intelligence" -> { intelligence++; availablePoints--; return true; }
            case "faith"        -> { faith++;        availablePoints--; return true; }
        }
        return false;
    }

    // ════════════════════════════════════════════════════════════════════
    //  Derived stats
    // ════════════════════════════════════════════════════════════════════
    /** Max additional HP hearts from Vigor (each 5 points = +1 heart above base) */
    public int getBonusMaxHearts() {
        return Math.max(0, (vigor - 10) / 5);
    }

    /** Damage multiplier from Strength (each 5 points = +10%) */
    public float getStrengthDamageMultiplier() {
        return 1f + (strength - 10) * 0.02f;
    }

    /** Attack cooldown reduction from Dexterity (each 5 points = -5%) */
    public float getDexteritySpeedBonus() {
        return (dexterity - 10) * 0.005f;
    }

    /** Max stamina (base 100, +10 per endurance above 10) */
    public float getMaxStamina() {
        return 100f + (endurance - 10) * 10f;
    }

    /** HP regen interval ticks from Faith (base 200 ticks, -5 per faith above 10, min 20) */
    public int getRegenIntervalTicks() {
        return Math.max(20, 200 - (faith - 10) * 5);
    }

    // ════════════════════════════════════════════════════════════════════
    //  Death handling
    // ════════════════════════════════════════════════════════════════════
    public void onDeath(Player player) {
        lostSouls = currentSouls;
        lostSoulsX = player.getX();
        lostSoulsY = player.getY();
        lostSoulsZ = player.getZ();
        hasPendingRetrieval = lostSouls > 0;
        currentSouls = 0;
    }

    public void retrieveLostSouls() {
        if (hasPendingRetrieval) {
            currentSouls += lostSouls;
            lostSouls = 0;
            hasPendingRetrieval = false;
        }
    }

    public void forfeitLostSouls() {
        lostSouls = 0;
        hasPendingRetrieval = false;
    }

    // ════════════════════════════════════════════════════════════════════
    //  NBT serialization
    // ════════════════════════════════════════════════════════════════════
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putLong  ("currentSouls",         currentSouls);
        tag.putLong  ("lostSouls",            lostSouls);
        tag.putDouble("lostSoulsX",           lostSoulsX);
        tag.putDouble("lostSoulsY",           lostSoulsY);
        tag.putDouble("lostSoulsZ",           lostSoulsZ);
        tag.putBoolean("hasPendingRetrieval", hasPendingRetrieval);
        tag.putInt   ("playerLevel",          playerLevel);
        tag.putInt   ("vigor",                vigor);
        tag.putInt   ("endurance",            endurance);
        tag.putInt   ("strength",             strength);
        tag.putInt   ("dexterity",            dexterity);
        tag.putInt   ("intelligence",         intelligence);
        tag.putInt   ("faith",                faith);
        tag.putInt   ("availablePoints",      availablePoints);
        tag.putFloat ("currentStamina",       currentStamina);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        currentSouls         = tag.getLong   ("currentSouls");
        lostSouls            = tag.getLong   ("lostSouls");
        lostSoulsX           = tag.getDouble ("lostSoulsX");
        lostSoulsY           = tag.getDouble ("lostSoulsY");
        lostSoulsZ           = tag.getDouble ("lostSoulsZ");
        hasPendingRetrieval  = tag.getBoolean("hasPendingRetrieval");
        playerLevel          = tag.getInt    ("playerLevel");
        vigor                = tag.getInt    ("vigor");
        endurance            = tag.getInt    ("endurance");
        strength             = tag.getInt    ("strength");
        dexterity            = tag.getInt    ("dexterity");
        intelligence         = tag.getInt    ("intelligence");
        faith                = tag.getInt    ("faith");
        availablePoints      = tag.getInt    ("availablePoints");
        currentStamina       = tag.contains  ("currentStamina") ? tag.getFloat("currentStamina") : getMaxStamina();
    }

    // ════════════════════════════════════════════════════════════════════
    //  Getters / Setters
    // ════════════════════════════════════════════════════════════════════
    public long    getCurrentSouls()        { return currentSouls; }
    public void    addSouls(long amount)    { currentSouls += amount; }
    public long    getLostSouls()           { return lostSouls; }
    public double  getLostSoulsX()          { return lostSoulsX; }
    public double  getLostSoulsY()          { return lostSoulsY; }
    public double  getLostSoulsZ()          { return lostSoulsZ; }
    public boolean hasPendingRetrieval()    { return hasPendingRetrieval; }
    public int     getPlayerLevel()         { return playerLevel; }
    public int     getVigor()               { return vigor; }
    public int     getEndurance()           { return endurance; }
    public int     getStrength()            { return strength; }
    public int     getDexterity()           { return dexterity; }
    public int     getIntelligence()        { return intelligence; }
    public int     getFaith()               { return faith; }
    public int     getAvailablePoints()     { return availablePoints; }
    public float   getCurrentStamina()      { return currentStamina; }
    public void    setCurrentStamina(float v) { this.currentStamina = Math.min(v, getMaxStamina()); }
    public long    getLastStaminaRegen()    { return lastStaminaRegen; }
    public void    setLastStaminaRegen(long t) { this.lastStaminaRegen = t; }
    public int     getRegenTickCounter()    { return regenTickCounter; }
    public void    incrementRegenTick()     { regenTickCounter++; }
    public void    resetRegenTick()         { regenTickCounter = 0; }
}
