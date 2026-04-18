package com.ic3dwtf.pingnametag.mixin;

import com.ic3dwtf.pingnametag.config.PingNametagConfig;
import com.ic3dwtf.pingnametag.config.PingNametagConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.entity.DisplayEntityRenderer;
import net.minecraft.client.render.entity.state.TextDisplayEntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisplayEntityRenderer.TextDisplayEntityRenderer.class)
public abstract class TextDisplayEntityRendererMixin {
    private static final long ping_nametag$PERF_LOG_INTERVAL_MS = 5000L;
    private static final Logger ping_nametag$LOGGER = LoggerFactory.getLogger("ping-nametag");
    private static long ping_nametag$PERF_LAST_LOG_MS = 0L;
    private static long ping_nametag$PERF_CALLS = 0L;
    private static long ping_nametag$PERF_PLAYER_MOUNT_CALLS = 0L;
    private static long ping_nametag$PERF_APPLIED_CALLS = 0L;
    private static long ping_nametag$PERF_TOTAL_NANOS = 0L;
    private static long ping_nametag$PERF_MAX_NANOS = 0L;

    @Shadow
    protected abstract DisplayEntity.TextDisplayEntity.TextLines getLines(Text text, int width);

    @Inject(
            method = "updateRenderState(Lnet/minecraft/entity/decoration/DisplayEntity$TextDisplayEntity;Lnet/minecraft/client/render/entity/state/TextDisplayEntityRenderState;F)V",
            at = @At("TAIL")
    )
    private void ping_nametag$appendPingToPlayerMountedTextDisplay(DisplayEntity.TextDisplayEntity entity, TextDisplayEntityRenderState renderState, float tickProgress, CallbackInfo ci) {
        PingNametagConfig config = PingNametagConfigManager.get();
        long perfStartNanos = config.debugLogging ? System.nanoTime() : 0L;
        boolean playerMounted = false;
        boolean pingApplied = false;

        try {
            if (!config.enabled) {
                return;
            }

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.getNetworkHandler() == null) {
                return;
            }

            Entity vehicle = entity.getVehicle();
            if (!(vehicle instanceof PlayerEntity player)) {
                return;
            }
            playerMounted = true;

            if (!config.showOwnPing && player.getUuid().equals(client.player.getUuid())) {
                return;
            }

            Text baseText = entity.getText();
            String baseTextValue = baseText == null ? "" : baseText.getString();
            if (baseTextValue.isEmpty()) {
                return;
            }

            int firstNewline = baseTextValue.indexOf('\n');
            if (firstNewline < 0 && !baseTextValue.contains(player.getNameForScoreboard())) {
                return;
            }

            PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(player.getUuid());
            MutableText suffix;
            if (entry == null) {
                suffix = Text.literal(" (??ms)").setStyle(Style.EMPTY.withColor(0xAAAAAA));
            } else {
                int latency = Math.max(0, entry.getLatency());
                suffix = Text.literal(" (" + latency + "ms)").setStyle(Style.EMPTY.withColor(config.colorForPing(latency)));
            }

            Text modifiedText = ping_nametag$appendSuffixToTopLine(baseText, suffix);
            ((TextDisplayEntityRenderStateAccessor) renderState).ping_nametag$setTextLines(getLines(modifiedText, entity.getLineWidth()));
            pingApplied = true;
        } finally {
            ping_nametag$recordPerf(config, perfStartNanos, playerMounted, pingApplied);
        }
    }

    private static Text ping_nametag$appendSuffixToTopLine(Text baseText, MutableText suffix) {
        String baseTextValue = baseText.getString();
        int firstNewline = baseTextValue.indexOf('\n');
        if (firstNewline < 0) {
            return baseText.copy().append(suffix);
        }

        String firstLine = baseTextValue.substring(0, firstNewline);
        String remainingLines = baseTextValue.substring(firstNewline);
        return Text.literal(firstLine)
                .setStyle(baseText.getStyle())
                .append(suffix)
                .append(Text.literal(remainingLines).setStyle(baseText.getStyle()));
    }

    private static void ping_nametag$recordPerf(PingNametagConfig config, long startNanos, boolean playerMounted, boolean pingApplied) {
        if (config == null || !config.debugLogging || startNanos == 0L) {
            return;
        }

        long elapsedNanos = System.nanoTime() - startNanos;
        ping_nametag$PERF_CALLS++;
        if (playerMounted) {
            ping_nametag$PERF_PLAYER_MOUNT_CALLS++;
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
                    "[perf:text_display_renderer] calls={}, player_mount_calls={}, applied_calls={}, avg_nanos={}, max_nanos={}",
                    ping_nametag$PERF_CALLS,
                    ping_nametag$PERF_PLAYER_MOUNT_CALLS,
                    ping_nametag$PERF_APPLIED_CALLS,
                    ping_nametag$PERF_TOTAL_NANOS / ping_nametag$PERF_CALLS,
                    ping_nametag$PERF_MAX_NANOS
            );
        }

        ping_nametag$PERF_LAST_LOG_MS = now;
        ping_nametag$PERF_CALLS = 0L;
        ping_nametag$PERF_PLAYER_MOUNT_CALLS = 0L;
        ping_nametag$PERF_APPLIED_CALLS = 0L;
        ping_nametag$PERF_TOTAL_NANOS = 0L;
        ping_nametag$PERF_MAX_NANOS = 0L;
    }
}
