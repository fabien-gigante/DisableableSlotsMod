package com.fabien_gigante.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.fabien_gigante.BitsContainerData;
import com.fabien_gigante.DisableableSlot;
import com.fabien_gigante.IDisableableSlots;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

@Mixin(HopperMenu.class)
public abstract class HopperMenuMixin extends AbstractContainerMenu implements IDisableableSlots {
	@Shadow	
	protected Container hopper;
	protected BitsContainerData disabledSlots;

	protected HopperMenuMixin(MenuType<?> type, int syncId) { super(type, syncId); }

	@Redirect(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;)V", at = @At(value = "NEW", target = "(Lnet/minecraft/world/Container;III)Lnet/minecraft/world/inventory/Slot;", ordinal = 0))
	private Slot newSlot(Container container, int id, int x, int y) {
		return new DisableableSlot(container, id, x, y, this);
	}

	@Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;)V", at = @At("TAIL"))
	private void init(int syncId, Inventory inventory, Container container, CallbackInfo ci) {
		this.disabledSlots = container instanceof IDisableableSlots owner ? owner.getDisableableSlots() : new BitsContainerData(HopperMenu.CONTAINER_SIZE);
		this.addDataSlots(this.disabledSlots);
	}

	public BitsContainerData getDisableableSlots() { return this.disabledSlots; }

	public boolean isSlotEnabled(int slot) {
		return slot >= 0 && slot < HopperMenu.CONTAINER_SIZE ? this.disabledSlots.get(slot) == 0 : true;
	}

	public void setSlotEnabled(int id, boolean enabled) {
		this.disabledSlots.set(id, enabled ? 0 : 1);
		this.broadcastChanges();
	}
}
