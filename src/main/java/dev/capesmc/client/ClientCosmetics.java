package dev.capesmc.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import dev.capesmc.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

public final class ClientCosmetics {
    public static final class Settings { public String cape = "", banner = "", aura = "NONE"; }
    private record Remote(String cape, Aura aura) {}

    public static Settings local = new Settings();
    private static final Map<UUID, Remote> REMOTE = new HashMap<>();
    private static final Map<String, Identifier> TEX = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path file() { return FabricLoader.getInstance().getConfigDir().resolve("capesmc.json"); }

    public static void load() {
        try {
            if (Files.exists(file())) local = GSON.fromJson(Files.readString(file()), Settings.class);
            if (local == null) local = new Settings();
        } catch (Exception e) { local = new Settings(); }
    }

    public static void save() {
        try { Files.writeString(file(), GSON.toJson(local)); } catch (IOException ignored) { }
    }

    public static void setRemote(CosmeticsPayload p) { REMOTE.put(p.uuid(), new Remote(p.cape(), Aura.parse(p.aura()))); }
    public static void clearRemote() { REMOTE.clear(); }

    public static void sendLocal() {
        if (ClientPlayNetworking.canSend(CosmeticsPayload.TYPE))
            ClientPlayNetworking.send(new CosmeticsPayload(new UUID(0, 0), local.cape, local.aura));
    }

    private static boolean isMe(UUID id) {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && mc.player.getUUID().equals(id);
    }

    public static Identifier capeFor(UUID id) {
        if (isMe(id)) return texture(local.cape, false);
        Remote r = REMOTE.get(id);
        return r == null ? null : texture(r.cape(), false);
    }

    public static Aura auraFor(UUID id) {
        if (isMe(id)) return Aura.parse(local.aura);
        Remote r = REMOTE.get(id);
        return r == null ? Aura.NONE : r.aura();
    }

    /** Builds (and caches) a dynamic texture for a design code. Render thread only. */
    public static Identifier texture(String code, boolean banner) {
        if (code == null || code.isEmpty()) return null;
        String key = (banner ? "b:" : "c:") + code;
        if (TEX.containsKey(key)) return TEX.get(key);
        Identifier result = null;
        Design d = Design.decode(code);
        if (d != null && (banner ? d.isBanner() : d.isCape())) {
            final Identifier tid = Identifier.fromNamespaceAndPath(CapesMC.ID,
                    "textures/" + (banner ? "banner/" : "cape/") + UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8)) + ".png");
            DynamicTexture t = new DynamicTexture(() -> tid.toString(), banner ? bannerImage(d) : capeImage(d));
            Minecraft.getInstance().getTextureManager().register(tid, t);
            result = tid;
        }
        TEX.put(key, result);
        return result;
    }

    /** 64x32 vanilla cape layout; the design goes on the outer face at (1,1). */
    static NativeImage capeImage(Design d) {
        NativeImage img = new NativeImage(64, 32, true);
        int w = d.w, h = d.h;
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                int a = d.argb(d.px[y * w + x]);
                img.setPixel(1 + x, 1 + y, a);    // outer face
                img.setPixel(12 + x, 1 + y, a);   // inner face
            }
        for (int x = 0; x < w; x++) {
            img.setPixel(1 + x, 0, d.argb(d.px[x]));
            img.setPixel(11 + x, 0, d.argb(d.px[(h - 1) * w + x]));
        }
        for (int y = 0; y < h; y++) {
            img.setPixel(0, 1 + y, d.argb(d.px[y * w]));
            img.setPixel(11, 1 + y, d.argb(d.px[y * w + w - 1]));
        }
        return img;
    }

    static NativeImage bannerImage(Design d) {
        NativeImage img = new NativeImage(d.w, d.h, true);
        for (int y = 0; y < d.h; y++)
            for (int x = 0; x < d.w; x++) img.setPixel(x, y, d.argb(d.px[y * d.w + x]));
        return img;
    }
}
