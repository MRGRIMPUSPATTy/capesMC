package dev.capesmc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class CustomBannerBlockEntity extends BlockEntity {
    private String code = "";

    public CustomBannerBlockEntity(BlockPos pos, BlockState state) { super(CapesMC.BANNER_BE, pos, state); }

    public String getCode() { return code; }

    public void sync() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); code = in.getStringOr("code", ""); }
    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); out.putString("code", code); }

    @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider r) { return saveCustomOnly(r); }

    @Override
    protected void applyImplicitComponents(DataComponentGetter getter) {
        super.applyImplicitComponents(getter);
        CustomData cd = getter.get(DataComponents.CUSTOM_DATA);
        if (cd != null) code = cd.copyTag().getStringOr("code", "");
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder b) {
        super.collectImplicitComponents(b);
        CompoundTag t = new CompoundTag();
        t.putString("code", code);
        b.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
    }
}
