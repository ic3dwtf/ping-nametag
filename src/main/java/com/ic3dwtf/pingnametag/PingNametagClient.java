package com.ic3dwtf.pingnametag;

import com.ic3dwtf.pingnametag.config.PingNametagConfigManager;
import net.fabricmc.api.ClientModInitializer;

public final class PingNametagClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PingNametagConfigManager.load();
    }
}
