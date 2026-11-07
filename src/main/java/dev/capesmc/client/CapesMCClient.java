package dev.capesmc.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.capesmc.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

public class CapesMCClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientCosmetics.load();

        KeyMapping.Category cat = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(CapesMC.ID, "main"));
        KeyMapping open = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.capesmc.open", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, cat));

        BlockEntityRenderers.register(CapesMC.BANNER_BE, ctx -> new BannerRenderer());

        ClientPlayNetworking.registerGlobalReceiver(CosmeticsPayload.TYPE, (p, ctx) -> ClientCosmetics.setRemote(p));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, mc) -> {
            ClientCosmetics.clearRemote();
            ClientCosmetics.sendLocal();
        });

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (open.consumeClick()) mc.setScreen(new PainterScreen());
            if (mc.level == null || mc.isPaused()) return;
            long t = mc.level.getGameTime();
            for (Player p : mc.level.players()) {
                Aura a = ClientCosmetics.auraFor(p.getUUID());
                if (a != Aura.NONE && !p.isInvisible()) a.tick(mc.level, p, mc.level.getRandom(), t);
            }
        });
    }
}
