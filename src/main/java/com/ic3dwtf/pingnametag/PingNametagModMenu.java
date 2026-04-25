package com.ic3dwtf.pingnametag;

import com.ic3dwtf.pingnametag.config.PingNametagConfig;
import com.ic3dwtf.pingnametag.config.PingNametagConfigManager;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public final class PingNametagModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            PingNametagConfig config = PingNametagConfigManager.get().copy();

            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.translatable("ping_nametag.config.title"));
            ConfigCategory general = builder.getOrCreateCategory(Text.translatable("ping_nametag.config.category.general"));
            ConfigEntryBuilder entries = builder.entryBuilder();

            general.addEntry(entries.startBooleanToggle(Text.translatable("ping_nametag.config.enabled"), config.enabled)
                    .setDefaultValue(true)
                    .setSaveConsumer(value -> config.enabled = value)
                    .build());

            general.addEntry(entries.startStrField(Text.translatable("ping_nametag.config.text_format"), config.textFormat)
                    .setDefaultValue(" (%ping%ms)")
                    .setTooltip(Text.translatable("ping_nametag.tooltip.text_format"))
                    .setSaveConsumer(value -> config.textFormat = value)
                    .build());

            general.addEntry(entries.startBooleanToggle(Text.translatable("ping_nametag.config.show_own_ping"), config.showOwnPing)
                    .setDefaultValue(true)
                    .setSaveConsumer(value -> config.showOwnPing = value)
                    .build());

            general.addEntry(entries.startIntField(Text.translatable("ping_nametag.config.good_ping_max"), config.goodPingMax)
                    .setDefaultValue(80)
                    .setMin(1)
                    .setMax(2000)
                    .setSaveConsumer(value -> config.goodPingMax = value)
                    .build());

            general.addEntry(entries.startIntField(Text.translatable("ping_nametag.config.medium_ping_max"), config.mediumPingMax)
                    .setDefaultValue(150)
                    .setMin(1)
                    .setMax(2000)
                    .setSaveConsumer(value -> config.mediumPingMax = value)
                    .build());

            general.addEntry(entries.startIntField(Text.translatable("ping_nametag.config.bad_ping_max"), config.badPingMax)
                    .setDefaultValue(250)
                    .setMin(1)
                    .setMax(2000)
                    .setSaveConsumer(value -> config.badPingMax = value)
                    .build());

            general.addEntry(entries.startColorField(Text.translatable("ping_nametag.config.good_color"), config.goodColor)
                    .setDefaultValue(0x55FF55)
                    .setSaveConsumer(value -> config.goodColor = value)
                    .build());

            general.addEntry(entries.startColorField(Text.translatable("ping_nametag.config.medium_color"), config.mediumColor)
                    .setDefaultValue(0xFFFF55)
                    .setSaveConsumer(value -> config.mediumColor = value)
                    .build());

            general.addEntry(entries.startColorField(Text.translatable("ping_nametag.config.bad_color"), config.badColor)
                    .setDefaultValue(0xFFAA00)
                    .setSaveConsumer(value -> config.badColor = value)
                    .build());

            general.addEntry(entries.startColorField(Text.translatable("ping_nametag.config.worst_color"), config.worstColor)
                    .setDefaultValue(0xFF5555)
                    .setSaveConsumer(value -> config.worstColor = value)
                    .build());

            builder.setSavingRunnable(() -> PingNametagConfigManager.save(config));
            return builder.build();
        };
    }
}
