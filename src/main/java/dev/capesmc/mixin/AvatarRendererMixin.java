package dev.capesmc.mixin;

import dev.capesmc.client.ClientCosmetics;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Compatibility note: we only touch the cape field of the render state at TAIL, after every other mod's
 * HEAD/RETURN work, and only when a capesMC design is set. Skin, model and everything else are untouched.
 */
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
            at = @At("TAIL"))
    private void capesmc$applyCape(Avatar avatar, AvatarRenderState state, float partialTick, CallbackInfo ci) {
        Identifier tex = ClientCosmetics.capeFor(avatar.getUUID());
        if (tex == null) return;
        PlayerSkin s = state.skin;
        ClientAsset.Texture cape = new ClientAsset.ResourceTexture(tex, tex);
        state.skin = new PlayerSkin(s.body(), cape, s.elytra(), s.model(), s.secure());
    }
}
