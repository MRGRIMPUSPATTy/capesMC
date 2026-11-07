package dev.capesmc;

import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Sent client->server (uuid ignored) and server->clients (uuid = owner). */
public record CosmeticsPayload(UUID uuid, String cape, String aura) implements CustomPacketPayload {
    public static final Type<CosmeticsPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(CapesMC.ID, "cosmetics"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CosmeticsPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, CosmeticsPayload::uuid,
            ByteBufCodecs.stringUtf8(4096), CosmeticsPayload::cape,
            ByteBufCodecs.stringUtf8(32), CosmeticsPayload::aura,
            CosmeticsPayload::new);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
