package com.ic3dwtf.pingnametag.mixin;

import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityRenderState.class)
public interface EntityRenderStateAccessor {
    @Accessor("displayName")
    Text ping_nametag$getDisplayName();

    @Accessor("displayName")
    void ping_nametag$setDisplayName(Text displayName);
}
