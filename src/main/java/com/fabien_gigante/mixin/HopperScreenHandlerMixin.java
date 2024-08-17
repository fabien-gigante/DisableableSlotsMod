package com.fabien_gigante.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.fabien_gigante.BitsPropertyDelegate;
import com.fabien_gigante.DisableableSlot;
import com.fabien_gigante.IDisableableSlots;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

@Mixin(HopperScreenHandler.class)
public abstract class HopperScreenHandlerMixin extends ScreenHandler implements IDisableableSlots {
	@Shadow	
	protected Inventory inventory;
	protected BitsPropertyDelegate disabledSlots;

	protected HopperScreenHandlerMixin(ScreenHandlerType<?> type, int syncId) { super(type, syncId); }

	@Redirect(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;)V", at = @At(value = "NEW", target = "(Lnet/minecraft/inventory/Inventory;III)Lnet/minecraft/screen/slot/Slot;", ordinal = 0))
	private Slot newSlot(Inventory inventory, int id, int x, int y) {
		return new DisableableSlot(inventory, id, x, y, this);
	}

	@Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;)V", at = @At("TAIL"))
	private void init(int syncId, PlayerInventory playerInventory, Inventory inventory, CallbackInfo ci) {
		this.disabledSlots = inventory instanceof IDisableableSlots owner ? owner.getDisableableSlots() : new BitsPropertyDelegate(HopperScreenHandler.SLOT_COUNT);
		this.addProperties(this.disabledSlots);
	}

	public BitsPropertyDelegate getDisableableSlots() { return this.disabledSlots; }

	public boolean isSlotEnabled(int slot) {
		return slot >= 0 && slot < HopperScreenHandler.SLOT_COUNT ? this.disabledSlots.get(slot) == 0 : true;
	}

	public void setSlotEnabled(int id, boolean enabled) {
		this.disabledSlots.set(id, enabled ? 0 : 1);
		this.sendContentUpdates();
	}
}
