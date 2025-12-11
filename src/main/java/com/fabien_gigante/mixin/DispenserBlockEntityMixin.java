package com.fabien_gigante.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.fabien_gigante.BitsContainerData;
import com.fabien_gigante.IDisableableSlots;

import net.caffeinemc.mods.lithium.api.inventory.LithiumInventory;
import net.caffeinemc.mods.lithium.common.hopper.InventoryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

@Mixin(DispenserBlockEntity.class)
public abstract class DispenserBlockEntityMixin extends RandomizableContainerBlockEntity implements IDisableableSlots {
	private static final String KEY_DISABLED_SLOTS = "disabled_slots";
	private static final int SLOT_COUNT = 9;

	@Shadow 
	protected NonNullList<ItemStack> items;
	protected final BitsContainerData disabledSlots = new BitsContainerData(SLOT_COUNT) {
		@Override
		public void set(int index, int value) {
			super.set(index, value); 
			onDisabledSlotsChanged();
		}
	};

	private DispenserBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
		super(blockEntityType, blockPos, blockState);
	}

	public BitsContainerData getDisableableSlots() { return this.disabledSlots; }

	private void onDisabledSlotsChanged() {
		if (this instanceof LithiumInventory lithiumInventory)
			InventoryHelper.getLithiumStackList(lithiumInventory).changed();
	}
	
	private boolean canToggleSlot(int slot) {
		return slot >= 0 && slot < SLOT_COUNT && this.items.get(slot).isEmpty();
	}
	public boolean isSlotEnabled(int slot) {
		return slot >= 0 && slot < SLOT_COUNT ? this.disabledSlots.get(slot) == 0 : true;
	}

	public void setSlotEnabled(int slot, boolean enabled) {
		if (this.canToggleSlot(slot)) {
			this.disabledSlots.set(slot, enabled ? 0 : 1);
			this.setChanged();
		}
	}

	@Override
	public void setItem (int slot, ItemStack stack) {
		if (this.isSlotDisabled(slot)) this.enableSlot(slot);
        super.setItem(slot, stack);
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return this.isSlotEnabled(slot) && super.canPlaceItem(slot, stack);
	}

	@Inject(method="saveAdditional", at=@At("TAIL"))
	private void saveDisabledSlots(ValueOutput view, CallbackInfo ci) {
		int[] disabled = this.disabledSlots.find(1).toArray();
		if (disabled.length > 0) view.putIntArray(KEY_DISABLED_SLOTS, disabled);
	}

	@Inject(method="loadAdditional", at=@At("TAIL"))
	private void loadDisabledSlots(ValueInput view, CallbackInfo ci) {
		this.disabledSlots.reset();
		view.getIntArray(KEY_DISABLED_SLOTS).ifPresent((slots) -> { 
			for (int i : slots) if (this.canToggleSlot(i)) this.disabledSlots.set(i, 1);
		});
	}

	@Redirect(
        method = "insertItem",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;get(I)Ljava/lang/Object;")
    )
    private Object getStackIfEnabled(NonNullList<ItemStack> inventory, int slot) {
        return this.isSlotEnabled(slot) ? inventory.get(slot) : INVALID_STACK;
    }	
}
