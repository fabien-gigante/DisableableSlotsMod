package com.fabien_gigante.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

import com.fabien_gigante.BitsPropertyDelegate;
import com.fabien_gigante.IDisableableSlots;

import net.caffeinemc.mods.lithium.api.inventory.LithiumInventory;
import net.caffeinemc.mods.lithium.common.hopper.InventoryHelper;

@Mixin(DispenserBlockEntity.class)
public abstract class DispenserBlockEntityMixin extends LootableContainerBlockEntity implements IDisableableSlots {
	private static final String KEY_DISABLED_SLOTS = "disabled_slots";
	private static final int SLOT_COUNT = 9;

	@Shadow 
	protected DefaultedList<ItemStack> inventory;
	protected final BitsPropertyDelegate disabledSlots = new BitsPropertyDelegate(SLOT_COUNT) {
		@Override
		public void set(int index, int value) {
			super.set(index, value); 
			onDisabledSlotsChanged();
		}
	};

	private DispenserBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
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

	@Override
	public void setStack (int slot, ItemStack stack) {
		if (this.isSlotDisabled(slot)) this.enableSlot(slot);
        super.setStack(slot, stack);
	}

	@Override
	public boolean isValid(int slot, ItemStack stack) {
		return this.isSlotEnabled(slot) && super.isValid(slot, stack);
	}

	@Inject(method="writeData", at=@At("TAIL"))
	private void writeData(WriteView view, CallbackInfo ci) {
		int[] disabled = this.disabledSlots.find(1).toArray();
		if (disabled.length > 0) view.putIntArray(KEY_DISABLED_SLOTS, disabled);
	}

	@Inject(method="readData", at=@At("TAIL"))
	private void readData(ReadView view, CallbackInfo ci) {
		this.disabledSlots.reset();
		view.getOptionalIntArray(KEY_DISABLED_SLOTS).ifPresent((slots) -> { 
			for (int i : slots) if (this.canToggleSlot(i)) this.disabledSlots.set(i, 1);
		});
	}

	@Redirect(
        method = "addToFirstFreeSlot",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;get(I)Ljava/lang/Object;")
    )
    private Object getStackIfEnabled(DefaultedList<ItemStack> inventory, int slot) {
        return this.isSlotEnabled(slot) ? inventory.get(slot) : INVALID_STACK;
    }	
}
