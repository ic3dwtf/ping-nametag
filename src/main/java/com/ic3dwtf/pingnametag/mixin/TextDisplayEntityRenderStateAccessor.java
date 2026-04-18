package com.ic3dwtf.pingnametag.mixin;

import net.minecraft.client.render.entity.state.TextDisplayEntityRenderState;
import net.minecraft.entity.decoration.DisplayEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TextDisplayEntityRenderState.class)
public interface TextDisplayEntityRenderStateAccessor {
    @Accessor("textLines")
    DisplayEntity.TextDisplayEntity.TextLines ping_nametag$getTextLines();

    @Accessor("textLines")
    void ping_nametag$setTextLines(DisplayEntity.TextDisplayEntity.TextLines textLines);
}
