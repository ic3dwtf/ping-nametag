package com.ic3dwtf.pingnametag.config;

public final class PingNametagConfig {
    public boolean enabled = true;
    public String textFormat = " (%ping%ms)";
    public boolean showOwnPing = true;
    public boolean debugLogging = false;
    public int goodPingMax = 80;
    public int mediumPingMax = 150;
    public int badPingMax = 250;
    public int goodColor = 0x55FF55;
    public int mediumColor = 0xFFFF55;
    public int badColor = 0xFFAA00;
    public int worstColor = 0xFF5555;

    public PingNametagConfig copy() {
        PingNametagConfig copy = new PingNametagConfig();
        copy.enabled = this.enabled;
        copy.showOwnPing = this.showOwnPing;
        copy.textFormat = this.textFormat;
        copy.goodPingMax = this.goodPingMax;
        copy.mediumPingMax = this.mediumPingMax;
        copy.badPingMax = this.badPingMax;
        copy.goodColor = this.goodColor;
        copy.mediumColor = this.mediumColor;
        copy.badColor = this.badColor;
        copy.worstColor = this.worstColor;
        return copy;
    }

    public void sanitize() {
        this.goodPingMax = clampPositive(this.goodPingMax, 80);
        this.mediumPingMax = Math.max(this.goodPingMax + 1, clampPositive(this.mediumPingMax, 150));
        this.badPingMax = Math.max(this.mediumPingMax + 1, clampPositive(this.badPingMax, 250));
        this.goodColor = clampColor(this.goodColor, 0x55FF55);
        this.mediumColor = clampColor(this.mediumColor, 0xFFFF55);
        this.badColor = clampColor(this.badColor, 0xFFAA00);
        this.worstColor = clampColor(this.worstColor, 0xFF5555);
    }

    public int colorForPing(int ping) {
        if (ping <= this.goodPingMax) {
            return this.goodColor;
        }
        if (ping <= this.mediumPingMax) {
            return this.mediumColor;
        }
        if (ping <= this.badPingMax) {
            return this.badColor;
        }
        return this.worstColor;
    }

    private static int clampPositive(int value, int fallback) {
        return value <= 0 ? fallback : Math.min(value, 2000);
    }

    private static int clampColor(int value, int fallback) {
        return value < 0x000000 || value > 0xFFFFFF ? fallback : value;
    }
}
