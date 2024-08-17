package com.fabien_gigante.mixin;

import java.util.stream.IntStream;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

import com.fabien_gigante.IDisableableSlots;
import com.fabien_gigante.IPropertiesAccessor;
import com.fabien_gigante.DisableableHopperSlotsMod;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin extends LootableContainerBlockEntity implements IPropertiesAccessor, IDisableableSlots {
	private static final String KEY_DISABLED_SLOTS = "disabled_slots";
    private static final int SLOT_COUNT = HopperScreenHandler.SLOT_COUNT;

	@Shadow protected DefaultedList<ItemStack> inventory;
    protected final PropertyDelegate propertyDelegate = new ArrayPropertyDelegate(SLOT_COUNT);
    
    private HopperBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

	public PropertyDelegate getProperties() { return propertyDelegate; }

	private boolean canToggleSlot(int slot) {
		return slot >= 0 && slot < SLOT_COUNT && this.inventory.get(slot).isEmpty();
	}
	public boolean isSlotDisabled(int slot) {
		return slot >= 0 && slot < SLOT_COUNT ? this.propertyDelegate.get(slot) == 1 : false;
	}

	public void setSlotEnabled(int slot, boolean enabled) {
		if (this.canToggleSlot(slot)) {
			this.propertyDelegate.set(slot, enabled ? 0 : 1);
			this.markDirty();
		}
	}

    @Inject(method="setStack", at=@At("HEAD"))
    private void setStack (int slot, ItemStack stack, CallbackInfo ci) {
		if (this.isSlotDisabled(slot)) this.enableSlot(slot);
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
         return isSlotEnabled(slot) && super.isValid(slot, stack);
    }

    @Inject(method="writeNbt", at=@At("TAIL"))
    private void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
		DisableableHopperSlotsMod.LOGGER.debug("HopperBlockEntityMixin.writeNbt");
		var disabled = IntArrayList.toList(IntStream.range(0, SLOT_COUNT).filter(i -> this.isSlotDisabled(i)));
		DisableableHopperSlotsMod.LOGGER.debug("- disabled={}", disabled);
		if (!disabled.isEmpty()) nbt.putIntArray(KEY_DISABLED_SLOTS, disabled);
    }

    @Inject(method="readNbt", at=@At("TAIL"))
	private void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
		DisableableHopperSlotsMod.LOGGER.debug("HopperBlockEntityMixin.readNbt");
		for (int i = 0; i < SLOT_COUNT; i++) this.propertyDelegate.set(i, 0);
		if (!nbt.contains(KEY_DISABLED_SLOTS, NbtElement.INT_ARRAY_TYPE)) return;
		int[] disabled = nbt.getIntArray(KEY_DISABLED_SLOTS);
		DisableableHopperSlotsMod.LOGGER.debug("- disabled={}", disabled);
		for (int i : disabled) if (this.canToggleSlot(i)) this.propertyDelegate.set(i, 1);
    }

}
