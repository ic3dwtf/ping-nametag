package com.ic3dwtf.pingnametag.mixin;

import com.ic3dwtf.pingnametag.config.PingNametagConfig;
import com.ic3dwtf.pingnametag.config.PingNametagConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    private static final long ping_nametag$DEBUG_LOG_INTERVAL_MS = 5000L;
    private static final long ping_nametag$PERF_LOG_INTERVAL_MS = 5000L;
    private static final Logger ping_nametag$LOGGER = LoggerFactory.getLogger("ping-nametag");
    private static final Map<String, Long> ping_nametag$DEBUG_LOG_COOLDOWNS = new HashMap<>();
    private static long ping_nametag$PERF_LAST_LOG_MS = 0L;
    private static long ping_nametag$PERF_CALLS = 0L;
    private static long ping_nametag$PERF_PLAYER_CALLS = 0L;
    private static long ping_nametag$PERF_APPLIED_CALLS = 0L;
    private static long ping_nametag$PERF_TOTAL_NANOS = 0L;
    private static long ping_nametag$PERF_MAX_NANOS = 0L;

    @Inject(method = "updateRenderState", at = @At("TAIL"))
    private void ping_nametag$appendPingOverlayToFinalLabel(Entity entity, EntityRenderState state, float tickProgress, CallbackInfo ci) {
        PingNametagConfig config = PingNametagConfigManager.get();
        long perfStartNanos = config.debugLogging ? System.nanoTime() : 0L;
        boolean playerTarget = false;
        boolean pingApplied = false;

        try {
            if (!config.enabled) {
                ping_nametag$debug(config, "disabled", () -> "Skipped: mod disabled in config");
                return;
            }

            if (!(entity instanceof AbstractClientPlayerEntity self)) {
                return;
            }
            playerTarget = true;

            Text originalText = ((EntityRenderStateAccessor) state).ping_nametag$getDisplayName();
            if (originalText == null || originalText.getString().isEmpty()) {
                ping_nametag$debug(config, "empty_text", () -> "Skipped: empty display name text");
                return;
            }

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.getNetworkHandler() == null) {
                ping_nametag$debug(config, "client_not_ready", () -> "Skipped: client player or network handler missing");
                return;
            }

            if (!config.showOwnPing && self.getUuid().equals(client.player.getUuid())) {
                ping_nametag$debug(config, "own_hidden", () -> "Skipped: own ping hidden by config");
                return;
            }

            PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(self.getUuid());
            if (entry == null) {
                MutableText unknownSuffix = Text.literal(" (??ms)").setStyle(Style.EMPTY.withColor(0xAAAAAA));
                ((EntityRenderStateAccessor) state).ping_nametag$setDisplayName(ping_nametag$baseLabelWithoutPingSuffix(originalText).append(unknownSuffix));
                pingApplied = true;
                ping_nametag$debug(config, "missing_tab_entry:" + self.getUuid(),
                        () -> "Applied unknown ping overlay: no PlayerListEntry for " + self.getName().getString() + " (" + self.getUuid() + "), tab players=" + client.getNetworkHandler().getPlayerList().size());
                return;
            }

            int latency = Math.max(0, entry.getLatency());
            MutableText suffix = Text.literal(" (" + latency + "ms)").setStyle(Style.EMPTY.withColor(config.colorForPing(latency)));
            ((EntityRenderStateAccessor) state).ping_nametag$setDisplayName(ping_nametag$baseLabelWithoutPingSuffix(originalText).append(suffix));
            pingApplied = true;
            ping_nametag$debug(config, "applied:" + self.getUuid(),
                    () -> "Applied ping overlay for " + self.getName().getString() + " (" + self.getUuid() + "), latency=" + latency + "ms");
        } finally {
            ping_nametag$recordPerf(config, perfStartNanos, playerTarget, pingApplied);
        }
    }

    private static MutableText ping_nametag$baseLabelWithoutPingSuffix(Text originalText) {
        MutableText baseText = originalText.copy();
        var siblings = baseText.getSiblings();
        if (siblings.isEmpty()) {
            return baseText;
        }

        Text lastSibling = siblings.get(siblings.size() - 1);
        if (ping_nametag$isPingSuffix(lastSibling.getString())) {
            siblings.remove(siblings.size() - 1);
        }

        return baseText;
    }

    private static boolean ping_nametag$isPingSuffix(String text) {
        if (!text.startsWith(" (") || !text.endsWith("ms)")) {
            return false;
        }

        String value = text.substring(2, text.length() - 3);
        if ("??".equals(value)) {
            return true;
        }

        if (value.isEmpty()) {
            return false;
        }

        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static void ping_nametag$recordPerf(PingNametagConfig config, long startNanos, boolean playerTarget, boolean pingApplied) {
        if (config == null || !config.debugLogging || startNanos == 0L) {
            return;
        }

        long elapsedNanos = System.nanoTime() - startNanos;
        ping_nametag$PERF_CALLS++;
        if (playerTarget) {
            ping_nametag$PERF_PLAYER_CALLS++;
        }
        if (pingApplied) {
            ping_nametag$PERF_APPLIED_CALLS++;
        }
        ping_nametag$PERF_TOTAL_NANOS += elapsedNanos;
        ping_nametag$PERF_MAX_NANOS = Math.max(ping_nametag$PERF_MAX_NANOS, elapsedNanos);

        long now = System.currentTimeMillis();
        if (now - ping_nametag$PERF_LAST_LOG_MS < ping_nametag$PERF_LOG_INTERVAL_MS) {
            return;
        }

        if (ping_nametag$PERF_CALLS > 0) {
            ping_nametag$LOGGER.info(
                    "[perf:entity_renderer] calls={}, player_calls={}, applied_calls={}, avg_nanos={}, max_nanos={}",
                    ping_nametag$PERF_CALLS,
                    ping_nametag$PERF_PLAYER_CALLS,
                    ping_nametag$PERF_APPLIED_CALLS,
                    ping_nametag$PERF_TOTAL_NANOS / ping_nametag$PERF_CALLS,
                    ping_nametag$PERF_MAX_NANOS
            );
        }

        ping_nametag$PERF_LAST_LOG_MS = now;
        ping_nametag$PERF_CALLS = 0L;
        ping_nametag$PERF_PLAYER_CALLS = 0L;
        ping_nametag$PERF_APPLIED_CALLS = 0L;
        ping_nametag$PERF_TOTAL_NANOS = 0L;
        ping_nametag$PERF_MAX_NANOS = 0L;
    }

    private static void ping_nametag$debug(PingNametagConfig config, String key, Supplier<String> message) {
        if (config == null || !config.debugLogging) {
            return;
        }

        long now = System.currentTimeMillis();
        Long last = ping_nametag$DEBUG_LOG_COOLDOWNS.get(key);
        if (last != null && now - last < ping_nametag$DEBUG_LOG_INTERVAL_MS) {
            return;
        }

        ping_nametag$DEBUG_LOG_COOLDOWNS.put(key, now);
        ping_nametag$LOGGER.info("[debug:{}] {}", key, message.get());
    }
}
