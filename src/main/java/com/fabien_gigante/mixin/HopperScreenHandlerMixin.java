package com.fabien_gigante.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.fabien_gigante.DisableableSlot;
import com.fabien_gigante.IDisableableSlots;
import com.fabien_gigante.IPropertiesAccessor;
import com.fabien_gigante.DisableableHopperSlotsMod;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

@Mixin(HopperScreenHandler.class)
public abstract class HopperScreenHandlerMixin extends ScreenHandler implements IDisableableSlots {
	@Shadow	protected Inventory inventory;
	protected PropertyDelegate propertyDelegate;

	protected HopperScreenHandlerMixin(ScreenHandlerType<?> type, int syncId) { super(type, syncId); }

	@Redirect(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;)V", at = @At(value = "NEW", target = "(Lnet/minecraft/inventory/Inventory;III)Lnet/minecraft/screen/slot/Slot;", ordinal = 0))
	private Slot newSlot(Inventory inventory, int id, int x, int y) {
		return new DisableableSlot(inventory, id, x, y, this);
	}

	@Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;)V", at = @At("TAIL"))
	private void init(int syncId, PlayerInventory playerInventory, Inventory inventory, CallbackInfo ci) {
		DisableableHopperSlotsMod.LOGGER.debug("HopperScreenHandlerMixin.init inventory.class={}", inventory.getClass());
		this.propertyDelegate = inventory instanceof IPropertiesAccessor owner ? owner.getProperties() : new ArrayPropertyDelegate(HopperScreenHandler.SLOT_COUNT);
		this.addProperties(this.propertyDelegate);
	}

	public boolean isSlotDisabled(int slot) {
		return slot > -1 && slot < HopperScreenHandler.SLOT_COUNT ? this.propertyDelegate.get(slot) == 1 : false;
	}

	public void setSlotEnabled(int id, boolean enabled) {
		DisableableHopperSlotsMod.LOGGER.debug("HopperScreenHandlerMixin.setSlotEnabled [{}]={}", id, enabled);
		this.setProperty(id, enabled ? 0 : 1);
		this.sendContentUpdates();
	}
}
