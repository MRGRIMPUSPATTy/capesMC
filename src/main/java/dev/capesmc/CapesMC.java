package dev.capesmc;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class CapesMC implements ModInitializer {
    public static final String ID = "capesmc";

    public static Block BANNER_BLOCK;
    public static Item BANNER_ITEM;
    public static BlockEntityType<CustomBannerBlockEntity> BANNER_BE;

    /** Server-side memory of who wears what, so every capesMC client can see everyone else's. */
    private static final Map<UUID, CosmeticsPayload> STATE = new ConcurrentHashMap<>();

    @Override
    public void onInitialize() {
        ResourceKey<Block> bk = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ID, "custom_banner"));
        BANNER_BLOCK = Registry.register(BuiltInRegistries.BLOCK, bk,
                new CustomBannerBlock(BlockBehaviour.Properties.of().setId(bk).strength(1.0f).sound(SoundType.WOOD).noOcclusion()));
        ResourceKey<Item> ik = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ID, "custom_banner"));
        BANNER_ITEM = Registry.register(BuiltInRegistries.ITEM, ik,
                new BlockItem(BANNER_BLOCK, new Item.Properties().setId(ik).useBlockDescriptionPrefix()));
        BANNER_BE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(ID, "custom_banner"),
                FabricBlockEntityTypeBuilder.create(CustomBannerBlockEntity::new, BANNER_BLOCK).build());
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COLORED_BLOCKS).register(e -> e.accept(BANNER_ITEM));

        PayloadTypeRegistry.serverboundPlay().register(CosmeticsPayload.TYPE, CosmeticsPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(CosmeticsPayload.TYPE, CosmeticsPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(BannerPayload.TYPE, BannerPayload.CODEC);

        // Player updated their look -> validate, remember, broadcast.
        ServerPlayNetworking.registerGlobalReceiver(CosmeticsPayload.TYPE, (payload, ctx) -> {
            ServerPlayer p = ctx.player();
            Design cape = Design.decode(payload.cape());
            String capeCode = (cape != null && cape.isCape()) ? payload.cape().trim() : "";
            String aura = Aura.parse(payload.aura()).name();
            CosmeticsPayload out = new CosmeticsPayload(p.getUUID(), capeCode, aura);
            STATE.put(p.getUUID(), out);
            for (ServerPlayer o : ctx.server().getPlayerList().getPlayers())
                if (ServerPlayNetworking.canSend(o, CosmeticsPayload.TYPE)) ServerPlayNetworking.send(o, out);
        });

        // Hand out a banner item. Cosmetic only; restrict with a permission check here if you run a public server.
        ServerPlayNetworking.registerGlobalReceiver(BannerPayload.TYPE, (payload, ctx) -> {
            Design d = Design.decode(payload.code());
            if (d == null || !d.isBanner()) return;
            ServerPlayer p = ctx.player();
            ItemStack st = new ItemStack(BANNER_ITEM);
            CompoundTag tag = new CompoundTag();
            tag.putString("code", payload.code().trim());
            st.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            st.set(DataComponents.CUSTOM_NAME, Component.literal("Custom Banner"));
            if (!p.getInventory().add(st)) p.drop(st, false);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            for (CosmeticsPayload c : STATE.values())
                if (ServerPlayNetworking.canSend(handler.getPlayer(), CosmeticsPayload.TYPE))
                    sender.sendPacket(c);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID id = handler.getPlayer().getUUID();
            STATE.remove(id);
            CosmeticsPayload clear = new CosmeticsPayload(id, "", "NONE");
            for (ServerPlayer o : server.getPlayerList().getPlayers())
                if (o != handler.getPlayer() && ServerPlayNetworking.canSend(o, CosmeticsPayload.TYPE))
                    ServerPlayNetworking.send(o, clear);
        });
    }
}
