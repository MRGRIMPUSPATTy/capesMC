package dev.capesmc;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Client asks the server to hand over a banner item carrying this design code. */
public record BannerPayload(String code) implements CustomPacketPayload {
    public static final Type<BannerPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(CapesMC.ID, "banner_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BannerPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.stringUtf8(4096), BannerPayload::code, BannerPayload::new);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
