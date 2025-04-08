package com.fabien_gigante.mixin;

import java.util.stream.Stream;

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
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

import com.fabien_gigante.BitsPropertyDelegate;
import com.fabien_gigante.IDisableableSlots;

import net.caffeinemc.mods.lithium.api.inventory.LithiumInventory;
import net.caffeinemc.mods.lithium.common.hopper.InventoryHelper;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin extends LootableContainerBlockEntity implements IDisableableSlots {
	private static final String KEY_DISABLED_SLOTS = "disabled_slots";
	private static final int SLOT_COUNT = HopperScreenHandler.SLOT_COUNT;

	@Shadow 
	protected DefaultedList<ItemStack> inventory;
	protected final BitsPropertyDelegate disabledSlots = new BitsPropertyDelegate(SLOT_COUNT) {
		@Override
		public void set(int index, int value) {
			super.set(index, value); 
			onDisabledSlotsChanged();
		}
	};

	private HopperBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
		super(blockEntityType, blockPos, blockState);
	}

	public BitsPropertyDelegate getDisableableSlots() { return this.disabledSlots; }

	private void onDisabledSlotsChanged() {
		if (this instanceof LithiumInventory lithiumInventory)
			InventoryHelper.getLithiumStackList(lithiumInventory).changed();
	}
	
	private boolean canToggleSlot(int slot) {
		return slot >= 0 && slot < SLOT_COUNT && this.inventory.get(slot).isEmpty();
	}
	public boolean isSlotEnabled(int slot) {
		return slot >= 0 && slot < SLOT_COUNT ? this.disabledSlots.get(slot) == 0 : true;
	}

	public void setSlotEnabled(int slot, boolean enabled) {
		if (this.canToggleSlot(slot)) {
			this.disabledSlots.set(slot, enabled ? 0 : 1);
			this.markDirty();
		}
	}

	@Inject(method="setStack", at=@At("HEAD"))
	private void setStack (int slot, ItemStack stack, CallbackInfo ci) {
		if (this.isSlotDisabled(slot)) this.enableSlot(slot);
	}

	@Override
	public boolean isValid(int slot, ItemStack stack) {
		return this.isSlotEnabled(slot) && super.isValid(slot, stack);
	}

	@Inject(method="writeNbt", at=@At("TAIL"))
	private void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
		int[] disabled = this.disabledSlots.find(1).toArray();
		if (disabled.length > 0) nbt.putIntArray(KEY_DISABLED_SLOTS, disabled);
	}

	@Inject(method="readNbt", at=@At("TAIL"))
	private void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
		this.disabledSlots.reset();
		for (int i : nbt.getIntArray(KEY_DISABLED_SLOTS).orElse(new int[0]))
			if (this.canToggleSlot(i)) this.disabledSlots.set(i, 1);
	}

}
