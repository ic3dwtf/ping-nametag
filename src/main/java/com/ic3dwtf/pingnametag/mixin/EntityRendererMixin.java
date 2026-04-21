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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    @Inject(method = "updateRenderState", at = @At("TAIL"))
    private void ping_nametag$appendPingOverlayToFinalLabel(Entity entity, EntityRenderState state, float tickProgress, CallbackInfo ci) {
        PingNametagConfig config = PingNametagConfigManager.get();

        if (!config.enabled) {
            return;
        }

        if (!(entity instanceof AbstractClientPlayerEntity self)) {
            return;
        }

        Text originalText = ((EntityRenderStateAccessor) state).ping_nametag$getDisplayName();
        if (originalText == null || originalText.getString().isEmpty()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.getNetworkHandler() == null) {
            return;
        }

        if (!config.showOwnPing && self.getUuid().equals(client.player.getUuid())) {
            return;
        }

        PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(self.getUuid());
        if (entry == null) {
            MutableText unknownSuffix = Text.literal(" (??ms)").setStyle(Style.EMPTY.withColor(0xAAAAAA));
            ((EntityRenderStateAccessor) state).ping_nametag$setDisplayName(
                    ping_nametag$baseLabelWithoutPingSuffix(originalText).append(unknownSuffix)
            );
            return;
        }

        int latency = Math.max(0, entry.getLatency());
        MutableText suffix = Text.literal(" (" + latency + "ms)")
                .setStyle(Style.EMPTY.withColor(config.colorForPing(latency)));

        ((EntityRenderStateAccessor) state).ping_nametag$setDisplayName(
                ping_nametag$baseLabelWithoutPingSuffix(originalText).append(suffix)
        );
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
}